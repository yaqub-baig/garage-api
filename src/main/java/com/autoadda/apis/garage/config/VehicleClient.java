package com.autoadda.apis.garage.config;

import com.autoadda.apis.garage.response.VehicleDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicle-api", url = "http://localhost:8030/api/v1")
public interface VehicleClient {
    @GetMapping("/{id}")
    public VehicleDetailResponse getVehicleDetailsById(@PathVariable Integer id);

}