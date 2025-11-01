package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.GarageProcurement;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.PartRequest;
import com.autoadda.apis.garage.entity.PartRequestItem;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.repository.PartRequestRepository;
import com.autoadda.apis.garage.request.InvoiceLineItemRequest;
import com.autoadda.apis.garage.util.JobCardStatus;
import com.autoadda.apis.garage.util.PartRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceLineItemGeneratorService {

    private final PartRequestRepository partRequestRepository;

    /**
     * Generates line items from a WorkOrder based on completed JobCards
     *
     * @param workOrder the WorkOrder to generate line items from
     * @return list of InvoiceLineItemRequest objects
     */
    public List<InvoiceLineItemRequest> generateLineItemsFromWorkOrder(WorkOrder workOrder) {
        List<InvoiceLineItemRequest> lineItems = new ArrayList<> ( );

        // Filter only completed job cards
        List<JobCard> completedJobCards = workOrder.getJobCards ( ).stream ( )
                .filter ( jc -> jc.getStatus ( ) == JobCardStatus.WORK_COMPLETED ||
                        jc.getStatus ( ) == JobCardStatus.QUALITY_CHECK_PASSED ||
                        jc.getStatus ( ) == JobCardStatus.COMPLETED )
                .toList ( );

        if ( completedJobCards.isEmpty ( ) ) {
            log.warn ( "No completed job cards found for WorkOrder ID: {}", workOrder.getId ( ) );
            return lineItems;
        }

        // Generate service line items from job cards
        for (JobCard jobCard : completedJobCards) {
            AppointmentServices appointmentService = jobCard.getService ( );
            if ( appointmentService != null && appointmentService.getService ( ) != null ) {
                AvailableService availableService = appointmentService.getService ( ).getAvailableService ( );

                InvoiceLineItemRequest serviceItem = InvoiceLineItemRequest.builder ( )
                        .description ( availableService.getName ( ) )
                        .quantity ( 1 )
                        .price ( appointmentService.getService ( ).getPrice ( ) )
                        .category ( "SERVICE" )
                        .serviceId ( availableService.getId ( ) )
                        .notes ( "JobCard ID: " + jobCard.getId ( ) )
                        .build ( );

                lineItems.add ( serviceItem );
            }
        }

        // Add labor charges from each appointment service
        for (JobCard jobCard : completedJobCards) {
            AppointmentServices appointmentService = jobCard.getService ( );
            if ( appointmentService != null && appointmentService.getLabourPrice ( ) != null && appointmentService.getLabourPrice ( ) > 0 ) {
                InvoiceLineItemRequest laborItem = InvoiceLineItemRequest.builder ( )
                        .description ( "Labor - " + (appointmentService.getService ( ) != null &&
                                appointmentService.getService ( ).getAvailableService ( ) != null ?
                                appointmentService.getService ( ).getAvailableService ( ).getName ( ) : "Service") )
                        .quantity ( 1 )
                        .price ( appointmentService.getLabourPrice ( ) )
                        .category ( "LABOR" )
                        .serviceId ( appointmentService.getService ( ) != null &&
                                appointmentService.getService ( ).getAvailableService ( ) != null ?
                                appointmentService.getService ( ).getAvailableService ( ).getId ( ) : null )
                        .notes ( "JobCard ID: " + jobCard.getId ( ) )
                        .build ( );

                lineItems.add ( laborItem );
            }
        }

        // Add parts/materials from part requests
        addPartsLineItems(completedJobCards, lineItems);

        return lineItems;
    }

    /**
     * Adds parts/materials line items from PartRequests associated with completed JobCards
     *
     * @param completedJobCards list of completed job cards
     * @param lineItems list to add part line items to
     */
    private void addPartsLineItems(List<JobCard> completedJobCards, List<InvoiceLineItemRequest> lineItems) {
        for (JobCard jobCard : completedJobCards) {
            Optional<PartRequest> partRequestOpt = partRequestRepository.findByJobCardId(jobCard.getId());

            if (partRequestOpt.isPresent()) {
                PartRequest partRequest = partRequestOpt.get();
                PartRequestStatus status = partRequest.getStatus();

                // Skip cancelled, pending, or already added to invoice
                if (status == PartRequestStatus.CANCELLED ||
                    status == PartRequestStatus.PENDING_CUSTOMER_RESPONSE ||
                    status == PartRequestStatus.ADDED_TO_INVOICE) {
                    continue;
                }

                // Process each part request item
                for (PartRequestItem item : partRequest.getItems()) {
                    InvoiceLineItemRequest partLineItem = createPartLineItem(item, partRequest.getStatus(), jobCard.getId());
                    if (partLineItem != null) {
                        lineItems.add(partLineItem);
                    }
                }
            }
        }
    }

    /**
     * Creates an invoice line item for a part request item based on its status
     *
     * @param item the part request item
     * @param status the part request status
     * @param jobCardId the job card ID for reference
     * @return InvoiceLineItemRequest or null if item should not be added
     */
    private InvoiceLineItemRequest createPartLineItem(PartRequestItem item, PartRequestStatus status, Integer jobCardId) {
        Double price = 0.0;
        String notes = "JobCard ID: " + jobCardId;

        // Handle garage procurement statuses
        if (status == PartRequestStatus.GARAGE_WILL_PROCURE ||
            status == PartRequestStatus.GARAGE_PROCURING ||
            status == PartRequestStatus.PART_RECEIVED) {

            GarageProcurement procurement = item.getGarageProcurement();
            if (procurement != null) {
                // Use actual purchase price
                price = procurement.getPurchasePrice();
                notes += ", Part Number: " + (item.getPartNumber() != null ? item.getPartNumber() : "N/A") +
                        ", Condition: " + procurement.getPartCondition() +
                        ", Supplier: " + (procurement.getSupplierName() != null ? procurement.getSupplierName() : "N/A");
            } else {
                // No procurement yet, use estimated price if available
                price = item.getEstimatedPrice() != null ? item.getEstimatedPrice() : 0.0;
                notes += ", Estimated Price (Procurement Pending)";
            }
        }
        // Handle customer-provided parts
        else if (status == PartRequestStatus.CUSTOMER_WILL_PROVIDE ||
                 status == PartRequestStatus.WAITING_FOR_CUSTOMER_DELIVERY) {
            price = 0.0;
            notes += ", Customer Provided";
        }
        // Skip other statuses
        else {
            return null;
        }

        return InvoiceLineItemRequest.builder()
                .description(item.getPartName() + " - " + item.getDescription())
                .quantity(item.getQuantity())
                .price(price)
                .category("PART")
                .notes(notes)
                .build();
    }

    /**
     * Generates standard line items that can be customized
     */
    public List<InvoiceLineItemRequest> generateStandardLineItems() {
        List<InvoiceLineItemRequest> standardItems = new ArrayList<> ( );

        // Common service items
        standardItems.add ( InvoiceLineItemRequest.builder ( )
                .description ( "Diagnostic Fee" )
                .quantity ( 1 )
                .price ( 25.0 )
                .category ( "SERVICE" )
                .build ( ) );

        standardItems.add ( InvoiceLineItemRequest.builder ( )
                .description ( "Shop Supplies" )
                .quantity ( 1 )
                .price ( 15.0 )
                .category ( "MATERIALS" )
                .build ( ) );

        return standardItems;
    }
}