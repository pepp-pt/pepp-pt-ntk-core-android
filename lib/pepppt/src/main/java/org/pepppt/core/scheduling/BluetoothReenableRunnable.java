package org.pepppt.core.scheduling;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;
import org.pepppt.core.ProximityTracingService;
import org.pepppt.core.ble.BluetoothHelper;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.AirplanMode;
import org.pepppt.core.util.Units;

/**
 * Runs in an interval and reenables the bluetooth adapter if the airplane-mode
 * is not enabled.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BluetoothReenableRunnable implements Runnable {
    private final static long reenableBluetoothDelay = 15 * Units.SECONDS;
    private static final String TAG = BluetoothReenableRunnable.class.getSimpleName();

    @Override
    public void run() {
        final CoreContext cc = CoreContext.getInstance();

        try {
            if (cc.getCoreSystems().hasBeenDecommissioned()) {
                Log.i(TAG, "hasBeenDecommissioned -> exiting");
                return;
            }

            if (ProximityTracingService.getInstance().getBluetoothKeepAlive() &&
                    !AirplanMode.isEnabled()) {
                BluetoothHelper.enableBluetooth();
            }
        } catch (Exception ex) {
            Telemetry.processException(ex);
        }

        cc.getComputationThread().postDelayed(this, reenableBluetoothDelay);
    }
}
