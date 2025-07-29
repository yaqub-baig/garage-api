package com.autoadda.apis.garage.config;

import com.autoadda.apis.garage.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Integer id);

}
