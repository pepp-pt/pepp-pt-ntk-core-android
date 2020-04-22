package org.pepppt.core.messages.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.telemetry.Telemetry;

/**
 * Model class of the MessageHeader
 */
public class MessageHeader {
    private static final String LOG_TAG = MessageHeader.class.getSimpleName();
    private String created_on;
    private String id;
    private String name;

    // TODO: remove on release
    private MessageHeader(String created_on, String id, String name) {
        this.created_on = created_on;
        this.id = id;
        this.name = name;
    }

    public MessageHeader(JSONObject jsonObject) throws JSONException {
        try {
            this.name = jsonObject.getString("ogit/name");
            this.id = jsonObject.getString("ogit/_id");
            this.created_on = jsonObject.getString("ogit/Mobile/enctime");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "MessageHeader Creation failed", e);
            Telemetry.processException(e);
            throw e;
        }
    }

    public String getCreated_on() {
        return created_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
