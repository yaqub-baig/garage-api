package com.autoadda.apis.garage.clients;

import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.ERole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "identity-api", url = "http://localhost:8010/api/v1")
public interface AuthClient {

    @GetMapping("/auth/{id}")
    UserResponse getUser(@PathVariable Integer id);

    @PutMapping("/auth/update/role/{id}")
    GenericResponse updateRole(@PathVariable Integer id, @RequestParam ERole role);

    @GetMapping("/auth/byNumber/{phoneNumber}")
    ResponseEntity<UserResponse> getUserDetails(@PathVariable String phoneNumber);
}
