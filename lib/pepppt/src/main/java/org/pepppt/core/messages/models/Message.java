package org.pepppt.core.messages.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.telemetry.Telemetry;

/**
 * Model class of the Message
 */
public class Message {
    private static final String LOG_TAG = Message.class.getSimpleName();

    private String mobileCta;
    private String mobileCtaTarget;
    private String mobileCtaType;
    private String mobileEnctime;
    private String mobileHeadline;
    private String mobileLocale;
    private String mobileMessage;
    private String mobileSubline;
    private String id;
    private String name;

    public Message(MessageHeader header, JSONObject jsonObject) throws JSONException {
        try {
            this.mobileEnctime = header.getCreated_on();
            this.mobileCta = jsonObject.getString("ogit/Mobile/cta");
            this.mobileCtaTarget = jsonObject.getString("ogit/Mobile/ctaTarget");
            this.mobileCtaType = jsonObject.getString("ogit/Mobile/ctaType");
            this.mobileHeadline = jsonObject.getString("ogit/Mobile/headline");
            this.mobileLocale = jsonObject.getString("ogit/Mobile/locale");
            this.mobileMessage = jsonObject.getString("ogit/Mobile/message");
            this.mobileSubline = jsonObject.getString("ogit/Mobile/subline");
            this.id = jsonObject.getString("ogit/_id");
            this.name = jsonObject.getString("ogit/name");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Message Creation failed", e);
            Telemetry.processException(e);
            throw e;
        }
    }

    public String getMobileCta() {
        return mobileCta;
    }

    public void setMobileCta(String mobile_cta) {
        this.mobileCta = mobile_cta;
    }

    public String getMobileCtaTarget() {
        return mobileCtaTarget;
    }

    public void setMobileCtaTarget(String mobileCtaTarget) {
        this.mobileCtaTarget = mobileCtaTarget;
    }

    public String getMobileCtaType() {
        return mobileCtaType;
    }

    public void setMobileCtaType(String mobile_ctaTypeM) {
        this.mobileCtaType = mobile_ctaTypeM;
    }

    public String getMobileEnctime() {
        return mobileEnctime;
    }

    public void setMobileEnctime(String mobileEnctime) {
        this.mobileEnctime = mobileEnctime;
    }

    public String getMobileHeadline() {
        return mobileHeadline;
    }

    public void setMobileHeadline(String mobileHeadline) {
        this.mobileHeadline = mobileHeadline;
    }

    public String getMobileLocale() {
        return mobileLocale;
    }

    public void setMobileLocale(String mobileLocale) {
        this.mobileLocale = mobileLocale;
    }

    public String getMobileMessage() {
        return mobileMessage;
    }

    public void setMobileMessage(String mobileMessage) {
        this.mobileMessage = mobileMessage;
    }

    public String getMobileSubline() {
        return mobileSubline;
    }

    public void setMobileSubline(String mobileSubline) {
        this.mobileSubline = mobileSubline;
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
