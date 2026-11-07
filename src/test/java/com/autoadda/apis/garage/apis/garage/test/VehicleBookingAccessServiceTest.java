package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.exception.ForbiddenException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.service.VehicleBookingAccessService;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleBookingAccessServiceTest {

    private static final int VEHICLE = 30;
    private static final int REGISTRANT = 7;
    private static final long COMPANY = 5L;
    private static final int COMPANY_OWNER = 8;
    private static final int DRIVER = 42;

    @Mock private VehicleClient vehicleClient;
    @InjectMocks private VehicleBookingAccessService service;

    private void vehicle(Long companyId) {
        UserResponse owner = new UserResponse();
        owner.setId(REGISTRANT);
        VehicleDetailResponse v = VehicleDetailResponse.builder().id(VEHICLE).owner(owner).companyId(companyId).build();
        when(vehicleClient.getVehicleDetailsByIdResponse(VEHICLE)).thenReturn(v);
    }

    private void companyWithDrivers(Integer... driverIds) {
        UserResponse owner = new UserResponse();
        owner.setId(COMPANY_OWNER);
        when(vehicleClient.getCompany(COMPANY)).thenReturn(
                new VehicleApiResponse<>("success", "200", "success", new VehicleCompanyResponse(COMPANY, "Fleet Co", owner)));
        lenient().when(vehicleClient.getCompanyDrivers(COMPANY, String.valueOf(COMPANY_OWNER))).thenReturn(
                new VehicleApiResponse<>("success", "200", "ok",
                        java.util.Arrays.stream(driverIds).map(id -> new CompanyDriverResponse(1L, COMPANY, id, "D", null)).toList()));
    }

    @Test
    void privateOwnerCanBook() {
        vehicle(null);
        VehicleBookingAccessService.BookingVehicle result = service.authorizeBooking(VEHICLE, REGISTRANT);
        assertNull(result.companyId());
        assertFalse(result.isCompanyVehicle());
        verify(vehicleClient, never()).getCompany(anyLong());
    }

    @Test
    void someoneElseCannotBookPrivateVehicle() {
        vehicle(null);
        ForbiddenException e = assertThrows(ForbiddenException.class, () -> service.authorizeBooking(VEHICLE, 99));
        assertTrue(e.getMessage().contains("your own vehicles"));
    }

    @Test
    void registrantCanBookCompanyVehicleAndCompanyIsRecorded() {
        vehicle(COMPANY);
        VehicleBookingAccessService.BookingVehicle result = service.authorizeBooking(VEHICLE, REGISTRANT);
        assertEquals(COMPANY, result.companyId());
        assertTrue(result.isCompanyVehicle());
    }

    @Test
    void companyOwnerCanBookCompanyVehicle() {
        vehicle(COMPANY);
        companyWithDrivers();
        assertEquals(COMPANY, service.authorizeBooking(VEHICLE, COMPANY_OWNER).companyId());
        verify(vehicleClient, never()).getCompanyDrivers(anyLong(), anyString());
    }

    @Test
    void companyDriverCanBookCompanyVehicle() {
        vehicle(COMPANY);
        companyWithDrivers(41, DRIVER);
        assertEquals(COMPANY, service.authorizeBooking(VEHICLE, DRIVER).companyId());
        verify(vehicleClient).getCompanyDrivers(COMPANY, "8");
    }

    @Test
    void outsiderCannotBookCompanyVehicle() {
        vehicle(COMPANY);
        companyWithDrivers(41);
        ForbiddenException e = assertThrows(ForbiddenException.class, () -> service.authorizeBooking(VEHICLE, 99));
        assertTrue(e.getMessage().contains("belongs to a company"));
    }

    @Test
    void inactiveCompanyOnlyAllowsRegistrant() {
        vehicle(COMPANY);
        when(vehicleClient.getCompany(COMPANY)).thenReturn(new VehicleApiResponse<>("success", "200", "Company is not Active", null));
        assertThrows(ForbiddenException.class, () -> service.authorizeBooking(VEHICLE, DRIVER));
        assertDoesNotThrow(() -> service.authorizeBooking(VEHICLE, REGISTRANT));
    }

    @Test
    void unknownVehicleIsNotFound() {
        Request req = Request.create(Request.HttpMethod.GET, "/vehicles/isc/30", new HashMap<>(), null, StandardCharsets.UTF_8, null);
        when(vehicleClient.getVehicleDetailsByIdResponse(VEHICLE))
                .thenThrow(new FeignException.NotFound("Vehicle deleted", req, null, null));
        assertThrows(ResourceNotFoundException.class, () -> service.authorizeBooking(VEHICLE, REGISTRANT));
    }

    @Test
    void vehicleApiDownRefusesBooking() {
        Request req = Request.create(Request.HttpMethod.GET, "/vehicles/isc/30", new HashMap<>(), null, StandardCharsets.UTF_8, null);
        when(vehicleClient.getVehicleDetailsByIdResponse(VEHICLE))
                .thenThrow(new FeignException.ServiceUnavailable("down", req, null, null));
        assertThrows(IllegalStateException.class, () -> service.authorizeBooking(VEHICLE, REGISTRANT));
    }

    @Test
    void companyIdFromVehicleApiIsNoLongerDropped() throws Exception {
        // Shape of vehicle-api's /vehicles/isc/{id} response (it also sends fields garage-api doesn't model)
        String json = "{\"id\":30,\"brand\":\"Toyota\",\"owner\":{\"id\":7,\"name\":\"Omar\"},"
                + "\"companyId\":5,\"companyResponse\":null,\"isForSale\":false}";
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .findAndRegisterModules()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        VehicleDetailResponse v = mapper.readValue(json, VehicleDetailResponse.class);
        assertEquals(5L, v.getCompanyId());
        assertEquals(7, v.getOwner().getId());
    }

    private static String anyString() {
        return org.mockito.ArgumentMatchers.anyString();
    }
}
