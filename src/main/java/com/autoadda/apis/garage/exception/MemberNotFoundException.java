package com.autoadda.apis.garage.exception;

/**
 * throw exception when username not exist
 */
public class MemberNotFoundException extends RuntimeException {

	private String message;

	public MemberNotFoundException() {
		super();
	}

	public MemberNotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public MemberNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
