package org.pepppt.core.scheduling;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.CoreContext;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.database.models.Setting;
import org.pepppt.core.network.ApiRequest;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.io.IOException;
import java.util.Objects;

/**
 * Runs in an interval and checks the expiration of the token and starts the refresh
 * runnable in case the token needs to be updated.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class TokenServiceRunnable implements Runnable {
    private final static long tokenServiceInterval = 15 * Units.SECONDS;
    private final static long restPeriod = 60 * Units.SECONDS;
    private static final String TAG = TokenServiceRunnable.class.getSimpleName();
    private static final long safeZone = 5 * Units.MINUTES;

    public String getToken() {
        return getTokenStringFromDatabase();
    }

    @Override
    public void run() {

        final CoreContext cc = CoreContext.getInstance();

        try {
            if (cc.getCoreSystems().hasBeenDecommissioned()) {
                Log.i(TAG, "hasBeenDecommissioned -> exiting");
                return;
            }

            if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                Log.e(TAG, "do not run on the main thread");
                Log.e(TAG, "use a computation thread instead");
            }

            boolean refreshNow = false;

            //
            // Only proceed if the onboarding process has been done.
            //
            if (Objects.equals(SettingsDatabaseHelper.getByKey(DataSourceKeys.DEVICE_IS_SIGNED_UP_KEY).getValue(), DataSourceKeys.VALUE_TRUE)) {

                final long now = System.currentTimeMillis();

                //
                // Get token from database
                //
                final String tokenFromDatabase = getTokenStringFromDatabase();
                if (!tokenFromDatabase.isEmpty()) {
                    String tokenExpiredATString = getTokenExpiredAT();

                    if (tokenExpiredATString.isEmpty()) {
                        refreshNow = true;
                    } else {
                        long tokenExpiredAt = Long.parseLong(tokenExpiredATString);
                    /*
                    Log.i(TAG, "token has expiration time");
                    Log.i(TAG, "\n" + "TokenExpireAT: " +  tokenExpiredAt +
                                    "\n" + "now: " +  now +
                                    "\n" + "TokenExpireAT - offset: " +  (tokenExpiredAt - offset)
                            );

                     */
                        //
                        // Check if token is expired or is about (offset) to expire.
                        //
                        if (now >= tokenExpiredAt - safeZone) {
                            Log.i(TAG, "token needs a refresh");
                            refreshNow = true;
                        } else {
                            refreshNow = false;
                        }
                    }
                } else {
                    Log.i(TAG, "token not in the database -> token needs a refresh");
                    refreshNow = true;
                }

                if (refreshNow) {
                    try {
                        Log.d(TAG, "Refreshing Token..");
                        final JSONObject apiResponse = ApiRequest.auth();
                        long expiresAtSecs = Long.parseLong(apiResponse.getString("expires-at"));
                        SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.TOKEN_EXPIRES_AT_KEY, Long.toString(expiresAtSecs));
                        SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.TOKEN_LAST_TOKEN_KEY, apiResponse.getString("_TOKEN"));
                    } catch (IOException | JSONException e) {
                        Telemetry.processException(e);
                        e.printStackTrace();
                    }

                }
            }

            if (refreshNow) {
                //
                // In case of a refresh (or a try) => create a rest period to avoid spamming the backend.
                //
                cc.getComputationThread().
                        postDelayed(new TokenServiceRunnable(), restPeriod);
            } else {
                //
                // No refresh => check again in after a short pause.
                //
                cc.getComputationThread().
                        postDelayed(new TokenServiceRunnable(), tokenServiceInterval);
            }
        } catch (Exception ex) {
            Telemetry.processException(ex);
        }

    }

    private String getTokenStringFromDatabase() {
        Setting savedToken = SettingsDatabaseHelper.getByKey(DataSourceKeys.TOKEN_LAST_TOKEN_KEY);
        return savedToken.getValue();
    }

    private String getTokenExpiredAT() {
        Setting tokenexpires = SettingsDatabaseHelper.getByKey(DataSourceKeys.TOKEN_EXPIRES_AT_KEY);
        return tokenexpires.getValue();
    }
}
