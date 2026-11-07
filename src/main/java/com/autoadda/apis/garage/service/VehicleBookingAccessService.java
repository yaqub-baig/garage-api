package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ForbiddenException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.response.CompanyDriverResponse;
import com.autoadda.apis.garage.response.VehicleApiResponse;
import com.autoadda.apis.garage.response.VehicleCompanyResponse;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Decides who may book a garage service for a vehicle.
 * <ul>
 *   <li>Privately owned vehicle: only its owner.</li>
 *   <li>Company (fleet / showroom) vehicle: the user who registered it, the company owner,
 *       or one of the company's drivers.</li>
 * </ul>
 * Ownership comes from vehicle-api; if it can't be verified the booking is refused.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleBookingAccessService {

    private final VehicleClient vehicleClient;

    /** Result of a successful check. {@code companyId} is null for privately owned vehicles. */
    public record BookingVehicle(Integer vehicleId, Long companyId, Integer vehicleOwnerId) {
        public boolean isCompanyVehicle() {
            return companyId != null;
        }
    }

    /**
     * @throws ResourceNotFoundException when the vehicle doesn't exist (or was deleted)
     * @throws ForbiddenException        when the user may not book for this vehicle (403)
     */
    public BookingVehicle authorizeBooking(Integer vehicleId, Integer userId) {
        if (vehicleId == null) {
            throw new BadRequestException("vehicleId is required");
        }
        if (userId == null) {
            throw new BadRequestException("customerId is required");
        }

        VehicleDetailResponse vehicle = fetchVehicle(vehicleId);
        Integer ownerId = vehicle.getOwner() != null ? vehicle.getOwner().getId() : null;
        Long companyId = vehicle.getCompanyId();
        BookingVehicle result = new BookingVehicle(vehicleId, companyId, ownerId);

        if (userId.equals(ownerId)) {
            return result;
        }
        if (companyId == null) {
            throw new ForbiddenException("You can only book services for your own vehicles");
        }

        Integer companyOwnerId = companyOwnerId(companyId);
        if (userId.equals(companyOwnerId) || isCompanyDriver(companyId, companyOwnerId, userId)) {
            return result;
        }
        throw new ForbiddenException(
                "This vehicle belongs to a company. Only the company owner or its drivers can book services for it");
    }

    private VehicleDetailResponse fetchVehicle(Integer vehicleId) {
        try {
            VehicleDetailResponse vehicle = vehicleClient.getVehicleDetailsByIdResponse(vehicleId);
            if (vehicle == null) {
                throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
            }
            return vehicle;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
        } catch (FeignException e) {
            log.error("Could not load vehicle {} from vehicle-api: {}", vehicleId, e.getMessage());
            throw new IllegalStateException("Could not verify the vehicle right now. Please try again.");
        }
    }

    /** Company owner's user id, or null when the company is inactive / unknown. */
    private Integer companyOwnerId(Long companyId) {
        try {
            VehicleApiResponse<VehicleCompanyResponse> response = vehicleClient.getCompany(companyId);
            VehicleCompanyResponse company = response != null ? response.getBody() : null;
            return company != null && company.getCompanyOwner() != null ? company.getCompanyOwner().getId() : null;
        } catch (FeignException.NotFound e) {
            return null;
        } catch (FeignException e) {
            log.error("Could not load company {} from vehicle-api: {}", companyId, e.getMessage());
            throw new IllegalStateException("Could not verify the vehicle's company right now. Please try again.");
        }
    }

    private boolean isCompanyDriver(Long companyId, Integer companyOwnerId, Integer userId) {
        if (companyOwnerId == null) {
            return false;
        }
        try {
            // vehicle-api only lists drivers to the company owner, so we ask on the owner's behalf.
            VehicleApiResponse<List<CompanyDriverResponse>> response =
                    vehicleClient.getCompanyDrivers(companyId, String.valueOf(companyOwnerId));
            List<CompanyDriverResponse> drivers = response != null && response.getBody() != null ? response.getBody() : List.of();
            return drivers.stream().map(CompanyDriverResponse::getDriverUserId).anyMatch(id -> Objects.equals(id, userId));
        } catch (FeignException e) {
            log.error("Could not load drivers of company {} from vehicle-api: {}", companyId, e.getMessage());
            throw new IllegalStateException("Could not verify the company's drivers right now. Please try again.");
        }
    }
}
