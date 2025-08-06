package com.service.garage.apis.exception;

/**
 * throw exception when username not exist
 */
public class CustomerNotFoundException extends RuntimeException {

	private String message;

	public CustomerNotFoundException() {
		super();
	}

	public CustomerNotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public CustomerNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
