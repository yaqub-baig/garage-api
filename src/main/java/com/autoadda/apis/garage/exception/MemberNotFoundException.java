package com.autoadda.apis.garage.exception;

/**
 * throw exception when username not exist
 */
public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super ( );
    }

    public MemberNotFoundException(String message) {
        super ( message );
    }

    public MemberNotFoundException(String message, Throwable cause) {
        super ( message, cause );
    }
}
