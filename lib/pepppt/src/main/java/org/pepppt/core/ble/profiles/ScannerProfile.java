package org.pepppt.core.ble.profiles;

import android.bluetooth.le.ScanSettings;

import org.pepppt.core.util.Units;

public class ScannerProfile {
    private static String TAG = ScannerProfile.class.getSimpleName();

    /**
     * Low power profile
     */
    public static ScannerProfile SCANNER_PROFILE_LOW = new ScannerProfile(
            ScanSettings.SCAN_MODE_LOW_POWER,
            15 * Units.SECONDS,
            8 * Units.SECONDS,
            10 * Units.SECONDS,
            10 * Units.MILLISECONDS,
            false,
            true);

    /**
     * Balanced profile.
     */
    @SuppressWarnings("WeakerAccess")
    public static ScannerProfile SCANNER_PROFILE_BALANCED = new ScannerProfile(
            ScanSettings.SCAN_MODE_BALANCED,
            30 * Units.SECONDS,
            25 * Units.SECONDS,
            10 * Units.SECONDS,
            10 * Units.MILLISECONDS,
            false,
            true);

    /**
     * Low latency profile. High battery consumption.
     */
    public static ScannerProfile SCANNER_PROFILE_LOWLATENCY = new ScannerProfile(
            ScanSettings.SCAN_MODE_LOW_LATENCY,
            60 * Units.SECONDS,
            58 * Units.SECONDS,
            10 * Units.SECONDS,
            10 * Units.MILLISECONDS,
            false,
            true);

    @SuppressWarnings("WeakerAccess")
    public static ScannerProfile SCANNER_PROFILE_1 = new ScannerProfile(
            ScanSettings.SCAN_MODE_LOW_POWER,
            60 * Units.SECONDS,
            58 * Units.SECONDS,
            10 * Units.SECONDS,
            10 * Units.MILLISECONDS,
            false,
            true);


    /**
     * Use this profile as a starting point.
     */
    public static ScannerProfile SCANNER_PROFILE_DEFAULT = SCANNER_PROFILE_1;

    /**
     * This is the current scanner profile that is set by default
     * parameters on start. Call setCurrentProfile() to switch
     * to a different profile.
     */
    private ScannerProfile currentProfile = SCANNER_PROFILE_BALANCED;

    // Restarts the scanner, when the BT is disabled.
    // This can happen when BT is turned off by the user
    // or our app. In this case the app will enable the
    // BT again. The scanner will be able to start the
    // scanning eventually.
    private int scannerRestartWhenBTDown;

    // Set the scan mode of scanning process. Higher rates of scanning
    // cost more battery life.
    private int scannerMode;

    // The length how long a scan should be active. Higher numbers
    // cost more battery life.
    private int scannerActiveTimeSpan;

    // The timespan between every san.
    private int scannerRate;

    private static int scannerRateWarningRange = 500 * Units.MILLISECONDS;

    // If set to true the scanner will buffer ignore
    // duplicate encounters during one scan.
    // keep this false.
    private boolean ignoreDuplicates;

    private boolean enable;

    private int reportDelay;

    public int getScannerRestartWhenBTDown() {
        return scannerRestartWhenBTDown;
    }

    public int getScannerMode() {
        return scannerMode;
    }

    public int getScannerActiveTimeSpan() {
        return scannerActiveTimeSpan;
    }

    public int getScannerRate() {
        return scannerRate;
    }

    public boolean getIgnoreDuplicates() {
        return ignoreDuplicates;
    }

    public boolean getEnable() {
        return enable;
    }

    public int getReportDelay() {
        return reportDelay;
    }

    public void setScannerRestartWhenBTDown(int scannerRestartWhenBTDown) {
        this.scannerRestartWhenBTDown = scannerRestartWhenBTDown;
    }

    public void setScannerMode(int scannerMode) {
        this.scannerMode = scannerMode;
    }

    public void setScannerActiveTimeSpan(int scannerActiveTimeSpan) {
        this.scannerActiveTimeSpan = scannerActiveTimeSpan;
    }

    public void setScannerRate(int scannerRate) {
        this.scannerRate = scannerRate;
    }

    public void setIgnoreDuplicates(boolean ignoreDuplicates) {
        this.ignoreDuplicates = ignoreDuplicates;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setReportDelay(int reportDelay) {
        this.reportDelay = reportDelay;
    }

    private ScannerProfile() { }

    /**
     * Creates a ScannerProfile with specific settings.
     * @param scannerMode              The mode.
     * @param scannerRate              The Rate.
     * @param scannerActiveTimeSpan    The timespan of the active scan.
     * @param scannerRestartWhenBTDown The retry timespan.
     * @param reportDelay              Report delay.
     * @param ignoreDuplicates         Keep this false.
     * @param enable                   True if the scanner should start immediatly. Can be
     *                                 enabled through Core.enableScanning().
     */
    @SuppressWarnings("WeakerAccess")
    public ScannerProfile(int scannerMode,
                          int scannerRate,
                          int scannerActiveTimeSpan,
                          int scannerRestartWhenBTDown,
                          int reportDelay,
                          boolean ignoreDuplicates,
                          boolean enable) {
        this.scannerMode = scannerMode;
        this.scannerRate = scannerRate;
        this.scannerActiveTimeSpan = scannerActiveTimeSpan;
        this.scannerRestartWhenBTDown = scannerRestartWhenBTDown;
        this.reportDelay = reportDelay;
        this.ignoreDuplicates = ignoreDuplicates;
        this.enable = enable;
    }
}
