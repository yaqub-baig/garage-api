package com.service.garage.apis.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageDocumentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "extension")
	private String extension;
	
	@Column(name = "byte_str")
	private String byteStrng;
	
	@Column(name = "status")
	private String status;

	@Column(name = "insert_tms")
	private Date insertTms;
}
