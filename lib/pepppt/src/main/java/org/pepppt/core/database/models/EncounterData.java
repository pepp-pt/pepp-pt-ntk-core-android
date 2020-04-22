package org.pepppt.core.database.models;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.pepppt.core.BuildConfig;

import java.util.Date;

/**
 * Model class of the Encounter data
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EncounterData implements IMasterDataModel {
    private static final String LOG_TAG = EncounterData.class.getSimpleName();
    public static final String TABLE_NAME = "encounters";
    public static final String COLUMN_Id = "_id";
    private static final String COLUMN_Json_Data = "json_data";
    public static final String COLUMN_Created_On = "created_on";
    private static final String COLUMN_App_Version = "app_version";

    public static final String Sql_Table_Create = "CREATE TABLE " + TABLE_NAME
            + "(" + COLUMN_Id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_Json_Data + " TEXT NOT NULL, "
            + COLUMN_App_Version + " TEXT NOT NULL, "
            + COLUMN_Created_On + " LONG NOT NULL);";

    public EncounterData() {
    }

    private EncounterData(String json_data) {
        setJsonData(json_data);
        setAddtionalData();
    }

    @Expose(serialize = false)
    private transient long id;

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    @SerializedName("value")
    private String json_data;

    public String getJsonData() {
        return json_data;
    }

    public void setJsonData(String json_data) {
        this.json_data = json_data;
    }

    @Expose(serialize = false)
    private transient String app_version;

    public String getAppVersion() {
        return app_version;
    }

    private void setAppVersion(String app_version) {
        this.app_version = app_version;
    }

    @SerializedName("timestamp")
    private long createdOn;

    public long getCreatedOn() {
        return createdOn;
    }

    private void setCreatedOn(long created_on) {
        this.createdOn = created_on;
    }

    public Date getCreatedOnAsDate() {
        return new Date(this.createdOn);
    }

    private void setCreatedOnFromDate(Date created_on) {
        this.createdOn = created_on.getTime();
    }

    private void setAddtionalData() {
        setAppVersion(BuildConfig.VERSION_NAME);
        if (createdOn <= 0) {
            setCreatedOn(System.currentTimeMillis());
        }
    }

    @NotNull
    @Override
    public String toString() {
        return id + " - " + json_data + " - " + createdOn + " - " + app_version;
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{COLUMN_Id, COLUMN_Json_Data, COLUMN_Created_On, COLUMN_App_Version};
    }

    @Override
    public EncounterData fromCursor(Cursor cursor) {
        EncounterData encounter_data = new EncounterData();
        encounter_data.setId(cursor.getLong(cursor.getColumnIndexOrThrow(EncounterData.COLUMN_Id)));
        encounter_data.setJsonData(cursor.getString(cursor.getColumnIndexOrThrow(EncounterData.COLUMN_Json_Data)));
        encounter_data.setCreatedOn(cursor.getLong(cursor.getColumnIndexOrThrow(EncounterData.COLUMN_Created_On)));
        encounter_data.setAppVersion(cursor.getString(cursor.getColumnIndexOrThrow(EncounterData.COLUMN_App_Version)));
        return encounter_data;
    }

    @Override
    public String getDefaultOrder() {
        return COLUMN_Id + " ASC";
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public ContentValues toContentValues() {
        setAddtionalData();
        ContentValues values = new ContentValues();
        values.put(COLUMN_Json_Data, json_data);
        values.put(COLUMN_App_Version, app_version);
        values.put(COLUMN_Created_On, createdOn);
        return values;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static EncounterData fromJSon(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, EncounterData.class);
    }
}
