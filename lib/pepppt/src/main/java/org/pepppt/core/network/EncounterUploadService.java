package org.pepppt.core.network;

import android.accounts.NetworkErrorException;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pepppt.core.CoreCallback;
import org.pepppt.core.database.EncounterDatabaseHelper;
import org.pepppt.core.database.models.EncounterData;
import org.pepppt.core.events.CoreEvent;
import org.pepppt.core.events.EventArgs;
import org.pepppt.core.events.UploadEventArgs;
import org.pepppt.core.network.models.UploadDataFormat;
import org.pepppt.core.CoreContext;
import org.pepppt.core.telemetry.Telemetry;
import org.pepppt.core.util.NetworkChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EncounterUploadService extends IntentService {
    private static final String LOG_TAG = EncounterUploadService.class.getName();

    private Context context;

    public EncounterUploadService() {
        super(LOG_TAG);
    }

    public EncounterUploadService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        CoreCallback.callUI(CoreEvent.UPLOAD_STARTED, new EventArgs());
        try {
            long last_id = EncounterDatabaseHelper.getLastId();
            int currentProgress = 10;
            if (last_id > 0) {
                float totalCount = EncounterDatabaseHelper.getCountByLastId(last_id);
                if (totalCount > 0) {
                    CoreCallback.callUI(CoreEvent.UPLOAD_PROGRESS, new UploadEventArgs(currentProgress));

                    String rawTimeseriesId = getTimeseriesId();

                    currentProgress = 20;

                    CoreCallback.callUI(CoreEvent.UPLOAD_PROGRESS, new UploadEventArgs(currentProgress));

                    int failCounter = 0;
                    float succeededCounter = 0;

                    while (true) {
                        if (failCounter > 10)
                            throw new Exception("too many failed attempts");

                        checkNetwork(currentProgress);

                        //Get the Export data
                        List<EncounterData> listToExport = EncounterDatabaseHelper.getForExport(last_id);
                        if (listToExport.size() <= 0)
                            break;

                        try {
                            GenerateJsonResult generateJsonResult = GenerateJson(listToExport);
                            //the real Upload
                            String sendData_response = ApiRequest.sendEncounterData(generateJsonResult.theJsonObject, rawTimeseriesId);
                            //TODO: check responseString
                            if (sendData_response != null && sendData_response.equalsIgnoreCase("{}")) {
                                EncounterDatabaseHelper.deleteByIds(generateJsonResult.ids_to_delete);
                                succeededCounter = succeededCounter + generateJsonResult.ids_to_delete.size();
                            }
                            currentProgress = 20 + (Math.round(75 / totalCount * succeededCounter));
                            if (currentProgress > 95)
                                currentProgress = 95;
                            CoreCallback.callUI(CoreEvent.UPLOAD_PROGRESS, new UploadEventArgs(currentProgress));
                            failCounter = 0;
                        } catch (Exception ex) {
                            Telemetry.processException(ex);
                            failCounter++;
                            Log.e(LOG_TAG, ex.getMessage(), ex);
                        }
                    }
                    //start the evaluation process in backend
                    finishUpload(currentProgress);

                    CoreCallback.callUI(CoreEvent.UPLOAD_FINISHED, null);
                    return;
                }
            }
            CoreCallback.callUI(CoreEvent.UPLOAD_FAILED, new UploadEventArgs("no data"));
        } catch (Exception ex) {
            CoreCallback.callUI(CoreEvent.UPLOAD_FAILED, new UploadEventArgs(ex.getMessage()));
        }
    }

    private void checkNetwork(int currentProgress) throws Exception {
        int sleepCounter = 0;
        while (!NetworkChecker.isConnectedFast(context)) {
            Thread.sleep(300000); //300000ms = 5min
            CoreCallback.callUI(CoreEvent.UPLOAD_PAUSED, new UploadEventArgs(currentProgress, "no fast internet"));
            sleepCounter++;
            if (sleepCounter > 100) {
                throw new NetworkErrorException("no fast internet");
            }
        }
    }

    private void finishUpload(int currentProgress) throws Exception {
        int failCounter;
        failCounter = 0;
        while (true) {
            try {
                CoreCallback.callUI(CoreEvent.UPLOAD_PROGRESS, new UploadEventArgs(currentProgress));
                String finishResponse = ApiRequest.finishUploadEncounterData();
                if (finishResponse != null)// && finishResponse.equalsIgnoreCase("{}"))
                {   //no 403 Exception => success
                    break;
                }
            } catch (Exception ex) {
                Telemetry.processException(ex);
                CoreCallback.callUI(CoreEvent.UPLOAD_PAUSED, new UploadEventArgs(currentProgress, "Finishing not allow"));
                Thread.sleep(300000);
                failCounter++;
                if (failCounter > 10)
                    throw ex;
            }
        }
    }

    private String getTimeseriesId() throws IOException, JSONException, IllegalAccessException {
        JSONObject meAccount_response = ApiRequest.getMeAccount();
        String uploadAllowed = meAccount_response.optString("/uploadAllowed", "");
        if (uploadAllowed.length() > 0) {
            if (!uploadAllowed.equalsIgnoreCase("true")) {
                throw new IllegalAccessException("Upload not Allowed, tan not valid");
            }
        } else {
            throw new IllegalAccessException("Upload not Allowed, tan not valid");
        }
        return meAccount_response.getString("/dataTimeseriesId");
    }

    private GenerateJsonResult GenerateJson(List<EncounterData> listToExport) throws JSONException, PackageManager.NameNotFoundException, NetworkErrorException {
        GenerateJsonResult generateJsonResult = new GenerateJsonResult();
        generateJsonResult.ids_to_delete = new ArrayList<>();

        //GroupBy Timestamp
        HashMap<Long, List<EncounterData>> hashMap = new HashMap<>();
        for (EncounterData encounter_data : listToExport) {
            long key = encounter_data.getCreatedOn();
            if (!hashMap.containsKey(key)) {
                List<EncounterData> list = new ArrayList<>();
                list.add(encounter_data);
                hashMap.put(key, list);
            } else {
                hashMap.get(key).add(encounter_data);
            }
        }

        //Make the Json
        JSONObject mainObj = new JSONObject();

        UploadDataFormat uploadDataFormat = new UploadDataFormat();
        UploadDataFormat.Upload_Data upload_data = new UploadDataFormat.Upload_Data(System.currentTimeMillis());
        uploadDataFormat.setUploadData(upload_data);

        final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        String ebid = CoreContext.getInstance().getKeystoreRunnable().getStorage().getActiveEBID().getId().toString();
        if (ebid == null || ebid.length() <= 0) {
            throw new NetworkErrorException("no backend connection, no ebid");
        }
        //TODO: Header data format version
        upload_data.setHeaderDataFormatVersion("1");
        upload_data.setLocalBtCgaid(ebid);
        upload_data.setLocalTxGain(CoreContext.getInstance().getAdvertiserRunnables().getCurrentAdvertiserProfile().getAdvertisingTxPowerLevel());
        //TODO: Static rx-gain of local device
        upload_data.setLocalRxGain(0);
        upload_data.setAppPackageName(info.packageName);
        //App_Version in last loop
        upload_data.setPlatform("android");
        upload_data.setDeviceManufacturer(Build.MANUFACTURER);
        upload_data.setDeviceModel(Build.MODEL);
        upload_data.setOsVersion(Build.VERSION.RELEASE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            upload_data.setAndroidAppVc(info.getLongVersionCode() + "");
        } else {
            upload_data.setAndroidAppVc(info.versionCode + "");
        }
        upload_data.setAndroidOsCore(Build.VERSION.SDK_INT + "");
        upload_data.setAndroidOsInk(Build.VERSION.INCREMENTAL);
        upload_data.setAndroidOsId(Build.ID);
        upload_data.setAndroidOsHardware(Build.HARDWARE);
        upload_data.setAndroidOsBootloader(Build.BOOTLOADER);
        upload_data.setAndroidOsBoard(Build.BOARD);
        upload_data.setAndroidOsRadio(Build.getRadioVersion());


        List<UploadDataFormat.BluetoothScanList> bluetooth_scan_lists = new ArrayList<>();
        uploadDataFormat.setListOfBluetoothScanList(bluetooth_scan_lists);

        for (long key : hashMap.keySet()) {
            UploadDataFormat.BluetoothScanList bsl = new UploadDataFormat.BluetoothScanList();
            bluetooth_scan_lists.add(bsl);
            bsl.setTimestamp(key);
            //TODO: BLUETOOTH_SCAN_LIST data format version
            bsl.setDataFormatVersion("4");
            bsl.setAppVersion(info.versionName);
            //TODO: BT snapshot scann interval in 32 bit integer in hexadecimal format without leading zeros
            bsl.setScanningInterval(0);
            //TODO: BT snapshot aggregation interval in 32 bit integer in hexadecimal format without leading zeros
            bsl.setAggregationInterval(0);
            //TODO: Rx correction factor of device A during the current interval
            bsl.setRxCorrectionFactor(0);
            List<UploadDataFormat.BluetoothScan> bluetooth_scans = new ArrayList<>();
            bsl.setBluetoothScans(bluetooth_scans);

            for (EncounterData encounter_data : hashMap.get(key)) {
                upload_data.setAppVersion(encounter_data.getAppVersion());
                JSONObject jo_inner = new JSONObject(encounter_data.getJsonData());
                int meanRssi = jo_inner.getInt("meanRssi");
                int rssiVariance = jo_inner.getInt("rssiVariance");
                String senderId = jo_inner.getString("senderId");
                int txCorrectionValue = jo_inner.getInt("txCorrectionValue");
                int countOfObservation = jo_inner.getInt("countOfObservation");
                int meanTx = jo_inner.getInt("meanTx");

                UploadDataFormat.BluetoothScan bs = new UploadDataFormat.BluetoothScan();

                bs.setRemoteBtCgaid(senderId);
                //TODO: manufacturer_id from remote device -> NICOs Code
                bs.setRemoteManufacturerId("0");
                bs.setMeanRssi(meanRssi);
                bs.setRssiVariance(rssiVariance);
                bs.setCountOfObservations(countOfObservation);
                bs.setMeanTxLevel(meanTx);
                bs.setTxRssiCorrectionValue(txCorrectionValue);

                bluetooth_scans.add(bs);
                generateJsonResult.ids_to_delete.add(encounter_data.getId());
            }
        }
        //TODO: Object -> JSONObject
        Gson gson = new GsonBuilder().create();
        JSONObject raw_format = new JSONObject(gson.toJson(uploadDataFormat));
        JSONObject arago_format = new JSONObject();
        arago_format.put("value", raw_format);
        arago_format.put("timestamp", System.currentTimeMillis());
        JSONArray ja = new JSONArray();
        ja.put(arago_format);

        mainObj.put("items", ja);

        generateJsonResult.theJsonObject = mainObj;

        return generateJsonResult;
    }

    private static class GenerateJsonResult {
        List<Long> ids_to_delete;
        JSONObject theJsonObject;

    }
}
