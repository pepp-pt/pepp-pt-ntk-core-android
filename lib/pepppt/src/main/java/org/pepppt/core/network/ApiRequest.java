package org.pepppt.core.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.ProximityTracingService;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.CoreContext;
import org.pepppt.core.telemetry.Telemetry;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class is for performing pre-defined
 * API Requests to the corresponding backend
 * associated with the app. Each reqeuest will
 * use ApiRequestHandler to execute the request.
 * Make sure to run the requests on non-ui blocking threads.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ApiRequest {

    private static final String LOG_TAG = ApiRequestHandler.class.getSimpleName();

    /**
     * Check the TAN is activated
     * A flag in account is used to store the fact, that the TAN was activated successful: “/uploadAllowed“.
     *
     * @return will return something like:
     * {
     * "ogit/Auth/Account/type": "device",
     * "/CGAID": "64df81b3-c420-46f9-bf23-b185bd851664",
     * "/dataTimeseriesId": "ck7zvz9za02gm0v94pat8fcdf_ck88t7jvp00am0t78nch71vhv",
     * "/uploadAllowed": "true",
     * // …
     * }
     * @throws JSONException on faulty/not parseable JSON object to send
     * @throws IOException   on faulty http connection
     */
    public static JSONObject getMeAccount() throws IOException, JSONException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());
            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();
            URL getMeAccountUrl = new URL(baseUrl + ProximityTracingService.getInstance().getEndpoints().getApiGetMeUrl());
            return apiRh.jsonGET(getMeAccountUrl.toString(), requestProperties);


        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error getMeAccountOutput: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Upload the data
     *
     * @param json            the data
     * @param rawTimeseriesId get via getMeAccount()
     * @return returns an empty map by success:  {}
     * @throws IOException on faulty http connection
     */
    public static String sendEncounterData(JSONObject json, String rawTimeseriesId) throws IOException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());
            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();

            URL sendDataUrl = new URL(baseUrl + String.format(ProximityTracingService.getInstance().getEndpoints().getApiSendDataUrl(), rawTimeseriesId));
            return apiRh.stringPOST(sendDataUrl.toString(), json, requestProperties);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error sendEncounterData: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Notify the upload is done:
     *
     * @return returns an empty map by success:  {}
     * @throws IOException on faulty http connection
     */
    public static String finishUploadEncounterData() throws IOException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());
            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();

            URL url = new URL(baseUrl + ProximityTracingService.getInstance().getEndpoints().getApiFinishUrl());
            return apiRh.stringGET(url.toString(), requestProperties);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error finishUplaodEncounterData: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Get a TAN (before upload data):
     *
     * @return {
     * "success": {
     * "expiration": "1585647774",
     * "tan": "8PPVGNAW"
     * }
     * }
     * @throws JSONException on faulty/not parseable JSON object to send
     * @throws IOException   on faulty http connection
     */
    public static JSONObject getTan() throws IOException, JSONException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());
            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();

            URL url = new URL(baseUrl + ProximityTracingService.getInstance().getEndpoints().getApiGetTanUrl());
            return apiRh.jsonGET(url.toString(), requestProperties);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error getTan: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Signing up the device/user at
     * the backend (see also backend_base_url)
     *
     * @param firebaseToken the valid user firebase token
     * @throws JSONException on faulty/not parseable JSON object to send
     * @throws IOException   on faulty http connection
     */

    public static JSONObject deviceSignup(@NonNull String firebaseToken) throws IOException, JSONException {


        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("client_id", ProximityTracingService.getInstance().getClientId());
            jsonObject.put("client_secret", ProximityTracingService.getInstance().getClientSecret());
            jsonObject.put("firebaseToken", firebaseToken);

            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();
            URL registerUrl = new URL(baseUrl + ProximityTracingService.getInstance().getEndpoints().getApiRegistrationUrl());
            return apiRh.jsonPOST(registerUrl.toString(), jsonObject, null);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error register: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Getting a new token issued by the backend
     * (see also base_url)
     *
     * @return JSONObject the CGA Backend response as JSON Object
     * @throws JSONException on faulty/not parseable JSON object to send
     * @throws IOException   on faulty http connection
     */
    public static JSONObject auth() throws IOException, JSONException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("client_id", ProximityTracingService.getInstance().getClientId());
            jsonObject.put("client_secret", ProximityTracingService.getInstance().getClientSecret());
            jsonObject.put("device_secret", Objects.requireNonNull(SettingsDatabaseHelper.getByKey(DataSourceKeys.DEVICE_SECRET_KEY)).getValue());
            jsonObject.put("device_id", Objects.requireNonNull(SettingsDatabaseHelper.getByKey(DataSourceKeys.DEVICE_ID_KEY)).getValue());
            ApiRequestHandler arh = ApiRequestHandler.getInstance();
            URL authUrl = new URL(baseUrl + ProximityTracingService.getInstance().getEndpoints().getApiAuthUrl());
            return arh.jsonPOST(authUrl.toString(), jsonObject, null);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error get new token: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Check for message availability (Are there any messages for given cgaid):
     *
     * @return returns:
     * <p>
     * {}      if no messages
     * <p>
     * otherwise:
     * <p>
     * {
     * "items": [
     * {
     * "ogit/Mobile/enctime": "2020-04-18",
     * "ogit/_created-on": 1586510916745,
     * "ogit/_id": "ck7zvz9za02gm0v94pat8fcdf_ck8tzmxzd7x1v0r70phah2elu",
     * "ogit/name": "selfisolation"
     * }
     * ]
     * }
     * @throws JSONException on faulty/not parseable JSON object to send
     * @throws IOException   on faulty http connection
     */
    public static JSONObject checkMessages() throws IOException, JSONException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());
            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();
            URL url = new URL(baseUrl + ProximityTracingService.getInstance().getEndpoints().getApiCheckMessagesUrl());
            return apiRh.jsonGET(url.toString(), requestProperties);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error checkMessages: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Get message for given locale and message name:
     *
     * @param name   message name like 'selfisolation'
     * @param locale locale like 'de'/'en'
     * @return returns:
     * <p>
     * {
     * "ogit/Mobile/cta": "Hotline 0800-23421969",
     * "ogit/Mobile/ctaTarget": "https://rki.de",
     * "ogit/Mobile/ctaType": "0",
     * "ogit/Mobile/headline": "Wichtige Mitteilung",
     * "ogit/Mobile/locale": "de",
     * "ogit/Mobile/message": "Begeben Sie sich bitte in h\u00e4usliche Quarant\u00e4ne.",
     * "ogit/Mobile/subline": "Sie hatten m\u00f6glicherweise Kontakt zu einer positiv auf COVID-19 getesteten Person. Bei Symptomen sollten Sie sich testen lassen",
     * "ogit/_id": "ck7zvz9za02gm0v94pat8fcdf_ck8sxio3r2p8j0r70idrfl5lh",
     * "ogit/name": "selfisolation"
     * }
     * @throws JSONException            on faulty/not parseable JSON object to send
     * @throws IOException              on faulty http connection
     * @throws IllegalArgumentException on faulty IllegalArgument
     */
    public static JSONObject getMessage(String name, String locale) throws IOException, JSONException, IllegalArgumentException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            if (name != null && name.length() <= 0)
                throw new IllegalArgumentException("no empty name");

            if (locale != null && locale.length() <= 0)
                throw new IllegalArgumentException("no empty locale");

            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());
            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();
            URL url = new URL(baseUrl + String.format(ProximityTracingService.getInstance().getEndpoints().getApiGetMessageUrl(), name, locale));
            return apiRh.jsonGET(url.toString(), requestProperties);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error getMessage: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * ‘confirm’ to remove the message from the list
     *
     * @param messageId the message_id
     * @throws IOException              on faulty http connection
     * @throws IllegalArgumentException on faulty Illegal Argument
     */
    public static void confirmMessage(String messageId) throws IOException, IllegalArgumentException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            if (messageId != null && messageId.length() <= 0)
                throw new IllegalArgumentException("no empty message_id");

            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());
            ApiRequestHandler apiRh = ApiRequestHandler.getInstance();
            URL url = new URL(baseUrl + String.format(ProximityTracingService.getInstance().getEndpoints().getApiConfirmMessageUrl(), messageId));
            apiRh.stringPOST(url.toString(), null, requestProperties);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error confirmMessage: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    /**
     * Acquiring Test Results via Lab
     *
     * @return JSONObject Returns a success if the results are available. The test results can be received from the messaging endpoint:
     * {"success": {"message": "result available"} }
     * Returns an error if the data is invalid (labId or order_is):
     * {"error": {"code": 400, "message": "bad request"} }
     * Returns an error if the results are not available yet. The request is stored, and a notification will be sent as soon as a result is provided from the Lab Server.
     * {"error": {"code": 404, "message": "not found"} }
     * @throws JSONException on faulty/not parseable JSON object to send
     * @throws IOException   on faulty http connection
     */
    public static JSONObject acquiringTestResult(String labId, String orderId) throws IOException, JSONException {
        try {
            String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lab_id", labId);
            jsonObject.put("order_id", orderId);

            Map<String, String> requestProperties = new HashMap<>();
            requestProperties.put("Authorization", "Bearer " + CoreContext.getInstance().getTokenServiceRunnable().getToken());

            ApiRequestHandler arh = ApiRequestHandler.getInstance();
            URL url = new URL(baseUrl + ProximityTracingService.getInstance().getEndpoints().getApiTestLabUrl());
            return arh.jsonPOST(url.toString(), jsonObject, requestProperties);

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error acquiringTestResult: " + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }


}
