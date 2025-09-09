package com.autoadda.apis.garage.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.autoadda.apis.garage.mapper.AppointmentDetailMapper;
import com.autoadda.apis.garage.response.AppointmentDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.AppointmentStatusActivity;
import com.autoadda.apis.garage.entity.Customer;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.VehicleDetails;
import com.autoadda.apis.garage.exception.CustomerNotFoundException;
import com.autoadda.apis.garage.exception.GarageNotFoundException;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.AppointmentServiceRepository;
import com.autoadda.apis.garage.repository.AppointmentStatusActivityRepository;
import com.autoadda.apis.garage.repository.CustomerRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageServiceRepository;
import com.autoadda.apis.garage.repository.JobCardRepository;
import com.autoadda.apis.garage.repository.VehicleDetailsRepository;
import com.autoadda.apis.garage.request.AppointmentStatusRequest;
import com.autoadda.apis.garage.request.CustomerAppointmentRequest;
import com.autoadda.apis.garage.response.AppointmentResponse;
import com.autoadda.apis.garage.util.BusinessValidator;
import com.autoadda.apis.garage.util.DateHelper;
import com.autoadda.apis.garage.util.Status;

import lombok.RequiredArgsConstructor;
import java.util.Objects;
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

	private final AppointmentDetailsRepository appointmentRepo;
	private final AppointmentStatusActivityRepository appointmentActivityRepo;
	private final AppointmentServiceRepository serviceRepo;
	private final GarageRepository garageRepo;
	private final CustomerRepository customerRepo;
	private final VehicleDetailsRepository vehicleRepo;
	private final GarageServiceRepository garageServiceRepo;
	private final JobCardRepository jobCardRepo;
	private final JobCardService jobCardService;
	private final AppointmentDetailMapper appointmentDetailMapper;
	private final BusinessValidator validator;

	/**
	 * Creates a new customer appointment.
	 *
	 * @param request the appointment request containing details like garage ID,
	 *                customer ID, vehicle ID, schedule date, service IDs, etc.
	 * @return the created AppointmentDetails object.
	 * @throws CustomerNotFoundException if the customer does not exist.
	 * @throws GarageNotFoundException   if the garage does not exist.
	 */
	@Transactional

	public AppointmentDetails createCustomerAppointment(CustomerAppointmentRequest request)
			throws CustomerNotFoundException, GarageNotFoundException {

		// Garage is mandatory
		Garage garage = garageRepo.findById(request.getGarageId())
				.orElseThrow(() -> new GarageNotFoundException("Garage not found"));

		// Create appointment
		AppointmentDetails appointment = AppointmentDetails.builder()
				.garage(garage)
				.vehicleId(request.getVehicleId())
				.customerId(request.getCustomerId())
				.scheduleDate(request.getScheduleDate())
				.status(Status.CONFIRMED)
				.insert_tms(DateHelper.dbDate())
				.build();
		appointmentRepo.save(appointment);

		// Save activity (memberId, memberType optional ho sakte hain)
		appointmentActivityRepo.save(AppointmentStatusActivity.builder()
				.appointmentId(appointment.getId())
				.status(appointment.getStatus())
				.memberId(request.getMemberId())
				.memberType(request.getMemberType())
				.insert_tms(DateHelper.dbDate())
				.build());

		// Services optional
		if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
			List<AppointmentServices> services = request.getServiceIds().stream()
					.filter(Objects::nonNull) // null serviceId ignore karega
					.map(serviceId -> {
						GarageService service = garageServiceRepo.findById(serviceId)
								.orElseThrow(() -> new RuntimeException("Service not found"));
						return AppointmentServices.builder()
								.appointmentId(appointment.getId())
								.service(service)
								.status(Status.NEW)
								.build();
					})
					.collect(Collectors.toList());

			serviceRepo.saveAll(services);
		}

		return appointment;
	}

	/**
	 * Retrieves all appointments for a customer, optionally filtered by status.
	 *
	 * @param customerId the ID of the customer.
	 * @param status     the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the customer.
	 */
