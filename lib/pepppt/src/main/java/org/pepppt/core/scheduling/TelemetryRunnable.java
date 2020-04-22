package org.pepppt.core.scheduling;

import android.content.Context;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.BuildConfig;
import org.pepppt.core.CoreContext;
import org.pepppt.core.Metrics;
import org.pepppt.core.ProximityTracingService;
import org.pepppt.core.database.DataSource;
import org.pepppt.core.systemmanagement.BatterLevelHelper;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.telemetry.KPI;
import org.pepppt.core.util.Units;

import java.io.File;
import java.util.ArrayList;

/**
 * Runs in an interval and sends the telemetry to the telementry server.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class TelemetryRunnable implements Runnable {
    private final static long Rate = 60 * Units.SECONDS;
    private static final String TAG = AggregationRunnable.class.getSimpleName();


    /**
     * Runs once every minute and sends the telemetry to the telemetry server.
     */
    @Override
    public void run() {
        final CoreContext cc = CoreContext.getInstance();

        //
        // return if the in debug mode or when the telemetry is disabled.
        //
        if (!(BuildConfig.DEBUG && ProximityTracingService.getInstance().getEnableTelemetry()))
            return;

        try {

            if (cc.getCoreSystems().hasBeenDecommissioned()) {
                Log.i(TAG, "hasBeenDecommissioned -> exiting");
                return;
            }

            if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                Log.e(TAG, "do not run on the main thread");
                Log.e(TAG, "use a computation thread instead");
            }

            if (ProximityTracingService.getInstance().getEnableTelemetry()) {
                Metrics m = cc.getMetrics();

                try {
                    ArrayList<KPI> kpis = new ArrayList<>();
                    kpis.add(new KPI("adsrxl1m", m.getAdsReceivedLast60Seconds()));
                    kpis.add(new KPI("adsrxl2h", m.getAdsReceivedLast2Hours()));
                    kpis.add(new KPI("adsrxl4h", m.getAdsReceivedLast4Hours()));
                    kpis.add(new KPI("adsrxl8h", m.getAdsReceivedLast8Hours()));
                    kpis.add(new KPI("adstxl1m", m.getAdsTransmittedLast60Seconds()));
                    kpis.add(new KPI("adstxl2h", m.getAdsTransmittedLast2Hours()));
                    kpis.add(new KPI("adstxl4h", m.getAdsTransmittedLast4Hours()));
                    kpis.add(new KPI("adstxl8h", m.getAdsTransmittedLast8Hours()));
                    kpis.add(new KPI("exceptions", Telemetry.ExceptionCount));
                    kpis.add(new KPI("ebidspd", m.getRotatedEbidLast24Hours()));
                    kpis.add(new KPI("ebidsib", m.EbidsLeftInTheBatch));
                    kpis.add(new KPI("mts", m.MaxTimeStall));
                    kpis.add(new KPI("adstxcnt", m.NumberOfAdvertisementsBroadcasted));
                    kpis.add(new KPI("adsrxcnt", m.NumberOfAdvertisementsReceived));
                    kpis.add(new KPI("adsrxcntls", m.NumberOfAdvertisementsReceivedInLastScan));
                    kpis.add(new KPI("scancnt", m.NumberOfScans));
                    kpis.add(new KPI("nofa", m.NumberOfFailedAdvertisements));
                    kpis.add(new KPI("nofaas", m.NumberOfFailedAdvertisementsAlreadyStarted));
                    kpis.add(new KPI("nofadtl", m.NumberOfFailedAdvertisementsDataTooLarge));
                    kpis.add(new KPI("nofatma", m.NumberOfFailedAdvertisementsTooManyAdvertisers));
                    kpis.add(new KPI("nofaie", m.NumberOfFailedAdvertisementsInternalError));
                    kpis.add(new KPI("nofafu", m.NumberOfFailedAdvertisementsFeatureUnsupported));
                    kpis.add(new KPI("nofau", m.NumberOfFailedAdvertisementsUnknown));
                    kpis.add(new KPI("encs", m.NumberOfEncounters));
                    kpis.add(new KPI("oscbs", m.isOffloadedScanBatchingSupported));
                    kpis.add(new KPI("ofs", m.isOffloadedFilteringSupported));
                    kpis.add(new KPI("mas", m.isMultipleAdvertisementSupported));
                    long lastEncounter = m.LastEncounterTime;
                    kpis.add(new KPI("lenct", lastEncounter));
                    if (lastEncounter == 0) {
                        kpis.add(new KPI("lencdt", 0));
                    } else {
                        double lastEncounterInMin = ((double) (System.currentTimeMillis() - lastEncounter)) / Units.MINUTES;
                        kpis.add(new KPI("lencdt", (float) lastEncounterInMin));
                    }
                    kpis.add(new KPI("battery", BatterLevelHelper.getBatteryLevel()));
                    File file = CoreContext.getAppContext().getDatabasePath(DataSource.DB_NAME);
                    int file_size = Integer.parseInt(String.valueOf(file.length()));
                    kpis.add(new KPI("dbsize", file_size));
                    if (m.LastBatchRefresh == 0) {
                        kpis.add(new KPI("lastbatch", 0));
                    } else {
                        double timeSinceLastBatchRefresh = ((double) (System.currentTimeMillis() - m.LastBatchRefresh)) / Units.MINUTES;
                        kpis.add(new KPI("lastbatch", (float) timeSinceLastBatchRefresh));
                    }
                    kpis.add(new KPI("uptime", (float) ((float) m.getSecondsSinceStart() / 60.0)));
                    kpis.add(new KPI("aggcnt", m.AggregationsCount));
                    kpis.add(new KPI("aggrcnt", m.AggregatedResultsCount));
                    kpis.add(new KPI("aggdtcnt", m.AggregatedDataCount));
                    kpis.add(new KPI("bles", m.isBluetoothLeSupported));
                    kpis.add(new KPI("bo", m.isBatterOptimizationTurnedOn));
                    kpis.add(new KPI("lse", getLocationServiceEnabledValue()));

                    Telemetry.sendKPIs(kpis);

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                    ex.printStackTrace();
                }
            }

            cc.getComputationThread().postDelayed(this, Rate);

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            ex.printStackTrace();
        }

    }

    /**
     * This method returns the status of the location service. The telemetry server can use
     * this information to solve issues related to different kind of models. GPS is not used
     * in the software.
     *
     * @return 1 if enabled, 0 if not enabled, -1 in case of an error.
     */
    private static long getLocationServiceEnabledValue() {
        Context context = CoreContext.getAppContext();
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (lm == null)
            return -1;

        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                return 1;
            else
                return 0;
        } catch (Exception ex) {
            return -1;
        }
    }

}
