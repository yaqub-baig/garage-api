package com.autoadda.apis.garage.exception;

/**
 * throw exception when username not exist
 */
public class OtherServiceNotFoundException extends RuntimeException {

    private String message;

    public OtherServiceNotFoundException() {
        super();
    }

    public OtherServiceNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public OtherServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
