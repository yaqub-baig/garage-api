package com.service.garage.apis.exception;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String message;

	public ResourceNotFoundException() {

		super();

	}

	public ResourceNotFoundException(String message) {

		super(message);

		this.message = message;

	}

	public ResourceNotFoundException(String message, Throwable cause) {

		super(message, cause);

	}

}