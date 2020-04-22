package org.pepppt.core.ble;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.ble.aggregation.Measurement;
import org.pepppt.core.ble.profiles.AdvertisementProfile;
import org.pepppt.core.CoreContext;

import java.util.List;
import java.util.UUID;

/**
 * This callback processes the received ads from other devices.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ScannerCallback extends android.bluetooth.le.ScanCallback {
    private static final String TAG = ScannerCallback.class.getSimpleName();

    static String byteArrayToHex(byte[] a) {
        final StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static UUID createUUIDFromByteString(final String data) {
        String builder = data.substring(0, 8) +
                "-" + data.substring(8, 12) +
                "-" + data.substring(12, 16) +
                "-" + data.substring(16, 20) +
                "-" + data.substring(20, 32);
        return UUID.fromString(builder);
    }

    private void addNewResult(final ScanResult result) {

        final CoreContext cc = CoreContext.getInstance();

        if (cc.getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> exiting");
            return;
        }

        cc.getMetrics().LastAdvertisementTime = System.currentTimeMillis();
        cc.getMetrics().NumberOfAdvertisementsReceived++;
        cc.getBLEScanner().increaseScanCounter();

        //
        // Results will only be processed if the app has a valid EBID
        //
        if (!cc.getKeystoreRunnable().getStorage().emptyEBID()) {

            final ScanRecord record = result.getScanRecord();
            if (record != null) {
                int rssi = result.getRssi();
                int txPowerLevel = record.getTxPowerLevel();

                //
                // Add the encounter to the database if the advertisement has our
                // CGAIdentifiers.ServiceDataUUIDString
                //
                final byte[] senderIdBytes = record.getServiceData(ParcelUuid.fromString(AdvertisementProfile.ServiceDataUUIDString));

                //
                // Only data with exactly 16 bytes will be processed.
                //
                if (senderIdBytes != null) {

                    if (senderIdBytes.length == 16) {

                        final String senderId = byteArrayToHex(senderIdBytes);

                        //
                        // Statistics
                        //
                        cc.getMetrics().NumberOfEncounters++;
                        cc.getMetrics().addAdReceived();
                        cc.getMetrics().saveUniqueEncounter(senderId);
                        cc.getMetrics().LastEncounterString = senderId + "(" + rssi + ")";
                        cc.getMetrics().LastEncounterTime = System.currentTimeMillis();

                        //
                        // Sends the scan result ot the aggregator if enabled.
                        //
                        final UUID receivedUuid = createUUIDFromByteString(senderId);

                        //TODO: add txCorrectionLevel
                        cc.getAggregator().
                            submit(new Measurement<>(receivedUuid, System.nanoTime(), rssi, txPowerLevel, 0));

                    } else {
                        Log.i(TAG, "payload is not 16 bytes -> ignoring");
                    }
                }
            }
        }
    }

    public void onScanResult(int callbackType, ScanResult result) {
        addNewResult(result);
    }

    /**
     * Callback when batch results are delivered.
     *
     * @param results List of scan results that are previously scanned.
     */
    public void onBatchScanResults(final List<ScanResult> results) {
        for (ScanResult result: results) {
            addNewResult(result);
        }
    }

    /**
     * Callback when scan could not be started.
     *
     * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
     */
    public void onScanFailed(int errorCode) {
        Log.e(TAG, "Scan failed");
    }

}
