package org.pepppt.core.database;

import androidx.annotation.RestrictTo;

/**
 * The keys used in the database.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DataSourceKeys {
    public static final String VALUE_EMPTY = "";
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";

    public static final String TELEMETRY_ID_KEY = "telemetry_id";
    public static final String EBIDS_KEY = "ebids";
    public static final String LAST_SUCCESSFULL_KEYSTORE_KEY = "lastskf";
    public static final String ACTIVE_EBID_KEY = "active_ebid";

    public static final String TOKEN_LAST_TOKEN_KEY = "token_lastToken";
    public static final String TOKEN_EXPIRES_AT_KEY = "token_expiresAt";

    public static final String DEVICE_ID_KEY = "device_id";
    public static final String DEVICE_SECRET_KEY = "device_secret";
    public static final String DEVICE_INSTANCE_KEY = "device_instance";
    public static final String DEVICE_IS_SIGNED_UP_KEY = "device_isSignedUp";
    public static final String DEVICE_FIREBASETOKEN_KEY = "device_firebaseToken";

    public static final String UPLOAD_TAN_EXPIRY_DATE_KEY = "upload_Tan_ExpiryDate";
    public static final String UPLOAD_TAN_KEY = "upload_Tan";
}
