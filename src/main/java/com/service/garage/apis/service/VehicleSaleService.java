package com.service.garage.apis.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.Customer;
import com.service.garage.apis.entity.Garage;
import com.service.garage.apis.entity.VehicleDetails;
import com.service.garage.apis.entity.VehicleListing;
import com.service.garage.apis.entity.VehicleSale;
import com.service.garage.apis.entity.VehicleSaleInvoice;
import com.service.garage.apis.repository.VehicleDetailsRepository;
import com.service.garage.apis.repository.VehicleListingRepository;
import com.service.garage.apis.repository.VehicleOwnerRepository;
import com.service.garage.apis.repository.VehicleSaleInvoiceRepository;
import com.service.garage.apis.repository.VehicleSaleRepository;
import com.service.garage.apis.request.VehicleListingRequest;
import com.service.garage.apis.request.VehicleSaleRequest;
import com.service.garage.apis.util.Status;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleSaleService {
		private final VehicleListingRepository vehicleListingRepo;
		private final VehicleSaleRepository vehicleSaleRepo;
		private final VehicleDetailsRepository vehicleRepo; // Assuming this exists
		private final VehicleOwnerRepository ownerRepo; // Assuming this exists
		private final VehicleSaleInvoiceRepository invoiceRepo; // Assuming this exists

	// Placeholder for future implementation

	/**
	 * Creates a new vehicle listing in the garage.
	 *
	 * @param garage The garage where the vehicle is listed.
	 * @param dto    The details of the vehicle listing.
	 * @return The created VehicleListing object.
	 */
	@Transactional
	public VehicleListing createListing(Garage garage, VehicleListingRequest dto) {
		VehicleListing listing = new VehicleListing();
		listing.setGarage(garage);
		listing.setVin(dto.getVin());
		listing.setMake(dto.getMake());
		listing.setModel(dto.getModel());
		listing.setYear(dto.getYear());
		listing.setPrice(dto.getPrice());
		listing.setDescription(dto.getDescription());
		listing.setStatus(Status.LISTED);
		listing.setListedDate(LocalDate.now());
		return vehicleListingRepo.save(listing);
	}

	/**
	 * Purchases a vehicle from a listing.
	 *
	 * @param listingId   The ID of the vehicle listing.
	 * @param buyer       The customer who is buying the vehicle.
	 * @param price       The sale price of the vehicle.
	 * @param paymentMode The mode of payment (e.g., cash, credit).
	 * @return The VehicleSale object representing the sale.
	 */
	@Transactional
	public VehicleSale purchaseVehicle(Integer listingId, Customer buyer, double price, String paymentMode) {
		VehicleListing listing = vehicleListingRepo.findById(listingId)
				.orElseThrow(() -> new RuntimeException("Listing not found"));

		if (listing.getStatus() != Status.LISTED) {
			throw new IllegalStateException("Vehicle already sold or removed");
		}

		VehicleSale sale = new VehicleSale();
		sale.setVehicleListing(listing);
		sale.setBuyer(buyer);
		sale.setSalePrice(price);
		sale.setSaleDate(LocalDate.now());
		sale.setPaymentMode(paymentMode);

		sale = vehicleSaleRepo.save(sale);

		listing.setStatus(Status.SOLD);
		listing.setSale(sale);
		vehicleListingRepo.save(listing);

		// Optional: assign to Vehicle table
		VehicleDetails vehicle = new VehicleDetails();
		vehicle.setVin(listing.getVin());
		vehicle.setMake(listing.getMake());
		vehicle.setModel(listing.getModel());
		vehicle.setYear(listing.getYear());
		vehicleRepo.save(vehicle);

		return sale;
	}

	/**
	 * Creates a new vehicle sale record.
	 *
	 * @param request The request containing details for the vehicle sale.
	 * @return The created VehicleSale object.
	 */
	@Transactional
	public VehicleSale createVehicleSale(VehicleSaleRequest request) {
		VehicleSale sale = new VehicleSale();
		sale.setVehicle(vehicleRepo.findById(request.getVehicleId()).orElseThrow());
		sale.setPreviousOwner(ownerRepo.findById(request.getPreviousOwnerId()).orElseThrow());
		sale.setNewOwner(ownerRepo.findById(request.getNewOwnerId()).orElseThrow());
		sale.setSalePrice(request.getSalePrice());
		sale.setSaleDate(LocalDate.now());
		sale.setStatus(Status.ACTIVE);

		return vehicleSaleRepo.save(sale);
	}

	/**
	 * Generates a sale invoice for a vehicle sale.
	 *
	 * @param saleId The ID of the vehicle sale.
	 * @return The generated VehicleSaleInvoice object.
	 */
	@Transactional
	public VehicleSaleInvoice generateVehicleSaleInvoice(Integer saleId) {
		VehicleSale sale = vehicleSaleRepo.findById(saleId).orElseThrow();

		VehicleSaleInvoice invoice = new VehicleSaleInvoice();
		invoice.setInvoiceDate(LocalDate.now());
		invoice.setTotalAmount(sale.getSalePrice());
		invoice.setPaymentMethod("CASH"); // or dynamic
		invoice.setVehicleSale(sale);

		return invoiceRepo.save(invoice);
	}

	/**
	 * Retrieves a vehicle sale by its ID.
	 *
	 * @param id The ID of the vehicle sale.
	 * @return The VehicleSale object.
	 */
	@Transactional
	public VehicleSale getVehicleSaleById(Integer id) {
		return vehicleSaleRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("VehicleSale not found"));
	}

	/**
	 * Soft deletes a vehicle sale by changing its status to REMOVED.
	 *
	 * @param saleId The ID of the vehicle sale to be soft deleted.
	 */
	@Transactional
	public void softDeleteSale(Integer saleId) {
		VehicleSale sale = vehicleSaleRepo.findById(saleId).orElseThrow();
		sale.setStatus(Status.REMOVED);
		vehicleSaleRepo.save(sale);
	}
}
