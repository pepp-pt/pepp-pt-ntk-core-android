package org.pepppt.core.ble;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import org.pepppt.core.ble.profiles.ScannerProfile;
import org.pepppt.core.CoreContext;
import org.pepppt.core.telemetry.Telemetry;

/**
 * This class starts and stops the scanning based on a defined rate.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ScannerRunnables implements Runnable {
    private static final String TAG = ScannerRunnables.class.getSimpleName();
    private Runnable stopRunnable;
    private long pauseUntil;
    private ScannerProfile currentScannerProfile;
    private ScannerProfile nextScannerProfile;


    public ScannerRunnables() {
        stopRunnable = () -> {
            try {
                final CoreContext cc = CoreContext.getInstance();

                cc.getBLEScanner().stop();

                cc.getMetrics().NumberOfAdvertisementsReceivedInLastScan =
                        cc.getBLEScanner().getScanCounter();
            } catch (Exception ex) {
                Log.e(TAG, "exception stopping the scan: " + ex.getMessage());
            }
        };
    }

    /**
     * Sets a new scanner profile.
     *
     * @param profile The new scanner profile.
     */
    public void setScannerProfile(@NonNull ScannerProfile profile) {
        if (currentScannerProfile == null)
            currentScannerProfile = profile;
        else
            nextScannerProfile = profile;
    }

    public ScannerProfile getCurrentScannerProfile() {
        return currentScannerProfile;
    }

    public ScannerProfile getNextScannerProfile() {
        return nextScannerProfile;
    }

    public void pauseUntil(long pause) {
        pauseUntil = pause;
    }

    private void switchToNextProfile() {
        if (nextScannerProfile != null) {
            currentScannerProfile = nextScannerProfile;
            nextScannerProfile = null;
        }
    }

    @Override
    public void run() {
        final CoreContext cc = CoreContext.getInstance();

        if (cc.getCoreSystems().hasBeenDecommissioned()) {
            Log.i(TAG, "hasBeenDecommissioned -> exiting");
            return;
        }

        //
        // switch to the next profile if available.
        //
        switchToNextProfile();

        try {
            long now = System.currentTimeMillis();

            //
            // If autopause enabled check if the time has run up to enable the
            // scanner again.
            //
            if (!currentScannerProfile.getEnable() && now > pauseUntil) {
                pauseUntil = 0;
                currentScannerProfile.setEnable(true);
                Log.i(TAG, "the pause is over => enabling scanner");
            }

            if (currentScannerProfile.getEnable()) {
                //
                // Start the scanning.
                //
                cc.getBLEScanner().start();

                //
                // Stop the scanning after active time span is over.
                //
                cc.getComputationThread().
                        postDelayed(stopRunnable, currentScannerProfile.getScannerActiveTimeSpan());
            }

            //
            // After the rate timespan restart the start of scanner.
            //
            cc.getComputationThread().
                    postDelayed(this, currentScannerProfile.getScannerRate());

        } catch (final IllegalStateException ex) {
            Telemetry.processException(ex);
            // Bluetooth might be turned off -> because of state change -> ignore and start
            // the handler again
            cc.getComputationThread().postDelayed(this, currentScannerProfile.getScannerRestartWhenBTDown());
            Log.i(TAG, "bluetooth might be off -> reschedule scanner");
        } catch (final Exception ex) {
            Telemetry.processException(ex);
            Log.e(TAG, "exception while scan: " + ex.getMessage());
        }
    }
}
