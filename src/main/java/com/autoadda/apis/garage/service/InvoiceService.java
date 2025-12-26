package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.AppointmentInvoiceMapper;
import com.autoadda.apis.garage.mapper.InvoiceLineItemMapper;
import com.autoadda.apis.garage.mapper.InvoiceMapper;
import com.autoadda.apis.garage.mapper.InvoiceResponseMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.InvoicePayRequest;
import com.autoadda.apis.garage.request.InvoiceRequest;
import com.autoadda.apis.garage.request.NotificationContent;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class InvoiceService {

    public static final String WORK_ORDER_NOT_FOUND = "Work order not found";
    private final AppointmentInvoiceRepository invoiceRepo;
    private final AppointmentInvoiceBreakDownRepository breakdownRepo;
    private final AppointmentDetailsRepository appointmentRepo;
    private final GarageRepository garageRepo;
    private final WorkOrderRepository workOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final AppointmentInvoiceMapper appointmentInvoiceMapper;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceLineItemMapper lineItemMapper;
    private final InvoiceResponseMapper invoiceResponseMapper;
    private final InvoiceLineItemGeneratorService lineItemGenerator;
    private final AuthClient authClient;
    private final VehicleClient vehicleClient;
    private final NotificationDispatchService notificationDispatchService;
    private final NotificationMessageBuilder notificationMessageBuilder;
    private final GarageSubscriptionRepository garageSubscriptionRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;

    /**
     * Prepares an invoice for a given appointment and garage.
     *
     * @param request the request containing appointment and garage details
     * @return the created AppointmentInvoice
     */
    @Transactional
    public AppointmentInvoiceResponse prepareInvoice(AppointmentInvoiceRequest request) {
        AppointmentDetails appointment = appointmentRepo.findById ( request.getAppointmentId ( ) )
                .orElseThrow ( () -> new RuntimeException ( "Appointment not found" ) );

        Garage garage = garageRepo.findById ( request.getGarageId ( ) )
                .orElseThrow ( () -> new RuntimeException ( "Garage not found" ) );

        AppointmentInvoice invoice = invoiceMapper.toEntity ( request, garage, appointment );
        invoiceRepo.save ( invoice );

        List<AppointmentInvoiceBreakDown> breakdowns = request.getService ( ).stream ( )
                .map ( service -> invoiceMapper.toBreakdown ( service, invoice, garage, appointment ) )
                .toList ( );
        breakdownRepo.saveAll ( breakdowns );

        appointment.setStatus ( Status.INVOICE_INITIATED );
        appointmentRepo.save ( appointment );

        return appointmentInvoiceMapper.fromEntity ( invoice );
    }

    /**
     * Updates the payment status of an invoice based on the appointment payment
     * request.
     *
     * @param request the request containing appointment ID and payment status
     * @return the updated AppointmentInvoice
     */
    @Transactional
    public InvoiceResponse updateInvoicePayment(InvoicePayRequest request) {
        Invoice invoice = invoiceRepository.findById ( request.getInvoiceId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Invoice not found" ) );

        // Update payment status
        invoice.setPaymentStatus ( request.getPaymentStatus ( ) );

        // Update payment method if provided
        if ( request.getPaymentMethod ( ) != null ) {
            invoice.setPaymentMethod ( request.getPaymentMethod ( ) );
        }

        // Update amount paid if provided
        if ( request.getAmountPaid ( ) != null ) {
            invoice.setAmountPaid ( request.getAmountPaid ( ) );
        }

        // Update notes/comments if provided
        if ( request.getComments ( ) != null ) {
            invoice.setNotes ( request.getComments ( ) );
        }

        // Set isPaid flag based on payment status
        invoice.setIsPaid ( request.getPaymentStatus ( ) == InvoiceStatus.PAID );

        WorkOrder workOrder = invoice.getWorkOrder ( );

        // Update WorkOrder and Appointment status based on payment
        if ( request.getPaymentStatus ( ) == InvoiceStatus.PAID ) {
            // Update WorkOrder status when payment is received
            if ( workOrder.getStatus ( ) != WorkOrderStatus.PAYMENT_RECEIVED ) {
                workOrder.updateStatus ( WorkOrderStatus.PAYMENT_RECEIVED );
                workOrderRepository.save ( workOrder );
            }

            // Update appointment status to ready for delivery
            AppointmentDetails appointment = workOrder.getAppointment ( );
            if ( appointment != null && appointment.getStatus ( ) != Status.READY_FOR_DELIVERY ) {
                appointment.setStatus ( Status.READY_FOR_DELIVERY );
                appointmentRepo.save ( appointment );
            }
        } else if ( request.getPaymentStatus ( ) == InvoiceStatus.PENDING && (workOrder.getStatus ( ) == WorkOrderStatus.INVOICE_INITIATED ||
                 workOrder.getStatus ( ) == WorkOrderStatus.INVOICED) ) {
                workOrder.updateStatus ( WorkOrderStatus.PENDING_PAYMENT );
                workOrderRepository.save ( workOrder );
            }


        return invoiceResponseMapper.toResponse ( invoiceRepository.save ( invoice ) );
    }

    /**
     * Retrieves all invoices for a specific garage.
     *
     * @param workOrderId the ID of the garage
     * @return a list of AppointmentInvoices for the specified garage
     */
    @Transactional
    public Invoice generateInvoice(Integer workOrderId, List<InvoiceLineItem> lineItems) {
        WorkOrder workOrder = workOrderRepository.findById ( workOrderId )
                .orElseThrow ( () -> new EntityNotFoundException (WORK_ORDER_NOT_FOUND) );

        boolean hasCompletedJobCards = workOrder.getJobCards ( ).stream ( )
                .anyMatch ( j -> j.getStatus ( ) == JobCardStatus.COMPLETED );

        if ( ! hasCompletedJobCards ) {
            throw new IllegalStateException ( "Cannot generate invoice for a work order with no completed job cards." );
        }

        Invoice invoice = new Invoice ( );
        invoice.setWorkOrder ( workOrder );
        invoice.setCreatedAt ( LocalDateTime.now ( ) );

        for (InvoiceLineItem item : lineItems) {
            if ( item.getPrice ( ) < 0 || item.getQuantity ( ) <= 0 ) {
                throw new IllegalArgumentException ( "Invalid line item: Price or quantity not valid." );
            }
            item.setInvoice ( invoice ); // link line item back to invoice
        }

        invoice.setLineItems ( lineItems );

        // Update WorkOrder status to INVOICE_INITIATED when invoice is created
        if ( workOrder.getStatus ( ) != WorkOrderStatus.INVOICE_INITIATED &&
                workOrder.getStatus ( ) != WorkOrderStatus.INVOICED &&
                workOrder.getStatus ( ) != WorkOrderStatus.PENDING_PAYMENT &&
                workOrder.getStatus ( ) != WorkOrderStatus.PAYMENT_RECEIVED ) {
            workOrder.updateStatus ( WorkOrderStatus.INVOICE_INITIATED );
            workOrderRepository.save ( workOrder );
        }

        return invoiceRepository.save ( invoice );
    }

    /**
     * Retrieves all invoices for a specific garage.
     *
     * @param id the ID of the garage
     * @return a list of AppointmentInvoices for the specified garage
     */
    @Transactional
    public Invoice getInvoiceById(Integer id) {
        return invoiceRepository.findById ( id ).orElseThrow ( () -> new EntityNotFoundException ( "Invoice not found" ) );
    }



    /**
     * Retrieves all invoices for a specific garage (through WorkOrder).
     *
     * @param garageId the ID of the garage
     * @return a list of Invoices for the specified garage
     */
    @Transactional
    public List<Invoice> getInvoicesForGarage(Integer garageId) {
        return invoiceRepository.findAllByGarageId ( garageId );
    }

    /**
     * Updates the payment status of a WorkOrder-based invoice.
     *
     * @param invoiceId     the ID of the invoice
     * @param paymentStatus the new payment status
     * @return the updated Invoice
     */
    @Transactional
    public Invoice updateInvoicePaymentStatus(Integer invoiceId, InvoiceStatus paymentStatus) {
        Invoice invoice = getInvoiceById ( invoiceId );
        invoice.setPaymentStatus ( paymentStatus );
        invoice.setIsPaid ( paymentStatus == InvoiceStatus.PAID );

        WorkOrder workOrder = invoice.getWorkOrder ( );

        if (Boolean.TRUE.equals(invoice.getIsPaid ( )) ) {
            // Update WorkOrder status when payment is received
            if ( workOrder.getStatus ( ) != WorkOrderStatus.PAYMENT_RECEIVED ) {
                workOrder.updateStatus ( WorkOrderStatus.PAYMENT_RECEIVED );
                workOrderRepository.save ( workOrder );
            }
        } else {
            // If not paid, update to appropriate pending status based on current state
            if ( workOrder.getStatus ( ) == WorkOrderStatus.INVOICE_INITIATED ) {
                workOrder.updateStatus ( WorkOrderStatus.PENDING_PAYMENT );
                workOrderRepository.save ( workOrder );
            }
        }

        return invoiceRepository.save ( invoice );
    }

    /**
     * Finds invoice by WorkOrder ID.
     *
     * @param workOrderId the ID of the work order
     * @return the Invoice if found
     */
    @Transactional
    public InvoiceResponse getInvoiceByWorkOrderId(Integer workOrderId) {
        return invoiceResponseMapper.toResponse ( invoiceRepository.findByWorkOrderId ( workOrderId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Invoice not found for WorkOrder ID: " + workOrderId ) ) );
    }

    // New DTO-based methods

    /**
     * Generates an invoice from InvoiceRequest DTO with automatic calculations
     */
    @Transactional
    public InvoiceResponse generateInvoiceFromRequest(InvoiceRequest request) {
        WorkOrder workOrder = workOrderRepository.findById ( request.getWorkOrderId ( ) )
                .orElseThrow ( () -> new EntityNotFoundException (WORK_ORDER_NOT_FOUND) );

        // Convert DTOs to entities
        List<InvoiceLineItem> lineItems = lineItemMapper.toEntityList ( request.getLineItems ( ) );

        Invoice invoice = new Invoice ( );
        invoice.setWorkOrder ( workOrder );
        invoice.setCreatedAt ( LocalDateTime.now ( ) );
        invoice.setTaxRate ( request.getTaxRate ( ) );
        invoice.setPaymentMethod ( request.getPaymentMethod ( ) );
        invoice.setNotes ( request.getNotes ( ) );

        // Calculate totals
        double subtotal = lineItems.stream ( )
                .mapToDouble ( item -> item.getPrice ( ) * item.getQuantity ( ) )
                .sum ( );

        // Calculate subscription discount if applicable
        double subscriptionDiscount = 0.0;
        if (workOrder.getAppointment() != null) {
            subscriptionDiscount = calculateSubscriptionDiscount(workOrder.getAppointment());
        }

        // Adjust total amount with subscription discount
        double adjustedSubtotal = subtotal - subscriptionDiscount;
        double taxAmount = adjustedSubtotal * (request.getTaxRate ( ) != null ? request.getTaxRate ( ) / 100.0 : 0.0);
        double totalAmount = adjustedSubtotal + taxAmount;

        invoice.setSubtotal ( subtotal );
        invoice.setSubscriptionDiscount ( subscriptionDiscount );
        invoice.setTotalAmount ( totalAmount );
        invoice.setTaxAmount ( taxAmount );

        // Set line items with invoice reference
        lineItems.forEach ( item -> item.setInvoice ( invoice ) );
        invoice.setLineItems ( lineItems );

        // Handle loyalty points if specified
        if ( request.getLoyaltyPointsToUse ( ) != null && request.getLoyaltyPointsToUse ( ) > 0 ) {
            invoice.setLoyaltyPointsUsed ( request.getLoyaltyPointsToUse ( ) );
            // TODO: Integrate with loyalty service
        }

        // Update WorkOrder status to INVOICE_INITIATED when invoice is created
        if ( workOrder.getStatus ( ) != WorkOrderStatus.INVOICE_INITIATED &&
                workOrder.getStatus ( ) != WorkOrderStatus.INVOICED &&
                workOrder.getStatus ( ) != WorkOrderStatus.PENDING_PAYMENT &&
                workOrder.getStatus ( ) != WorkOrderStatus.PAYMENT_RECEIVED ) {
            workOrder.updateStatus ( WorkOrderStatus.INVOICE_INITIATED );
            workOrderRepository.save ( workOrder );
        }

        Invoice savedInvoice = invoiceRepository.save ( invoice );
        return invoiceResponseMapper.toResponse ( savedInvoice );
    }

    /**
     * Auto-generates line items from WorkOrder and creates invoice
     */
    @Transactional
    public InvoiceResponse generateInvoiceFromWorkOrder(Integer workOrderId, Double taxRate, String notes) {
        WorkOrder workOrder = workOrderRepository.findById ( workOrderId )
                .orElseThrow ( () -> new EntityNotFoundException (WORK_ORDER_NOT_FOUND) );

        // Auto-generate line items from WorkOrder
        var generatedLineItems = lineItemGenerator.generateLineItemsFromWorkOrder ( workOrder );

        InvoiceRequest request = InvoiceRequest.builder ( )
                .workOrderId ( workOrderId )
                .lineItems ( generatedLineItems )
                .taxRate ( taxRate )
                .notes ( notes )
                .build ( );

        return generateInvoiceFromRequest ( request );
    }

    /**
     * Get invoice as DTO response
     */
    @Transactional
    public InvoiceResponse getInvoiceResponse(Integer invoiceId) {
        Invoice invoice = getInvoiceById ( invoiceId );
        return invoiceResponseMapper.toResponse ( invoice );
    }

    /**
     * Sends invoice details to customer via SMS
     *
     * @param invoiceId the ID of the invoice to send
     * @return GenericResponse indicating SMS status
     */
    @Transactional
    public GenericResponse sendInvoiceToCustomer(Integer invoiceId) {
        try {
            // Get invoice details
            Invoice invoice = getInvoiceById ( invoiceId );
            WorkOrder workOrder = invoice.getWorkOrder ( );
            AppointmentDetails appointment = workOrder.getAppointment ( );

            if ( appointment == null ) {
                return GenericResponse.failed ( "No appointment found for this invoice", null );
            }

            // Get vehicle details to extract customer phone number
            Integer vehicleId = appointment.getVehicleId ( );
            VehicleDetailResponse vehicleDetails = vehicleClient.getVehicleDetailsByIdResponse ( vehicleId );

            if ( vehicleDetails == null || vehicleDetails.getOwner ( ) == null ||
                    vehicleDetails.getOwner ( ).getMobileNumber ( ) == null ) {
                return GenericResponse.failed ( "Customer mobile number not found", null );
            }

            // Send multi-channel notification with preference checking
            Integer customerId = appointment.getCustomerId();

            NotificationContent content = notificationMessageBuilder.buildInvoiceReady(
                invoice,
                vehicleDetails
            );

            NotificationResult result = notificationDispatchService.sendNotification(
                customerId,
                NotificationType.SERVICE_UPDATES,
                content,
                true,  // critical
                "CUSTOMER"  // targetRole
            );

            if ( result.hasAnySuccess() ) {
                // Update WorkOrder status to INVOICED when invoice is successfully sent to customer
                if ( workOrder.getStatus ( ) == WorkOrderStatus.INVOICE_INITIATED ) {
                    workOrder.updateStatus ( WorkOrderStatus.INVOICED );
                    workOrderRepository.save ( workOrder );
                }
                return GenericResponse.makeSuccessResponse( "Invoice sent to customer successfully. " + result.getSummary(), result );
            } else {
                return GenericResponse.failed ( "Failed to send invoice to customer", result );
            }

        }
        catch ( Exception e ) {
            return GenericResponse.failed ( "Error sending invoice to customer: " + e.getMessage ( ), null );
        }
    }

    /**
     * Builds the SMS message for invoice notification
     */
    private String buildInvoiceMessage(Invoice invoice, WorkOrder workOrder, VehicleDetailResponse vehicleDetails) {
        StringBuilder message = new StringBuilder ( );

        // Header
        message.append ( "🧾 INVOICE READY - AutoAdda\n\n" );

        // Vehicle info
        message.append ( "Vehicle: " ).append ( vehicleDetails.getBrand ( ) )
                .append ( " " ).append ( vehicleDetails.getModel ( ) ).append ( "\n" );
        message.append ( "Reg. No: " ).append ( vehicleDetails.getNumber ( ) ).append ( "\n\n" );

        // Invoice details
        message.append ( "Invoice ID: " ).append ( invoice.getId ( ) ).append ( "\n" );
        message.append ( "Work Order: " ).append ( workOrder.getId ( ) ).append ( "\n" );

        // Amount details
        if ( invoice.getTotalAmount ( ) != null ) {
            message.append ( "Total Amount: ₹" ).append ( String.format ( "%.2f", invoice.getTotalAmount ( ) ) ).append ( "\n" );
        }

        if ( invoice.getTaxAmount ( ) != null && invoice.getTaxAmount ( ) > 0 ) {
            message.append ( "Tax: ₹" ).append ( String.format ( "%.2f", invoice.getTaxAmount ( ) ) ).append ( "\n" );
        }

        // Payment status
        if ( invoice.getIsPaid ( ) != null && invoice.getIsPaid ( ) ) {
            message.append ( "Status: ✅ PAID\n" );
        } else {
            message.append ( "Status: ⏳ PENDING PAYMENT\n" );
        }

        // Service summary
        if ( invoice.getLineItems ( ) != null && ! invoice.getLineItems ( ).isEmpty ( ) ) {
            message.append ( "\nServices:\n" );
            invoice.getLineItems ( ).stream ( )
                    .limit ( 3 ) // Show max 3 services
                    .forEach ( item -> message.append ( "• " ).append ( item.getDescription ( ) ).append ( "\n" ) );

            if ( invoice.getLineItems ( ).size ( ) > 3 ) {
                message.append ( "• ... and " ).append ( invoice.getLineItems ( ).size ( ) - 3 ).append ( " more\n" );
            }
        }

        // Footer
        message.append ( "\nView full details in your AutoAdda app." );
        message.append ( "\nFor queries: Contact your garage" );

        return message.toString ( );
    }

    /**
     * Calculate discount amount based on subscription.
     * If appointment used subscription, services are free or discounted.
     *
     * @param appointment The appointment with subscription
     * @return Discount amount
     */
    private double calculateSubscriptionDiscount(AppointmentDetails appointment) {
        if (appointment.getGarageSubscriptionPlanId() == null) {
            return 0.0;
        }

        // Fetch subscription plan details
        GarageSubscription plan;
        try {
            plan = garageSubscriptionRepository
                    .findById(Integer.parseInt(appointment.getGarageSubscriptionPlanId()))
                    .orElse(null);
        } catch (NumberFormatException e) {
            return 0.0;
        }

        if (plan == null) {
            return 0.0;
        }

        // Get appointment services
        List<AppointmentServices> services = appointmentServiceRepository
                .findByAppointmentId(appointment.getId());

        double discount = 0.0;

        for (AppointmentServices service : services) {
            if (service.getService() == null || service.getService().getAvailableService() == null) {
                continue;
            }

            Integer serviceId = service.getService().getAvailableService().getId();

            // Check if service is included in subscription
            boolean isIncluded = plan.getServicesIncluded().stream()
                    .anyMatch(s -> s.getId().equals(serviceId));

            if (isIncluded) {
                // Service is free under subscription
                double labourPrice = service.getLabourPrice() != null ? service.getLabourPrice() : 0.0;
                double price = service.getPrice() != null ? service.getPrice() : 0.0;
                double serviceTotal = labourPrice + price;
                discount += serviceTotal;
            }
        }

        return discount;
    }

}
