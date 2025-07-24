package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.stream.Collectors;

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

	private final BusinessValidator validator;

	@Transactional
	public AppointmentDetails createCustomerAppointment(CustomerAppointmentRequest request)
			throws CustomerNotFoundException, GarageNotFoundException {
//		validator.validateGarage(request.getGarageId());
//		validator.validateCustomer(request.getCustomerId());

		Garage garage = garageRepo.findById(request.getGarageId())
				.orElseThrow(() -> new GarageNotFoundException("Garage not found"));
		Customer customer = customerRepo.findById(request.getCustomerId())
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
		VehicleDetails vehicle = vehicleRepo.findById(request.getVehicleId())
				.orElseThrow(() -> new RuntimeException("Vehicle not found"));

		AppointmentDetails appointment = AppointmentDetails.builder().customer(customer).garage(garage).vehicle(vehicle)
				.scheduleDate(request.getScheduleDate()).status(Status.CONFIRMED).insert_tms(DateHelper.dbDate())
				.build();

		appointmentRepo.save(appointment);

		appointmentActivityRepo.save(AppointmentStatusActivity.builder().appointmentId(appointment.getId())
				.status(appointment.getStatus()).memberId(request.getMemberId()).memberType(request.getMemberType())
				.insert_tms(DateHelper.dbDate()).build());

		List<AppointmentServices> services = request.getServiceIds().stream().map(serviceId -> {
			GarageService service = garageServiceRepo.findById(serviceId)
					.orElseThrow(() -> new RuntimeException("Service not found"));
			return AppointmentServices.builder().appointmentId(appointment.getId()).service(service).status(Status.NEW)
					.build();
		}).collect(Collectors.toList());

		serviceRepo.saveAll(services);

		return appointment;
	}

	@Transactional(readOnly = true)
	public List<AppointmentDetails> getCustomerServiceAppointments(Integer customerId, Status status) {
//		validator.validateCustomer(customerId);

		Customer customer = customerRepo.findById(customerId)
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		if (status == null || Status.ALL.equals(status)) {
			return appointmentRepo.findByCustomer_Id(customerId);
		} else {
			return appointmentRepo.findByCustomer_IdAndStatus(customerId, status);
		}
	}

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

	@Transactional(readOnly = true)
	public AppointmentResponse getAppointmentsDetails(Integer appointmentId) {
		AppointmentDetails appointment = appointmentRepo.findById(appointmentId)
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		List<AppointmentServices> services = serviceRepo.findByAppointmentId(appointmentId);
		List<JobCard> jobCards = jobCardService.getJobCardsByAppointmentId(appointmentId);

		return AppointmentResponse.builder().appointment(appointment).services(services).jobcards(jobCards).build();
	}

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

	@Transactional(readOnly = true)
	public List<AppointmentDetails> getAppointmentsByCustomer(Integer customerId, Status status)
			throws CustomerNotFoundException {
		validator.validateAndGetCustomer(customerId);

		if (status == null) {
			return appointmentRepo.findByCustomer_Id(customerId);
		}
		return appointmentRepo.findByCustomer_IdAndStatus(customerId, status);
	}

	@Transactional(readOnly = true)
	public List<AppointmentDetails> getAppointmentsByGarage(Integer garageId, String statusStr)
			throws GarageNotFoundException {
		validator.validateAndGetGarage(garageId);

		if (StringUtils.isBlank(statusStr)) {
			return appointmentRepo.findByGarage_Id(garageId);
		}

		Status status = Status.valueOf(statusStr.toUpperCase());
		return appointmentRepo.findByGarage_IdAndStatus(garageId, status);
	}
}
