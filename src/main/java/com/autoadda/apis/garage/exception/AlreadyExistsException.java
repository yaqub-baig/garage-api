package com.autoadda.apis.garage.exception;

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