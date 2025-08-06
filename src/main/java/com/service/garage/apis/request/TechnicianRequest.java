package com.service.garage.apis.request;

import java.util.List;

import com.service.garage.apis.entity.Garage;
import com.service.garage.apis.util.Status;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TechnicianRequest {

	private Integer id;
	
	private String emiratesId;

	private String firstName;

	private String lastName;

	private String emailId;

	private String mobileNumber;

	private String vatNumber;

    private List<String> skillSet;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Garage garage;
    private Integer garageId;    
	
	private String profileImg;
	
}
