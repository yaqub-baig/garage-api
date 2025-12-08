package com.autoadda.apis.garage.clients;

import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "part-api", url = "http://localhost:8060/api/v1")
public interface PartClient {

    @GetMapping("/products/partnumber/{partNumber}")
    ProductResponse getProductByPartNumber(@PathVariable String partNumber);
}
