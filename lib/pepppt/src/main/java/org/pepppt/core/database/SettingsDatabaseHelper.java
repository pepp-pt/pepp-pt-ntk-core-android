package org.pepppt.core.database;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.database.models.Setting;
import org.pepppt.core.CoreContext;
import org.pepppt.core.telemetry.Telemetry;

import java.util.HashMap;

/**
 * HelperClass for managing the Settings in the database
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SettingsDatabaseHelper {
    private static final String LOG_TAG = SettingsDatabaseHelper.class.getSimpleName();

    public static boolean insertOrUpdate (String key, String value) {
        return  insertOrUpdate(new Setting(key, value));
    }
    private static boolean insertOrUpdate (Setting setting) {
        return CoreContext.getInstance().getDataSource().replace(setting) >= 0;
    }
    public static boolean deleteByKey (String key) {
        if (key.contains(" "))
            throw  new IllegalArgumentException ("no spaces within the key");

        try {
            CoreContext.getInstance().getDataSource().execSQL("DELETE FROM " + Setting.TABLE_NAME + " WHERE " + Setting.COLUMN_Key + " = '" + key + "';");
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "delete_By_Key failed", e);
            Telemetry.processException(e);
        }
        return false;
    }
    public static Setting getByKey (String key) {
        if (key.contains(" "))
            throw  new IllegalArgumentException ("no spaces within the key");

        Setting setting = new Setting();
        Cursor cursor = CoreContext.getInstance().getDataSource().query(Setting.TABLE_NAME, null, Setting.COLUMN_Key+"=?", new String[] {key}, null, null, null);
        try {
            if (cursor.getCount()>0) {
                cursor.moveToFirst();
                return (Setting) setting.fromCursor(cursor);
            }
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "get_By_Key failed", ex);
            Telemetry.processException(ex);
        } finally {
            cursor.close();
        }
        return null;
    }

    public static HashMap<String,Setting> get_All () {
        Setting setting = new Setting();
        HashMap<String,Setting> returnMap = new HashMap<>();
        Cursor cursor = CoreContext.getInstance().getDataSource().rawQuery("SELECT * FROM " + Setting.TABLE_NAME + ";", null);
        try {
            while (cursor.moveToNext()) {
                Setting s = (Setting) setting.fromCursor(cursor);
                returnMap.put(s.getKey(),s);
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "get_All failed", ex);
            Telemetry.processException(ex);
        } finally {
            cursor.close();
        }
        return returnMap;
    }
}
