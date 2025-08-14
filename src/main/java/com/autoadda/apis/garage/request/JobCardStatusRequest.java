package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobCardStatusRequest {
	private Integer jobcardId;
	private Status status;
	private Integer memberId;
	private String memberType;
	private String comments;
}
