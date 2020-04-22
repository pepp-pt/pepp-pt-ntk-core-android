package org.pepppt.core.tan;

import android.util.Log;

import androidx.annotation.RestrictTo;

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
import org.pepppt.core.events.NewTanEventArgs;
import org.pepppt.core.network.ApiRequest;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class TanRequestRunnable implements Runnable {
    private static final String TAG = TanRequestRunnable.class.getSimpleName();

    @Override
    public void run() {
        try {

            Tan tan = null;
            try {
                Setting expiryDateSetting = SettingsDatabaseHelper.getByKey(DataSourceKeys.UPLOAD_TAN_EXPIRY_DATE_KEY);
                if (expiryDateSetting != null) {
                    long expiryDate = Long.parseLong(expiryDateSetting.getValue());
                    long now = System.currentTimeMillis();
                    if (expiryDate > now) {   //TAN valid
                        Setting tanSetting = SettingsDatabaseHelper.getByKey(DataSourceKeys.UPLOAD_TAN_KEY);
                        if (tanSetting != null) {
                            tan = new Tan(tanSetting.getValue(), expiryDate);
                        }
                    }
                }
            } catch (Exception ex) {
                Telemetry.processException(ex);
                Log.e(TAG, "get TAN from DB failed", ex);
            }

            if (tan == null) {   //request the Tan
                tan = getTan();
            }
            SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.UPLOAD_TAN_KEY, tan.getTanData());
            SettingsDatabaseHelper.insertOrUpdate(DataSourceKeys.UPLOAD_TAN_EXPIRY_DATE_KEY, tan.getExpiration() + "");

            CoreCallback.callUI(CoreEvent.NEW_TAN, new NewTanEventArgs(tan));

            CoreContext.getInstance().getApiRequestThread().postDelayed(new TanActivationRunnable(), 10 * Units.SECONDS);

            ProximityTracingService.getInstance().getTanModel().setNewTan(tan);

        } catch (IOException | JSONException e) {
            Log.e(TAG, "GetTANRequest failed", e);
            Telemetry.processException(e);
            CoreCallback.callUI(CoreEvent.TAN_REQUEST_FAILED, new EventArgs());
        }
    }

    private Tan getTan() throws IOException, JSONException {
        JSONObject getTan_response = ApiRequest.getTan();
        JSONObject getTan_success = getTan_response.getJSONObject("success");
        String tanData = getTan_success.getString("tan");
        long expiration = getTan_success.getLong("expiration") * 1000; //sek -> millis


        return new Tan(tanData, expiration);
    }
}
