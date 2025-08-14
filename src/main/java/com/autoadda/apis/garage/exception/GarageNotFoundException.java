package com.autoadda.apis.garage.exception;

/**
 * throw exception when username not exist
 */
public class GarageNotFoundException extends RuntimeException {

	private String message;

	public GarageNotFoundException() {
		super();
	}

	public GarageNotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public GarageNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
