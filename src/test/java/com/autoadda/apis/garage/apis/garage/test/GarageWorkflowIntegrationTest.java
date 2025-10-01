package com.autoadda.apis.garage.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.autoadda.apis.garage.response.JobCardResponse;
import com.autoadda.apis.garage.util.JobCardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.entity.InvoiceLineItem;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.VehicleDetails;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.AppointmentServiceRepository;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageServiceRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.repository.JobCardRepository;
import com.autoadda.apis.garage.request.AssignServiceRequest;
import com.autoadda.apis.garage.service.InvoiceService;
import com.autoadda.apis.garage.service.JobCardService;
import com.autoadda.apis.garage.service.WorkOrderService;
import com.autoadda.apis.garage.util.Status;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class GarageWorkflowIntegrationTest {

	@Autowired
	private GarageRepository garageRepository;

	@Autowired
	private GarageServiceRepository garageServiceRepo;

	@Autowired
	private TechnicianRepository technicianRepository;

	@Autowired
	private AppointmentDetailsRepository appointmentRepo;

	@Autowired
	private AppointmentServiceRepository appointmentServiceRepo;

	@Autowired
	private JobCardRepository jobCardRepo;

	@Autowired
	AvailableServiceRepository availableServiceRepo;

	@Autowired
	private WorkOrderService workOrderService;

	@Autowired
	private JobCardService jobCardService;

	@Autowired
	private InvoiceService invoiceService;

	private Garage garage;
	private Technician technician;

	@BeforeEach
	void setup() {
		garage = new Garage();
		garage.setName ("FastFix Garage");
		garageRepository.save(garage);

		technician = new Technician();
		technician.setFirstName("John");
		technician.setMobileNumber("1234567890");
		technician.setGarage(garage);
		technicianRepository.save(technician);
	}

	@Test
	void testEndToEndWorkOrderJobCardInvoiceFlow() {
		// Step 1: Create Appointment
		AppointmentDetails appointment = new AppointmentDetails();
		VehicleDetails vehicle = new VehicleDetails();
		vehicle.setVin("ABC123");
		vehicle.setYear(2020);
		vehicle.setName("Toyota Camry");

		appointment.setVehicleId(vehicle.getId());
		appointment.setScheduleDate(LocalDate.now());
		appointment.setGarage(garage);
		appointment = appointmentRepo.save(appointment);

		// Step 2. Create or fetch AvailableService (the actual service details)
		AvailableService availableService = new AvailableService();
		availableService.setName("Brake Pad Replacement");
		availableService.setPrice(150.00);
		// save available service first if new
		availableService = availableServiceRepo.save(availableService);

		// Step 3. Create GarageService (links Garage and AvailableService)
		GarageService garageService = new GarageService();
		garageService.setGarage(garage);
		garageService.setAvailableService(availableService);
		garageService.setIsActive(true);
		garageService = garageServiceRepo.save(garageService);

		// Step 4. Create AppointmentServices linking appointment and garageService
		AppointmentServices apptService = new AppointmentServices();
		apptService.setAppointmentId(appointment.getId());
		apptService.setService(garageService);
		apptService.setStatus(Status.PENDING);
		apptService = appointmentServiceRepo.save(apptService);

		// Step 5: Assign Technician and Create JobCard
		AssignServiceRequest assignRequest = new AssignServiceRequest();
		assignRequest.setAppointmentId(appointment.getId());
		assignRequest.setServiceId(apptService.getId());
		assignRequest.setTechnicianId(technician.getId());
		assignRequest.setGarageId(garage.getId());
		assignRequest.setMemberId(999); // dummy user id
		assignRequest.setMemberType("ADMIN");
		assignRequest.setStatus("ASSIGNED");
		assignRequest.setComments("Assigning technician for brake fix");

		JobCardResponse jobCard = jobCardService.assignTechnicianToService(assignRequest);
		assertNotNull(jobCard);
		assertEquals(Status.ASSIGNED, jobCard.getStatus());
		assertEquals(apptService.getId(), jobCard.getAppointmentService ().getId ());
		assertEquals(technician.getId(), jobCard.getTechnicianId ());

		// Step 6: Create Work Order
		WorkOrder workOrder = workOrderService.createWorkOrder(List.of(new JobCard (  )));
		assertNotNull(workOrder);
		assertEquals(1, workOrder.getJobCards().size());

		// Step 7: Mark JobCard Complete
		jobCardService.markJobCardComplete(jobCard.getId());
		JobCard completedCard = jobCardRepo.findById(jobCard.getId()).orElseThrow();
		assertEquals(Status.COMPLETED, completedCard.getStatus());

		// Step 8: Dynamically create line items from completed job cards
		List<InvoiceLineItem> lineItems = workOrder.getJobCards().stream()
				.filter(jc -> jc.getStatus() == JobCardStatus.COMPLETED).map( jc -> {
					AvailableService availableSrc = jc.getService().getService().getAvailableService();
					InvoiceLineItem item = new InvoiceLineItem();
					item.setDescription(availableSrc.getName());
					item.setPrice(availableSrc.getPrice());
					item.setQuantity(1); // or however quantity is determined
					return item;
				}).collect(Collectors.toList());
		assertFalse(lineItems.isEmpty(), "Line items should not be empty after job card completion");

		// Step 9: Generate Invoice using dynamic line items
		Invoice invoice = invoiceService.generateInvoice(workOrder.getId(), lineItems);
		assertNotNull(invoice);
		assertEquals(workOrder.getId(), invoice.getWorkOrder().getId());

		double expectedTotal = lineItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
		assertEquals(expectedTotal, invoice.getTotalAmount());
	}

}
