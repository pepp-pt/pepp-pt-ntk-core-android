package org.pepppt.core.messages;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.json.JSONException;
import org.pepppt.core.CoreCallback;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.network.ApiRequest;
import org.pepppt.core.telemetry.Telemetry;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RequestTestResultRunnable implements Runnable {
    private static final String TAG = RequestTestResultRunnable.class.getSimpleName();

    private String labId;
    private String orderId;

    public RequestTestResultRunnable(String labId, String orderId) {

        this.labId = labId;
        this.orderId = orderId;
    }

    @Override
    public void run() {
        try {
            ApiRequest.acquiringTestResult(labId, orderId);
            CoreCallback.callUI(CoreEvent.LABTEST_RESULT_AVAILABLE, new EventArgs());
        } catch (IOException | JSONException e) {
            Log.e(TAG, "acquiringTestResult failed", e);
            Telemetry.processException(e);
            CoreCallback.callUI(CoreEvent.LABTEST_REQUEST_FAILED, new EventArgs());
        }
    }
}
