package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentInvoice;
import com.autoadda.apis.garage.entity.AppointmentInvoiceBreakDown;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.mapper.InvoiceMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.AppointmentInvoiceBreakDownRepository;
import com.autoadda.apis.garage.repository.AppointmentInvoiceRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.AppointmentPayRequest;
import com.autoadda.apis.garage.util.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {

	private final AppointmentInvoiceRepository invoiceRepo;
	private final AppointmentInvoiceBreakDownRepository breakdownRepo;
	private final AppointmentDetailsRepository appointmentRepo;
	private final GarageRepository garageRepo;

	private final InvoiceMapper invoiceMapper;

	@Transactional
	public AppointmentInvoice prepareInvoice(AppointmentInvoiceRequest request) {
		AppointmentDetails appointment = appointmentRepo.findById(request.getAppointmentId())
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		Garage garage = garageRepo.findById(request.getGarageId())
				.orElseThrow(() -> new RuntimeException("Garage not found"));

		AppointmentInvoice invoice = invoiceMapper.toEntity(request, garage, appointment);
		invoiceRepo.save(invoice);

		List<AppointmentInvoiceBreakDown> breakdowns = request.getService().stream()
				.map(service -> invoiceMapper.toBreakdown(service, invoice, garage, appointment))
				.collect(Collectors.toList());
		breakdownRepo.saveAll(breakdowns);

		appointment.setStatus(Status.INVOICE_INITIATED);
		appointmentRepo.save(appointment);

		return invoice;
	}

	@Transactional
	public AppointmentInvoice updateInvoicePayment(AppointmentPayRequest request) {
		AppointmentInvoice invoice = invoiceRepo.findByAppointmentId(request.getAppointmentId());
		invoice.setStatus(request.getStatus());

		if ("PAID".equalsIgnoreCase(request.getStatus())) {
			AppointmentDetails appointment = invoice.getAppointment();
			appointment.setStatus(Status.READY_FOR_DELIVERY);
			appointmentRepo.save(appointment);
		}

		return invoiceRepo.save(invoice);
	}
}
