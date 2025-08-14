package com.autoadda.apis.garage.exception;

/**
 * throw exception when username already exist
 */
public class MemberAlreadyExistException extends RuntimeException {

	private String message;

	public MemberAlreadyExistException() {
		super();
	}

	public MemberAlreadyExistException(String message) {
		super(message);
		this.message = message;
	}

	public MemberAlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
