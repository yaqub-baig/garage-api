package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.service.GarageResolver;
import com.autoadda.apis.garage.service.GarageAccessGuard;
import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.request.InvoicePayRequest;
import com.autoadda.apis.garage.request.InvoiceRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.InvoiceResponse;
import com.autoadda.apis.garage.service.InvoiceService;
import com.autoadda.apis.garage.util.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final GarageAccessGuard guard;
    private final GarageResolver garageResolver;


    @PutMapping("/pay")
    public ResponseEntity<GenericResponse> payInvoice(@RequestBody InvoicePayRequest request,
            @RequestHeader(value = "X-Auto-Adda-Id", required = false) String caller) {
        guard.requireUnlessCustomer(caller, garageResolver.ofInvoice(request.getInvoiceId()), garageResolver.customerOfInvoice(request.getInvoiceId()), GaragePermission.INVOICE_MANAGE);
        InvoiceResponse invoice = invoiceService.updateInvoicePayment(request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Invoice updated as paid", invoice));
    }

    // Modern Invoice endpoints (WorkOrder-based) with DTO support

    @PostMapping("/generate")
    public ResponseEntity<GenericResponse> generateInvoice(@RequestBody InvoiceRequest request,
            @RequestHeader(value = "X-Auto-Adda-Id", required = false) String caller) {
        guard.require(caller, garageResolver.ofWorkOrder(request.getWorkOrderId()), GaragePermission.INVOICE_MANAGE);
        InvoiceResponse invoice = invoiceService.generateInvoiceFromRequest(request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Invoice generated successfully", invoice));
    }

    @PostMapping("/work-order/{workOrderId}/auto-generate")
    public ResponseEntity<GenericResponse> autoGenerateInvoice(
            @PathVariable Integer workOrderId,
            @RequestParam(required = false, defaultValue = "10.0") Double taxRate,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long voiceNoteId,
            @RequestHeader(value = "X-Auto-Adda-Id", required = false) String caller) {
        guard.require(caller, garageResolver.ofWorkOrder(workOrderId), GaragePermission.INVOICE_MANAGE);
        InvoiceResponse invoice = invoiceService.generateInvoiceFromWorkOrder(workOrderId, taxRate, notes, voiceNoteId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Invoice auto-generated successfully", invoice));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<GenericResponse> getInvoiceById(@PathVariable Integer invoiceId) {
        InvoiceResponse invoice = invoiceService.getInvoiceResponse(invoiceId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Invoice retrieved successfully", invoice));
    }

    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<GenericResponse> getInvoiceByWorkOrderId(@PathVariable Integer workOrderId) {
        InvoiceResponse invoice = invoiceService.getInvoiceByWorkOrderId(workOrderId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Invoice retrieved successfully", invoice));
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getInvoicesForGarage(@PathVariable Integer garageId) {
        List<Invoice> invoices = invoiceService.getInvoicesForGarage(garageId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Invoices retrieved successfully", invoices));
    }

    @PutMapping("/{invoiceId}/payment-status")
    public ResponseEntity<GenericResponse> updateInvoicePaymentStatus(
            @PathVariable Integer invoiceId,
            @RequestParam InvoiceStatus paymentStatus,
            @RequestHeader(value = "X-Auto-Adda-Id", required = false) String caller) {
        guard.require(caller, garageResolver.ofInvoice(invoiceId), GaragePermission.INVOICE_MANAGE);
        Invoice invoice = invoiceService.updateInvoicePaymentStatus(invoiceId, paymentStatus);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Invoice payment status updated", invoice));
    }

    @PostMapping("/{invoiceId}/send-to-customer")
    public ResponseEntity<GenericResponse> sendInvoiceToCustomer(@PathVariable Integer invoiceId,
            @RequestHeader(value = "X-Auto-Adda-Id", required = false) String caller) {
        guard.require(caller, garageResolver.ofInvoice(invoiceId), GaragePermission.INVOICE_MANAGE);
        GenericResponse smsResponse = invoiceService.sendInvoiceToCustomer(invoiceId);
        return ResponseEntity.ok(smsResponse);
    }
}
