package org.pepppt.core.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.RestrictTo;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteStatement;

import org.pepppt.core.CoreContext;
import org.pepppt.core.ProximityTracingService;
import org.pepppt.core.database.models.EncounterData;
import org.pepppt.core.database.models.IMasterDataModel;
import org.pepppt.core.database.models.Setting;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.SharedPrefUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
/**
 * Class for managing the database
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DataSource  extends SQLiteOpenHelper {
    private static final String LOG_TAG = DataSource.class.getSimpleName();
    public static final String DB_NAME = "pepppt.db";
    private static final int DB_VERSION = 1;
    private static SQLiteDatabase database;
    private static File dbFile;
    private final Context context;

    public DataSource(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        this.context = context;

        if (!CoreContext.getInstance().getCoreSystems().hasBeenDecommissioned()) {
            SQLiteDatabase.loadLibs(context);

            dbFile = context.getDatabasePath(DB_NAME);

            HashMap<String, String> tablesToCreation = new HashMap<>();
            tablesToCreation.put(Setting.TABLE_NAME, Setting.Sql_Table_Create);
            tablesToCreation.put(EncounterData.TABLE_NAME, EncounterData.Sql_Table_Create);
            for (String key : tablesToCreation.keySet()) {
                Cursor c = rawQuery("SELECT * FROM sqlite_master WHERE name=? and type=?", new String[] {key, "table"});
                if (c.getCount() <= 0) {   //Table Creation - only once at first start
                    execute(tablesToCreation.get(key));
                    initTable(key);
                }
                c.close();
            }
        }
    }

    private void initTable(String tableName) {
        if (tableName.equalsIgnoreCase(Setting.TABLE_NAME)) {
            Log.i(LOG_TAG, "initializing database");
            insert(new Setting (DataSourceKeys.DEVICE_ID_KEY, DataSourceKeys.VALUE_EMPTY));
            insert(new Setting (DataSourceKeys.DEVICE_SECRET_KEY, DataSourceKeys.VALUE_EMPTY));
            insert(new Setting (DataSourceKeys.DEVICE_INSTANCE_KEY, DataSourceKeys.VALUE_EMPTY));
            insert(new Setting (DataSourceKeys.DEVICE_IS_SIGNED_UP_KEY, DataSourceKeys.VALUE_FALSE));
            insert(new Setting (DataSourceKeys.TOKEN_EXPIRES_AT_KEY, "0"));
            insert(new Setting (DataSourceKeys.TOKEN_LAST_TOKEN_KEY, DataSourceKeys.VALUE_EMPTY));
            insert(new Setting (DataSourceKeys.EBIDS_KEY, DataSourceKeys.VALUE_EMPTY));
            insert(new Setting (DataSourceKeys.LAST_SUCCESSFULL_KEYSTORE_KEY, DataSourceKeys.VALUE_EMPTY));
            insert(new Setting (DataSourceKeys.TELEMETRY_ID_KEY, DataSourceKeys.VALUE_EMPTY));
            insert(new Setting (DataSourceKeys.ACTIVE_EBID_KEY, DataSourceKeys.VALUE_EMPTY));
        }
    }

    public long getDbSize()
    {
        return dbFile.length();
    }

    /**
     * Will be called if the core has been decommissioned.
     * @return true if the database could be deleted.
     */
    public boolean deleteDatabase() {
        try {
            if (database != null) {
                database.close();
            }
            //noinspection ResultOfMethodCallIgnored
            dbFile.delete();
            return true;
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "deleteDatabase failed", ex);
            Telemetry.processException(ex);
            return false;
        }
    }


    @Override
    public void onCreate (SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private SQLiteDatabase openDatabase() {
        if (database==null || !database.isOpen() || database.isReadOnly()) {
            if (!SharedPrefUtils.contains(context, ProximityTracingService.class.getPackage().getName())) {   //only once at the first start
                SharedPrefUtils.put(context, ProximityTracingService.class.getPackage().getName(), UUID.randomUUID().toString());
            }
            database = SQLiteDatabase.openOrCreateDatabase(dbFile, SharedPrefUtils.get(context, ProximityTracingService.class.getPackage().getName()), null);
        }
        return database;
    }

    long replace(IMasterDataModel dataModel) {
        try {
            return openDatabase().replace(dataModel.getTableName(), null, dataModel.toContentValues());
        } catch (Exception ex) {
            Log.e(LOG_TAG, "replace failed for [" + dataModel.toString() + "] \n" + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    long insert(IMasterDataModel dataModel) {
        try {
            return openDatabase().insert(dataModel.getTableName(), null, dataModel.toContentValues());
        } catch (Exception ex) {
            Log.e(LOG_TAG, "insert failed for [" + dataModel.toString() + "] \n" + ex.getMessage(), ex);
            Telemetry.processException(ex);
            throw ex;
        }
    }

    @SuppressWarnings("SameParameterValue")
    Cursor rawQuery(String query, String[] selectionArgs) {
        return openDatabase().rawQuery(query, selectionArgs);
    }

    Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return openDatabase().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }
    void execSQL(String query)
    {
         openDatabase().execSQL(query);
    }

    void execute(String sql) {
        openDatabase().compileStatement(sql).execute();
    }

    public List<IMasterDataModel> getAll(IMasterDataModel d) {
        return cursorToList(d,  getByQuery(d.getTableName(), d.getAllColumns(), d.getDefaultOrder(), "", null, null));
    }

    List<IMasterDataModel> getAllWithLimit(IMasterDataModel d, Integer limit) {
         return cursorToList(d, getByQuery(d.getTableName(), d.getAllColumns(), d.getDefaultOrder(), "", null, limit));
    }

    static List<IMasterDataModel> cursorToList(IMasterDataModel d, Cursor cursor) {
        List<IMasterDataModel> items = new ArrayList<>();
        while(cursor.moveToNext()) {
            items.add(d.fromCursor(cursor));
        }
        cursor.close();
        return  items;
    }

    public int delete (String tableName, String where, String[] whereArgs) {
        return openDatabase().delete(tableName, where, whereArgs);
    }

    Cursor getByQuery(String tableName, String[] columns, String sortOrder, String where, String[] whereArgs, Integer limit) {
        // Filter results WHERE "title" = 'My Title'
        String selection = "";//FeedEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = null;//{ "My Title" };

        return openDatabase().query(
                tableName,   // The table to query
                columns,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,          // don't group the rows
                null,           // don't filter by row groups
                sortOrder,    // The sort order
                (limit!=null)?limit.toString():null //limit
        );
    }
}
