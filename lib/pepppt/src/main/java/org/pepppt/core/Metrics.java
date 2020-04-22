package org.pepppt.core;

import org.pepppt.core.util.EventSeries;
import org.pepppt.core.util.Units;

import java.util.ArrayList;

/**
 * This class is a container for the measurements taken
 * in the core.
 */
public class Metrics {

    /**
     * The number of scans done by the ble scanner.
     * <p>
     * The corresponding KPI is "scancnt"
     */
    public int NumberOfScans;

    /**
     * The number of advertisement received from nearby devices. This includes
     * all advertisements with the correct AdvertisementProfile.ManufacturerID.
     * Included are also ads with a wrong ServiceUUID. see NumberOfEncounters.
     * <p>
     * The corresponding KPI is "adsrxcnt"
     */
    public int NumberOfAdvertisementsReceived;


    /**
     * The time of the last advertisement.
     * <p>
     * No KPI.
     */
    public long LastAdvertisementTime;


    /**
     * The number of received advertisements in the last scan.
     * Included are also ads with a wrong ServiceUUID. see NumberOfEncounters.
     * <p>
     * The corresponding KPI is "adsrxcntls"
     */
    public int NumberOfAdvertisementsReceivedInLastScan;

    /**
     * The number of broadcasted ads.
     * <p>
     * The corresponding KPI is "adstxcnt"
     */
    public int NumberOfAdvertisementsBroadcasted;

    /**
     * The number of failed advertisements. When the advertisement is
     * send to the bluetooth chip there can be numerous errors. This
     * number is overall counted number.
     * <p>
     * The corresponding KPI is "nofa"
     */
    public int NumberOfFailedAdvertisements;

    /**
     * The number of failed advertisements with error ADVERTISE_FAILED_ALREADY_STARTED.
     * <p>
     * The corresponding KPI is "nofaas"
     */
    public int NumberOfFailedAdvertisementsAlreadyStarted;

    /**
     * The number of failed advertisements with error ADVERTISE_FAILED_DATA_TOO_LARGE.
     * <p>
     * The corresponding KPI is "nofadtl"
     */
    public int NumberOfFailedAdvertisementsDataTooLarge;

    /**
     * The number of failed advertisements with error ADVERTISE_FAILED_TOO_MANY_ADVERTISERS.
     * <p>
     * The corresponding KPI is "nofatma"
     */
    public int NumberOfFailedAdvertisementsTooManyAdvertisers;

    /**
     * The number of failed advertisements with error ADVERTISE_FAILED_INTERNAL_ERROR.
     * <p>
     * The corresponding KPI is "nofaie"
     */
    public int NumberOfFailedAdvertisementsInternalError;

    /**
     * The number of failed advertisements with error ADVERTISE_FAILED_FEATURE_UNSUPPORTED.
     * <p>
     * The corresponding KPI is "nofafu"
     */
    public int NumberOfFailedAdvertisementsFeatureUnsupported;

    /**
     * The number of failed advertisements with an unknown error.
     * <p>
     * The corresponding KPI is "nofau"
     */
    public int NumberOfFailedAdvertisementsUnknown;

    /**
     * Number of advertisements with correct ServiceUUID and the correct length
     * of 16 bytes.
     * <p>
     * The corresponding KPI is "encs"
     */
    public int NumberOfEncounters;

    /**
     * A string with last encounter. Looks like this:
     * "[ebid-last-encounter] ([rssi-value])"
     * <p>
     * No KPI.
     */
    public String LastEncounterString;

    /**
     * Time of the last encounter.
     * <p>
     * The KPI "lencdt" will hold the time since the last encounter in minutes.
     */
    public long LastEncounterTime;

    /**
     * True if the scan filtering is enabled. Should alway be true.
     * <p>
     * No KPI.
     */
    public boolean FilteringEnabled;

    /**
     * Time of the last refresh of the ebid list.
     * <p>
     * The KPI "lastbatch" will hold the time since the last batch refresh in minutes.
     */
    public long LastBatchRefresh;

    /**
     * The time when the app started.
     * <p>
     * The corresponding KPI is "encs"
     */
    private long started;

    /**
     * The number of advertisements transmitted in the last 60 seconds.
     * <p>
     * The corresponding KPI is "adstxl1m"
     */
    private int adsTransmittedLast60Seconds;

    /**
     * The number of advertisements transmitted in the last 2 hours.
     * <p>
     * The corresponding KPI is "adstxl2h"
     */
    private int adsTransmittedLast2Hours;

