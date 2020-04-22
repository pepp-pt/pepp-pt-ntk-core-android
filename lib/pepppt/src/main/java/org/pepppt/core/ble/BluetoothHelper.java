package org.pepppt.core.ble;

import android.bluetooth.BluetoothAdapter;

import androidx.annotation.RestrictTo;

/**
 * Class to enable bluetooth
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BluetoothHelper {
    public static void enableBluetooth() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null)
            if (!adapter.isEnabled())
                adapter.enable();
    }
}
