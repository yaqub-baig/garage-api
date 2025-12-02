package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.*;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartRequestService {

    private final PartRequestRepository partRequestRepo;
    private final PartRequestItemRepository partRequestItemRepo;
    private final CustomerPartResponseRepository customerPartResponseRepo;
    private final GarageProcurementRepository garageProcurementRepo;
    private final JobCardRepository jobCardRepo;
    private final TechnicianRepository technicianRepo;
    private final VehicleClient vehicleClient;
    private final AuthClient authClient;
    private final InvoiceRepository invoiceRepo;
    private final InvoiceLineItemRepository invoiceLineItemRepo;

    /**
     * Create or update a part request when technician updates job status to WAITING_FOR_PARTS
     */
    @Transactional
    public PartRequestResponse createPartRequest(CreatePartRequestRequest request) {
        log.info("Creating/updating part request for job card: {}", request.getJobCardId());

        // Validate job card exists
        JobCard jobCard = jobCardRepo.findById(request.getJobCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found"));

        // Validate technician exists
        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        // Get customer and vehicle details from the appointment
        Integer customerId = jobCard.getAppointment().getCustomerId();
        Integer vehicleId = jobCard.getAppointment().getVehicleId();

        // Check if part request already exists for this job card
        PartRequest partRequest = partRequestRepo.findByJobCardId(request.getJobCardId())
                .orElse(null);

        if (partRequest != null) {
            // Update existing part request
            log.info("Part request already exists for job card {}. Updating...", request.getJobCardId());

            // Delete old part request items
            List<PartRequestItem> oldItems = partRequestItemRepo.findByPartRequestId(partRequest.getId());
            partRequestItemRepo.deleteAll(oldItems);

            // Update part request fields
            partRequest.setRequestedBy(technician);
            partRequest.setNotes(request.getNotes());
            partRequest.setRequestDate(LocalDateTime.now());

            // Reset status only if customer hasn't responded yet
            if (partRequest.getStatus() == PartRequestStatus.PENDING_CUSTOMER_RESPONSE) {
                partRequest.setStatus(PartRequestStatus.PENDING_CUSTOMER_RESPONSE);
                partRequest.setCustomerResponseDate(null);
            }

            partRequest = partRequestRepo.save(partRequest);
        } else {
            // Create new part request
            log.info("Creating new part request for job card {}", request.getJobCardId());

            partRequest = PartRequest.builder()
                    .jobCard(jobCard)
                    .requestedBy(technician)
                    .status(PartRequestStatus.PENDING_CUSTOMER_RESPONSE)
                    .customerId(customerId)
                    .vehicleId(vehicleId)
                    .notes(request.getNotes())
                    .build();

            partRequest = partRequestRepo.save(partRequest);
        }

        // Create new part request items
        List<PartRequestItem> items = new ArrayList<>();
        for (PartRequestItemDto itemDto : request.getItems()) {
            PartRequestItem item = PartRequestItem.builder()
                    .partRequest(partRequest)
                    .partName(itemDto.getPartName())
                    .partNumber(itemDto.getPartNumber())
                    .quantity(itemDto.getQuantity())
                    .description(itemDto.getDescription())
                    .estimatedPrice(itemDto.getEstimatedPrice())
                    .procurementStatus(com.autoadda.apis.garage.util.ItemProcurementStatus.PENDING_PROCUREMENT)
                    .build();
            items.add(partRequestItemRepo.save(item));
        }
        log.info("Part saved :: {}", items.size());
        log.info("Part request created/updated successfully with ID: {}", partRequest.getId());

        // Send SMS notification to customer
        sendPartRequestNotificationToCustomer(partRequest);

        return convertToResponse(partRequest);
    }

    /**
     * Update basic details of an existing part request
     */
    @Transactional
    public PartRequestResponse updatePartRequest(Integer partRequestId, UpdatePartRequestRequest request) {
        log.info("Updating part request: {}", partRequestId);

        // Find part request
        PartRequest partRequest = partRequestRepo.findById(partRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Part request not found"));

        // Validate that customer hasn't responded yet (only allow updates before customer response)
        if (partRequest.getStatus() != PartRequestStatus.PENDING_CUSTOMER_RESPONSE) {
            throw new IllegalStateException(
                    "Cannot update part request after customer has responded. Current status: " + partRequest.getStatus());
        }

        // Validate at least one field is provided
        if ((request.getNotes() == null || request.getNotes().trim().isEmpty()) &&
                (request.getItems() == null || request.getItems().isEmpty())) {
            throw new IllegalArgumentException("At least one field (notes or items) must be provided for update");
        }

        // Update notes if provided
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            partRequest.setNotes(request.getNotes());
            log.info("Updated notes for part request {}", partRequestId);
        }

        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Delete old items
            List<PartRequestItem> oldItems = partRequestItemRepo.findByPartRequestId(partRequestId);
            partRequestItemRepo.deleteAll(oldItems);
            log.info("Deleted {} old part items for part request {}", oldItems.size(), partRequestId);

            // Create new items
            List<PartRequestItem> newItems = new ArrayList<>();
            for (PartRequestItemDto itemDto : request.getItems()) {
                PartRequestItem item = PartRequestItem.builder()
                        .partRequest(partRequest)
                        .partName(itemDto.getPartName())
                        .partNumber(itemDto.getPartNumber())
                        .quantity(itemDto.getQuantity())
                        .description(itemDto.getDescription())
                        .estimatedPrice(itemDto.getEstimatedPrice())
                        .procurementStatus(com.autoadda.apis.garage.util.ItemProcurementStatus.PENDING_PROCUREMENT)
                        .build();
                newItems.add(partRequestItemRepo.save(item));
            }
            log.info("Created {} new part items for part request {}", newItems.size(), partRequestId);
        }

        // Save updated part request
        partRequest = partRequestRepo.save(partRequest);

        log.info("Part request {} updated successfully", partRequestId);

        // Send updated SMS notification to customer
        sendPartRequestNotificationToCustomer(partRequest);

        return convertToResponse(partRequest);
    }

    /**
     * Customer responds to part request with their choice
     */
    @Transactional
    public PartRequestResponse submitCustomerResponse(Integer partRequestId, CustomerPartResponseRequest request) {
        log.info("Customer submitting response for part request: {}", partRequestId);

        PartRequest partRequest = partRequestRepo.findById(partRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Part request not found"));

        // Validate current status
        if (partRequest.getStatus() != PartRequestStatus.PENDING_CUSTOMER_RESPONSE) {
            throw new IllegalStateException("Customer response already submitted");
        }

        // Validate part condition is provided if garage will procure
        if (request.getChoice() == CustomerChoice.GARAGE_PROCURE && request.getPartCondition() == null) {
            throw new IllegalArgumentException("Part condition must be specified when garage procures");
        }

        // Create customer response
        CustomerPartResponse customerResponse = CustomerPartResponse.builder()
                .partRequest(partRequest)
                .choice(request.getChoice())
                .partCondition(request.getPartCondition())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .customerComments(request.getCustomerComments())
                .build();

        customerPartResponseRepo.save(customerResponse);

        // Initialize procurement status for all items based on customer choice
        List<PartRequestItem> items = partRequestItemRepo.findByPartRequestId(partRequestId);
        if (request.getChoice() == CustomerChoice.SELF_PROVIDE) {
            // Customer will provide all items
            for (PartRequestItem item : items) {
                item.setProcurementStatus(com.autoadda.apis.garage.util.ItemProcurementStatus.CUSTOMER_WILL_PROVIDE);
                partRequestItemRepo.save(item);
            }
            partRequest.setStatus(PartRequestStatus.WAITING_FOR_CUSTOMER_DELIVERY);
        } else {
            // Garage will procure all items
            for (PartRequestItem item : items) {
                item.setProcurementStatus(com.autoadda.apis.garage.util.ItemProcurementStatus.PENDING_PROCUREMENT);
                partRequestItemRepo.save(item);
            }
            partRequest.setStatus(PartRequestStatus.GARAGE_WILL_PROCURE);
        }

        partRequest.setCustomerResponseDate(LocalDateTime.now());
        partRequestRepo.save(partRequest);

        log.info("Customer response submitted successfully. Choice: {}. Updated {} items procurement status",
                request.getChoice(), items.size());

        // Send confirmation SMS to customer
        sendCustomerResponseConfirmation(partRequest, request.getChoice());

        return convertToResponse(partRequest);
    }

    /**
     * Garage adds procurement details and automatically adds to invoice
     */
    @Transactional
    public PartRequestResponse addGarageProcurement(GarageProcurementRequest request) {
        log.info("Adding garage procurement for part request item: {}", request.getPartRequestItemId());

        PartRequestItem item = partRequestItemRepo.findById(request.getPartRequestItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Part request item not found"));

        PartRequest partRequest = item.getPartRequest();

        // Validate status
        if (partRequest.getStatus() != PartRequestStatus.GARAGE_WILL_PROCURE &&
                partRequest.getStatus() != PartRequestStatus.GARAGE_PROCURING) {
            throw new IllegalStateException("Garage cannot procure parts for this request");
        }

        // Get technician who procured (optional)
        Technician procuredBy = null;
        if (request.getProcuredByUserId() != null) {
            procuredBy = technicianRepo.findById(request.getProcuredByUserId())
                    .orElse(null);
        }

        // Create garage procurement record
        GarageProcurement procurement = GarageProcurement.builder()
                .partRequestItem(item)
                .supplierName(request.getSupplierName())
                .supplierContact(request.getSupplierContact())
                .purchasePrice(request.getPurchasePrice())
                .partCondition(request.getPartCondition())
                .receiptNumber(request.getReceiptNumber())
                .procuredBy(procuredBy)
                .notes(request.getNotes())
                .build();

        garageProcurementRepo.save(procurement);

        // Update this item's procurement status
        item.setProcurementStatus(com.autoadda.apis.garage.util.ItemProcurementStatus.PROCURED);
        partRequestItemRepo.save(item);

        log.info("Garage procurement added successfully for item: {}", item.getId());

        // Check if all items have been procured
        List<PartRequestItem> allItems = partRequestItemRepo.findByPartRequestId(partRequest.getId());
        boolean allItemsProcured = allItems.stream()
                .allMatch(i -> i.getProcurementStatus() == com.autoadda.apis.garage.util.ItemProcurementStatus.PROCURED ||
                               i.getProcurementStatus() == com.autoadda.apis.garage.util.ItemProcurementStatus.CUSTOMER_PROVIDED);

        // Update parent part request status only if all items are procured
        if (allItemsProcured) {
            partRequest.setStatus(PartRequestStatus.PART_RECEIVED);
            partRequestRepo.save(partRequest);
            log.info("All items procured/provided. Updated part request {} status to PART_RECEIVED", partRequest.getId());
        } else {
            // Update to GARAGE_PROCURING if not already
            if (partRequest.getStatus() != PartRequestStatus.GARAGE_PROCURING) {
                partRequest.setStatus(PartRequestStatus.GARAGE_PROCURING);
                partRequestRepo.save(partRequest);
            }
            long procuredCount = allItems.stream()
                    .filter(i -> i.getProcurementStatus() == com.autoadda.apis.garage.util.ItemProcurementStatus.PROCURED ||
                                 i.getProcurementStatus() == com.autoadda.apis.garage.util.ItemProcurementStatus.CUSTOMER_PROVIDED)
                    .count();
            log.info("Partial procurement: {}/{} items procured for part request {}",
                    procuredCount, allItems.size(), partRequest.getId());
        }

        // Automatically add to invoice
        addProcuredPartToInvoice(partRequest, item, procurement);

        return convertToResponse(partRequest);
    }

    /**
     * Mark part as received when customer delivers it
     * This marks ALL items as customer-provided
     */
    @Transactional
    public PartRequestResponse markPartReceived(Integer partRequestId) {
        log.info("Marking parts as received for part request: {}", partRequestId);

        PartRequest partRequest = partRequestRepo.findById(partRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Part request not found"));

        // Validate status
        if (partRequest.getStatus() != PartRequestStatus.WAITING_FOR_CUSTOMER_DELIVERY) {
            throw new IllegalStateException("Part request is not waiting for customer delivery");
        }

        // Mark all items as customer provided
        List<PartRequestItem> items = partRequestItemRepo.findByPartRequestId(partRequestId);
        for (PartRequestItem item : items) {
            item.setProcurementStatus(com.autoadda.apis.garage.util.ItemProcurementStatus.CUSTOMER_PROVIDED);
            partRequestItemRepo.save(item);
        }

        partRequest.setStatus(PartRequestStatus.PART_RECEIVED);
        partRequestRepo.save(partRequest);

        log.info("All {} parts marked as customer-provided successfully", items.size());

        return convertToResponse(partRequest);
    }

    /**
     * Mark individual part request item as received when customer delivers it
     * This allows for partial deliveries
     */
    @Transactional
    public PartRequestResponse markPartItemReceived(Integer partRequestItemId) {
        log.info("Marking individual part item as received: {}", partRequestItemId);

        PartRequestItem item = partRequestItemRepo.findById(partRequestItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Part request item not found"));

        PartRequest partRequest = item.getPartRequest();

        // Validate status
        if (partRequest.getStatus() != PartRequestStatus.WAITING_FOR_CUSTOMER_DELIVERY) {
            throw new IllegalStateException("Part request is not waiting for customer delivery");
        }

        // Mark this specific item as customer provided
        item.setProcurementStatus(com.autoadda.apis.garage.util.ItemProcurementStatus.CUSTOMER_PROVIDED);
        partRequestItemRepo.save(item);

        log.info("Part item {} marked as customer-provided", partRequestItemId);

        // Check if all items have been provided by customer
        List<PartRequestItem> allItems = partRequestItemRepo.findByPartRequestId(partRequest.getId());
        boolean allItemsProvided = allItems.stream()
                .allMatch(i -> i.getProcurementStatus() == com.autoadda.apis.garage.util.ItemProcurementStatus.CUSTOMER_PROVIDED);

        // Update parent part request status only if all items are provided
        if (allItemsProvided) {
            partRequest.setStatus(PartRequestStatus.PART_RECEIVED);
            partRequestRepo.save(partRequest);
            log.info("All items customer-provided. Updated part request {} status to PART_RECEIVED", partRequest.getId());
        } else {
            long providedCount = allItems.stream()
                    .filter(i -> i.getProcurementStatus() == com.autoadda.apis.garage.util.ItemProcurementStatus.CUSTOMER_PROVIDED)
                    .count();
            log.info("Partial delivery: {}/{} items provided for part request {}",
                    providedCount, allItems.size(), partRequest.getId());
        }

        return convertToResponse(partRequest);
    }

    /**
     * Get part request by ID
     */
    public PartRequestResponse getPartRequestById(Integer id) {
        PartRequest partRequest = partRequestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part request not found"));
        return convertToResponse(partRequest);
    }

    /**
     * Get part request by job card ID
     */
    public PartRequestResponse getPartRequestByJobCardId(Integer jobCardId) {
        PartRequest partRequest = partRequestRepo.findByJobCardId(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Part request not found for this job card"));
        return convertToResponse(partRequest);
    }

    /**
     * Get all part requests for a customer
     */
    public List<PartRequestResponse> getPartRequestsByCustomerId(Integer customerId) {
        List<PartRequest> partRequests = partRequestRepo.findByCustomerId(customerId);
        return partRequests.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Get pending part requests for a customer
     */
    public List<PartRequestResponse> getPendingPartRequestsByCustomerId(Integer customerId) {
        List<PartRequest> partRequests = partRequestRepo.findByCustomerIdAndStatus(
                customerId, PartRequestStatus.PENDING_CUSTOMER_RESPONSE);
        return partRequests.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Get part requests by garage ID
     */
    public List<PartRequestResponse> getPartRequestsByGarageId(Integer garageId) {
        List<PartRequest> partRequests = partRequestRepo.findByJobCard_WorkOrder_Garage_Id(garageId);
        return partRequests.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Send SMS notification to customer about part request
     */
    private void sendPartRequestNotificationToCustomer(PartRequest partRequest) {
        try {
            VehicleDetailResponse vehicleResponse = vehicleClient.getVehicleDetailsByIdResponse(
                    partRequest.getVehicleId());

            if (vehicleResponse != null && vehicleResponse.getOwner() != null) {
                String customerPhone = vehicleResponse.getOwner().getMobileNumber();
                String serviceName = partRequest.getJobCard().getService().getService().getAvailableService().getName();

                StringBuilder message = new StringBuilder();
                message.append("🔧 PARTS REQUIRED - AutoAdda\n\n");
                message.append("Service: ").append(serviceName).append("\n");
                message.append("Vehicle: ").append(vehicleResponse.getBrand()).append(" ")
                        .append(vehicleResponse.getModel()).append("\n");
                message.append("Reg No: ").append(vehicleResponse.getNumber()).append("\n\n");
                message.append("Parts needed:\n");

                List<PartRequestItem> items = partRequestItemRepo.findByPartRequestId(partRequest.getId());
                for (int i = 0; i < Math.min(items.size(), 3); i++) {
                    PartRequestItem item = items.get(i);
                    message.append("• ").append(item.getPartName())
                            .append(" (Qty: ").append(item.getQuantity()).append(")\n");
                }

                if (items.size() > 3) {
                    message.append("...and ").append(items.size() - 3).append(" more\n");
                }

                message.append("\nPlease open the AutoAdda app to choose:\n");
                message.append("1. You'll provide the parts\n");
                message.append("2. Garage should buy (genuine/used)");

                SmsRequest smsRequest = new SmsRequest();
                smsRequest.setPhoneNumber(customerPhone);
                smsRequest.setMessage(message.toString());

                authClient.sendSms(smsRequest);
                log.info("Part request notification sent to customer: {}", customerPhone);
            }
        } catch (Exception e) {
            log.error("Failed to send part request notification", e);
        }
    }

    /**
     * Send confirmation SMS after customer response
     */
    private void sendCustomerResponseConfirmation(PartRequest partRequest, CustomerChoice choice) {
        try {
            VehicleDetailResponse vehicleResponse = vehicleClient.getVehicleDetailsByIdResponse(
                    partRequest.getVehicleId());

            if (vehicleResponse != null && vehicleResponse.getOwner() != null) {
                String customerPhone = vehicleResponse.getOwner().getMobileNumber();
                String serviceName = partRequest.getJobCard().getService().getService().getAvailableService().getName();

                StringBuilder message = new StringBuilder();
                message.append("✅ RESPONSE CONFIRMED - AutoAdda\n\n");
                message.append("Service: ").append(serviceName).append("\n");
                message.append("Your choice: ");

                if (choice == CustomerChoice.SELF_PROVIDE) {
                    message.append("You will provide the parts\n\n");
                    message.append("Please deliver the parts to the garage soon.\n");
                    message.append("Work will resume once parts are received.");
                } else {
                    message.append("Garage will purchase\n\n");
                    message.append("The garage will procure the parts.\n");
                    message.append("Part cost will be added to your invoice.");
                }

                SmsRequest smsRequest = new SmsRequest();
                smsRequest.setPhoneNumber(customerPhone);
                smsRequest.setMessage(message.toString());

                authClient.sendSms(smsRequest);
                log.info("Response confirmation sent to customer: {}", customerPhone);
            }
        } catch (Exception e) {
            log.error("Failed to send response confirmation", e);
        }
    }

    /**
     * Automatically add procured part to invoice
     */
    private void addProcuredPartToInvoice(PartRequest partRequest, PartRequestItem item,
                                          GarageProcurement procurement) {
        try {
            // Get or create invoice for the work order
            Integer workOrderId = partRequest.getJobCard().getWorkOrder().getId();
            Invoice invoice = invoiceRepo.findByWorkOrderId(workOrderId).orElse(null);

            if (invoice == null) {
                log.warn("No invoice found for work order: {}. Part will be added when invoice is generated.",
                        workOrderId);
                // Don't update parent status here - invoice doesn't exist yet
                return;
            }

            // Create invoice line item for the part
            InvoiceLineItem lineItem = InvoiceLineItem.builder()
                    .invoice(invoice)
                    .description(item.getPartName() + " - " + item.getDescription())
                    .quantity(item.getQuantity())
                    .price(procurement.getPurchasePrice())
                    .category("PART")
                    .notes("Part Number: " + (item.getPartNumber() != null ? item.getPartNumber() : "N/A") +
                            ", Condition: " + procurement.getPartCondition() +
                            ", Supplier: " + (procurement.getSupplierName() != null ? procurement.getSupplierName() : "N/A"))
                    .build();

            // Save the line item
            invoiceLineItemRepo.save(lineItem);

            // Mark this item as added to invoice
            item.setAddedToInvoice(true);
            partRequestItemRepo.save(item);

            // Recalculate invoice totals
            double subtotal = invoice.getLineItems().stream()
                    .mapToDouble(li -> li.getPrice() * li.getQuantity())
                    .sum();
            double taxAmount = subtotal * (invoice.getTaxRate() / 100.0);
            invoice.setTotalAmount(subtotal + taxAmount);
            invoice.setTaxAmount(taxAmount);

            // Save updated invoice
            invoiceRepo.save(invoice);

            log.info("Part item {} added to invoice successfully. Invoice ID: {}, Amount: {}",
                    item.getId(), invoice.getId(), procurement.getPurchasePrice());

            // Check if all procured items have been added to invoice
            List<PartRequestItem> allItems = partRequestItemRepo.findByPartRequestId(partRequest.getId());

            // Only items that were procured by garage need to be added to invoice
            List<PartRequestItem> garageProcuredItems = allItems.stream()
                    .filter(i -> i.getProcurementStatus() == com.autoadda.apis.garage.util.ItemProcurementStatus.PROCURED)
                    .toList();

            if (!garageProcuredItems.isEmpty()) {
                boolean allAddedToInvoice = garageProcuredItems.stream()
                        .allMatch(i -> Boolean.TRUE.equals(i.getAddedToInvoice()));

                // Only update parent status if all garage-procured items are added to invoice
                if (allAddedToInvoice) {
                    partRequest.setStatus(PartRequestStatus.ADDED_TO_INVOICE);
                    partRequestRepo.save(partRequest);
                    log.info("All procured items added to invoice. Updated part request {} status to ADDED_TO_INVOICE",
                            partRequest.getId());
                } else {
                    long addedCount = garageProcuredItems.stream()
                            .filter(i -> Boolean.TRUE.equals(i.getAddedToInvoice()))
                            .count();
                    log.info("Partial invoice addition: {}/{} procured items added to invoice for part request {}",
                            addedCount, garageProcuredItems.size(), partRequest.getId());
                }
            }

        } catch (Exception e) {
            log.error("Failed to add part to invoice", e);
            // Keep current status so it can be manually added later
        }
    }

    /**
     * Convert entity to response DTO
     */
    private PartRequestResponse convertToResponse(PartRequest partRequest) {
        // Get vehicle details
        String vehicleDetails = "";
        try {
            VehicleDetailResponse vehicleResponse = vehicleClient.getVehicleDetailsByIdResponse(
                    partRequest.getVehicleId());
            if (vehicleResponse != null) {
                vehicleDetails = vehicleResponse.getBrand() + " " +
                        vehicleResponse.getModel() + " - " +
                        vehicleResponse.getNumber();
            }
        } catch (Exception e) {
            log.error("Failed to fetch vehicle details", e);
        }

        // Convert items
        List<PartRequestItem> items = partRequestItemRepo.findByPartRequestId(partRequest.getId());
        List<PartRequestItemResponse> itemResponses = items.stream()
                .map(this::convertItemToResponse)
                .toList();

        // Convert customer response if exists
        CustomerPartResponseResponse customerResponseResponse = null;
        if (partRequest.getCustomerResponse() != null) {
            customerResponseResponse = convertCustomerResponseToResponse(partRequest.getCustomerResponse());
        }

        return PartRequestResponse.builder()
                .id(partRequest.getId())
                .jobCardId(partRequest.getJobCard().getId())
                .jobCardServiceName(partRequest.getJobCard().getService().getService().getAvailableService().getName())
                .requestedByTechnicianId(partRequest.getRequestedBy().getId())
                .requestedByTechnicianName(partRequest.getRequestedBy().getFirstName())
                .status(partRequest.getStatus())
                .requestDate(partRequest.getRequestDate())
                .customerResponseDate(partRequest.getCustomerResponseDate())
                .customerId(partRequest.getCustomerId())
                .vehicleId(partRequest.getVehicleId())
                .vehicleDetails(vehicleDetails)
                .items(itemResponses)
                .customerResponse(customerResponseResponse)
                .notes(partRequest.getNotes())
                .build();
    }

    private PartRequestItemResponse convertItemToResponse(PartRequestItem item) {
        GarageProcurementResponse procurementResponse = null;
        if (item.getGarageProcurement() != null) {
            procurementResponse = convertProcurementToResponse(item.getGarageProcurement());
        }

        return PartRequestItemResponse.builder()
                .id(item.getId())
                .partName(item.getPartName())
                .partNumber(item.getPartNumber())
                .quantity(item.getQuantity())
                .description(item.getDescription())
                .estimatedPrice(item.getEstimatedPrice())
                .procurementStatus(item.getProcurementStatus())
                .addedToInvoice(item.getAddedToInvoice())
                .procurement(procurementResponse)
                .build();
    }

    private GarageProcurementResponse convertProcurementToResponse(GarageProcurement procurement) {
        String procuredByName = procurement.getProcuredBy() != null ?
                procurement.getProcuredBy().getFirstName() : null;

        return GarageProcurementResponse.builder()
                .id(procurement.getId())
                .supplierName(procurement.getSupplierName())
                .supplierContact(procurement.getSupplierContact())
                .purchasePrice(procurement.getPurchasePrice())
                .partCondition(procurement.getPartCondition())
                .receiptNumber(procurement.getReceiptNumber())
                .procurementDate(procurement.getProcurementDate())
                .procuredByUserName(procuredByName)
                .notes(procurement.getNotes())
                .build();
    }

    private CustomerPartResponseResponse convertCustomerResponseToResponse(CustomerPartResponse response) {
        return CustomerPartResponseResponse.builder()
                .id(response.getId())
                .choice(response.getChoice())
                .partCondition(response.getPartCondition())
                .responseDate(response.getResponseDate())
                .expectedDeliveryDate(response.getExpectedDeliveryDate())
                .customerComments(response.getCustomerComments())
                .build();
    }
}
