package org.pepppt.core.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreCallback;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;

/**
 * Class that signals the state change of the bluetooth adapter.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BluetoothStateChange {
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                String TAG = "BluetoothStateChange";
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "STATE_OFF");
                        CoreCallback.callUI(CoreEvent.BLUETOOTH_HAS_BEEN_TURNED_OFF, new EventArgs());
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "STATE_TURNING_OFF");
                        // Tell the UI that we reenabled bt.
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "STATE_ON");
                        CoreCallback.callUI(CoreEvent.BLUETOOTH_HAS_BEEN_TURNED_ON, new EventArgs());
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "STATE_TURNING_ON");
                        break;
                }
            }
        }
    };

    public void init(Context context) {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, filter);
    }
}
