package org.pepppt.core.messages;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.CoreCallback;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.network.ApiRequest;
import org.pepppt.core.telemetry.Telemetry;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ConfirmMessageRunnable implements Runnable {
    private static final String TAG = ConfirmMessageRunnable.class.getSimpleName();

    private String id;

    public ConfirmMessageRunnable(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        try {
            ApiRequest.confirmMessage(id);
            CoreCallback.callUI(CoreEvent.MESSAGE_CONFIRMED, new EventArgs());
        } catch (IOException e) {
            Log.e(TAG, "confirmMessage failed", e);
            Telemetry.processException(e);
            CoreCallback.callUI(CoreEvent.CONFIRM_MESSAGES_FAILED, new EventArgs());
        }
    }
}
