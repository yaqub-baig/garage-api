package com.autoadda.apis.garage.config;

import com.autoadda.apis.garage.response.GenericResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("media-service")
public interface MediaClient {
    @GetMapping("/{mediaFileId}")
    public GenericResponse get(@PathVariable Long mediaFileId);
}