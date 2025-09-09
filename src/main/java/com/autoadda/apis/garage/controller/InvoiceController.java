package com.autoadda.apis.garage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.garage.entity.AppointmentInvoice;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.AppointmentPayRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.InvoiceService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/prepare")
    public ResponseEntity<GenericResponse> prepareInvoice(@RequestBody AppointmentInvoiceRequest request) {
        AppointmentInvoice invoice = invoiceService.prepareInvoice(request);
        return ResponseEntity.ok(GenericResponse.success("Invoice prepared", invoice));
    }

    @PutMapping("/pay")
    public ResponseEntity<GenericResponse> payInvoice(@RequestBody AppointmentPayRequest request) {
        AppointmentInvoice invoice = invoiceService.updateInvoicePayment(request);
        return ResponseEntity.ok(GenericResponse.success("Invoice updated as paid", invoice));
    }
}
