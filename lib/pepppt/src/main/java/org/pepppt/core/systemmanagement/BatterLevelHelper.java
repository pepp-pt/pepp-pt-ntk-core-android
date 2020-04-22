package org.pepppt.core.systemmanagement;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;

/**
 * This class is a utility class that retrieves the battery level from the device.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BatterLevelHelper {

    /**
     * Returns the battery level of the device in percent.
     * @return The battery level in percent.
     */
    public static float getBatteryLevel() {
        final IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = CoreContext.getAppContext().registerReceiver(null, ifilter);

        if (batteryStatus == null)
            return 0;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level * 100 / (float) scale;
    }
}
