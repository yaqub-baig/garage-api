package com.autoadda.apis.garage.exception;

public class UnauthorizedAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public UnauthorizedAccessException() {

        super ( );

    }

    public UnauthorizedAccessException(String message) {

        super ( message );


    }

    public UnauthorizedAccessException(String message, Throwable cause) {

        super ( message, cause );

    }

}