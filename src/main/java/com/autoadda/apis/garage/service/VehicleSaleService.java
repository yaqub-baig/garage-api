package com.autoadda.apis.garage.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Customer;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.VehicleDetails;
import com.autoadda.apis.garage.entity.VehicleListing;
import com.autoadda.apis.garage.entity.VehicleSale;
import com.autoadda.apis.garage.entity.VehicleSaleInvoice;
import com.autoadda.apis.garage.repository.VehicleDetailsRepository;
import com.autoadda.apis.garage.repository.VehicleListingRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerRepository;
import com.autoadda.apis.garage.repository.VehicleSaleInvoiceRepository;
import com.autoadda.apis.garage.repository.VehicleSaleRepository;
import com.autoadda.apis.garage.request.VehicleListingRequest;
import com.autoadda.apis.garage.request.VehicleSaleRequest;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class VehicleSaleService {

	// This service will handle vehicle sale operations
	// For now, it is empty but can be expanded with methods for vehicle sales
	// management

	// Example methods could include:
	// - createVehicleSale
	// - getVehicleSaleById
	// - updateVehicleSaleStatus
	// - getAllVehicleSalesByGarageId
	// - etc.

	private final VehicleListingRepository vehicleListingRepo;
	private final VehicleSaleRepository vehicleSaleRepo;
	private final VehicleDetailsRepository vehicleRepo; // Assuming this exists
	private final VehicleOwnerRepository ownerRepo; // Assuming this exists
	private final VehicleSaleInvoiceRepository invoiceRepo; // Assuming this exists

	// Placeholder for future implementation

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

	public VehicleSale purchaseVehicle(Integer listingId, Customer buyer, double price, String paymentMode) {
		VehicleListing listing = vehicleListingRepo.findById(listingId)
				.orElseThrow(() -> new RuntimeException("Listing not found"));

		if (listing.getStatus() != Status.LISTED) {
			throw new IllegalStateException("Vehicle already sold or removed");
		}

		VehicleSale sale = new VehicleSale();
		sale.setListing(listing);
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
		vehicle.setCustomerId(buyer.getId());
		vehicleRepo.save(vehicle);

		return sale;
	}

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

	public VehicleSaleInvoice generateVehicleSaleInvoice(Integer saleId) {
		VehicleSale sale = vehicleSaleRepo.findById(saleId).orElseThrow();

		VehicleSaleInvoice invoice = new VehicleSaleInvoice();
		invoice.setInvoiceDate(LocalDate.now());
		invoice.setTotalAmount(sale.getSalePrice());
		invoice.setPaymentMethod("CASH"); // or dynamic
		invoice.setVehicleSale(sale);

		return invoiceRepo.save(invoice);
	}

	public VehicleSale getVehicleSaleById(Integer id) {
		return vehicleSaleRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("VehicleSale not found"));
	}

	public void softDeleteSale(Integer saleId) {
		VehicleSale sale = vehicleSaleRepo.findById(saleId).orElseThrow();
		sale.setStatus(Status.REMOVED);
		vehicleSaleRepo.save(sale);
	}

}
