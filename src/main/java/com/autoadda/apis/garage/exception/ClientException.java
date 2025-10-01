package com.autoadda.apis.garage.exception;

/**
 * Base exception for all client-related errors
 */
public class ClientException extends RuntimeException {
    private final String clientName;
    private final int statusCode;

    public ClientException(String clientName, String message) {
        super ( message );
        this.clientName = clientName;
        this.statusCode = 0;
    }

    public ClientException(String clientName, String message, int statusCode) {
        super ( message );
        this.clientName = clientName;
        this.statusCode = statusCode;
    }

    public ClientException(String clientName, String message, Throwable cause) {
        super ( message, cause );
        this.clientName = clientName;
        this.statusCode = 0;
    }

    public ClientException(String clientName, String message, int statusCode, Throwable cause) {
        super ( message, cause );
        this.clientName = clientName;
        this.statusCode = statusCode;
    }

    public String getClientName() {
        return clientName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
