package com.autoadda.apis.garage.exception;

public class InsufficientPointsException extends RuntimeException {
    public InsufficientPointsException(String insufficientPoints) {
        super(insufficientPoints);
    }
}
