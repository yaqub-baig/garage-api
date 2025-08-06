package com.service.garage.apis.exception;

public class UnauthorizedAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String message;

	public UnauthorizedAccessException() {

		super();

	}

	public UnauthorizedAccessException(String message) {

		super(message);

		this.message = message;

	}

	public UnauthorizedAccessException(String message, Throwable cause) {

		super(message, cause);

	}

}