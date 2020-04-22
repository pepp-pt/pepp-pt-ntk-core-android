package org.pepppt.core.ble.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import androidx.annotation.RestrictTo;

import java.util.List;

/**
 * The callback for the gatt service
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class GattCallback extends BluetoothGattCallback {
    private static final String TAG = GattCallback.class.getSimpleName();

    /**
     * Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i(TAG, "onConnectionStateChange: status=" + status + " newState=" + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(TAG, "Connected to GATT server.");

            boolean discoverServicesStarted = gatt.discoverServices();
            if (discoverServicesStarted) {
                Log.i(TAG, "discovery of services started.");
            }

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(TAG, "Disconnected from GATT server.");
        }
    }

    /**
     * Callback reporting the result of a characteristic read operation.
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "characteristics available");
            //
            // TODO send data
            //
        }
    }

    /**
     * Callback indicating the result of a characteristic write operation.
     * <p>
     * If this callback is invoked while a reliable write transaction is in progress, the
     * value of the characteristic represents the value reported by the remote device. An
     * application should compare this value to the desired value to be written. If the
     * values don't match, the application must abort the reliable write transaction.
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    /**
     * Callback invoked when the list of remote services, characteristics and descriptors
     * for the remote device have been updated, ie new services have been discovered.
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "service discovered");

            List<BluetoothGattService> serviceList = gatt.getServices();
            for (BluetoothGattService service : serviceList) {
                Log.i(TAG, "service = " + service.getUuid().toString());
                for (BluetoothGattService includedService : service.getIncludedServices()) {
                    Log.i(TAG, "  included service = " + includedService.getUuid().toString());
                }
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    Log.i(TAG, "  characteristic = " + characteristic.getUuid().toString());
                }
            }
        }
    }
}
