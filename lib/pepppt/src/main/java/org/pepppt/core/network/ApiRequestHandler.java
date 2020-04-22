package org.pepppt.core.network;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.pepppt.core.ProximityTracingService;
import org.pepppt.core.telemetry.Telemetry;

/**
 * <h1>ApiRequestHandler</h1>
 * Class for conveying between the
 * app and the Api Backend,
 * implemented as singleton instance
 * See class ApiRequest for pre-defined calls.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ApiRequestHandler {

    private static final String LOG_TAG = ApiRequestHandler.class.getSimpleName();
    private OkHttpClient client;
    private static volatile ApiRequestHandler instance;

    private enum Method {
        GET, POST
    }


    // private singleton constructor
    public ApiRequestHandler() {
        instance = this;
    }

    /**
     * Initializes the api request handler. This method will be called by the
     * CoreSystems class.
     */
    public void init() {
        ProximityTracingService pts = ProximityTracingService.getInstance();
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        CertificatePinner cp = createCertificatePinner();
        if (cp == null) {
            Log.e(LOG_TAG, "could not create certificate pinner");
            return;
        }

        clientBuilder.connectTimeout(pts.getServerConnectTimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                .readTimeout(pts.getServerReadTimeout(), TimeUnit.MILLISECONDS)
                .callTimeout(pts.getServerCallTimeout(), TimeUnit.MILLISECONDS)
                .certificatePinner(cp);

        if (pts.getProtocol() != null)
            clientBuilder.protocols(Collections.singletonList(pts.getProtocol()));

        if (pts.getInterceptor() != null)
            clientBuilder.addInterceptor(pts.getInterceptor());

        client = clientBuilder.build();
    }

    /**
     * Creates the certificate pinner that is used by the init method. It uses
     * the certificate pinner information provided by the UI.
     * @return Returns the certificat pinner information.
     */
    private CertificatePinner createCertificatePinner() {
        CertificatePinner.Builder builder = new CertificatePinner.Builder();
        if (ProximityTracingService.getInstance().getCertificatePinningHost() == null || ProximityTracingService.getInstance().getCertificatePinningHost().isEmpty()) {
            Log.e(LOG_TAG, "Core.getCertificatePinningHost() is empty");
            return null;
        }

        if (ProximityTracingService.getInstance().getCertificatePinningData() == null || ProximityTracingService.getInstance().getCertificatePinningData().length == 0) {
            Log.e(LOG_TAG, "Core.getCertificatePinningData() is empty");
            return null;
        }

        for (String pin : ProximityTracingService.getInstance().getCertificatePinningData()) {
            builder.add(ProximityTracingService.getInstance().getCertificatePinningHost(), pin);
        }
        return builder.build();
    }

    // Singleton implementation
    public static ApiRequestHandler getInstance() {
        return instance;
    }

    /**
     * This method is perfoming an outoging API Request
     * to the PEPP-PT Backend (see also CGA_API_Base_Url)
     *
     * @param API_URL           the desired endpoint to contact
     * @param payload           the payload to send as Json Object
     *                          (null if not necessary)
     * @param method            the methdo to use (GET, POST, ...)
     * @param requestProperties set header with Map<key, value> (Strings)
     * @return String will return the response body as String
     * @throws IOException on faulty http connection
     */
    private String apiRequest(String API_URL, JSONObject payload, Method method, Map<String, String> requestProperties) throws IOException {
        final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        if (payload == null) {
            payload = new JSONObject();
        }

        switch (method) {
            case GET: {
                Request request;
                Request.Builder builder = new Request.Builder();


                builder.url(API_URL);
                if (requestProperties != null && requestProperties.size() > 0) {
                    for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                        builder.addHeader(entry.getKey(), entry.getValue());
                    }
                }
                request = builder.build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody body =response.body();
                    if (body != null) {
                        String reString = body.string();
                        //Log.d(LOG_TAG, "apiRequest -> Body: " + reString);
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code: " + response);
                        //                Headers responseHeaders = response.headers();
                        //                for (int i = 0; i < responseHeaders.size(); i++) {
                        //                    Log.d(LOG_TAG, responseHeaders.name(i) + ":" + responseHeaders.value(i));
                        //                }
                        return reString;
                    } else
                        throw new IOException("Unexpected: body is empty");
                } catch (UnknownHostException ex) {
                    Log.e(LOG_TAG, "Error GET " + API_URL + ": " + ex.toString());
                    Telemetry.processException(ex);
                    throw ex;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.e(LOG_TAG, "Error GET " + API_URL + ": " + ex.toString());
                    Telemetry.processException(ex);
                    throw ex;
                }
            }
            case POST: {
                /*
                Log.d(LOG_TAG, "apiRequest -> URL: " + API_URL + "\n" +
                 "apiRequest -> Payload: " + payload.toString());
                 
                 */

                Request.Builder builder = new Request.Builder();
                builder.url(API_URL);
                if (requestProperties != null && requestProperties.size() > 0) {
                    for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                        builder.addHeader(entry.getKey(), entry.getValue());
                    }
                }

                Request request = builder.post(RequestBody.create(payload.toString(), MEDIA_TYPE_JSON)).build();

                try (Response response = client.newCall(request).execute()) {
                    //                Headers responseHeaders = response.headers();
                    //                for (int i = 0; i < responseHeaders.size(); i++) {
                    //                    Log.i("apiRequest -> Headers", responseHeaders.name(i) + ":" + responseHeaders.value(i));
                    //                }
                    ResponseBody body =response.body();
                    if (body != null) {
                        String reString = body.string();
                        //Log.d(LOG_TAG, "apiRequest ("+API_URL+") -> Body: " + reString);
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code: " + response);
                        //                Headers responseHeaders = response.headers();
                        //                for (int i = 0; i < responseHeaders.size(); i++) {
                        //                    Log.d(LOG_TAG, responseHeaders.name(i) + ":" + responseHeaders.value(i));
                        //                }
                        return reString;
                    } else
                        throw new IOException("Unexpected: body is empty");
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error POST " + API_URL + ": " + e.toString());
                    e.printStackTrace();
                    Telemetry.processException(e);
                    throw e;
                }
            }
        }
        return null;
    }

    //
    // Helper functions following up
    //
    private JSONObject jsonSend(String API_URL, Method method, JSONObject payload, Map<String, String> requestProperties) throws IOException, JSONException {
        String apiResponse = apiRequest(API_URL, payload, method, requestProperties);
        //Log.d(LOG_TAG, apiResponse);
        // TODO String -> JSONOBject
        return new JSONObject(Objects.requireNonNull(apiResponse));
    }

    private String stringSend(String API_URL, Method method, JSONObject payload, Map<String, String> requestProperties) throws IOException {
        return apiRequest(API_URL, payload, method, requestProperties);
        //Log.d(LOG_TAG, apiResponse);
    }


    JSONObject jsonPOST(String API_URL, JSONObject payload, Map<String, String> requestProperties) throws IOException, JSONException {
        return jsonSend(API_URL, Method.POST, payload, requestProperties);
    }


    public JSONObject jsonGET(String API_URL, Map<String, String> requestProperties) throws IOException, JSONException {
        return jsonSend(API_URL, Method.GET, null, requestProperties);
    }

    String stringPOST(String API_URL, JSONObject payload, Map<String, String> requestProperties) throws IOException {
        return stringSend(API_URL, Method.POST, payload, requestProperties);
    }

    String stringGET(String API_URL, Map<String, String> requestProperties) throws IOException {
        return stringSend(API_URL, Method.GET, null, requestProperties);
    }


}

