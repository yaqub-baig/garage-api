package com.autoadda.apis.garage.service.os;

import com.autoadda.apis.garage.entity.os.*;
import com.autoadda.apis.garage.exception.CustomerNotFoundException;
import com.autoadda.apis.garage.exception.GarageNotFoundException;
import com.autoadda.apis.garage.exception.OtherServiceNotFoundException;
import com.autoadda.apis.garage.mapper.os.OsAppointmentDetailMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.repository.os.*;
import com.autoadda.apis.garage.request.os.OsAppointmentStatusRequest;
import com.autoadda.apis.garage.request.os.OsCustomerAppointmentRequest;
import com.autoadda.apis.garage.response.os.OsAppointmentResponse;
import com.autoadda.apis.garage.response.os.OsAppointmentDetailResponse;
import com.autoadda.apis.garage.util.BusinessValidator;
import com.autoadda.apis.garage.util.DateHelper;
import com.autoadda.apis.garage.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsAppointmentService {

    private final OsAppointmentDetailsRepository osAppointmentDetailsRepository;
    private final OsAppointmentStatusActivityRepository osAppointmentActivityRepo;
    private final OsAppointmentServiceRepository serviceRepo;
    private final GarageRepository garageRepo;
    private final OtherServiceRepository otherServiceRepository;
    private final CustomerRepository customerRepo;
    private final VehicleDetailsRepository vehicleRepo;
    private final GarageServiceRepository garageServiceRepo;
    private final OsServiceRepository osServiceRepo;
    private final JobCardRepository jobCardRepo;
    private final OsJobCardService jobCardService;
    private final OsAppointmentDetailMapper appointmentDetailMapper;
    private final BusinessValidator validator;

    /**
     * Creates a new customer appointment.
     *
     * @param request the appointment request containing details like garage ID,
     *                customer ID, vehicle ID, schedule date, service IDs, etc.
     * @return the created AppointmentDetails object.
     * @throws CustomerNotFoundException     if the customer does not exist.
     * @throws OtherServiceNotFoundException if the garage does not exist.
     */
    @Transactional

    public OsAppointmentDetails createCustomerAppointment(OsCustomerAppointmentRequest request)
            throws CustomerNotFoundException, OtherServiceNotFoundException {

        // Garage is mandatory
        OtherService service = otherServiceRepository.findById ( request.getOtherServiceId ( ) )
                .orElseThrow ( () -> new OtherServiceNotFoundException ( "Other Service not found" ) );

        // Create appointment
        OsAppointmentDetails osAppointmentDetails = OsAppointmentDetails.builder ( )
                .otherService ( service )
                .vehicleId ( request.getVehicleId ( ) )
                .customerId ( request.getCustomerId ( ) )
                .scheduleDate ( request.getScheduleDate ( ) )
                .status ( Status.CONFIRMED )
                .insertTms( DateHelper.dbDate ( ) )
                .build ( );
        osAppointmentDetailsRepository.save ( osAppointmentDetails );

        // Save activity (memberId, memberType optional ho sakte hain)
        osAppointmentActivityRepo.save ( com.autoadda.apis.garage.entity.os.OsAppointmentStatusActivity.builder ( )
                .osAppointmentId ( osAppointmentDetails.getId ( ) )
                .status ( osAppointmentDetails.getStatus ( ) )
                .memberId ( request.getMemberId ( ) )
                .memberType ( request.getMemberType ( ) )
                .insertTms( DateHelper.dbDate ( ) )
                .build ( ) );

        // Services optional

        if ( request.getServiceIds ( ) != null && ! request.getServiceIds ( ).isEmpty ( ) ) {
            List<OsAppointmentServices> services = request.getServiceIds ( ).stream ( )
                    .filter ( Objects::nonNull ) // null serviceId ignore karega
                    .map ( serviceId -> {
                        OsService osService = osServiceRepo.findById ( serviceId )
                                .orElseThrow ( () -> new RuntimeException ( "Service not found" ) );
                        return OsAppointmentServices.builder ( )
                                .appointmentId ( osAppointmentDetails.getId ( ) )
                                .osService ( osService )
                                .status ( Status.NEW )
                                .build ( );
                    } )
                    .toList ( );

            serviceRepo.saveAll ( services );
        }

        return osAppointmentDetails;
    }




    /**
     * Retrieves all appointments for a garage, optionally filtered by status.
     *
     * @param otherServiceId the ID of the garage.
     * @param status         the status to filter appointments by, or null for all.
     * @return a list of AppointmentDetails for the garage.
     * @throws GarageNotFoundException if the garage does not exist.
     */
    @Transactional(readOnly = true)
    public List<OsAppointmentDetails> getOtherServiceAppointment(Integer otherServiceId, Status status)
            throws OtherServiceNotFoundException {

         validator.validateAndGetOtherService ( otherServiceId );

        if ( StringUtils.isEmpty ( status.toString ( ) ) ) {
            return osAppointmentDetailsRepository.findByOtherServiceId ( otherServiceId );
        } else {
            return osAppointmentDetailsRepository.findByOtherServiceIdAndStatus ( otherServiceId, status );
        }
    }

    /**
     * Retrieves the details of an appointment, including services and job cards.
     *
     * @param osAppointmentId the ID of the appointment.
     * @return an AppointmentResponse containing appointment details, services, and
     * job cards.
     */
    @Transactional(readOnly = true)
    public OsAppointmentResponse getAppointmentsDetails(Integer osAppointmentId) {
        OsAppointmentDetails appointment = osAppointmentDetailsRepository.findById ( osAppointmentId )
                .orElseThrow ( () -> new RuntimeException ( "Appointment not found" ) );

        List<com.autoadda.apis.garage.entity.os.OsAppointmentServices> services = serviceRepo.findByAppointmentId ( osAppointmentId );
        List<OsJobCard> jobCards = jobCardService.getJobCardsByAppointmentId ( osAppointmentId );

        return OsAppointmentResponse.builder ( ).appointment ( appointment ).services ( services ).jobcards ( jobCards ).build ( );
    }

    /**
     * Updates the status of an appointment.
     *
     * @param request the request containing appointment ID, new status, comments,
     *                and member details.
     * @return the updated AppointmentDetails object.
     */
    @Transactional
    public OsAppointmentDetails updateAppointmentStatus(OsAppointmentStatusRequest request) {
        OsAppointmentDetails appointment = osAppointmentDetailsRepository.findById ( request.getAppointmentId ( ) )
                .orElseThrow ( () -> new RuntimeException ( "Appointment not found" ) );

        Status newStatus = request.getStatus ( );

        appointment.setStatus ( newStatus );
        if ( newStatus.equals ( Status.WORK_INPROGRESS ) ) {
            appointment.setServiceStartTime ( DateHelper.dbTime ( ) );
        } else if ( newStatus.equals ( Status.COMPLETED ) ) {
            appointment.setServiceEndTime ( DateHelper.dbTime ( ) );
        } else if ( newStatus.equals ( Status.DELIVERED ) ) {
            appointment.setStatus ( Status.DONE );
            appointment.setDeliveryTime ( DateHelper.dbTime ( ) );
        }

        appointment.setComments ( request.getComments ( ) );
        osAppointmentDetailsRepository.save ( appointment );

        osAppointmentActivityRepo.save ( OsAppointmentStatusActivity.builder ( ).osAppointmentId ( appointment.getId ( ) )
                .status ( appointment.getStatus ( ) ).memberId ( request.getMemberId ( ) ).memberType ( request.getMemberType ( ) )
                .comments ( request.getComments ( ) ).insertTms( DateHelper.dbDate ( ) ).build ( ) );

        return appointment;
    }


    /**
     * Retrieves all appointments for a garage, optionally filtered by status.
     *
     * @param otherServiceId the ID of the garage.
     * @param statusStr      the status to filter appointments by, or null for all.
     * @return a list of AppointmentDetails for the garage.
     * @throws GarageNotFoundException if the garage does not exist.
     */
    @Transactional(readOnly = true)
    public List<OsAppointmentDetailResponse> getAppointmentsByGarage(Integer otherServiceId, String statusStr)
            throws GarageNotFoundException {
        validator.validateAndGetOtherService ( otherServiceId );

        if ( StringUtils.isBlank ( statusStr ) ) {
            return osAppointmentDetailsRepository.findByOtherServiceId ( otherServiceId ).stream ( ).map ( appointmentDetailMapper::entityToResponse ).toList ( );
        }

        Status status = Status.valueOf ( statusStr.toUpperCase ( ) );
        return osAppointmentDetailsRepository.findByOtherServiceIdAndStatus ( otherServiceId, status ).stream ( ).map ( appointmentDetailMapper::entityToResponse ).toList ( );
    }

    public String generateAppointmentCode() {
        // Get current year and month
        LocalDateTime now = LocalDateTime.now ( );
        String yearMonth = now.format ( DateTimeFormatter.ofPattern ( "yyMM" ) );

        // Format: RQ-YYMM-XXXX
        String codePrefix = "APP-" + yearMonth + "-";

        // Find the latest code for this prefix
        List<String> latestCodes = osAppointmentDetailsRepository.findLatestCodesByPrefix (
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
