package com.autoadda.apis.garage.exception;

/**
 * The caller is identified but lacks the garage permission needed for this action (HTTP 403).
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
