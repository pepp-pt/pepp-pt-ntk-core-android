package org.pepppt.core.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.ble.profiles.AdvertisementProfile;
import org.pepppt.core.CoreContext;
import org.pepppt.core.telemetry.Telemetry;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Class that can start and stop a bluetooth le scan.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Scanner {
    private static final String TAG = Scanner.class.getSimpleName();
    private ScannerCallback callback;
    private int scanCounter;

    public Scanner()
    {
        Log.i(TAG, "created");
        callback = new ScannerCallback();
    }

    int getScanCounter() {
        return scanCounter;
    }

    void increaseScanCounter() {
        scanCounter++;
    }

    public void start() {

        final CoreContext cc = CoreContext.getInstance();

        try {

            final ScannerRunnables sr = cc.getScannerRunnables();
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.isEnabled()) {
                final BluetoothLeScanner blescanner = adapter.getBluetoothLeScanner();
                final ScanSettings.Builder builder = new ScanSettings.Builder();
                builder.setScanMode(sr.getCurrentScannerProfile().getScannerMode());
                builder.setReportDelay(sr.getCurrentScannerProfile().getReportDelay());
                ByteBuffer manData = ByteBuffer.allocate(1);
                ByteBuffer manMask = ByteBuffer.allocate(1);
                manData.put(0, (byte) AdvertisementProfile.ManufacturerData);
                manMask.put(0, (byte) 0x01);

                //
                // Setting up the scan filtering.
                //
                final ScanFilter.Builder scanfilterbuilder = new ScanFilter.Builder();
                scanfilterbuilder.setManufacturerData(AdvertisementProfile.ManufacturerID, manData.array(), manMask.array());
                final Vector<ScanFilter> filterlist = new Vector<>();
                filterlist.add(scanfilterbuilder.build());

                //
                // statistics
                //
                cc.getMetrics().NumberOfScans++;
                scanCounter = 0;
                cc.getMetrics().FilteringEnabled = filterlist.size() > 0;

                blescanner.startScan(filterlist, builder.build(), callback);
            }
        } catch (final Exception ex) {
            // Can happen when the user turns off the BT.
            Telemetry.processException(ex);
            Log.w(TAG, "exception while start scanning: " + ex.getMessage());
        }
    }

    void stop() {
        CoreContext cc = CoreContext.getInstance();

        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.isEnabled()) {
                BluetoothLeScanner blescanner = adapter.getBluetoothLeScanner();
                blescanner.stopScan(callback);
                blescanner.flushPendingScanResults(callback);
            }
        } catch (Exception ex) {
            // Can happen when the user turns off the BT.
            Telemetry.processException(ex);
            Log.w(TAG, "exception while stop scanning: " + ex.getMessage());
        }
    }
}
