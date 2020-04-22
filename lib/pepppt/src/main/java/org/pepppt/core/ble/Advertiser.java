package org.pepppt.core.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.ble.profiles.AdvertiserProfile;
import org.pepppt.core.telemetry.Telemetry;

/**
 * Start and stops the advertisements based on the advertise settings and the
 * advertise data.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Advertiser {
    private static final String TAG = "Advertiser";
    private AdvertiserCallback callback;

    public Advertiser() {
        callback = new AdvertiserCallback();
    }

    /**
     * Starts the advertising.
     */
    void start() {
        try {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                final BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
                if (advertiser != null) {
                    final AdvertiseSettings settings = AdvertiserProfile.createSettingsFromCurrentProfile();
                    final AdvertiseData data = AdvertiserDataHelper.createData();
                    advertiser.startAdvertising(settings, data, callback);
                }
            }
        } catch (Exception ex) {
            Telemetry.processException(ex);
            Log.i(TAG, "exception while advertising: " + ex.toString());
        }
    }

    /**
     * Stops the advertising.
     */
    void stop() {
        try {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                final BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
                if (advertiser != null) {
                    advertiser.stopAdvertising(callback);
                }
            }
        } catch (final Exception ex) {
            Telemetry.processException(ex);
            Log.i(TAG, "exception while stop advertising: " + ex.toString());
        }
    }
}
