package org.pepppt.core.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.RestrictTo;

/**
 * This class will be called through different intents from the
 * os.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RestartReceiver extends BroadcastReceiver {
    private static final String TAG = RestartReceiver.class.getSimpleName();

    // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive" + action);
        if ("android.intent.action.BOOT_COMPLETED".equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
                "android.intent.action.MY_PACKAGE_REPLACED".equals(action))
            RestartWorker.scheduleOneTimeRestartNow(context);
    }
}
