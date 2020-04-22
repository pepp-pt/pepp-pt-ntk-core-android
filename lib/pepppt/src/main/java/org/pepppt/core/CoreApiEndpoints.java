package org.pepppt.core;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.exceptions.InvalidBackendEndpointException;
import org.pepppt.core.exceptions.InvalidBackendEndpointFormatException;
import org.pepppt.core.exceptions.MissingBackendEndpointException;

/**
 * This class holds a list of all endpoints. The endpoints can be checked.
 */
public class CoreApiEndpoints {
    private static final String apiBaseUrlString = "backend_API_Base_Url";
    private static final String apiBroadcastKeysUrlString = "backend_API_broadcastkeys";
    private static final String apiGetMeUrlString = "backend_API_GetMe_Url";
    private static final String apiRegistrationUrlString = "backend_API_registration_Url";
    private static final String apiAuthUrlString = "backend_API_auth_Url";
    private static final String apiGetTanUrlString = "backend_API_getTan_Url";
    private static final String apiFinishUrlString = "backend_API_finish_Url";
    private static final String apiTestLabUrlString = "backend_API_testLab_Url";
    private static final String apiSendDataUrlString = "backend_API_sendData_Url";
    private static final String apiCheckMessagesUrlString = "backend_API_checkMessages_Url";
    private static final String apiGetMessageUrlString = "backend_API_getMessage_Url";
    private static final String apiConfirmMessageUrlString = "backend_API_confirmMessage_Url";

    private static final String endpoints = "endpoints";
    private String apiBaseUrl;
    private String apiBroadcastKeysUrl;
    private String apiGetMeUrl;
    private String apiRegistrationUrl;
    private String apiAuthUrl;
    private String apiGetTanUrl;
    private String apiFinishUrl;
    private String apiTestLabUrl;
    private String apiSendDataUrl;
    private String apiCheckMessagesUrl;
    private String apiGetMessageUrl;
    private String apiConfirmMessageUrl;

    public CoreApiEndpoints() {
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getApiBroadcastKeysUrl() {
        return apiBroadcastKeysUrl;
    }

    public String getApiGetMeUrl() {
        return apiGetMeUrl;
    }

    public String getApiRegistrationUrl() {
        return apiRegistrationUrl;
    }

    public String getApiAuthUrl() {
        return apiAuthUrl;
    }

    public String getApiGetTanUrl() {
        return apiGetTanUrl;
    }

    public String getApiFinishUrl() {
        return apiFinishUrl;
    }

    public String getApiTestLabUrl() {
        return apiTestLabUrl;
    }

    public String getApiSendDataUrl() {
        return apiSendDataUrl;
    }

    public String getApiCheckMessagesUrl() {
        return apiCheckMessagesUrl;
    }

    public String getApiGetMessageUrl() {
        return apiGetMessageUrl;
    }

    public String getApiConfirmMessageUrl() {
        return apiConfirmMessageUrl;
    }

    public boolean check() throws MissingBackendEndpointException {
        if (apiBaseUrl == null)
            throw new MissingBackendEndpointException(apiBaseUrlString);
        if (apiBroadcastKeysUrl == null)
            throw new MissingBackendEndpointException(apiBroadcastKeysUrlString);
        if (apiGetMeUrl == null)
            throw new MissingBackendEndpointException(apiGetMeUrlString);
        if (apiRegistrationUrl == null)
            throw new MissingBackendEndpointException(apiRegistrationUrlString);
        if (apiAuthUrl == null)
            throw new MissingBackendEndpointException(apiAuthUrlString);
        if (apiGetTanUrl == null)
            throw new MissingBackendEndpointException(apiGetTanUrlString);
        if (apiFinishUrl == null)
            throw new MissingBackendEndpointException(apiFinishUrlString);
        if (apiTestLabUrl == null)
            throw new MissingBackendEndpointException(apiTestLabUrlString);
        if (apiSendDataUrl == null)
            throw new MissingBackendEndpointException(apiSendDataUrlString);
        if (apiCheckMessagesUrl == null)
            throw new MissingBackendEndpointException(apiCheckMessagesUrlString);
        if (apiGetMessageUrl == null)
            throw new MissingBackendEndpointException(apiGetMessageUrlString);
        if (apiConfirmMessageUrl == null)
            throw new MissingBackendEndpointException(apiConfirmMessageUrlString);
        return true;
    }

    public void set(@NonNull String json) throws JSONException, InvalidBackendEndpointException,
            MissingBackendEndpointException, InvalidBackendEndpointFormatException {
        JSONObject obj = new JSONObject(json);
        JSONArray items = obj.getJSONArray(endpoints);
        int length = items.length();
        if (length != 12)
            throw new InvalidBackendEndpointFormatException();
        for (int i = 0; i < length; i++) {
            JSONObject endpoint = items.getJSONObject(i);
            String key = endpoint.keys().next();
            String value = endpoint.getString(key);
            switch (key) {
                case apiBaseUrlString:
                    apiBaseUrl = value;
                    break;
                case apiBroadcastKeysUrlString:
                    apiBroadcastKeysUrl = value;
                    break;
                case apiGetMeUrlString:
                    apiGetMeUrl = value;
                    break;
                case apiRegistrationUrlString:
                    apiRegistrationUrl = value;
                    break;
                case apiAuthUrlString:
                    apiAuthUrl = value;
                    break;
                case apiGetTanUrlString:
                    apiGetTanUrl = value;
                    break;
                case apiFinishUrlString:
                    apiFinishUrl = value;
                    break;
                case apiTestLabUrlString:
                    apiTestLabUrl = value;
                    break;
                case apiSendDataUrlString:
                    apiSendDataUrl = value;
                    break;
                case apiCheckMessagesUrlString:
                    apiCheckMessagesUrl = value;
                    break;
                case apiGetMessageUrlString:
                    apiGetMessageUrl = value;
                    break;
                case apiConfirmMessageUrlString:
                    apiConfirmMessageUrl = value;
                    break;
                default:
                    throw new InvalidBackendEndpointException(value);
            }
        }
        check();
    }
}
