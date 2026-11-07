package com.autoadda.apis.garage.clients;

import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;
import com.autoadda.apis.garage.response.CompanyDriverResponse;
import com.autoadda.apis.garage.response.VehicleCompanyResponse;
import com.autoadda.apis.garage.response.VehicleApiResponse;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicle-api", url = "http://localhost:8030/api/v1")
public interface VehicleClient {
    @GetMapping("/vehicles/isc/{vehicleId}")
    public VehicleDetailResponse getVehicleDetailsByIdResponse(
            @PathVariable Integer vehicleId);

    /** Company (fleet / showroom) details; body is null when the company is inactive. */
    @GetMapping("/company/{companyId}")
    VehicleApiResponse<VehicleCompanyResponse> getCompany(@PathVariable Long companyId);

    /**
     * A company's drivers. vehicle-api only answers this for the company owner, so the caller passes
     * the owner's id as {@code X-Auto-Adda-Id}.
     */
    @GetMapping("/companies/{companyId}/drivers")
    VehicleApiResponse<List<CompanyDriverResponse>> getCompanyDrivers(@PathVariable Long companyId,
                                                                      @RequestHeader("X-Auto-Adda-Id") String companyOwnerId);

}