package org.pepppt.core.util;

import android.provider.Settings;

import org.pepppt.core.CoreContext;

/**
 * Class that checks the air plane mode.
 */
public abstract class AirplanMode {
    public static boolean isEnabled() {
        return Settings.System.getInt(CoreContext.getAppContext().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}
