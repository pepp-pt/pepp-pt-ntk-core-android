package org.pepppt.core.exceptions;

import androidx.annotation.Nullable;

public class PackageNotFoundException extends Exception {
    @Nullable
    @Override
    public String getMessage() {
        return "could not find pepp-pt core package";
    }
}
