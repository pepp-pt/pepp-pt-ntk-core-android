package org.pepppt.core.network.models;

import androidx.annotation.RestrictTo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UploadDataFormat {
    @SerializedName("header")
    private Upload_Data uploadData;
    @SerializedName("bt-scans")
    private List<BluetoothScanList> listOfBluetoothScanList;

    public Upload_Data getUploadData() {
        return uploadData;
    }

    public void setUploadData(Upload_Data uploadData) {
        this.uploadData = uploadData;
    }

    public List<BluetoothScanList> getListOfBluetoothScanList() {
        return listOfBluetoothScanList;
    }

    public void setListOfBluetoothScanList(List<BluetoothScanList> listOfBluetoothScanList) {
        this.listOfBluetoothScanList = listOfBluetoothScanList;
    }


    @SuppressWarnings("unused")
    public static class Upload_Data {
        public Upload_Data(long timestamp) {
            this.timestamp = timestamp;
        }

        @SerializedName("dt")
        private long timestamp;
        @SerializedName("ver")
        private String headerDataFormatVersion;
        @SerializedName("id")
        private String localBtCgaid;
        @SerializedName("tx-gain")
        private int localTxGain;
        @SerializedName("rx-gain")
        private int localRxGain;
        @SerializedName("app-pn")
        private String appPackageName;
        @SerializedName("app-version")
        private String appVersion;
        @SerializedName("platform")
        private String platform;
        @SerializedName("device-manufacturer")
        private String deviceManufacturer;
        @SerializedName("device-model")
        private String deviceModel;
        @SerializedName("os-version")
        private String osVersion;

        @SerializedName("android-app-vc")
        private String androidAppVc;

        @SerializedName("android-os-core")
        private String androidOsCore;

        @SerializedName("android-os-ink")
        private String androidOsInk;

        @SerializedName("android-os-id")
        private String androidOsId;

        @SerializedName("android-os-hardware")
        private String androidOsHardware;

        @SerializedName("android-os-bootloader")
        private String androidOsBootloader;

        @SerializedName("android-os-board")
        private String androidOsBoard;

        @SerializedName("android-os-radio")
        private String androidOsRadio;

        @SerializedName("android-os-root")
        private String androidOsRoot;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getHeaderDataFormatVersion() {
            return headerDataFormatVersion;
        }

        public void setHeaderDataFormatVersion(String headerDataFormatVersion) {
            this.headerDataFormatVersion = headerDataFormatVersion;
        }

        public String getLocalBtCgaid() {
            return localBtCgaid;
        }

        public void setLocalBtCgaid(String localBtCgaid) {
            this.localBtCgaid = localBtCgaid;
        }

        public int getLocalTxGain() {
            return localTxGain;
        }

        public void setLocalTxGain(int localTxGain) {
            this.localTxGain = localTxGain;
        }

        public int getLocalRxGain() {
            return localRxGain;
        }

        public void setLocalRxGain(int localRxGain) {
            this.localRxGain = localRxGain;
        }

        public String getAppPackageName() {
            return appPackageName;
        }

        public void setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getDeviceManufacturer() {
            return deviceManufacturer;
        }

        public void setDeviceManufacturer(String deviceManufacturer) {
            this.deviceManufacturer = deviceManufacturer;
        }

        public String getDeviceModel() {
            return deviceModel;
        }

        public void setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getAndroidAppVc() {
            return androidAppVc;
        }

        public void setAndroidAppVc(String androidAppVc) {
            this.androidAppVc = androidAppVc;
        }

        public String getAndroidOsCore() {
            return androidOsCore;
        }

        public void setAndroidOsCore(String androidOsCore) {
            this.androidOsCore = androidOsCore;
        }

        public String getAndroidOsInk() {
            return androidOsInk;
        }

        public void setAndroidOsInk(String androidOsInk) {
            this.androidOsInk = androidOsInk;
        }

        public String getAndroidOsId() {
            return androidOsId;
        }

        public void setAndroidOsId(String androidOsId) {
            this.androidOsId = androidOsId;
        }

        public String getAndroidOsHardware() {
            return androidOsHardware;
        }

        public void setAndroidOsHardware(String androidOsHardware) {
            this.androidOsHardware = androidOsHardware;
        }

        public String getAndroidOsBootloader() {
            return androidOsBootloader;
        }

        public void setAndroidOsBootloader(String androidOsBootloader) {
            this.androidOsBootloader = androidOsBootloader;
        }

        public String getAndroidOsBoard() {
            return androidOsBoard;
        }

        public void setAndroidOsBoard(String androidOsboard) {
            this.androidOsBoard = androidOsboard;
        }

        public String getAndroidOsRadio() {
            return androidOsRadio;
        }

        public void setAndroidOsRadio(String androidOsRadio) {
            this.androidOsRadio = androidOsRadio;
        }

        public String getAndroidOsRoot() {
            return androidOsRoot;
        }

        public void setAndroidOsRoot(String androidOsRoot) {
            this.androidOsRoot = androidOsRoot;
        }
    }

    @SuppressWarnings("unused")
    public static class BluetoothScanList {
        public BluetoothScanList() {

        }

        @SerializedName("dt")
        private long timestamp;
        @SerializedName("v")
        private String dataFormatVersion;
        @SerializedName("av")
        private String appVersion;
        @SerializedName("sci")
        private long scanningInterval;
        @SerializedName("agi")
        private long aggregationInterval;
        @SerializedName("rxc")
        private long rxCorrectionFactor;
        @SerializedName("bts")
        private List<BluetoothScan> bluetoothScans;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getDataFormatVersion() {
            return dataFormatVersion;
        }

        public void setDataFormatVersion(String dataFormatVersion) {
            this.dataFormatVersion = dataFormatVersion;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public long getAggregationInterval() {
            return aggregationInterval;
        }

        public void setAggregationInterval(long aggregationInterval) {
            this.aggregationInterval = aggregationInterval;
        }

        public List<BluetoothScan> getBluetoothScans() {
            return bluetoothScans;
        }

        public void setBluetoothScans(List<BluetoothScan> bluetoothScans) {
            this.bluetoothScans = bluetoothScans;
        }

        public long getScanningInterval() {
            return scanningInterval;
        }

        public void setScanningInterval(long scanningInterval) {
            this.scanningInterval = scanningInterval;
        }

        public long getRxCorrectionFactor() {
            return rxCorrectionFactor;
        }

        public void setRxCorrectionFactor(long rxCorrectionFactor) {
            this.rxCorrectionFactor = rxCorrectionFactor;
        }
    }

    @SuppressWarnings("unused")
    public static class BluetoothScan {
        @SerializedName("id")
        private String remoteBtCgaid;
        @SerializedName("mf")
        private String remoteManufacturerId;

        @SerializedName("rm")
        private int meanRssi;
        @SerializedName("rv")
        private int rssiVariance;
        @SerializedName("ct")
        private int countOfObservations;
        @SerializedName("tp")
        private int meanTxLevel;

        public int getMeanRssi() {
            return meanRssi;
        }

        public void setMeanRssi(int meanRssi) {
            this.meanRssi = meanRssi;
        }

        public int getRssiVariance() {
            return rssiVariance;
        }

        public void setRssiVariance(int rssiVariance) {
            this.rssiVariance = rssiVariance;
        }

        public int getCountOfObservations() {
            return countOfObservations;
        }

        public void setCountOfObservations(int countOfObservations) {
            this.countOfObservations = countOfObservations;
        }

        public int getMeanTxLevel() {
            return meanTxLevel;
        }

        public void setMeanTxLevel(int meanTxLevel) {
            this.meanTxLevel = meanTxLevel;
        }

        public long getTxRssiCorrectionValue() {
            return txRssiCorrectionValue;
        }

        public void setTxRssiCorrectionValue(long txRssiCorrectionValue) {
            this.txRssiCorrectionValue = txRssiCorrectionValue;
        }

        @SerializedName("tc")
        private long txRssiCorrectionValue;

        public String getRemoteBtCgaid() {
            return remoteBtCgaid;
        }

        public void setRemoteBtCgaid(String remoteBtCgaid) {
            this.remoteBtCgaid = remoteBtCgaid;
        }

        public String getRemoteManufacturerId() {
            return remoteManufacturerId;
        }

        public void setRemoteManufacturerId(String remoteManufacturerId) {
            this.remoteManufacturerId = remoteManufacturerId;
        }
    }
}
