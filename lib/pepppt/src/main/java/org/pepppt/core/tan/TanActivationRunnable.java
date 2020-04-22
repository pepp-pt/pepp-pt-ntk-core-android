package org.pepppt.core.tan;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.CoreCallback;
import org.pepppt.core.CoreContext;
import org.pepppt.core.database.DataSourceKeys;
import org.pepppt.core.database.SettingsDatabaseHelper;
import org.pepppt.core.database.models.Setting;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.network.ApiRequest;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.Units;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class TanActivationRunnable implements Runnable {
    private static final String TAG = TanActivationRunnable.class.getSimpleName();

    @Override
    public void run() {
        try {
            int loopCounter = 0;

            Setting expiryDateSetting = SettingsDatabaseHelper.getByKey(DataSourceKeys.UPLOAD_TAN_EXPIRY_DATE_KEY);
            if (expiryDateSetting == null) {   //no TAN
                CoreCallback.callUI(CoreEvent.TAN_ACTIVATION_FAILED, new EventArgs());
                return;
            }

            long expiryDate = Long.parseLong(expiryDateSetting.getValue()) * 1000;
            long now = System.currentTimeMillis();
            if (expiryDate < now) {   //TAN expired
                CoreCallback.callUI(CoreEvent.TAN_ACTIVATION_FAILED, new EventArgs());
                return;
            }

            if (isTanActivated()) {
                CoreCallback.callUI(CoreEvent.TAN_ACTIVATION_SUCCEDED, new EventArgs());
                return;
            }

            CoreContext.getInstance().getApiRequestThread().postDelayed(new TanActivationRunnable(), 10 * Units.SECONDS);

        } catch (IOException | JSONException e) {
            Log.e(TAG, "GetTanActivation failed", e);
            Telemetry.processException(e);
            CoreCallback.callUI(CoreEvent.TAN_ACTIVATION_FAILED, new EventArgs());
        }
    }

    private boolean isTanActivated() throws IOException, JSONException {
        JSONObject meAccount_response = ApiRequest.getMeAccount();
        String uploadAllowed = meAccount_response.optString("/uploadAllowed", "");
        if (uploadAllowed.length() > 0) {
            return uploadAllowed.equalsIgnoreCase("true");
        }
        return false;
    }
}
