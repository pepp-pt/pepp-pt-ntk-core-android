package org.pepppt.core.network;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.json.JSONObject;
import org.pepppt.core.CoreCallback;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.RegistrationEvents;
import org.pepppt.core.telemetry.Telemetry;

import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Class for registration process of a new device / user
 * Runnable to run on a non-ui thread.
 * Trigger events:
 * REGISTRATION_SUCCESS: if the registration was successfully
 * REGISTRATION_FAILED: if the registration was faulty e.g. bad connection, wrong input etc.
 * Gets called via main ui adapter class
 * ProximityTracingService -> public void signUpUser(@NonNull String firebaseToken)
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DeviceRegistrationService implements Runnable {
    private static final String LOG_TAG = DeviceRegistrationService.class.getSimpleName();

    public DeviceRegistrationService() {
    }

    @Override
    public void run() {
        try {
            if (Objects.equals(SettingsDatabaseHelper.getByKey(DataSourceKeys.DEVICE_IS_SIGNED_UP_KEY).getValue(), DataSourceKeys.VALUE_TRUE)) {
                CoreCallback.callUI(CoreEvent.REGISTRATION_FAILED, new RegistrationEvents("device is already registered", "error"));
                return;
            }

            String firebaseToken = Objects.requireNonNull(SettingsDatabaseHelper.getByKey(DataSourceKeys.DEVICE_FIREBASETOKEN_KEY)).getValue();
            if (firebaseToken == null || Objects.equals(firebaseToken, "")) {
                CoreCallback.callUI(CoreEvent.REGISTRATION_FAILED, new RegistrationEvents("illegal Firebase Token", "error"));
                return;
            }
            // REGISTER DEVICE AT CGA BACKEND
            JSONObject apiResponse = ApiRequest.deviceSignup(firebaseToken);

            if (apiResponse.has("error")) {
                Log.e(LOG_TAG, "Error signing up the device at backend!");
                CoreCallback.callUI(CoreEvent.REGISTRATION_FAILED, new RegistrationEvents("registration backend error", "error"));
            } else {
                // Create user with given data from Backend
                SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.DEVICE_ID_KEY, apiResponse.getString("device_id"));
                SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.DEVICE_SECRET_KEY, apiResponse.getString("device_secret"));
                SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.DEVICE_INSTANCE_KEY, apiResponse.getString("device_instance"));
                SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.DEVICE_FIREBASETOKEN_KEY, firebaseToken);
                SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.DEVICE_IS_SIGNED_UP_KEY, DataSourceKeys.VALUE_TRUE);
                CoreCallback.callUI(CoreEvent.REGISTRATION_SUCCESS, new RegistrationEvents("registration was successful", "info"));

            }


        } catch (UnknownHostException ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            Telemetry.processException(ex);
            CoreCallback.callUI(CoreEvent.REGISTRATION_FAILED, new RegistrationEvents("backend connection error", "error"));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            Telemetry.processException(e);

            CoreCallback.callUI(CoreEvent.REGISTRATION_FAILED, new RegistrationEvents("error: " + e.toString() +"|" + e.getMessage(), "error"));
        }
    }
}
