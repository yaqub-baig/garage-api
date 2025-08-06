package com.service.garage.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.service.garage.apis.entity.Customer;
import com.service.garage.apis.entity.Garage;
import com.service.garage.apis.entity.VehicleDetails;
import com.service.garage.apis.entity.VehicleListing;
import com.service.garage.apis.entity.VehicleOwner;
import com.service.garage.apis.entity.VehicleSale;
import com.service.garage.apis.entity.VehicleSaleInvoice;
import com.service.garage.apis.repository.VehicleDetailsRepository;
import com.service.garage.apis.repository.VehicleListingRepository;
import com.service.garage.apis.repository.VehicleOwnerRepository;
import com.service.garage.apis.repository.VehicleSaleInvoiceRepository;
import com.service.garage.apis.repository.VehicleSaleRepository;
import com.service.garage.apis.request.VehicleListingRequest;
import com.service.garage.apis.request.VehicleSaleRequest;
import com.service.garage.apis.service.VehicleSaleService;
import com.service.garage.apis.util.Status;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class VehicleSaleServiceTest {

	@InjectMocks
	private VehicleSaleService vehicleSaleService;

	@Mock
	private VehicleListingRepository vehicleListingRepo;

	@Mock
	private VehicleSaleRepository vehicleSaleRepo;

	@Mock
	private VehicleDetailsRepository vehicleRepo;

	@Mock
	private VehicleOwnerRepository ownerRepo;

	@Mock
	private VehicleSaleInvoiceRepository invoiceRepo;

	@Test
	void testCreateListing() {
		Garage garage = new Garage();
		VehicleListingRequest request = new VehicleListingRequest();
		request.setVin("123VIN");
		request.setMake("Toyota");
		request.setModel("Camry");
		request.setYear(2020);
		request.setPrice(10000.0);
		request.setDescription("Great condition");

		VehicleListing saved = new VehicleListing();
		saved.setId(1);

		when(vehicleListingRepo.save(any())).thenReturn(saved);

		VehicleListing result = vehicleSaleService.createListing(garage, request);

		assertNotNull(result);
		assertEquals(1, result.getId());
		verify(vehicleListingRepo).save(any());
	}

	@Test
	void testPurchaseVehicle_Success() {
		int listingId = 1;
		Customer buyer = new Customer();
		buyer.setId(10);

		VehicleListing listing = new VehicleListing();
		listing.setId(listingId);
		listing.setStatus(Status.LISTED);
		listing.setVin("123VIN");
		listing.setMake("Toyota");
		listing.setModel("Camry");
		listing.setYear(2020);

		when(vehicleListingRepo.findById(listingId)).thenReturn(Optional.of(listing));
		when(vehicleSaleRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(vehicleListingRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(vehicleRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		VehicleSale result = vehicleSaleService.purchaseVehicle(listingId, buyer, 9500.0, "CARD");

		assertNotNull(result);
		assertEquals(9500.0, result.getSalePrice());
		verify(vehicleSaleRepo).save(any());
		verify(vehicleRepo).save(any());
	}

	@Test
	void testPurchaseVehicle_AlreadySold() {
		int listingId = 1;
		VehicleListing listing = new VehicleListing();
		listing.setId(listingId);
		listing.setStatus(Status.SOLD);

		when(vehicleListingRepo.findById(listingId)).thenReturn(Optional.of(listing));

		assertThrows(IllegalStateException.class,
				() -> vehicleSaleService.purchaseVehicle(listingId, new Customer(), 10000, "CASH"));
	}

	@Test
	void testCreateVehicleSale_Success() {
		VehicleSaleRequest request = new VehicleSaleRequest();
		request.setVehicleId(1);
		request.setPreviousOwnerId(2);
		request.setNewOwnerId(3);
		request.setSalePrice(12000.0);

		when(vehicleRepo.findById(1)).thenReturn(Optional.of(new VehicleDetails()));
		when(ownerRepo.findById(2)).thenReturn(Optional.of(new VehicleOwner()));
		when(ownerRepo.findById(3)).thenReturn(Optional.of(new VehicleOwner()));
		when(vehicleSaleRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		VehicleSale result = vehicleSaleService.createVehicleSale(request);

		assertNotNull(result);
		assertEquals(Status.ACTIVE, result.getStatus());
	}

	@Test
	void testGenerateVehicleSaleInvoice() {
		VehicleSale sale = new VehicleSale();
		sale.setId(1);
		sale.setSalePrice(15000.0);

		when(vehicleSaleRepo.findById(1)).thenReturn(Optional.of(sale));
		when(invoiceRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		VehicleSaleInvoice invoice = vehicleSaleService.generateVehicleSaleInvoice(1);

		assertNotNull(invoice);
		assertEquals(15000.0, invoice.getTotalAmount());
		assertEquals("CASH", invoice.getPaymentMethod());
	}

	@Test
	void testGetVehicleSaleById_Found() {
		VehicleSale sale = new VehicleSale();
		sale.setId(1);

		when(vehicleSaleRepo.findById(1)).thenReturn(Optional.of(sale));

		VehicleSale result = vehicleSaleService.getVehicleSaleById(1);

		assertEquals(1, result.getId());
	}

	@Test
	void testGetVehicleSaleById_NotFound() {
		when(vehicleSaleRepo.findById(1)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> vehicleSaleService.getVehicleSaleById(1));
	}

	@Test
	void testSoftDeleteSale() {
		VehicleSale sale = new VehicleSale();
		sale.setId(1);
		sale.setStatus(Status.ACTIVE);

		when(vehicleSaleRepo.findById(1)).thenReturn(Optional.of(sale));
		when(vehicleSaleRepo.save(any())).thenReturn(sale);

		vehicleSaleService.softDeleteSale(1);

		assertEquals(Status.REMOVED, sale.getStatus());
		verify(vehicleSaleRepo).save(sale);
	}
}
