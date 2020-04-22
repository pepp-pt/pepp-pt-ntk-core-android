package org.pepppt.core.messages;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.CoreCallback;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.events.MessagesEventArgs;
import org.pepppt.core.messages.models.Message;
import org.pepppt.core.messages.models.MessageHeader;
import org.pepppt.core.network.ApiRequest;
import org.pepppt.core.telemetry.Telemetry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LoadAllMessagesRunnable implements Runnable {
    private static final String TAG = LoadAllMessagesRunnable.class.getSimpleName();

    private String locale;

    public LoadAllMessagesRunnable(String locale) {
        this.locale = locale;
    }

    @Override
    public void run() {
        try {
            List<Message> messages = new ArrayList<>();

            JSONObject raw = ApiRequest.checkMessages();
            JSONArray items = raw.optJSONArray("items");
            if (items != null) {
                for (int index = 0; index < items.length(); index++) {
                    try {
                        JSONObject item = items.getJSONObject(index);
                        MessageHeader messageHeader = new MessageHeader(item);
                        JSONObject json_message = ApiRequest.getMessage(messageHeader.getName(), locale);
                        if (json_message != null) {
                            messages.add(new Message(messageHeader, json_message));
                        }
                    } catch (JSONException e) {
                        Telemetry.processException(e);
                        Log.e(TAG, "JsonConvert failed for Message", e);
                    }
                }
            }
            if (messages.size() > 0) {
                CoreCallback.callUI(CoreEvent.LOAD_ALL_MESSAGES_SUCCESSFULLY, new MessagesEventArgs(messages));
                return;
            }
            CoreCallback.callUI(CoreEvent.LOAD_ALL_MESSAGES_FAILED, new EventArgs());
        } catch (IOException | JSONException e) {
            Log.e(TAG, "LoadMessages failed", e);
            Telemetry.processException(e);
            CoreCallback.callUI(CoreEvent.LOAD_ALL_MESSAGES_FAILED, new EventArgs());
        }
    }
}
