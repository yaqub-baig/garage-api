package com.autoadda.apis.garage.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentInvoice;
import com.autoadda.apis.garage.entity.AppointmentInvoiceBreakDown;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.entity.InvoiceLineItem;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.mapper.InvoiceMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.AppointmentInvoiceBreakDownRepository;
import com.autoadda.apis.garage.repository.AppointmentInvoiceRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.InvoiceRepository;
import com.autoadda.apis.garage.repository.WorkOrderRepository;
import com.autoadda.apis.garage.request.AppointmentInvoiceRequest;
import com.autoadda.apis.garage.request.AppointmentPayRequest;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {

	@Autowired
	private final AppointmentInvoiceRepository invoiceRepo;

	@Autowired
	private final AppointmentInvoiceBreakDownRepository breakdownRepo;

	@Autowired
	private final AppointmentDetailsRepository appointmentRepo;

	@Autowired
	private final GarageRepository garageRepo;

	@Autowired
	private final WorkOrderRepository workOrderRepository;

	@Autowired
	private final InvoiceRepository invoiceRepository;

	@Autowired
	private final InvoiceMapper invoiceMapper;

	/**
	 * Prepares an invoice for a given appointment and garage.
	 *
	 * @param request the request containing appointment and garage details
	 * @return the created AppointmentInvoice
	 */
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

	/**
	 * Updates the payment status of an invoice based on the appointment payment
	 * request.
	 *
	 * @param request the request containing appointment ID and payment status
	 * @return the updated AppointmentInvoice
	 */
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

	/**
	 * Retrieves all invoices for a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @return a list of AppointmentInvoices for the specified garage
	 */
	@Transactional
	public Invoice generateInvoice(Integer workOrderId, List<InvoiceLineItem> lineItems) {
		WorkOrder workOrder = workOrderRepository.findById(workOrderId)
				.orElseThrow(() -> new EntityNotFoundException("Work order not found"));

		boolean hasCompletedJobCards = workOrder.getJobCards().stream()
				.anyMatch(j -> j.getStatus() == Status.COMPLETED);

		if (!hasCompletedJobCards) {
			throw new IllegalStateException("Cannot generate invoice for a work order with no completed job cards.");
		}

		Invoice invoice = new Invoice();
		invoice.setWorkOrder(workOrder);
		invoice.setCreatedAt(LocalDateTime.now());

		for (InvoiceLineItem item : lineItems) {
			if (item.getPrice() < 0 || item.getQuantity() <= 0) {
				throw new IllegalArgumentException("Invalid line item: Price or quantity not valid.");
			}
			item.setInvoice(invoice); // link line item back to invoice
		}

		invoice.setLineItems(lineItems);

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

		return invoiceRepository.save(invoice);
	}

	/**
	 * Retrieves all invoices for a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @return a list of AppointmentInvoices for the specified garage
	 */
	@Transactional
	public Invoice getInvoiceById(Integer id) {
		return invoiceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
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
