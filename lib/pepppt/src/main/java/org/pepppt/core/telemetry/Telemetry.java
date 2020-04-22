package org.pepppt.core.telemetry;

import android.util.Log;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;

import org.pepppt.core.BuildConfig;
import org.pepppt.core.CoreContext;
import org.pepppt.core.ProximityTracingService;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.database.models.Setting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class stores informations about the call statistics of the functions in the core.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Telemetry {
    private static final String TAG = Telemetry.class.getSimpleName();
    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final String telemetryServerUrl = "https://pepp-dev.rhtech.online/telej";
    private static final int ForeignStackTraceLevels = 3;
    static KPIAuxData deviceModelData;
    static String telemetryId = DataSourceKeys.VALUE_EMPTY;
    public static long ExceptionCount;
    private static List<String> caughtExceptions = new ArrayList<>();

    /**
     * Initializes the telemetry class
     */
    public static void init() {
        initTelemetryId();
        initDeviceModelInformation();
    }

    /**
     * Loads the telemetry id from the database. Creates a new one
     * if needed.
     */
    private static void initTelemetryId() {
        String tid = getTelemetryIdFromDatabase();
        if (tid.equals(DataSourceKeys.VALUE_EMPTY)) {
            telemetryId = createTelemetryId();
            setTelemetryIdInDatabase(telemetryId);
        } else {
            telemetryId = tid;
        }
    }

    /**
     * Initializes the daviceModelData with data based on the
     * context.
     */
    private static void initDeviceModelInformation() {
        deviceModelData = new KPIAuxData();
        deviceModelData.create(CoreContext.getAppContext());
    }

    /**
     * Loads the telemetry id from the database.
     *
     * @return The telemetry id from the database. DataSourceKeys.VALUE_EMPTY if
     * there is no id in the database.
     */
    private static String getTelemetryIdFromDatabase() {
        Setting telemetryid = SettingsDatabaseHelper.getByKey(DataSourceKeys.TELEMETRY_ID_KEY);
        if (telemetryid == null)
            return DataSourceKeys.VALUE_EMPTY;
        return telemetryid.getValue();
    }

    /**
     * Returns a new telemetry id as a string.
     *
     * @return The new telemetry id.
     */
    private static String createTelemetryId() {
        UUID id = UUID.randomUUID();
        String ids = id.toString();
        return ids.substring(0, 8);
    }

    /**
     * Stores the telemetry id into the database.
     *
     * @param id The telemetry id.
     */
    private static void setTelemetryIdInDatabase(String id) {
        SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.TELEMETRY_ID_KEY, id);
    }

    /**
     * Sends a list of KPI objects to the telemetry server.
     *
     * @param kpis A list of KPI objects
     */
    public static void sendKPIs(ArrayList<KPI> kpis) {
        Gson gson = new Gson();
        String json = gson.toJson(kpis);
        sendTelemetryData(json);
    }

    /**
     * Sends a KPI object to the telemetry server.
     *
     * @param kpi The KPI object.
     */
    public static void sendKPI(KPI kpi) {
        ArrayList<KPI> array = new ArrayList<>();
        array.add(kpi);
        Gson gson = new Gson();
        String json = gson.toJson(array);
        sendTelemetryData(json);
    }

    /**
     * Sends a string to the telemetry server.
     *
     * @param data The string.
     */
    private static void sendTelemetryData(String data) {
        //
        // Send to telemetry only if debug is on and the ui has enabled it.
        //
        if (BuildConfig.DEBUG && ProximityTracingService.getInstance().getEnableTelemetry()) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            OkHttpClient client = clientBuilder.connectTimeout(10, TimeUnit.SECONDS).build();
            Request request = new Request.Builder()
                    .url(telemetryServerUrl)
                    .post(RequestBody.create(data, MEDIA_TYPE_TEXT))
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    Log.e(TAG, "response error = " + response.code());
            } catch (IOException ex) {
                Log.e(TAG, ex.toString());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Converts a stack trace list into a string and keeps only the first layers.
     *
     * @param elements The stack trace list.
     * @return Resturns the converted string.
     */
    static String getStripedStackTrace(StackTraceElement[] elements) {
        List<StackTraceElement> array = new ArrayList<>(Arrays.asList(elements));
        List<String> striped = new ArrayList<>();
        int count = 0;
        for (StackTraceElement ste: array) {
            if (count <= ForeignStackTraceLevels)
                striped.add(ste.toString());
            else {
                if (ste.toString().startsWith("org.pepppt.")) {
                    striped.add(ste.toString());
                }
            }
            count++;
        }
        return striped.toString();
    }

    /**
     * Adds an exception to the caughtexception list if its the
     * first time.
     *
     * @param ex The exception.
     * @return Returns true if it was the first time for that
     * exception.
     */
    static boolean updateExceptionList(Exception ex) {
        String stripedStack = getStripedStackTrace(ex.getStackTrace());
        if (!caughtExceptions.contains(stripedStack)) {
            caughtExceptions.add(stripedStack);
            return true;
        }
        return false;
    }

    /**
     * Returns the caught exceptions.
     *
     * @return The list of caught exceptins.
     */
    static List<String> getCaughtExceptions() {
        return caughtExceptions;
    }

    /**
     * Processes the exception. Each exception is sent only once.
     *
     * @param ex The Exception.
     */
    public static void processException(Exception ex) {
        Log.e(TAG, ex.toString());
        ExceptionCount++;
        if (updateExceptionList(ex))
            sendKPI(new KPI(ex));
    }
}
