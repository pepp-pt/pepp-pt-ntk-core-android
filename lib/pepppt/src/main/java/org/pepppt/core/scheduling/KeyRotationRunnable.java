package org.pepppt.core.scheduling;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;

import org.pepppt.core.CoreCallback;
import org.pepppt.core.CoreContext;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.storage.BatchIdsStorage;
import org.pepppt.core.storage.EBID;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

/**
 * Runs in an interval and rotates to new ids every 30 minutes.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class KeyRotationRunnable implements Runnable {
    private static String TAG = KeyRotationRunnable.class.getSimpleName();
    @SuppressWarnings("PointlessArithmeticExpression")
    private final static long BackendRestPeriod = 1 * Units.MINUTES;
    private final static int PeriodicRotation = 10 * Units.SECONDS;
    private final static int KeyRotationSafeZone = 20 * Units.SECONDS;
    private long lastTryRotation;
    private long startStallTime;

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

            final KeystoreRunnable kr = cc.getKeystoreRunnable();
            final long now = kr.getServerTime();

            // if the current EBID is empty or its time to rotate to a new one
            // rotate to the next valid id.
            if (shouldRotateNow(kr.getStorage().getActiveEBID(),
                    now, lastTryRotation, BackendRestPeriod, KeyRotationSafeZone)) {
                lastTryRotation = kr.getServerTime();
                rotateToNextID();
            }
        } catch (Exception ex) {
            Telemetry.processException(ex);
        }

        cc.getComputationThread().postDelayed(this, PeriodicRotation);
    }

    static boolean shouldRotateNow(EBID ebid, long correctedTime, long lastTryRotation,
                                   long restPeriod, long keyRotationSafeZone) {
        if (correctedTime - lastTryRotation >= restPeriod) {
            return ebid == null || ebid.getId() == null || ebid.getId().toString().isEmpty() ||
                    correctedTime >= ebid.getExpired() - keyRotationSafeZone;
        }
        return false;
    }

    /**
     * Switches to the next ebid in the ebid list.
     */
    private void rotateToNextID() {
        final CoreContext cc = CoreContext.getInstance();
        final BatchIdsStorage storage = cc.getKeystoreRunnable().getStorage();

        //
        // Before selecting a new ebids remove all ids that are expired or
        // are about to expire in the next minutes. The storage will have only ids
        // that are last for at least 3 minutes.
        //
        storage.removeExpiredIds();

        //
        // Its possible that the storage is empty. In that case we have to wait for
        // a new batch of ebids.
        //
        final EBID takefirst = storage.takeFirstId();
        if (takefirst != null) {
            //
            // Set the active ebid.
            //
            storage.setActiveEbid(takefirst);
            storeEbidToDatabase(takefirst);

            // Write to database
            KeystoreRunnable.storeEbidListToDatabase();

            CoreCallback.callUI(CoreEvent.NEW_EBID, new EventArgs());
            cc.getCoreSystems().resumeFromStall();

            //
            // Statistics and metrics
            //

            // Compute the stall time for the telemetry
            if (startStallTime != 0) {
                long stalltimedelta = System.currentTimeMillis() - startStallTime;
                if (stalltimedelta > cc.getMetrics().MaxTimeStall)
                    cc.getMetrics().MaxTimeStall = stalltimedelta;
                startStallTime = 0;
            }

            // statistics
            cc.getMetrics().EbidsLeftInTheBatch = storage.count();
            cc.getMetrics().addEbidRotation();

            Log.i(TAG, "ids left in the batch: " + storage.count());
        } else {
            cc.getMetrics().EbidsLeftInTheBatch = 0;
            cc.getCoreSystems().setStalled();

            // set the stall time for the telemetry
            if (startStallTime == 0)
                startStallTime = System.currentTimeMillis();

            Log.e(TAG, "takefirst is null -> idle");
            Log.e(TAG, "INFO: this can happen if the app starts for the first time or the batchlist hasn't been update for a long time.");
        }
    }

    /**
     * Store the ebid to the database.
     *
     * @param ebid The ebid.
     */
    private void storeEbidToDatabase(EBID ebid) {
        if (ebid != null) {
            Gson gson = new Gson();
            String ebidjson = gson.toJson(ebid);
            Log.i(TAG, "store ebid = " + ebidjson);
            SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.ACTIVE_EBID_KEY, ebidjson);
        }
    }
}
