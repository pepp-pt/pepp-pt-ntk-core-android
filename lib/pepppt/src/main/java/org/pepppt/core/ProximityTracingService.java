package org.pepppt.core;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import org.json.JSONException;
import org.pepppt.core.ble.AdvertiserRunnables;
import org.pepppt.core.ble.ScannerRunnables;
import org.pepppt.core.ble.profiles.AdvertiserProfile;
import org.pepppt.core.ble.profiles.ScannerProfile;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.exceptions.InvalidBackendEndpointException;
import org.pepppt.core.exceptions.InvalidBackendEndpointFormatException;
import org.pepppt.core.exceptions.MissingBackendEndpointException;
import org.pepppt.core.exceptions.PackageNotFoundException;
import org.pepppt.core.livedata.TanModel;
import org.pepppt.core.messages.CheckMessagesRunnable;
import org.pepppt.core.messages.ConfirmMessageRunnable;
import org.pepppt.core.messages.LoadAllMessagesRunnable;
import org.pepppt.core.messages.RequestTestResultRunnable;
import org.pepppt.core.network.DeviceRegistrationService;
import org.pepppt.core.network.EncounterUploadService;
import org.pepppt.core.permissions.PermissionHelper;
import org.pepppt.core.service.CoreService;
import org.pepppt.core.systemmanagement.PowerManagementHelper;
import org.pepppt.core.tan.Tan;
import org.pepppt.core.tan.TanRequestRunnable;
import org.pepppt.core.telemetry.KPI;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.util.ArrayList;

import okhttp3.Interceptor;
import okhttp3.Protocol;

/**
 * This main api class fot the ui.
 */
@SuppressWarnings("unused")
public class ProximityTracingService {
    private static ProximityTracingService instance;

    private static final String TAG = ProximityTracingService.class.getSimpleName();

    /**
     * The notification icon in the top of the phone. Because its a foreground service
     */
    private Notification foregroundServiceNotification;

    /**
     * The callback to the user UI
     */
    private CoreCallback uiCallback;

    /**
     * The backend client id
     */
    private String backendClientId;

    /**
     * The backend client secret
     */
    private String backendClientSecret;

    /**
     * The api endpoints
     */
    private CoreApiEndpoints endpoints;

    /**
     * The maximum time a user can put the scanner and advertise to sleep.
     */
    @SuppressWarnings("WeakerAccess")
    public final static long MAX_PAUSE_TIME = 2 * Units.HOURS;

    /**
     * The certificate pinning data
     */
    private String[] certificatePinningData;
    private String certificatePinningHost;

    /**
     * If true, the core will keep bluetooth switched on.
     */
    private boolean bluetoothKeepAlive = false;

    private TanModel tanModel;

    private boolean enableTelemetry;

    //
    // Server settings
    //
    private long serverConnectTimeout;
    private long serverReadTimeout;
    private long serverWriteTimeout;
    private long serverCallTimeout;
    private Interceptor interceptor;
    private Protocol protocol;

    /**
     * Returns the singleton instance.
     *
     * @return Returns the singleton instance.
     */
    public static ProximityTracingService getInstance() {
        return instance;
    }

    public ProximityTracingService(@NonNull Context context) throws PackageNotFoundException {
        instance = this;
        tanModel = new TanModel();

        serverConnectTimeout = 30 * Units.SECONDS;
        serverReadTimeout = 30 * Units.SECONDS;
        serverWriteTimeout = 30 * Units.SECONDS;
        serverCallTimeout = 0;

        //
        // Create the core context for the whole core.
        //
        CoreContext.create(context);

        //
        // Create the foreground service.
        //
        CoreService.init(context);

        if (hasBeenDecommissioned())
            CoreContext.getInstance().getCoreSystems().decommission();
    }

