package com.autoadda.apis.garage.config;

import com.autoadda.apis.garage.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-api", url = "http://localhost:8010/api/v1")
public interface AuthClient {
    @GetMapping("/auth/{id}")
    public UserResponse getUser(@PathVariable Integer id);

}
