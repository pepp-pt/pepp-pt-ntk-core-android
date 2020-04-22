package org.pepppt.core.messages;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.CoreCallback;
import org.pepppt.core.CoreContext;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.network.ApiRequest;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.io.IOException;

/**
 * Runs in an interval to check for messages
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CheckMessagesRunnable implements Runnable {
    private static final String TAG = CheckMessagesRunnable.class.getSimpleName();

    private boolean runOnlyOnce;
    private boolean withFeedbackWhenNoMessages;

    public CheckMessagesRunnable(boolean runOnlyOnce, boolean withFeedbackWhenNoMessages) {
        this.runOnlyOnce = runOnlyOnce;
        this.withFeedbackWhenNoMessages = withFeedbackWhenNoMessages;
    }

    @Override
    public void run() {
        try {
            JSONObject raw = ApiRequest.checkMessages();
            JSONArray items = raw.optJSONArray("items");
            if (items != null && items.length() > 0) {
                CoreCallback.callUI(CoreEvent.NEW_MESSAGES_ARRIVED, new EventArgs());
            } else {
                if (withFeedbackWhenNoMessages) {
                    CoreCallback.callUI(CoreEvent.NO_NEW_MESSAGES, new EventArgs());
                }
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "CheckMessages failed", e);
            Telemetry.processException(e);
            CoreCallback.callUI(CoreEvent.CHECK_MESSAGES_FAILED, new EventArgs());
        } finally {
            if (!runOnlyOnce) {
                CoreContext.getInstance().getApiRequestThread().postDelayed(new CheckMessagesRunnable(false, false), 12 * Units.HOURS);
            }
        }
    }
}
