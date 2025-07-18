package com.autoadda.apis.garage.exception;

/**
 * throw exception when category already exist
 */
public class AlreadyExistException extends RuntimeException {

	private String message;

	public AlreadyExistException() {
		super();
	}

	public AlreadyExistException(String message) {
		super(message);
		this.message = message;
	}

	public AlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
