package org.pepppt.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;

/**
 * The ui should extend from this class and override the following
 * methods: onEvent, onCreateForegroundServiceNotification.
 */
public class CoreCallback {
    private static final String TAG = CoreCallback.class.getSimpleName();

    /**
     * Override this method.
     * Will be called from the core. see CoreEvent.
     *
     * @param event The event that has been fired by the core.
     * @param args  The parameter of the event.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void onEvent(CoreEvent event, EventArgs args) {
        Log.i(TAG, "onEvent: " + event.toString());
    }


    /**
     * This function will be used by the core. core functions can call this method
     * to send events to the ui.
     *
     * @param event The event that will be send to the ui.
     * @param args  The args for the event.
     */
    public static void callUI(@NonNull CoreEvent event, EventArgs args) {

        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> exiting");
            return;
        }

        if (ProximityTracingService.getInstance().getCallback() != null) {

            //
            // Run the user callback on the main thread.
            //
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = () -> {
                try {
                    ProximityTracingService.getInstance().getCallback().onEvent(event, args);
                } catch (Exception ex) {
                    Log.e(TAG, "the user code has thrown an exception: " + ex.toString());
                    ex.printStackTrace();
                }
            };
            mainHandler.post(myRunnable);

        }
    }
}
