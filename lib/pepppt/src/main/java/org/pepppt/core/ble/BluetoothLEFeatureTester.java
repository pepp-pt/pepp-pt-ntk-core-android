package org.pepppt.core.ble;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.RestrictTo;

/**
 * Class to check the bluetooth le feature.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BluetoothLEFeatureTester {
    public static Boolean checkHasFeature(Context context) {
        final PackageManager pm = context.getPackageManager();
        if (pm == null)
            return false;
        return pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}
