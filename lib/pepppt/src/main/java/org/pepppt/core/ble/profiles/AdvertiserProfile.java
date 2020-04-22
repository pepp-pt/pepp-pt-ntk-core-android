package org.pepppt.core.ble.profiles;
import android.bluetooth.le.AdvertiseSettings;

import org.pepppt.core.CoreContext;
import org.pepppt.core.util.Units;

@SuppressWarnings("unused")
public class AdvertiserProfile {
    private static String TAG = AdvertiserProfile.class.getSimpleName();

    public static AdvertiserProfile ADVERTISER_PROFILE_LOWPOWER = new AdvertiserProfile(
            6000 * Units.MILLISECONDS,
            5000 * Units.MILLISECONDS,
            AdvertiseSettings.ADVERTISE_MODE_LOW_POWER,
            AdvertiseSettings.ADVERTISE_TX_POWER_LOW,
            true);

    public static AdvertiserProfile ADVERTISER_PROFILE_BALANCED = new AdvertiserProfile(
            3000 * Units.MILLISECONDS,
            2500 * Units.MILLISECONDS,
            AdvertiseSettings.ADVERTISE_MODE_BALANCED,
            AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM,
            true);

    public static AdvertiserProfile ADVERTISER_PROFILE_HIGHPOWER = new AdvertiserProfile(
            500 * Units.MILLISECONDS,
            400 * Units.MILLISECONDS,
            AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY,
            AdvertiseSettings.ADVERTISE_TX_POWER_HIGH,
            true);

    @SuppressWarnings("WeakerAccess")
    public static AdvertiserProfile ADVERTISER_PROFILE_1 = new AdvertiserProfile(
            500 * Units.MILLISECONDS,
            400 * Units.MILLISECONDS,
            AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY,
            AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM,
            true);

    public static AdvertiserProfile ADVERTISER_PROFILE_DEFAULT = ADVERTISER_PROFILE_1;

    /**
     * The timespan between every advertising.
     */
    private int advertisingRate;

    /**
     * The active timespan of one advertising
     */
    private int advertisingActiveTimespan;

    /**
     * Defines the mode of the advertising. Higher modes cost more
     * battery-life.
     */
    private int advertisingMode;

    /**
     * Defines the PowerLevel of the transmit. Higher modes cost more battery-life.
     */
    private int advertisingTxPowerLevel;

    /**
     * Enables/disables the advertising.
     */
    private boolean enable;

    public int getAdvertisingRate() {
        return advertisingRate;
    }

    @SuppressWarnings("WeakerAccess")
    public int getAdvertisingActiveTimespan() {
        return advertisingActiveTimespan;
    }

    @SuppressWarnings("WeakerAccess")
    public int getAdvertisingMode() {
        return advertisingMode;
    }

    public int getAdvertisingTxPowerLevel() {
        return advertisingTxPowerLevel;
    }

    public boolean getEnable() {
        return enable;
    }

    public void setAdvertisingRate(int advertisingRate) {
        this.advertisingRate = advertisingRate;
    }

    public void setAdvertisingActiveTimespan(int advertisingActiveTimespan) {
        this.advertisingActiveTimespan = advertisingActiveTimespan;
    }

    public void setAdvertisingMode(int advertisingMode) {
        this.advertisingMode = advertisingMode;
    }

    public void setAdvertisingTxPowerLevel(int advertisingTxPowerLevel) {
        this.advertisingTxPowerLevel = advertisingTxPowerLevel;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    private AdvertiserProfile() { }

    @SuppressWarnings("WeakerAccess")
    public AdvertiserProfile(int advertisingRate,
                             int advertisingActiveTimespan,
                             int advertisingMode,
                             int advertisingTxPowerLevel,
                             boolean enable) {
        this.advertisingRate = advertisingRate;
        this.advertisingActiveTimespan = advertisingActiveTimespan;
        this.advertisingMode = advertisingMode;
        this.advertisingTxPowerLevel = advertisingTxPowerLevel;
        this.enable = enable;
    }


    public static AdvertiseSettings createSettingsFromCurrentProfile() {
        AdvertiserProfile ap = CoreContext.getInstance().getAdvertiserRunnables().getCurrentAdvertiserProfile();
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(ap.getAdvertisingMode());
        builder.setConnectable(false);
        builder.setTimeout(ap.getAdvertisingActiveTimespan());
        builder.setTxPowerLevel(ap.getAdvertisingTxPowerLevel());
        return builder.build();
    }
}
