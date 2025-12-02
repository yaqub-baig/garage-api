package com.autoadda.apis.garage.exception;

/**
 * throw exception when username not exist
 */
public class GarageNotFoundException extends RuntimeException {


    public GarageNotFoundException() {
        super ( );
    }

    public GarageNotFoundException(String message) {
        super ( message );
    }

    public GarageNotFoundException(String message, Throwable cause) {
        super ( message, cause );
    }
}
