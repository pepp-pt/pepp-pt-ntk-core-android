package org.pepppt.core.exceptions;

public class InvalidBackendEndpointException extends Exception {
    private String wrongendpoint;

    public String getWrongendpoint() {
        return wrongendpoint;
    }

    private InvalidBackendEndpointException() {
    }

    public InvalidBackendEndpointException(String wrongendpoint) {
        this.wrongendpoint = wrongendpoint;
    }
}
