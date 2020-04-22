package org.pepppt.core.ble;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;
import org.pepppt.core.ble.profiles.AdvertiserProfile;

/**
 * Starts and stops the advertising based on fixed rate.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AdvertiserRunnables implements Runnable {
    private static final String TAG = ScannerRunnables.class.getSimpleName();
    private long pauseUntil;
    private AdvertiserProfile currentAdvertiserProfile;
    private AdvertiserProfile nextAdvertiserProfile;

    /**
     * Sets a new advertiser profile.
     *
     * @param profile The new scanner profile.
     */
    public void setAdvertiserProfile(@NonNull AdvertiserProfile profile) {
        if (currentAdvertiserProfile == null)
            currentAdvertiserProfile = profile;
        else
            nextAdvertiserProfile = profile;
    }

    /**
     * Returns the current advertiser profile.
     *
     * @return The current advertiser profile.
     */
    public AdvertiserProfile getCurrentAdvertiserProfile() {
        return currentAdvertiserProfile;
    }

    public AdvertiserProfile getNextAdvertiserProfile() {
        return nextAdvertiserProfile;
    }

    /**
     * Pauses the advertising for a specific amount of time.
     * @param pause The pause.
     */
    public void pauseUntil(long pause) {
        pauseUntil = pause;
    }

    /**
     * Switches to the next profile.
     */
    private void switchToNextProfile() {
        if (nextAdvertiserProfile != null) {
            currentAdvertiserProfile = nextAdvertiserProfile;
            nextAdvertiserProfile = null;
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

        final long now = System.currentTimeMillis();

        if (!currentAdvertiserProfile.getEnable() && now > pauseUntil) {
            pauseUntil = 0;
            currentAdvertiserProfile.setEnable(true);
            Log.i(TAG, "the pause is over => enabling advertiser");
        }

        if (currentAdvertiserProfile.getEnable()) {
            // Wait for a valid EBID
            // During the startup the EBID can be empty.
            if (!cc.getKeystoreRunnable().getStorage().emptyEBID()) {
                try {
                    cc.getAdvertiser().stop();
                    cc.getAdvertiser().start();
                } catch (Exception ex) {
                    // Can happen when the user turns off the BT.
                    Log.w(TAG, "exception while advertising: " + ex.getMessage());
                }
            }
        }
        cc.getComputationThread().postDelayed(this, currentAdvertiserProfile.getAdvertisingRate());
    }
}
