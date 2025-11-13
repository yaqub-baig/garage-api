package com.autoadda.apis.garage.clients;

import com.autoadda.apis.garage.request.SmsRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.ERole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "identity-api", url = "https://identity-api-wbad.onrender.com/api/v1")
public interface AuthClient {

    @GetMapping("/auth/{id}")
    UserResponse getUser(@PathVariable Integer id);

    @PutMapping("/auth/update/role/{id}")
    GenericResponse updateRole(@PathVariable Integer id, @RequestParam ERole role);

    @GetMapping("/auth/byNumber/{phoneNumber}")
    ResponseEntity<UserResponse> getUserDetails(@PathVariable String phoneNumber);

    @PostMapping("/auth/send-sms")
    public GenericResponse sendSms(@RequestBody SmsRequest request);
}
