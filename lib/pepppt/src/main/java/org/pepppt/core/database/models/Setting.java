package org.pepppt.core.database.models;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.RestrictTo;

/**
 * Model class of the Settings
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Setting implements IMasterDataModel {
    private static final String LOG_TAG = IMasterDataModel.class.getSimpleName();
    public static final String TABLE_NAME = "settings";
    public static final String COLUMN_Key = "clavis";
    private static final String COLUMN_Value = "valorem";
    public static final String Sql_Table_Create = "CREATE TABLE " + TABLE_NAME
            + "(" + COLUMN_Key + " TEXT NOT NULL PRIMARY KEY, "
            + COLUMN_Value + " TEXT);";

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Setting() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String key;
    private String value;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_Key, key);
        values.put(COLUMN_Value, value);
        return values;
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{COLUMN_Key, COLUMN_Value};
    }

    @Override
    public IMasterDataModel fromCursor(Cursor cursor) {
        return new Setting(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_Key)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_Value)));
    }

    @Override
    public String getDefaultOrder() {
        return COLUMN_Key + " ASC";
    }
}
