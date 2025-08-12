package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobCardStatusActivity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "jobcard_id")
	private Integer jobcardId;

	@Column(name = "status")
	private Status status;

	@Column(name = "comments")
	private String comments;

	@Column(name = "insert_tms")
	private LocalDate insert_tms;

	@Column(name = "member_id")
	private Integer memberId;

	@Column(name = "member_type")
	private String memberType;

}