    /**
     * The number of advertisements transmitted in the last 4 hours.
     * <p>
     * The corresponding KPI is "adstxl4h"
     */
    private int adsTransmittedLast4Hours;

    /**
     * The number of advertisements transmitted in the last 8 hours.
     * <p>
     * The corresponding KPI is "adstxl8h"
     */
    private int adsTransmittedLast8Hours;

    /**
     * The number of calls to the aggregator class from the Fraunhofer IIS.
     * The aggregator will be called every 1 minute by default.
     * <p>
     * The corresponding KPI is "aggcnt"
     */
    public long AggregationsCount;

    /**
     * The number of overall results received by the aggregator.process.
     * <p>
     * The corresponding KPI is "aggrcnt"
     */
    public long AggregatedResultsCount;

    /**
     * The number of overall senders aggregated.
     * <p>
     * The corresponding KPI is "aggdtcnt"
     */
    public long AggregatedDataCount;

    /**
     * The number of advertisements transmitted in the last 60 seconds.
     * <p>
     * The corresponding KPI is "adsrxl1m"
     */
    private int adsReceivedLast60Seconds;

    /**
     * The number of advertisements transmitted in the last 2 hours.
     * <p>
     * The corresponding KPI is "adsrxl2h"
     */
    private int adsReceivedLast2Hours;

    /**
     * The number of advertisements transmitted in the last 4 hours.
     * <p>
     * The corresponding KPI is "adsrxl4h"
     */
    private int adsReceivedLast4Hours;

    /**
     * The number of advertisements transmitted in the last 8 hours.
     * <p>
     * The corresponding KPI is "adsrxl8h"
     */
    private int adsReceivedLast8Hours;

    /**
     * The number of rotations in the last 24 hours. This number should not be higher
     * then 48.
     * <p>
     * The corresponding KPI is "ebidspd"
     */
    private int ebidsRotatedLast24Hours;

    /**
     * The number of ebids left in the ebid list. If this number reaches 0 the core
     * will stall and wont process nor broadcast any more.
     * <p>
     * The corresponding KPI is "ebidsib"
     */
    public int EbidsLeftInTheBatch;

    /**
     * The max time in seconds of the stalled core since start of the app.
     * <p>
     * The corresponding KPI is "mts"
     */
    public long MaxTimeStall;


    /**
     * Is 1 if offloaded scan batching is supported
     * <p>
     * The corresponding KPI is "oscbs"
     */
    public long isOffloadedScanBatchingSupported;

    /**
     * Is 1 if offloaded filters are supported
     * <p>
     * The corresponding KPI is "ofs"
     */
    public long isOffloadedFilteringSupported;

    /**
     * Is 1 if the multi advertisement is supported by the chipset
     * <p>
     * The corresponding KPI is "mas"
     */
    public long isMultipleAdvertisementSupported;


    /**
     * Is 1 if bluetooth le is supported.
     * <p>
     * The corresponding KPI is "bles"
     */
    public long isBluetoothLeSupported;


    /**
     * Is 1 if battery optimization is turned on.
     * <p>
     * The corresponding KPI is "bo"
     */
    public long isBatterOptimizationTurnedOn;


    /**
     * The ebid series.
     */
    private EventSeries EbidsSeries;
    private EventSeries AdsTransmittedSeries60s;
    private EventSeries AdsTransmittedSeries2h;
    private EventSeries AdsTransmittedSeries4h;
    private EventSeries AdsTransmittedSeries8h;
    private EventSeries AdsReceivedSeries60s;
    private EventSeries AdsReceivedSeries2h;
    private EventSeries AdsReceivedSeries4h;
    private EventSeries AdsReceivedSeries8h;
    private ArrayList<String> uniqueEncounter = new ArrayList<>();

    Metrics() {
        started = System.currentTimeMillis();
        AdsTransmittedSeries60s = new EventSeries();
        AdsTransmittedSeries2h = new EventSeries();
        AdsTransmittedSeries4h = new EventSeries();
        AdsTransmittedSeries8h = new EventSeries();
        AdsReceivedSeries60s = new EventSeries();
        AdsReceivedSeries2h = new EventSeries();
        AdsReceivedSeries4h = new EventSeries();
        AdsReceivedSeries8h = new EventSeries();
        EbidsSeries = new EventSeries();
    }

    public long getStart() {
        return started;
    }

    /**
     * The uptime in seconds.
     *
     * @return Returns the uptime in seconds.
     */
    public long getSecondsSinceStart() {
        return (System.currentTimeMillis() - started) / Units.SECONDS;
    }

