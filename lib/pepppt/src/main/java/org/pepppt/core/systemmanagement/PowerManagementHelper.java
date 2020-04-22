package org.pepppt.core.systemmanagement;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;

import static android.content.Context.POWER_SERVICE;

/**
 * This class is a utility class that retrieves battery optimization from the device.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PowerManagementHelper {
    private static final String TAG = PowerManagementHelper.class.getSimpleName();

    /**
     * Retrieves the battery optimization for this app on this device.
     * @return True if the optimization is enabled.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isIgnoringBatteryOptimizations() {
        Log.i(TAG, "isIgnoringBatteryOptimizations");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = CoreContext.getAppContext().getPackageName();
            PowerManager pm = (PowerManager) CoreContext.getAppContext().getSystemService(POWER_SERVICE);
            if (pm != null)
                return pm.isIgnoringBatteryOptimizations(packageName);
        } else {
            return true;
        }
        return false;
    }

    /**
     * Starts the battery optimization activity for this app.
     * @param context The context of this app.
     */
    public static void startChangeBatteryOptimization(Context context) {
        Log.i(TAG, "startChangeBatteryOptimization");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String packageName = context.getPackageName();
            Log.i(TAG, "package: " + packageName);
            intent.setData(Uri.parse("package:" + packageName));
            Log.i(TAG, "startActivity(" + intent.toString() + ")");
            context.startActivity(intent);
        }
    }
}
