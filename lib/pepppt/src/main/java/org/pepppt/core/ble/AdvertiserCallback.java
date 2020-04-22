package org.pepppt.core.ble;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;

/**
 * This callback signals the success and the failures of advertisements.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class AdvertiserCallback extends AdvertiseCallback {
    private static final String TAG = "AdvertiserCallback";

    public void onStartFailure(int errorCode) {
        CoreContext.getInstance().getMetrics().NumberOfFailedAdvertisements++;
        switch (errorCode) {
            case ADVERTISE_FAILED_ALREADY_STARTED:
                CoreContext.getInstance().getMetrics().NumberOfFailedAdvertisementsAlreadyStarted++;
                Log.e(TAG, "onStartFailure: ADVERTISE_FAILED_ALREADY_STARTED");
                break;
            case ADVERTISE_FAILED_DATA_TOO_LARGE:
                CoreContext.getInstance().getMetrics().NumberOfFailedAdvertisementsDataTooLarge++;
                Log.e(TAG, "onStartFailure: ADVERTISE_FAILED_DATA_TOO_LARGE");
                break;
            case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                CoreContext.getInstance().getMetrics().NumberOfFailedAdvertisementsTooManyAdvertisers++;
                Log.e(TAG, "onStartFailure: ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
                break;
            case ADVERTISE_FAILED_INTERNAL_ERROR:
                CoreContext.getInstance().getMetrics().NumberOfFailedAdvertisementsInternalError++;
                Log.e(TAG, "onStartFailure: ADVERTISE_FAILED_INTERNAL_ERROR");
                break;
            case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                CoreContext.getInstance().getMetrics().NumberOfFailedAdvertisementsFeatureUnsupported++;
                Log.e(TAG, "onStartFailure: ADVERTISE_FAILED_FEATURE_UNSUPPORTED");
                break;
            default:
                CoreContext.getInstance().getMetrics().NumberOfFailedAdvertisementsUnknown++;
                Log.e(TAG, "onStartFailure: UNKNOWN error");
                break;
        }
        Log.e(TAG, "ERROR: onStartFailure: " + errorCode);
    }

    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        CoreContext.getInstance().getMetrics().NumberOfAdvertisementsBroadcasted++;
        CoreContext.getInstance().getMetrics().addAdTransmitted();
    }


}
