package com.autoadda.apis.garage.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.util.Status;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
	List<Invoice> findByWorkOrderJobCardGarageId(Integer garageId);

	// Find invoices by Work Order
	Optional<Invoice> findByWorkOrderId(Integer workOrderId);

	// Find all invoices for a given garage (through work order)
	@Query("SELECT i FROM Invoice i WHERE i.workOrder.garage.id = :garageId")
	List<Invoice> findAllByGarageId(@Param("garageId") Integer garageId);

	// Filter by payment status
	List<Invoice> findByPaymentStatus(Status status);

	// Filter by creation date range
	List<Invoice> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

	// Search by partial note content
	List<Invoice> findByNotesContainingIgnoreCase(String keyword);

	@Query("SELECT SUM(i.amountPaid) FROM Invoice i WHERE i.createdAt BETWEEN :start AND :end AND i.jobCard.garage.id = :garageId")
	BigDecimal getRevenueByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("garageId") Integer garageId);

	@Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.createdAt BETWEEN :from AND :to")
	Double calculateTotalRevenue(LocalDateTime from, LocalDateTime to);

	@Query("SELECT i FROM Invoice i WHERE i.createdAt BETWEEN :from AND :to")
	List<Invoice> findInvoicesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}