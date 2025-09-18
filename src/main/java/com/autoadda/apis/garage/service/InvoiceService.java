package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.mapper.AppointmentInvoiceMapper;
import com.autoadda.apis.garage.mapper.InvoiceLineItemMapper;
import com.autoadda.apis.garage.mapper.InvoiceMapper;
import com.autoadda.apis.garage.mapper.InvoiceResponseMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.AppointmentPayRequest;
import com.autoadda.apis.garage.request.InvoiceRequest;
import com.autoadda.apis.garage.response.AppointmentInvoiceResponse;
import com.autoadda.apis.garage.response.InvoiceResponse;
import com.autoadda.apis.garage.util.JobCardStatus;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class InvoiceService {

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
    public AppointmentInvoiceResponse updateInvoicePayment(AppointmentPayRequest request) {
        AppointmentInvoice invoice = invoiceRepo.findByAppointmentId ( request.getAppointmentId ( ) );
        invoice.setStatus ( request.getStatus ( ) );

        if ( "PAID".equalsIgnoreCase ( request.getStatus ( ) ) ) {
            AppointmentDetails appointment = invoice.getAppointment ( );
            appointment.setStatus ( Status.READY_FOR_DELIVERY );
            appointmentRepo.save ( appointment );
        }

        return appointmentInvoiceMapper.fromEntity ( invoiceRepo.save ( invoice ) );
    }

    /**
     * Retrieves all invoices for a specific garage.
     *
     * @param garageId the ID of the garage
     * @return a list of AppointmentInvoices for the specified garage
     */
    @Transactional
    public Invoice generateInvoice(Integer workOrderId, List<InvoiceLineItem> lineItems) {
        WorkOrder workOrder = workOrderRepository.findById ( workOrderId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Work order not found" ) );

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

//		double totalCost = lineItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
//		int pointsValue = pointsToRedeem; // 1 point = $1 for example
//		BigDecimal discount = BigDecimal.valueOf(pointsValue);
//
//		if (discount.compareTo(totalCost) > 0) {
//			discount = totalCost;
//			pointsToRedeem = discount.intValue();
//		}
//
//		BigDecimal amountPaid = totalCost.subtract(discount);
//
//		loyaltyService.applyLoyaltyPoints(owner, pointsToRedeem);
//		int pointsEarned = loyaltyService.calculateEarnedPoints(amountPaid);
//		loyaltyService.rewardPoints(owner, pointsEarned);
//		invoice.setLoyaltyPointsUsed(pointsToRedeem);
//		invoice.setTotalAmount(total);

        return invoiceRepository.save ( invoice );
    }

    /**
     * Retrieves all invoices for a specific garage.
     *
     * @param garageId the ID of the garage
     * @return a list of AppointmentInvoices for the specified garage
     */
    @Transactional
    public Invoice getInvoiceById(Integer id) {
        return invoiceRepository.findById ( id ).orElseThrow ( () -> new EntityNotFoundException ( "Invoice not found" ) );
    }

    /**
     * Retrieves all invoices for a specific garage.
     *
     * @param garageId the ID of the garage
     * @return a list of AppointmentInvoices for the specified garage
     */
//	@Transactional
//	public List<Invoice> getInvoicesForGarage(Integer garageId) {
//		return invoiceRepository.findByWorkOrderJobCardGarageId(garageId);
//	}

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
    public Invoice updateInvoicePaymentStatus(Integer invoiceId, com.autoadda.apis.garage.util.Status paymentStatus) {
        Invoice invoice = getInvoiceById ( invoiceId );
        invoice.setPaymentStatus ( paymentStatus );
        invoice.setIsPaid ( "PAID".equalsIgnoreCase ( paymentStatus.toString ( ) ) );

        if ( invoice.getIsPaid ( ) ) {
            // Update WorkOrder status when payment is received
            WorkOrder workOrder = invoice.getWorkOrder ( );
            if ( workOrder.getStatus ( ) != com.autoadda.apis.garage.util.WorkOrderStatus.PAYMENT_RECEIVED ) {
                workOrder.updateStatus ( com.autoadda.apis.garage.util.WorkOrderStatus.PAYMENT_RECEIVED );
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
    public Invoice getInvoiceByWorkOrderId(Integer workOrderId) {
        return invoiceRepository.findByWorkOrderId ( workOrderId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Invoice not found for WorkOrder ID: " + workOrderId ) );
    }

    // New DTO-based methods

    /**
     * Generates an invoice from InvoiceRequest DTO with automatic calculations
     */
    @Transactional
    public InvoiceResponse generateInvoiceFromRequest(InvoiceRequest request) {
        WorkOrder workOrder = workOrderRepository.findById ( request.getWorkOrderId ( ) )
                .orElseThrow ( () -> new EntityNotFoundException ( "Work order not found" ) );

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

        double taxAmount = subtotal * (request.getTaxRate ( ) != null ? request.getTaxRate ( ) / 100.0 : 0.0);
        double totalAmount = subtotal + taxAmount;

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

        Invoice savedInvoice = invoiceRepository.save ( invoice );
        return invoiceResponseMapper.toResponse ( savedInvoice );
    }

    /**
     * Auto-generates line items from WorkOrder and creates invoice
     */
    @Transactional
    public InvoiceResponse generateInvoiceFromWorkOrder(Integer workOrderId, Double taxRate, String notes) {
        WorkOrder workOrder = workOrderRepository.findById ( workOrderId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Work order not found" ) );

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
     * Retrieves all invoices created within a specific date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of invoices created within the specified date range
     */
//	@Transactional
//	public List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
//		return invoiceRepository.findByCreatedDateBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
//	}
}
