package com.service.garage.apis.exception;

public class AlreadyExistsException extends RuntimeException {

    private String message;

    public AlreadyExistsException() {
        super();
    }

    public AlreadyExistsException(String message) {
        super(message);
        this.message = message;
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}