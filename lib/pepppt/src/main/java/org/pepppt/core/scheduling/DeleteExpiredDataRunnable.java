package org.pepppt.core.scheduling;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;
import org.pepppt.core.database.EncounterDatabaseHelper;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

/**
 * Runs in an interval an delete expired data in the database.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DeleteExpiredDataRunnable implements Runnable {
    private static final String TAG = DeleteExpiredDataRunnable.class.getSimpleName();
    private final static int PeriodicRotation = 30 * Units.MINUTES;
    private final static int DeleteExpiredEncounterDataRowsEvery = 24 * Units.HOURS;
    private final static long DeleteExpiredRestPeriod = 6 * Units.HOURS;
    private long lastSuccessfullExpiredDelete;
    private long lastTryExpiredDelete;

    @Override
    public void run() {

        final CoreContext cc = CoreContext.getInstance();

        try {
            if (cc.getCoreSystems().hasBeenDecommissioned()) {
                Log.i(TAG, "hasBeenDecommissioned -> exiting");
                return;
            }

            if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                Log.e(TAG, "do not run on the main thread");
                Log.e(TAG, "use a computation thread instead");
            }

            final long now = System.currentTimeMillis();

            // Expired data must be deleted every day
            if (now - lastTryExpiredDelete >= DeleteExpiredRestPeriod) {
                if (now - lastSuccessfullExpiredDelete > DeleteExpiredEncounterDataRowsEvery) {
                    if (EncounterDatabaseHelper.deleteExpiredRows()) {
                        lastSuccessfullExpiredDelete = now;
                    }
                    lastTryExpiredDelete = now;
                }
            }
        } catch (Exception ex) {
            Telemetry.processException(ex);
        }

        cc.getComputationThread().postDelayed(this, PeriodicRotation);
    }
}
