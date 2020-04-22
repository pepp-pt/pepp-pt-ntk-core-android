package org.pepppt.core.scheduling;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.CoreCallback;
import org.pepppt.core.CoreContext;
import org.pepppt.core.ProximityTracingService;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.database.models.Setting;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.network.ApiRequestHandler;
import org.pepppt.core.storage.BatchIdsStorage;
import org.pepppt.core.storage.EBID;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Runs in an interval and refreshes the list of ids every 24 hours.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class KeystoreRunnable implements Runnable {
    private static final String TAG = KeystoreRunnable.class.getSimpleName();
    private final static int RequestNewEBIDFromStoreEvery = 24 * Units.HOURS;
    private final static int PeriodicRotation = 30 * Units.SECONDS;
    private final static long KeystoreFetchRestperiod = 10 * Units.MINUTES;

    private long lastSuccessfullKeystorefetch;
    private long lastTryKeystorefetch;
    private BatchIdsStorage storage;
    private long correction;

    public BatchIdsStorage getStorage() {
        return storage;
    }

    @Override
    public void run() {

        if (CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            Log.i(TAG, "hasBeenDecommissioned -> exiting");
            return;
        }

        if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
            Log.e(TAG, "do not run on the main thread");
            Log.e(TAG, "use a computation thread instead");
        }

        final long now = System.currentTimeMillis();

        // if the batchid storage is empty or its time to request a new one
        // fetch a fresh one from the store. To prevent a ddos attack on the
        // backend we take a timeout between backend calls.
        if (now - lastTryKeystorefetch >= KeystoreFetchRestperiod) {
            if ((storage.count() == 0) ||
                    now - lastSuccessfullKeystorefetch > RequestNewEBIDFromStoreEvery) {
                fetchFromKeystore();
            }
        }
        CoreContext.getInstance().getComputationThread().postDelayed(this, PeriodicRotation);
    }

    public KeystoreRunnable() {
        storage = new BatchIdsStorage();
    }

    private void fetchFromKeystore() {
        //
        // Gets a new batch of ebids.
        //
        final BatchIdsStorage newids = getEBIDs();

        //
        // The result can be null. This can happen if the token from the token
        // service is not valid yet.
        //
        if (newids != null && newids.count() > 0) {
            CoreContext.getInstance().getMetrics().LastBatchRefresh = System.currentTimeMillis();
            storage = newids;

            // logEBIDs(storage);

            lastSuccessfullKeystorefetch = System.currentTimeMillis();
            storeEbidListToDatabase();
            setLastSuccessfulKeystoreFetch(lastSuccessfullKeystorefetch);

            CoreCallback.callUI(CoreEvent.NEW_EBID_LIST, new EventArgs());

            //
            // Write the ids into the db
            //


        } else {
            Log.e(TAG, "could not get a fresh batch list from the keyserver");
            Log.e(TAG, "INFO: This can happen if the application starts and the user token is not valid yet.");
        }
    }

    static void storeEbidListToDatabase() {
        BatchIdsStorage storage = CoreContext.getInstance().getKeystoreRunnable().getStorage();

        try {
            JSONObject obj = new JSONObject();
            JSONArray array = new JSONArray();
            obj.put("items", array);
            for (int i = 0; i < storage.count(); i++) {
                JSONObject item = new JSONObject();
                EBID ebid = storage.getAt(i);
                item.put("id", ebid.getId());
                item.put("expired", ebid.getExpired());
                array.put(item);
            }
            String ebidsjson = obj.toString();
            SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.EBIDS_KEY, ebidsjson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void setLastSuccessfulKeystoreFetch(long lastSuccessfullKeystoreFetch) {
        SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.LAST_SUCCESSFULL_KEYSTORE_KEY, String.valueOf(lastSuccessfullKeystoreFetch));
    }

    private long getLastSuccessfulKeystoreFetch() {
        final Setting last = SettingsDatabaseHelper.getByKey(DataSourceKeys.LAST_SUCCESSFULL_KEYSTORE_KEY);
        if (last == null)
            return 0;
        if (last.getValue().equals(DataSourceKeys.VALUE_EMPTY))
            return 0;
        return Long.parseLong(last.getValue());
    }

    private String getEbidListKeyFromDatabase() {
        final Setting ebids = SettingsDatabaseHelper.getByKey(DataSourceKeys.EBIDS_KEY);
        if (ebids == null)
            return DataSourceKeys.VALUE_EMPTY;
        return ebids.getValue();
    }

    private static void logEBIDs(BatchIdsStorage storage) {
        if (storage != null) {
            for (EBID id : storage.getIds()) {
                Log.i(TAG, id.toString());
            }
        }
    }

    /**
     * Loads the ebid list, the last keystore fetch timestamp the last active
     * ebid from the database.
     */
    public void loadFromDatabase() {
        //
        // After start load ebids from the database
        //
        final String ebidsjson = getEbidListKeyFromDatabase();
        if (ebidsjson != null && !ebidsjson.equals(DataSourceKeys.VALUE_EMPTY)) {
            try {
                JSONObject obj = new JSONObject(ebidsjson);
                JSONArray items = obj.getJSONArray("items");
                for (int index = 0; index < items.length(); index++) {
                    JSONObject item = items.getJSONObject(index);
                    String ebid = item.getString("id");
                    long expired = item.getLong("expired");
                    storage.add(new EBID(UUID.fromString(ebid), expired));
                }

                //
                // Sets the server time received from the backend.
                // Not implemented yet.
                //
                /*
                long serverTime = 0;
                setCorrection(serverTime);
                 */

                // logEBIDs(storage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        lastSuccessfullKeystorefetch = getLastSuccessfulKeystoreFetch();

        final String lastActiveEbid = loadEbidKeyFromDatabase();
        if (lastActiveEbid != null && !lastActiveEbid.equals(DataSourceKeys.VALUE_EMPTY)) {
            Gson gson = new Gson();
            EBID ebid = gson.fromJson(lastActiveEbid, EBID.class);
            if (ebid != null)
                storage.setActiveEbid(ebid);
        }
    }

    /**
     * Returns the corrected time that is synchronized with the backend.
     *
     * @return Corrected time.
     */
    long getServerTime() {
        return System.currentTimeMillis() + correction;
    }

    /**
     * Sets the correction based on the server time.
     *
     * @param serverTime The servertime.
     */
    private void setCorrection(long serverTime) {
        correction = System.currentTimeMillis() - serverTime;
    }


    /**
     * Loads the active ebid key from the database.
     *
     * @return Returns the active ebid key from the database.
     */
    private String loadEbidKeyFromDatabase() {
        final Setting ebid = SettingsDatabaseHelper.getByKey(DataSourceKeys.ACTIVE_EBID_KEY);
        if (ebid == null)
            return DataSourceKeys.VALUE_EMPTY;
        return ebid.getValue();
    }


    /**
     * Get the ebids from the backend.
     *
     * @return Return the new storage.
     */
    private BatchIdsStorage getEBIDs() {
        final long now = System.currentTimeMillis();
        try {
            BatchIdsStorage newids = new BatchIdsStorage();
            Map<String, String> requestProperties = new HashMap<>();

            //
            // Get the endpoints...
            //
            final String baseUrl = ProximityTracingService.getInstance().getEndpoints().getApiBaseUrl();
            final String broadcastKeysPath = ProximityTracingService.getInstance().getEndpoints().getApiBroadcastKeysUrl();
            final String addressendpoint = baseUrl + broadcastKeysPath;

            //
            // Get the token from the token service. Its possible that the token service
            // has no valid token yet. In that case it returns null.
            //
            final String token = CoreContext.getInstance().getTokenServiceRunnable().getToken();
            if (token == null || token.isEmpty()) {
                Log.e(TAG, "getEBIDs: token null => try again later");
                return null;
            }

            //
            // The token is valid and now the webrequest can take place.
            //
            lastTryKeystorefetch = now;
            requestProperties.put("Authorization", "Bearer " + token);
            final JSONObject idsResponse = ApiRequestHandler.getInstance().jsonGET(addressendpoint, requestProperties);

            //
            // After the request the received json will be parsed for the ebids.
            //
            if (idsResponse != null) {
                JSONArray items = idsResponse.getJSONArray("items");
                for (int index = 0; index < items.length(); index++) {
                    JSONObject item = items.getJSONObject(index);
                    if (item != null) {
                        String id = item.getString("btcgaid");
                        String expiredstring = item.getString("expired");
                        if (!expiredstring.isEmpty()) {
                            //
                            // The received expiration times are in seconds. We need
                            // to multiply them with 1000 to get the milliseconds.
                            //
                            long expired = Long.parseLong(expiredstring) * Units.SECONDS;
                            newids.add(new EBID(UUID.fromString(id), expired));
                        }
                    }
                }
            }

            //
            // After receiving the ebids => sort them. ids with shorter lifespan
            // first. ids with longer lifespan are at the end of the storage.
            //
            newids.sort();

            return newids;
        } catch (Exception ex) {
            Log.e(TAG, "getEBIDs: " + ex.toString());
            Telemetry.processException(ex);
            ex.printStackTrace();
        }

        return null;
    }

}
