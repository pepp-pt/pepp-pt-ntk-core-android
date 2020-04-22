package org.pepppt.core.ble.gatt;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;

/**
 * This class receives scanned advertises and connects to the associated devices.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class GattManager {
    private GattCallback callback;

    public GattManager() {
        callback = new GattCallback();
    }

    /**
     * Handles the device of a received advertisement.
     */
    public void handleRemoteDevice(BluetoothDevice remoteDevice) {
        remoteDevice.connectGatt(CoreContext.getAppContext(), true, callback);

    }
}
