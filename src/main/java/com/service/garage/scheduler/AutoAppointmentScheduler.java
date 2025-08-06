package com.service.garage.scheduler;

import com.service.garage.apis.entity.FutureAppointment;
import com.service.garage.apis.entity.VehicleDetails;
import com.service.garage.apis.repository.FutureAppointmentRepository;
import com.service.garage.apis.repository.VehicleDetailsRepository;
import com.service.garage.apis.service.RuleEngineService;
import com.service.garage.apis.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoAppointmentScheduler {

    private final VehicleDetailsRepository vehicleRepo;
    private final FutureAppointmentRepository appointmentRepo;
    private final RuleEngineService ruleEngine;

    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2AM
    public void runAutoSchedulingJob() {
        List<VehicleDetails> vehicles = vehicleRepo.findAll ( );

        for (VehicleDetails vehicle : vehicles) {
            ruleEngine.findApplicableRule ( vehicle ).ifPresent ( rule -> {
                LocalDate nextDate = ruleEngine.calculateNextServiceDate ( vehicle, rule );

                boolean alreadyScheduled = appointmentRepo.existsByVehicleIdAndServiceTypeAndStatus ( vehicle.getId ( ),
                        rule.getServiceType ( ), Status.SCHEDULED );

                if ( ! alreadyScheduled ) {
                    FutureAppointment appointment = new FutureAppointment ( );
                    // appointment.setId(UUID.randomUUID().toString());
                    appointment.setVehicleId ( vehicle.getId ( ) );
                    appointment.setServiceType ( rule.getServiceType ( ) );
                    appointment.setScheduledDate ( nextDate );
                    appointment.setEstimatedMileage ( ruleEngine.estimateMileageAtDate ( vehicle, nextDate ) );
                    appointment.setStatus ( Status.SCHEDULED );
                    appointment.setCreatedBy ( "SYSTEM" );

                    appointmentRepo.save ( appointment );

                    log.info ( "Scheduled future appointment for vehicle {} on {}", vehicle.getId ( ), nextDate );
                }
            } );
        }
    }
}