    /**
     * Starts the core systems.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return True if okay. False if the core has been decommissioned.
     */
    public final boolean start() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return false;
        }
        CoreContext.getInstance().getCoreSystems().start();
        return true;
    }


    /**
     * Sets the callback method that the ui can provide to get indications from the
     * core. see CoreEvent.java for further details.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param callback The ui should provide a callback that will called by the core
     *                 in case of important events. see CoreCallback.
     */
    public final void setCallback(@NonNull CoreCallback callback) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        uiCallback = callback;
    }


    /**
     * Sets the foreground service notification. Call this method in your application
     * class in onCreate().
     * <p>Does not work if the core has been decommissioned.
     */
    public final void setForegroundServiceNotification(@NonNull Notification fgsn) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        foregroundServiceNotification = fgsn;
    }


    /**
     * Gets the foreground service notification.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return The foreground service notification.
     */
    public final Notification getForegroundServiceNotification() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }
        return foregroundServiceNotification;
    }


    /**
     * Returns true if the core has been stalled.
     * See CoreEvent.CORE_IS_STALLED and CoreEvent.CORE_IS_READY.
     *
     * @return Returns true if the core has been stalled. Returns true
     * if the core has been decommissioned.
     */
    public final boolean isStalled() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return true;
        }

        return CoreContext.getInstance().getCoreSystems().isStalled();
    }


    public TanModel getTanModel() {
        return tanModel;
    }

    /**
     * Will be used by the core.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return The callback that has been set by the user.
     */
    CoreCallback getCallback() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }
        return uiCallback;
    }


    /**
     * Returns the endpoints.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the endpoints.
     */
    @NonNull
    public final CoreApiEndpoints getEndpoints() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return new CoreApiEndpoints();
        }
        return endpoints;
    }


    /**
     * Starts the sign up process for the app.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param firebaseToken Your firebase token.
     */
    public final void signUpUser(@NonNull String firebaseToken) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.DEVICE_FIREBASETOKEN_KEY, firebaseToken);
        CoreContext.getInstance().getApiRequestThread().post(new DeviceRegistrationService());
    }


    /**
     * Enables the broadcasting of advertisements.
     * <p>Does not work if the core has been decommissioned.
     */
    @SuppressWarnings("WeakerAccess")
    public final void enableAdvertising(boolean enable) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        AdvertiserRunnables ar = CoreContext.getInstance().getAdvertiserRunnables();
        if (ar.getNextAdvertiserProfile() != null)
            ar.getNextAdvertiserProfile().setEnable(enable);
        else
            ar.getCurrentAdvertiserProfile().setEnable(enable);
    }


    /**
     * Returns the true if advertising is enabled.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the true if advertising is enabled.
     */
    public final boolean isAdvertisingEnabled() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return false;
        }

        AdvertiserRunnables ar = CoreContext.getInstance().getAdvertiserRunnables();
        if (ar.getNextAdvertiserProfile() != null)
            return ar.getNextAdvertiserProfile().getEnable();
        else
            return ar.getCurrentAdvertiserProfile().getEnable();
    }


    /**
     * Enables the scanning of Advertisements.
     * <p>Does not work if the core has been decommissioned.
     */
    @SuppressWarnings("WeakerAccess")
    public final void enableScanning(boolean enable) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        ScannerRunnables sr = CoreContext.getInstance().getScannerRunnables();
        if (sr.getNextScannerProfile() != null)
            sr.getNextScannerProfile().setEnable(enable);
        else
            sr.getCurrentScannerProfile().setEnable(enable);
    }


    /**
     * Paues the scanning for other apps for the amount of minutes.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param time The length of time how long the scanner has to wait.
     * @return false if the number is bigger then MAX_PAUSE_TIME or the core
     * has been decommissioned.
     */
    public final boolean pauseScanningFor(long time) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return false;
        }

        if (time > MAX_PAUSE_TIME)
            return false;
        long now = System.currentTimeMillis();
        CoreContext.getInstance().getScannerRunnables().pauseUntil(now + time);
        enableScanning(false);
        return true;
    }


    /**
     * Pauses the advertising for the specified amount of time.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param time The amount of time to pause.
     * @return Returns true if the pause have been set. False if the pause
     * was too long. Max 2h. Or the core has been decommissioned.
     */
    public final boolean pauseAdvertisingFor(long time) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return false;
        }

        if (time > MAX_PAUSE_TIME)
            return false;
        long now = System.currentTimeMillis();
        CoreContext.getInstance().getAdvertiserRunnables().pauseUntil(now + time);
        enableAdvertising(false);
        return true;
    }


    /**
     * Return true if scanning is enabled.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Return true if scanning is enabled.
     */
    public final boolean isScanningEnabled() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return false;
        }

        ScannerRunnables sr = CoreContext.getInstance().getScannerRunnables();
        if (sr.getNextScannerProfile() != null)
            return sr.getNextScannerProfile().getEnable();
        else
            return sr.getCurrentScannerProfile().getEnable();
    }


    /**
     * Retrieves the statistics of the bluetooth subsystem.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return The statistics.
     */
    public final Metrics getMetrics() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }
        return CoreContext.getInstance().getMetrics();
    }


    /**
     * True if the core should keep the bluetooth switched on. Warning: This can
     * conflict with the user experience.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param enable true if the core should keep the bluetooth switched on.
     */
    public final void setBluetoothKeepAlive(boolean enable) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        bluetoothKeepAlive = enable;
    }


    /**
     * Returns the state of keepBTon.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns true if the core keeps bluetooth switched on.
     */
    public final boolean getBluetoothKeepAlive() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned())
            return false;

        return bluetoothKeepAlive;
    }


    /**
     * Sets a new scanner profile.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param profile The new scanner profile.
     */
    public final void setScannerProfile(@NonNull ScannerProfile profile) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        CoreContext.getInstance().getScannerRunnables().setScannerProfile(profile);
    }


    /**
     * Sets a new advertise profile.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param profile The new adveritse profile.
     */
    public final void setAdvertiserProfile(@NonNull AdvertiserProfile profile) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        CoreContext.getInstance().getAdvertiserRunnables().setAdvertiserProfile(profile);
    }


    /**
     * Gets the Tan live data.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the tan live data.
     */
    public final LiveData<Tan> getTan() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }

        return tanModel.getLiveData_Tan();
    }

    /**
     * Gets the state of the tan request.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the state of the tan request.
     */
    public final LiveData<String> getTanRequestState() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }

        return tanModel.getRequestState();
    }

    /**
     * Sends a request to the backend for a TAN, that is needed for the export process.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns true.
     */
    public final boolean requestTan() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return false;
        }
        tanModel.requestTan();
        CoreContext.getInstance().getApiRequestThread().post(new TanRequestRunnable());
        return true;
    }

    /**
     * Sends a request to the backend for a LabTestResult.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param labId   the lab_id
     * @param orderId the order_id
     */
    public final void requestLabTestResult(@NonNull String labId, @NonNull String orderId) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
        }
        CoreContext.getInstance().getApiRequestThread().post(new RequestTestResultRunnable(labId, orderId));
    }


    /**
     * Starts the export of the user data to the backend. see CoreCallback and CoreEvent
     * to catch to correct export events.
     * <p>Does not work if the core has been decommissioned.
     */
    public final void startExport() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        Intent i = new Intent(CoreContext.getAppContext(), EncounterUploadService.class);
        CoreContext.getAppContext().startService(i);
    }

    /**
     * Returns a list of missing permissions.
     *
     * @return Returns a list of missing permissions.
     */
    @NonNull
    public final ArrayList<String> checkAllPermissions() {
        return PermissionHelper.checkAllPermissions(CoreContext.getAppContext());
    }


    /**
     * Returns true if the battery optimization is enabled for the app.
     *
     * @return Returns true if the battery optimization is enabled for the app.
     */
    public final boolean isBatteryOptimizationEnabled() {
        return !PowerManagementHelper.isIgnoringBatteryOptimizations();
    }


    /**
     * Disables the battery optimization for the app.
     * <p>Does not work if the core has been decommissioned.
     */
    public final void disableBatteryOptimization() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        PowerManagementHelper.startChangeBatteryOptimization(CoreContext.getAppContext());
    }


    /**
     * Clears the client id and client secret.
     */
    private void clearData() {
        backendClientId = "";
        backendClientSecret = "";
    }


    /**
     * Set the client id and the client secret.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param clientId     The client id.
     * @param clientSecret The client secret.
     */
    public final void setClientData(@NonNull String clientId, @NonNull String clientSecret) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        backendClientId = clientId;
        backendClientSecret = clientSecret;
    }


    /**
     * Returns the client id.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the client id.
     */
    public final String getClientId() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }
        return backendClientId;
    }


    /**
     * Returns the client secret.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the client secret.
     */
    public final String getClientSecret() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }
        return backendClientSecret;
    }


    /**
     * Sets the endpoints.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param json Ths json with endpoints. You can find an example in the docs/util folder.
     * @throws JSONException                         If the json format is wrong.
     * @throws InvalidBackendEndpointException       If the user has specified an invalid endpoint name.
     * @throws MissingBackendEndpointException       If a specific endpoint is missing.
     * @throws InvalidBackendEndpointFormatException If the number of endpoints in the json is wrong.
     */
    public final void setApiEndpoints(@NonNull String json) throws JSONException,
            InvalidBackendEndpointException, MissingBackendEndpointException,
            InvalidBackendEndpointFormatException {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        endpoints = new CoreApiEndpoints();
        endpoints.set(json);
    }


    /**
     * Set the certificate pinner information.
     * <p>Does not work if the core has been decommissioned.
     *
     * @param host The hostname.
     * @param data The data.
     */
    public final void setCertificatePinnerInformation(@NonNull String host, @NonNull String[] data) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        certificatePinningHost = host;
        certificatePinningData = data;
    }


    /**
     * Returns the certificate pinner data.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the certificate pinner data.
     */
    public final String[] getCertificatePinningData() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }
        return certificatePinningData;
    }


    /**
     * Returns the certificate pinner host.
     * <p>Does not work if the core has been decommissioned.
     *
     * @return Returns the certificate pinner host.
     */
    public final String getCertificatePinningHost() {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return null;
        }
        return certificatePinningHost;
    }


    /**
     * Decommissions the core functions. After calling this method the core functions
     * can't be used anymore. A new installation is necessary to make the app ready for
     * use again.
     * <p>This method should be used after the upload has been finished.</p>
     * <p>Does not work if the core has been decommissioned.</p>
     */
    public final void decommission() throws PackageNotFoundException {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }

        CoreContext.getInstance().getCoreSystems().decommission();

        if (!CoreContext.getInstance().getDataSource().deleteDatabase())
            Log.e(TAG, "deleteDatabase -> failed");

        Package p = ProximityTracingService.class.getPackage();
        if (p != null) {
            String packageName = p.getName();
            if (packageName != null) {
                String key = packageName + "decommission";
                SharedPreferences prefs = CoreContext.getAppContext().getSharedPreferences(key, Context.MODE_PRIVATE);
                prefs.edit().putBoolean("decommission", true).apply();
                return;
            }
        }
        throw new PackageNotFoundException();
    }


    /**
     * Returns true if the core functions has been decommissioned.
     *
     * @return Returns true if the core functions has been decommissioned.
     */
    public final boolean hasBeenDecommissioned() throws PackageNotFoundException {
        Package p = ProximityTracingService.class.getPackage();
        if (p != null) {
            String packageName = p.getName();
            if (packageName != null) {
                String key = packageName + "decommission";
                SharedPreferences prefs = CoreContext.getAppContext().getSharedPreferences(key, Context.MODE_PRIVATE);
                return prefs.getBoolean("decommission", false);
            }
        }
        throw new PackageNotFoundException();
    }


    /**
     * Enables or disables the telemetry data.
     * <p>Do not use this function on the final product.</p>
     * <p>Does not work if the core has been decommissioned.</p>
     *
     * @param enable True if the telemetry should be used. Only works in debug.
     */
    public final void setEnableTelemetry(boolean enable) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        enableTelemetry = enable;
    }


    /**
     * Returns true if telementry is enabled.
     *
     * @return Returns true if telementry is enabled.
     */
    public final boolean getEnableTelemetry() {
        return enableTelemetry;
    }


    /**
     * Starts a request to check if new messages have arrived by the
     * health authority that are directed to you.
     * Possible events:
     * NEW_MESSAGES_ARRIVED, CHECK_MESSAGES_FAILED.
     *
     * <p>Does not work if the core has been decommissioned.</p>
     *
     * @param withFeedbackWhenNoMessages When true you also get NO_NEW_MESSAGES in case
     *                                   there is no message.
     */
    public final void checkForNewMessages(boolean withFeedbackWhenNoMessages) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        CoreContext.getInstance().getApiRequestThread().post(new CheckMessagesRunnable(true, withFeedbackWhenNoMessages));
    }

    /**
     * Requests a list of Messages objects.
     * Possible events:
     * LOAD_ALL_MESSAGES_SUCCESSFULLY, LOAD_ALL_MESSAGES_FAILED.
     *
     * <p>Does not work if the core has been decommissioned.</p>
     *
     * @param locale The language of the messages you are requesting.
     */
    public final void requestAllMessages(@NonNull String locale) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        CoreContext.getInstance().getApiRequestThread().post(new LoadAllMessagesRunnable(locale));
    }

    /**
     * After retrieving the new messages with requestAllMessages you need to confirm
     * each one of them.
     *
     * <p>Does not work if the core has been decommissioned.</p>
     *
     * @param messageId The id of the message you want to confirm. see Message.getId().
     */
    public final void confirmMessage(@NonNull String messageId) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        CoreContext.getInstance().getApiRequestThread().post(new ConfirmMessageRunnable(messageId));
    }

    /**
     * Send a key performance indicator to the telemetry server. Only works in debug.
     *
     * <p>Does not work if the core has been decommissioned.</p>
     *
     * @param kpi The kpi.
     */
    public final void sendTelemetry(@NonNull final KPI kpi) {
        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.e(TAG, "hasBeenDecommissioned -> aborting call");
            return;
        }
        if (BuildConfig.DEBUG)
            Telemetry.sendKPI(kpi);
    }

    /**
     * Sets the server timeout values.
     *
     * @param connectTimeout Sets the default connect timeout for new connections. A value of 0 means no timeout
     *                       otherwise values must be between 1 and [Integer.MAX_VALUE] when converted to milliseconds.
     * @param readTimeout    Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
     *                       values must be between 1 and [Integer.MAX_VALUE] when converted to milliseconds.
     * @param writeTimeout   Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
     *                       values must be between 1 and [Integer.MAX_VALUE] when converted to milliseconds.
     * @param callTimeout    Sets the default timeout for complete calls. A value of 0 means no timeout, otherwise values
     *                       must be between 1 and [Integer.MAX_VALUE] when converted to milliseconds.
     * @param protocol       Protocol to use. Can be null if not used.
     * @param interceptor    Interceptor object. Can be null if not used.
     */
    public final void setServerTimeouts(long connectTimeout,
                                        long readTimeout,
                                        long writeTimeout,
                                        long callTimeout,
                                        @Nullable Protocol protocol,
                                        @Nullable Interceptor interceptor) {
        this.serverConnectTimeout = connectTimeout;
        this.serverReadTimeout = readTimeout;
        this.serverWriteTimeout = writeTimeout;
        this.serverCallTimeout = callTimeout;
        this.interceptor = interceptor;
    }

    public long getServerConnectTimeout() {
        return serverConnectTimeout;
    }

    public long getServerReadTimeout() {
        return serverReadTimeout;
    }

    public long getServerWriteTimeout() {
        return serverWriteTimeout;
    }

    public long getServerCallTimeout() {
        return serverCallTimeout;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }

    public Protocol getProtocol() {
        return protocol;
    }
}
