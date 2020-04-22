package org.pepppt.core.database;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.database.models.IMasterDataModel;
import org.pepppt.core.CoreContext;
import org.pepppt.core.database.models.EncounterData;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.ListHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * HelperClass for managing the Encounter_Data in the database
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EncounterDatabaseHelper
{
    private static final String LOG_TAG = EncounterDatabaseHelper.class.getSimpleName();
    private final static int daysUntilExpiration = 21;

    /**
     * Inserts a aggregates data set.
     */
    public static void addAggregatedData(
            long startTime,
            String senderid,
            int rssiVariance,
            int meanRssi,
            int txCorrectionValue,
            int countOfObservation,
            int meanTx
    ) {
        if (CoreContext.getInstance().getDataSource() == null) {
            Log.e(LOG_TAG, "NO DATABASE");
            return;
        }
        EncounterData encdata = new EncounterData();
        String jsondata = new AggregationJson(
                startTime,
                senderid,
                rssiVariance,
                meanRssi,
                txCorrectionValue,
                countOfObservation,
                meanTx).createJson();
        encdata.setJsonData(jsondata);
        CoreContext.getInstance().getDataSource().insert(encdata);
    }

    public static List<EncounterData> getForExport (long lastId) {
        List<EncounterData> returnList = new ArrayList<>();

        EncounterData d = new EncounterData();
        List<IMasterDataModel> list = DataSource.cursorToList(d,  CoreContext.getInstance().getDataSource().getByQuery(d.getTableName(), d.getAllColumns(), d.getDefaultOrder(), EncounterData.COLUMN_Id + "<= ?", new String[]{lastId+""}, 1000));
        for (IMasterDataModel model: list) {
            EncounterData encounter_data = (EncounterData)model;
            returnList.add(encounter_data);
        }
        return  returnList;
    }

    public static long getFirstDate () {
        Cursor cursor= CoreContext.getInstance().getDataSource().rawQuery(
                "SELECT " + EncounterData.COLUMN_Created_On +
                        " FROM "+ EncounterData.TABLE_NAME +
                        " ORDER BY " + EncounterData.COLUMN_Created_On +
                        " ASC LIMIT 1;", null);
        try {
            cursor.moveToFirst();
            return cursor.getLong(0);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "getFirstDate failed", ex);
            Telemetry.processException(ex);
        } finally {
            cursor.close();
        }
        return -1;
    }

    public static long getLastDate () {
        EncounterData d = new EncounterData();

        Cursor cursor= CoreContext.getInstance().getDataSource().rawQuery(
                "SELECT "+ EncounterData.COLUMN_Created_On +
                        " FROM "+d.getTableName()+" ORDER BY "+ EncounterData.COLUMN_Created_On+" DESC LIMIT 1;", null);

        try {
            cursor.moveToFirst();
            return cursor.getLong(0);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "getLastDate_Of_Encounter_Data failed", ex);
            Telemetry.processException(ex);
        } finally {
            cursor.close();
        }
        return -1;
    }

    public static int getCountByLastId (long lastId) {
        EncounterData d = new EncounterData();

        Cursor cursor= CoreContext.getInstance().getDataSource().rawQuery("SELECT count(*) FROM "+d.getTableName()+" WHERE "+ EncounterData.COLUMN_Id+"<= " + lastId + ";", null);

        try {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "get_Count_By_Last_Id failed", ex);
            Telemetry.processException(ex);
        } finally {
            cursor.close();
        }
        return -1;
    }

    public static long getLastId () {
        EncounterData d = new EncounterData();
        String[] columns = new String[]{EncounterData.COLUMN_Id};
        Cursor cursor = CoreContext.getInstance().getDataSource().getByQuery(d.getTableName(), columns, EncounterData.COLUMN_Id + " DESC", "", null, 1);
        try {
            cursor.moveToFirst();
            return cursor.getLong(cursor.getColumnIndexOrThrow(EncounterData.COLUMN_Id));
        } catch (Exception ex) {
            Log.e(LOG_TAG, "get_Last_Id failed", ex);
            Telemetry.processException(ex);
        } finally {
            cursor.close();
        }
        return -1;
    }

    public static void deleteAll () {
        try {
            CoreContext.getInstance().getDataSource().execSQL("DELETE FROM " + EncounterData.TABLE_NAME + ";");
        } catch (Exception e) {
            Log.e(LOG_TAG, "delete_All failed", e);
            Telemetry.processException(e);
        }
    }

    public static void deleteByIds (List<Long> idsToDelete) {
        try {
            List<List<Long>> ids_to_delete_splits = ListHelper.splitIntoChunks(idsToDelete, 100);

            for (List<Long> ids_to_delete_split:ids_to_delete_splits) {
                CoreContext.getInstance().getDataSource().execSQL("DELETE FROM " + EncounterData.TABLE_NAME + " WHERE "+ EncounterData.COLUMN_Id+" IN ("+ TextUtils.join(", ", ids_to_delete_split)+");");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "delete_By_Ids failed", e);
            Telemetry.processException(e);
        }
    }

    public static boolean deleteExpiredRows () {
        try {
            Date cro = new Date(System.currentTimeMillis());
            Calendar c = Calendar.getInstance();
            c.setTime(cro);
            c.add(Calendar.DATE, 0 - daysUntilExpiration);
            long limit = c.getTimeInMillis();
            CoreContext.getInstance().getDataSource().execSQL("DELETE FROM " + EncounterData.TABLE_NAME + " WHERE " + EncounterData.COLUMN_Created_On + " < " + limit + ";");
            return  true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "delete_Expired_Rows failed", e);
            Telemetry.processException(e);
        }
        return false;
    }

    public static List<EncounterData> exportAll(Integer limit) {
        List<EncounterData> returnList = new ArrayList<>();

        List<IMasterDataModel> list = CoreContext.getInstance().getDataSource().getAllWithLimit(new EncounterData(), limit);
        for (IMasterDataModel model: list) {
            EncounterData encounter_data = (EncounterData)model;
            returnList.add(encounter_data);
        }
        return  returnList;
    }
}
