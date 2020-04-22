package org.pepppt.core.exceptions;

public class MissingBackendEndpointException extends Exception {
    private String missingendpoint;

    public String getMissingEndpoint() {
        return missingendpoint;
    }

    private MissingBackendEndpointException() {
    }

    public MissingBackendEndpointException(String missingendpoint) {
        this.missingendpoint = missingendpoint;
    }
}