    public int getUniqueIdsCount() {
        return uniqueEncounter.size();
    }

    public void saveUniqueEncounter(String transmittedId) {
        if (!uniqueEncounter.contains(transmittedId)) {
            uniqueEncounter.add(transmittedId);
        }
    }

    public void addAdTransmitted() {
        AdsTransmittedSeries60s.push(System.currentTimeMillis());
        AdsTransmittedSeries2h.push(System.currentTimeMillis());
        AdsTransmittedSeries4h.push(System.currentTimeMillis());
        AdsTransmittedSeries8h.push(System.currentTimeMillis());
    }

    public void addAdReceived() {
        AdsReceivedSeries60s.push(System.currentTimeMillis());
        AdsReceivedSeries2h.push(System.currentTimeMillis());
        AdsReceivedSeries4h.push(System.currentTimeMillis());
        AdsReceivedSeries8h.push(System.currentTimeMillis());
    }

    public void addEbidRotation() {
        EbidsSeries.push(System.currentTimeMillis());
    }

    public int getAdsTransmittedLast60Seconds() {
        updateAdsTransmittedLast60Seconds();
        return adsTransmittedLast60Seconds;
    }

    public int getAdsTransmittedLast2Hours() {
        updateAdsTransmittedLast2Hours();
        return adsTransmittedLast2Hours;
    }

    public int getAdsTransmittedLast4Hours() {
        updateAdsTransmittedLast4Hours();
        return adsTransmittedLast4Hours;
    }

    public int getAdsTransmittedLast8Hours() {
        updateAdsTransmittedLast8Hours();
        return adsTransmittedLast8Hours;
    }


    public int getAdsReceivedLast60Seconds() {
        updateAdsReceivedLast60Seconds();
        return adsReceivedLast60Seconds;
    }

    public int getAdsReceivedLast2Hours() {
        updateAdsReceivedLast2Hours();
        return adsReceivedLast2Hours;
    }

    public int getAdsReceivedLast4Hours() {
        updateAdsReceivedLast4Hours();
        return adsReceivedLast4Hours;
    }

    public int getAdsReceivedLast8Hours() {
        updateAdsReceivedLast8Hours();
        return adsReceivedLast8Hours;
    }


    public int getRotatedEbidLast24Hours() {
        updateRotatedEbidLast24Hours();
        return ebidsRotatedLast24Hours;
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    private void updateAdsTransmittedLast60Seconds() {
        long now = System.currentTimeMillis();
        adsTransmittedLast60Seconds = AdsTransmittedSeries60s.removeOlderThen(now - 1 * Units.MINUTES);
    }

    private void updateAdsTransmittedLast2Hours() {
        long now = System.currentTimeMillis();
        adsTransmittedLast2Hours = AdsTransmittedSeries2h.removeOlderThen(now - 2 * Units.HOURS);
    }

    private void updateAdsTransmittedLast4Hours() {
        long now = System.currentTimeMillis();
        adsTransmittedLast4Hours = AdsTransmittedSeries4h.removeOlderThen(now - 4 * Units.HOURS);
    }

    private void updateAdsTransmittedLast8Hours() {
        long now = System.currentTimeMillis();
        adsTransmittedLast8Hours = AdsTransmittedSeries8h.removeOlderThen(now - 8 * Units.HOURS);
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    private void updateAdsReceivedLast60Seconds() {
        long now = System.currentTimeMillis();
        adsReceivedLast60Seconds = AdsReceivedSeries60s.removeOlderThen(now - 1 * Units.MINUTES);
    }

    private void updateAdsReceivedLast2Hours() {
        long now = System.currentTimeMillis();
        adsReceivedLast2Hours = AdsReceivedSeries2h.removeOlderThen(now - 2 * Units.HOURS);
    }

    private void updateAdsReceivedLast4Hours() {
        long now = System.currentTimeMillis();
        adsReceivedLast4Hours = AdsReceivedSeries4h.removeOlderThen(now - 4 * Units.HOURS);
    }

    private void updateAdsReceivedLast8Hours() {
        long now = System.currentTimeMillis();
        adsReceivedLast8Hours = AdsReceivedSeries8h.removeOlderThen(now - 8 * Units.HOURS);
    }

    private void updateRotatedEbidLast24Hours() {
        long now = System.currentTimeMillis();
        ebidsRotatedLast24Hours = EbidsSeries.removeOlderThen(now - 24 * Units.HOURS);
    }

}
