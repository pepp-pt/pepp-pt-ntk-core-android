package org.pepppt.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import net.sqlcipher.BuildConfig;

import org.pepppt.core.ble.Advertiser;
import org.pepppt.core.ble.AdvertiserRunnables;
import org.pepppt.core.ble.BluetoothStateChange;
import org.pepppt.core.ble.ScannerRunnables;
import org.pepppt.core.ble.aggregation.Aggregator;
import org.pepppt.core.ble.gatt.GattManager;
import org.pepppt.core.database.DataSource;
import org.pepppt.core.network.ApiRequestHandler;
import org.pepppt.core.scheduling.AggregationRunnable;
import org.pepppt.core.scheduling.BluetoothReenableRunnable;
import org.pepppt.core.scheduling.DeleteExpiredDataRunnable;
import org.pepppt.core.scheduling.KeyRotationRunnable;
import org.pepppt.core.scheduling.KeystoreRunnable;
import org.pepppt.core.scheduling.TelemetryRunnable;
import org.pepppt.core.scheduling.TokenServiceRunnable;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.threads.LooperThread;

import java.util.UUID;

/**
 * This class is a container for all singletons in the core.
 */
@SuppressWarnings("WeakerAccess")
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CoreContext {
    private static final String TAG = CoreContext.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static CoreContext contextInstance = null;
    private Context context;
    private CoreSystems coreSystems;
    private Advertiser advertiser;
    private org.pepppt.core.ble.Scanner bleScanner;
    private BluetoothStateChange bluetoothStatechange;
    private DataSource dataSource;
    private Metrics metrics;
    private LooperThread computationThread;
    private LooperThread apiRequestThread;
    private DeleteExpiredDataRunnable deleteExpiredDataRunnable;
    private BluetoothReenableRunnable bluetoothReenableRunnable;
    private Aggregator<UUID> aggregator;
    private ApiRequestHandler apiRequestHandler;
    private GattManager gattManager;

    //
    // Scheduled Runnables
    //
    private KeyRotationRunnable keyRotationRunnable;
    private KeystoreRunnable keystoreRunnable;
    private ScannerRunnables scannerRunnables;
    private AdvertiserRunnables advertiserRunnables;
    private AggregationRunnable aggregationRunnable;
    private TokenServiceRunnable tokenServiceRunnable;
    private TelemetryRunnable telemetryRunnable;

    private CoreContext(@NonNull Context appContext) {
        context = appContext;
        if (BuildConfig.DEBUG) {
            Log.w("ProximityTracingService", "You're using a debug build! DO NOT use it in a live environment!");
        }
    }

    private void init() {
        coreSystems = new CoreSystems();
        dataSource = new DataSource(context);
        Log.i(TAG, "create apiRequestHandler");
        apiRequestHandler = new ApiRequestHandler();
        Log.i(TAG, "create apiRequestHandler done");
        Telemetry.init();
        computationThread = new LooperThread();
        apiRequestThread = new LooperThread();
        advertiser = new Advertiser();
        bleScanner = new org.pepppt.core.ble.Scanner();
        scannerRunnables = new ScannerRunnables();
        advertiserRunnables = new AdvertiserRunnables();
        bluetoothStatechange = new BluetoothStateChange();
        metrics = new Metrics();
        gattManager = new GattManager();

        //
        // Runnables
        //
        deleteExpiredDataRunnable = new DeleteExpiredDataRunnable();
        keyRotationRunnable = new KeyRotationRunnable();
        keystoreRunnable = new KeystoreRunnable();
        keystoreRunnable.loadFromDatabase();
        bluetoothReenableRunnable = new BluetoothReenableRunnable();
        aggregationRunnable = new AggregationRunnable();
        tokenServiceRunnable = new TokenServiceRunnable();
        telemetryRunnable = new TelemetryRunnable();
        aggregator = new Aggregator<>();
    }

    public static void create(@NonNull Context context) {
        if (contextInstance == null)
            contextInstance = new CoreContext(context);
        contextInstance.init();
    }

    public static CoreContext getInstance() {
        return contextInstance;
    }

    public static Context getAppContext() {
        return contextInstance.context;
    }

    public CoreSystems getCoreSystems() {
        return coreSystems;
    }

    BluetoothStateChange getBluetoothStateChange() {
        return bluetoothStatechange;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public org.pepppt.core.ble.Scanner getBLEScanner() {
        return bleScanner;
    }

    public ScannerRunnables getScannerRunnables() {
        return scannerRunnables;
    }

    public AdvertiserRunnables getAdvertiserRunnables() {
        return advertiserRunnables;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public LooperThread getComputationThread() {
        return computationThread;
    }

    public LooperThread getApiRequestThread() {
        return apiRequestThread;
    }

    public DeleteExpiredDataRunnable getDeleteExpiredDataRunnable() {
        return deleteExpiredDataRunnable;
    }

    public KeyRotationRunnable getKeyRotationRunnable() {
        return keyRotationRunnable;
    }

    public KeystoreRunnable getKeystoreRunnable() {
        return keystoreRunnable;
    }

    public BluetoothReenableRunnable getBluetoothReenableRunnable() {
        return bluetoothReenableRunnable;
    }

    public Aggregator<UUID> getAggregator() {
        return aggregator;
    }

    public AggregationRunnable getAggregationRunnable() {
        return aggregationRunnable;
    }

    public TokenServiceRunnable getTokenServiceRunnable() {
        return tokenServiceRunnable;
    }

    public TelemetryRunnable getTelemetryRunnable() {
        return telemetryRunnable;
    }

    public ApiRequestHandler getApiRequestHandler() {
        return apiRequestHandler;
    }

    @SuppressWarnings("unused")
    public GattManager getGattManager() {
        return gattManager;
    }
}
