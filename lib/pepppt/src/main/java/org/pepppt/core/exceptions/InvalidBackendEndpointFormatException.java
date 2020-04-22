package org.pepppt.core.exceptions;

import androidx.annotation.Nullable;

public class InvalidBackendEndpointFormatException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "the number of backend endpoints has to be exactly 5.";
    }
}