//	@Transactional(readOnly = true)
//	public List<AppointmentDetails> getCustomerServiceAppointments(Integer customerId, Status status) {
////		validator.validateCustomer(customerId);
//
//		Customer customer = customerRepo.findById(customerId)
//				.orElseThrow(() -> new RuntimeException("Customer not found"));
//
//		if (status == null || Status.ALL.equals(status)) {
//			return appointmentRepo.findByCustomer_Id(customerId);
//		} else {
//			return appointmentRepo.findByCustomer_IdAndStatus(customerId, status);
//		}
//	}

	/**
	 * Retrieves all appointments for a garage, optionally filtered by status.
	 *
	 * @param garageId the ID of the garage.
	 * @param status   the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the garage.
	 * @throws GarageNotFoundException if the garage does not exist.
	 */
	@Transactional(readOnly = true)
	public List<AppointmentDetails> getGarageServiceAppointments(Integer garageId, Status status)
			throws GarageNotFoundException {

		Garage garage = validator.validateAndGetGarage(garageId);

		if (StringUtils.isEmpty(status.toString())) {
			return appointmentRepo.findByGarage_Id(garageId);
		} else {
			return appointmentRepo.findByGarage_IdAndStatus(garageId, status);
		}
	}

	/**
	 * Retrieves the details of an appointment, including services and job cards.
	 *
	 * @param appointmentId the ID of the appointment.
	 * @return an AppointmentResponse containing appointment details, services, and
	 *         job cards.
	 */
	@Transactional(readOnly = true)
	public AppointmentResponse getAppointmentsDetails(Integer appointmentId) {
		AppointmentDetails appointment = appointmentRepo.findById(appointmentId)
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		List<AppointmentServices> services = serviceRepo.findByAppointmentId(appointmentId);
		List<JobCard> jobCards = jobCardService.getJobCardsByAppointmentId(appointmentId);

		return AppointmentResponse.builder().appointment(appointment).services(services).jobcards(jobCards).build();
	}

	/**
	 * Updates the status of an appointment.
	 *
	 * @param request the request containing appointment ID, new status, comments,
	 *                and member details.
	 * @return the updated AppointmentDetails object.
	 */
	@Transactional
	public AppointmentDetails updateAppointmentStatus(AppointmentStatusRequest request) {
		AppointmentDetails appointment = appointmentRepo.findById(request.getAppointmentId())
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		Status newStatus = request.getStatus();

		appointment.setStatus(newStatus);
		if (newStatus.equals(Status.WORK_INPROGRESS)) {
			appointment.setServiceStartTime(DateHelper.dbTime());
		} else if (newStatus.equals(Status.COMPLETED)) {
			appointment.setServiceEndTime(DateHelper.dbTime());
		} else if (newStatus.equals(Status.DELIVERED)) {
			appointment.setStatus(Status.DONE);
			appointment.setDeliveryTime(DateHelper.dbTime());
		}

		appointment.setComments(request.getComments());
		appointmentRepo.save(appointment);

		appointmentActivityRepo.save(AppointmentStatusActivity.builder().appointmentId(appointment.getId())
				.status(appointment.getStatus()).memberId(request.getMemberId()).memberType(request.getMemberType())
				.comments(request.getComments()).insert_tms(DateHelper.dbDate()).build());

		return appointment;
	}

	/**
	 * Retrieves all appointments for a customer, optionally filtered by status.
	 *
	 * @param customerId the ID of the customer.
	 * @param status     the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the customer.
	 * @throws CustomerNotFoundException if the customer does not exist.
	 */
//	@Transactional(readOnly = true)
//	public List<AppointmentDetails> getAppointmentsByCustomer(Integer customerId, Status status)
//			throws CustomerNotFoundException {
//		validator.validateAndGetCustomer(customerId);
//
//		if (status == null) {
//			return appointmentRepo.findByCustomer_Id(customerId);
//		}
//		return appointmentRepo.findByCustomer_IdAndStatus(customerId, status);
//	}

	/**
	 * Retrieves all appointments for a garage, optionally filtered by status.
	 *
	 * @param garageId  the ID of the garage.
	 * @param statusStr the status to filter appointments by, or null for all.
	 * @return a list of AppointmentDetails for the garage.
	 * @throws GarageNotFoundException if the garage does not exist.
	 */
	@Transactional(readOnly = true)
	public List<AppointmentDetailResponse> getAppointmentsByGarage(Integer garageId, String statusStr)
			throws GarageNotFoundException {
		validator.validateAndGetGarage(garageId);

		if (StringUtils.isBlank(statusStr)) {
			return appointmentRepo.findByGarage_Id(garageId).stream().map(appointmentDetailMapper::entityToResponse).toList();
		}

		Status status = Status.valueOf(statusStr.toUpperCase());
		return appointmentRepo.findByGarage_IdAndStatus(garageId, status).stream().map(appointmentDetailMapper::entityToResponse).toList();
	}

	public String generateAppointmentCode() {
		// Get current year and month
		LocalDateTime now = LocalDateTime.now ( );
		String yearMonth = now.format ( DateTimeFormatter.ofPattern ( "yyMM" ) );

		// Format: RQ-YYMM-XXXX
		String codePrefix = "APP-" + yearMonth + "-";

		// Find the latest code for this prefix
		List<String> latestCodes = appointmentRepo.findLatestCodesByPrefix (
				codePrefix );

		int nextSequence = 1;
		if ( ! latestCodes.isEmpty ( ) ) {
			String latestCode = latestCodes.get ( 0 );
			try {
				// Extract the sequence number from the latest code
				String sequencePart = latestCode.substring ( codePrefix.length ( ) );
				nextSequence = Integer.parseInt ( sequencePart ) + 1;
			}
			catch ( NumberFormatException | IndexOutOfBoundsException e ) {
				log.warn ( "Failed to parse sequence number, starting from 1", e );
			}
		}

		// Format the final code with the sequence padded to 4 digits
		return String.format ( "%s%06d", codePrefix, nextSequence );
	}



}
