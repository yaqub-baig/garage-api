package com.autoadda.apis.garage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
@EnableScheduling
public class GarageServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run( GarageServiceApplication.class, args);
	}
}