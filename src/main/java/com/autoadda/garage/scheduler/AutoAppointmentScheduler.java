package com.autoadda.garage.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.autoadda.apis.garage.entity.FutureAppointment;
import com.autoadda.apis.garage.entity.VehicleDetails;
import com.autoadda.apis.garage.repository.FutureAppointmentRepository;
import com.autoadda.apis.garage.repository.VehicleDetailsRepository;
import com.autoadda.apis.garage.service.RuleEngineService;
import com.autoadda.apis.garage.util.Status;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AutoAppointmentScheduler {

	@Autowired
	private VehicleDetailsRepository vehicleRepo;

	@Autowired
	private FutureAppointmentRepository appointmentRepo;

	@Autowired
	private RuleEngineService ruleEngine;

	@Scheduled(cron = "0 0 2 * * *") // Run daily at 2AM
	public void runAutoSchedulingJob() {
		List<VehicleDetails> vehicles = vehicleRepo.findAll();

		for (VehicleDetails vehicle : vehicles) {
			ruleEngine.findApplicableRule(vehicle).ifPresent(rule -> {
				LocalDate nextDate = ruleEngine.calculateNextServiceDate(vehicle, rule);

				boolean alreadyScheduled = appointmentRepo.existsByVehicleIdAndServiceTypeAndStatus(vehicle.getId(),
						rule.getServiceType(), Status.SCHEDULED);

				if (!alreadyScheduled) {
					FutureAppointment appointment = new FutureAppointment();
					// appointment.setId(UUID.randomUUID().toString());
					appointment.setVehicleId(vehicle.getId());
					appointment.setGarageId(vehicle.getPreferredGarageId());
					appointment.setServiceType(rule.getServiceType());
					appointment.setScheduledDate(nextDate);
					appointment.setEstimatedMileage(ruleEngine.estimateMileageAtDate(vehicle, nextDate));
					appointment.setStatus(Status.SCHEDULED);
					appointment.setCreatedBy("SYSTEM");

					appointmentRepo.save(appointment);

					log.info("Scheduled future appointment for vehicle {} on {}", vehicle.getId(), nextDate);
				}
			});
		}
	}
}
