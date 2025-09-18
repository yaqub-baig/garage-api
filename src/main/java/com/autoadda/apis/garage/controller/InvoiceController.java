package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.entity.InvoiceLineItem;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.AppointmentPayRequest;
import com.autoadda.apis.garage.request.InvoiceRequest;
import com.autoadda.apis.garage.response.AppointmentInvoiceResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.InvoiceResponse;
import com.autoadda.apis.garage.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/prepare")
    public ResponseEntity<GenericResponse> prepareInvoice(@RequestBody AppointmentInvoiceRequest request) {
        AppointmentInvoiceResponse invoice = invoiceService.prepareInvoice ( request );
        return ResponseEntity.ok ( GenericResponse.success ( "Invoice prepared", invoice ) );
    }

    @PutMapping("/pay")
    public ResponseEntity<GenericResponse> payInvoice(@RequestBody AppointmentPayRequest request) {
        AppointmentInvoiceResponse invoice = invoiceService.updateInvoicePayment ( request );
        return ResponseEntity.ok ( GenericResponse.success ( "Invoice updated as paid", invoice ) );
    }

    // Modern Invoice endpoints (WorkOrder-based) with DTO support

    @PostMapping("/generate")
    public ResponseEntity<GenericResponse> generateInvoice(@RequestBody InvoiceRequest request) {
        InvoiceResponse invoice = invoiceService.generateInvoiceFromRequest(request);
        return ResponseEntity.ok(GenericResponse.success("Invoice generated successfully", invoice));
    }

    @PostMapping("/work-order/{workOrderId}/auto-generate")
    public ResponseEntity<GenericResponse> autoGenerateInvoice(
            @PathVariable Integer workOrderId,
            @RequestParam(required = false, defaultValue = "10.0") Double taxRate,
            @RequestParam(required = false) String notes) {
        InvoiceResponse invoice = invoiceService.generateInvoiceFromWorkOrder(workOrderId, taxRate, notes);
        return ResponseEntity.ok(GenericResponse.success("Invoice auto-generated successfully", invoice));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<GenericResponse> getInvoiceById(@PathVariable Integer invoiceId) {
        InvoiceResponse invoice = invoiceService.getInvoiceResponse(invoiceId);
        return ResponseEntity.ok(GenericResponse.success("Invoice retrieved successfully", invoice));
    }

    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<GenericResponse> getInvoiceByWorkOrderId(@PathVariable Integer workOrderId) {
        Invoice invoice = invoiceService.getInvoiceByWorkOrderId(workOrderId);
        return ResponseEntity.ok(GenericResponse.success("Invoice retrieved successfully", invoice));
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getInvoicesForGarage(@PathVariable Integer garageId) {
        List<Invoice> invoices = invoiceService.getInvoicesForGarage(garageId);
        return ResponseEntity.ok(GenericResponse.success("Invoices retrieved successfully", invoices));
    }

    @PutMapping("/{invoiceId}/payment-status")
    public ResponseEntity<GenericResponse> updateInvoicePaymentStatus(
            @PathVariable Integer invoiceId,
            @RequestParam com.autoadda.apis.garage.util.Status paymentStatus) {
        Invoice invoice = invoiceService.updateInvoicePaymentStatus(invoiceId, paymentStatus);
        return ResponseEntity.ok(GenericResponse.success("Invoice payment status updated", invoice));
    }
}
