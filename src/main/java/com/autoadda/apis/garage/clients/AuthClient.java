package com.autoadda.apis.garage.clients;

import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.ERole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "identity-api", url = "http://localhost:8010/api/v1")
public interface AuthClient {

    @GetMapping("/auth/{id}")
    public UserResponse getUser(@PathVariable Integer id);

    @PutMapping("/auth/update/role/{id}")
    public GenericResponse updateRole(@PathVariable Integer id, @RequestParam ERole role);
}
