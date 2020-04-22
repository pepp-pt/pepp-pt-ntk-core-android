package org.pepppt.core.scheduling;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreContext;
import org.pepppt.core.ble.aggregation.AggregationResult;
import org.pepppt.core.database.EncounterDatabaseHelper;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.util.Map;
import java.util.UUID;


/**
 * Run in an interval and aggregates scanned results from other
 * apps nearby.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AggregationRunnable implements Runnable {
    private final static long aggregatorRate = 60 * Units.SECONDS;
    private static final String TAG = AggregationRunnable.class.getSimpleName();

    public static long getAggregatorRate() {
        return aggregatorRate;
    }

    @Override
    public void run() {

        final CoreContext cc = CoreContext.getInstance();

        try {
            if (cc.getCoreSystems().hasBeenDecommissioned()) {
                Log.i(TAG, "hasBeenDecommissioned -> exiting");
                return;
            }

            cc.getMetrics().AggregationsCount++;

            for (AggregationResult<UUID> result : cc.getAggregator().process()) {
                cc.getMetrics().AggregatedResultsCount++;
                final long startTime = result.getTimeNanosStart();
                final Map<UUID, AggregationResult.AggregationData> aggregatedData = result.getData();
                for (UUID senderId : aggregatedData.keySet()) {
                    if (senderId != null && aggregatedData.get(senderId) != null) {
                        AggregationResult.AggregationData data = aggregatedData.get(senderId);
                        if (data != null) {
                            final int rv = data.getRssiVariance();
                            final int rm = data.getMeanRssi();
                            final int tc = data.getMeanTxCorrectionLevel();
                            final int ct = data.getCountOfObservations();
                            final int tl = data.getMeanTxLevel();

                            cc.getMetrics().AggregatedDataCount++;

                            try {

                                //
                                // Store the scan result directly to database.
                                //
                                EncounterDatabaseHelper.addAggregatedData(
                                        startTime,
                                        senderId.toString(),
                                        rv,
                                        rm,
                                        tc,
                                        ct,
                                        tl
                                );
                            } catch (Exception ex) {
                                Telemetry.processException(ex);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Telemetry.processException(ex);
        }

        cc.getComputationThread().postDelayed(this, aggregatorRate);
    }
}
