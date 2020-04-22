package org.pepppt.core;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.ble.BluetoothHelper;
import org.pepppt.core.ble.BluetoothLEFeatureTester;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.events.InsufficientPermissionsEventArgs;
import org.pepppt.core.exceptions.MissingBackendEndpointException;
import org.pepppt.core.messages.CheckMessagesRunnable;
import org.pepppt.core.permissions.PermissionHelper;
import org.pepppt.core.systemmanagement.PowerManagementHelper;
import org.pepppt.core.scheduling.AggregationRunnable;
import org.pepppt.core.telemetry.KPI;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.util.ArrayList;

/**
 * This class spools up the different services and runnables in the
 * core.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CoreSystems {
    private static final String TAG = CoreSystems.class.getSimpleName();

    // When the service(core) dont have the necessary permissions it
    // will go into idle mode and wait a short period and tries again.
    private final static int PermissionRetryTimeSpan = 20 * Units.SECONDS;

    private boolean decommissioned;
    private boolean stalled = false;
    private Runnable startup;

    public void start() {
        final ProximityTracingService pts = ProximityTracingService.getInstance();

        if (pts.getForegroundServiceNotification() == null) {
            Log.e(TAG, "you forgot to call Core.setForegroundServiceNotification().");
            Log.e(TAG, "stopping core systems.");
            return;
        }

        if (pts.getCallback() == null) {
            Log.e(TAG, "you forgot to call Core.setCallback().");
            Log.e(TAG, "stopping core systems.");
            return;
        }

        try {
            pts.getEndpoints().check();
        } catch (MissingBackendEndpointException e) {
            Log.e(TAG, "you are missing an endpoint: " + e.getMissingEndpoint());
            Log.e(TAG, "stopping core systems.");
            return;
        }

        if (pts.getCertificatePinningHost() == null ||
                pts.getCertificatePinningHost().isEmpty()) {
            Log.e(TAG, "you haven't set the certificate pinning host.");
            Log.e(TAG, "see Core.setCertificatePinnerInformation.");
            Log.e(TAG, "stopping core systems.");
            return;
        }

        if (pts.getCertificatePinningData() == null ||
                pts.getCertificatePinningData().length == 0) {
            Log.e(TAG, "you haven't set the certificate pinning data.");
            Log.e(TAG, "see Core.setCertificatePinnerInformation.");
            Log.e(TAG, "stopping core systems.");
            return;
        }

        //////////////////////////////////////////////////////////////////////////
        // Thread creation
        //////////////////////////////////////////////////////////////////////////

        //
        // Starts the computation thread for the core system handlers.
        //
        CoreContext.getInstance().getComputationThread().create();

        //
        // Starts the api request thread for the core system handlers.
        //
        CoreContext.getInstance().getApiRequestThread().create();

        //
        // Create the api request handler.
        //
        CoreContext.getInstance().getApiRequestHandler().init();


        startup = () -> {
            Log.i(TAG, "checking all permissions");
            ArrayList<String> missingpermission =
                    PermissionHelper.checkAllPermissions(CoreContext.getAppContext());
            if (missingpermission.size() == 0) {
                Log.i(TAG, "all permissions are granted");
                // If all permissions are set start the
                // work.
                startCoreSystems();
            } else {
                Log.i(TAG, "some permissions are missing");
                // Inform the user that we need important permission
                // to start the service.
                CoreCallback.callUI(CoreEvent.INSUFFICIENT_PERMISSIONS,
                        new InsufficientPermissionsEventArgs(missingpermission));

                // check the permissions again after some time.
                CoreContext.getInstance().getComputationThread().postDelayed(startup, PermissionRetryTimeSpan);
            }
        };
        CoreContext.getInstance().getComputationThread().post(startup);
    }

    // Some of these workers need specific permissions to run
    private void startCoreSystems() {
        Log.i(TAG, "core version = " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        final CoreContext cc = CoreContext.getInstance();

        // Start the bluetooth state receiver.
        // When the user turns off the BT we can
        // act on it.
        Log.i(TAG, "initializing statechange...");
        cc.getBluetoothStateChange().init(CoreContext.getAppContext());

        //
        // Start the periodic jobs.
        //
        startScheduledRunnables();

        //
        // Enables Bluetooth if it turns off.
        //
        Log.i(TAG, "initializing BluetoothReenableRunnable...");
        cc.getComputationThread().post(cc.getBluetoothReenableRunnable());

        // Start the BT services when BT is installed in
        // the device.
        if (BluetoothLEFeatureTester.checkHasFeature(CoreContext.getAppContext())) {
            Log.i(TAG, "BLE feature");
            cc.getMetrics().isBluetoothLeSupported = 1;

            //
            // Collect chip features for the telemetry.
            //
            collectBluetoothLeChipFeatures();

            //
            // Start the advertiser runnable that broadcasts our data.
            //
            Log.i(TAG, "AdvertiserHandler.start");
            cc.getComputationThread().post(cc.getAdvertiserRunnables());

            //
            // Start the scanner runnable that receive other broadcasts.
            //
            Log.i(TAG, "Scanner start");
            cc.getComputationThread().post(cc.getScannerRunnables());

            //
            // Bluetooth-Keep-Alive can be enabled by the UI. Enable bluetooth now and
            // keep it on with the BluetoothReenableRunnable.
            //
            if (ProximityTracingService.getInstance().getBluetoothKeepAlive()) {
                // Make sure that BT is turned on. When the user
                // disables BT later on our bluetooth state receiver
                // will catch it and can inform the user that we need
                // BT for the app.
                Log.i(TAG, "BluetoothHelper.enableBluetooth");
                BluetoothHelper.enableBluetooth();
            }

            if (!PowerManagementHelper.isIgnoringBatteryOptimizations()) {
                Log.w(TAG, "battery optimization is turned on");
                cc.getMetrics().isBatterOptimizationTurnedOn = 1;
                CoreCallback.callUI(CoreEvent.APP_HAS_BATTERY_OPTIMIZATION_ENABLED, new EventArgs());
            } else {
                cc.getMetrics().isBatterOptimizationTurnedOn = 0;
            }

            //
            // Start the telemetry if in debug and when the ui has enabled it.
            //
            if (BuildConfig.DEBUG && ProximityTracingService.getInstance().getEnableTelemetry()) {
                //
                // Start the telemetry runnable that collects telemetry data and sends it to the
                // telemetry server.
                //
                Log.i(TAG, "initializing TelemetryRunnable...");
                cc.getComputationThread().post(cc.getTelemetryRunnable());
            }
            CoreCallback.callUI(CoreEvent.CORE_IS_READY, new EventArgs());

        } else {
            Log.e(TAG, "no BLE feature");
            cc.getMetrics().isBluetoothLeSupported = 0;
            CoreCallback.callUI(CoreEvent.MISSING_FEATURE_BLE, new EventArgs());
            //
            // Send the absence of the bluetooth le feature now because we will
            // shutdown right after that.
            //
            Telemetry.sendKPI(new KPI("bles", 0));
        }

        //
        // ! No further code allowed here !
        //
    }

    /**
     * Starts the runnables that will run periodically.
     */
    private void startScheduledRunnables() {
        final CoreContext cc = CoreContext.getInstance();

        //
        // Will delete user data on a periodic basis.
        //
        Log.i(TAG, "initializing DeleteExpiredDataRunnable...");
        cc.getComputationThread().post(cc.getDeleteExpiredDataRunnable());

        //
        // Receives the ebids on a daily basis.
        //
        Log.i(TAG, "initializing KeystoreRunnable...");
        cc.getComputationThread().postDelayed(cc.getKeystoreRunnable(), 10 * Units.SECONDS);

        //
        // Rotates to a new ebids every 30 Minutes.
        //
        Log.i(TAG, "initializing KeyRotationRunnable...");
        cc.getComputationThread().postDelayed(cc.getKeyRotationRunnable(), 20 * Units.SECONDS);

        //
        // Aggregates the received advertisements.
        //
        Log.i(TAG, "initializing AggregationRunnable...");
        cc.getComputationThread().postDelayed(cc.getAggregationRunnable(), AggregationRunnable.getAggregatorRate());

        //
        // Start the token refresh service.
        //
        Log.i(TAG, "initializing TokenServiceRunnable...");
        cc.getApiRequestThread().post(cc.getTokenServiceRunnable());


        //
        // Start check messages runnable.
        //
        Log.i(TAG, "initializing CheckMessagesRunnable...");
        //noinspection PointlessArithmeticExpression
        cc.getApiRequestThread().postDelayed(new CheckMessagesRunnable(false, false), 1 * Units.HOURS);
    }

    private void collectBluetoothLeChipFeatures() {
        final CoreContext cc = CoreContext.getInstance();

        //
        // offload scan batching support
        //
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isOffloadedScanBatchingSupported()) {
            cc.getMetrics().isOffloadedScanBatchingSupported = 1;
            Log.i(TAG, "isOffloadedScanBatchingSupported yes");
        } else {
            cc.getMetrics().isOffloadedScanBatchingSupported = 0;
            Log.i(TAG, "isOffloadedScanBatchingSupported no");
        }

        //
        // offload filtering support
        //
        if (adapter.isOffloadedFilteringSupported()) {
            cc.getMetrics().isOffloadedFilteringSupported = 1;
            Log.i(TAG, "isOffloadedFilteringSupported yes");
        } else {
            cc.getMetrics().isOffloadedFilteringSupported = 0;
            Log.i(TAG, "isOffloadedFilteringSupported no");
        }

        //
        // multiple advertisement support
        //
        if (adapter.isMultipleAdvertisementSupported()) {
            cc.getMetrics().isMultipleAdvertisementSupported = 1;
            Log.i(TAG, "isMultipleAdvertisementSupported yes");
        } else {
            cc.getMetrics().isMultipleAdvertisementSupported = 0;
            Log.i(TAG, "isMultipleAdvertisementSupported no");
        }
    }

    public void setStalled() {
        if (!stalled) {
            stalled = true;
            CoreCallback.callUI(CoreEvent.CORE_IS_STALLED, new EventArgs());
        }
    }

    public void resumeFromStall() {
        if (stalled) {
            stalled = false;
            CoreCallback.callUI(CoreEvent.CORE_RESUMED_FROM_STALL, new EventArgs());
        }
    }

    boolean isStalled() {
        return stalled;
    }

    void decommission() {
        Log.i(TAG, "decommission");
        decommissioned = true;
    }

    public boolean hasBeenDecommissioned() {
        return decommissioned;
    }
}
