package com.autoadda.apis.garage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class GarageManagementServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(GarageManagementServiceApplication.class, args);
	}
}