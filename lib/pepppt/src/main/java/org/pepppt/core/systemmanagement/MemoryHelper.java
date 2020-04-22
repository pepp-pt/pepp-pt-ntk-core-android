package org.pepppt.core.systemmanagement;

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;

/**
 * This class is a utility class that retrieves the memory info from the device.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class MemoryHelper {

    /**
     * Return general information about the memory state of the system.
     * @return The memory information.
     */
    public static ActivityManager.MemoryInfo getAvailableMemory() {
        Context context = CoreContext.getAppContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo;
        }
        return null;
    }
}
