package com.autoadda.apis.garage.exception;

public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String message;

    public ConflictException() {

        super ( );
    }

    public ConflictException(String message) {

        super ( message );
        this.message = message;
    }

    public ConflictException(String message, Throwable cause) {
        super ( message, cause );
    }

}