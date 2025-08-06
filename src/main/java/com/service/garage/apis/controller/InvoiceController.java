package com.service.garage.apis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.entity.AppointmentInvoice;
import com.service.garage.apis.request.AppointmentInvoiceRequest;
import com.service.garage.apis.request.AppointmentPayRequest;
import com.service.garage.apis.response.GenericResponse;
import com.service.garage.apis.service.InvoiceService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
