package org.pepppt.core.events;

/**
 * Events will be send to the callback of the ui.
 */
public enum CoreEvent {

    //////////////////////////////////////////////////////////////////////////
    // Bluetooth
    //////////////////////////////////////////////////////////////////////////

    /**
     * This event will be send if bluetooth has been disabled by the user or some other
     * instance.
     */
    BLUETOOTH_HAS_BEEN_TURNED_OFF,

    /**
     * This event can occur when the user switches off the bluetooth. The core will
     * automatically turn it on again and then sends this event. The ui should then generate
     * a notification that will inform the user about this automatic enabling and can
     * provide further instructions and reasons why bluetooth is neccessary for the app
     * when the user clicks on the notification.
     * args is null.
     */
    BLUETOOTH_HAS_BEEN_TURNED_ON,

    //////////////////////////////////////////////////////////////////////////
    // Permissions and features
    //////////////////////////////////////////////////////////////////////////

    /**
     * This event will be send to the ui in case of missing permissions. The ui should then
     * generate a notification. Clicking on the notification should provide informations on why
     * the app needs these permissions.
     * args: MissingPermissionsEventArgs.
     */
    INSUFFICIENT_PERMISSIONS,

    /**
     * This event will be send if the core cant find the bluetooth le feature on the device.
     * Are you running in the emulator?
     * args is null.
     */
    MISSING_FEATURE_BLE,

    /**
     * This event will be send if the app is not ignoring battery optimization. The app should
     * then disable the battery optimization by its own.
     * args is null.
     */
    APP_HAS_BATTERY_OPTIMIZATION_ENABLED,

    //////////////////////////////////////////////////////////////////////////
    // IDs
    //////////////////////////////////////////////////////////////////////////

    /**
     * This event will be send by the core when a new broadcast id has been set by the core.
     * args is null. This will happen every 30min.
     */
    NEW_EBID,

    /**
     * This event will be send by the core when it successfully receives a new batch of
     * ids from the keyserver. This will happen every 24h
     * args is null.
     */
    NEW_EBID_LIST,

    //////////////////////////////////////////////////////////////////////////
    // Core
    //////////////////////////////////////////////////////////////////////////

    /**
     * Will be send after Core.create(). Signals that the core is running.
     * args is null.
     */
    CORE_IS_READY,

    /**
     * Will be send if the core has been stalled.
     * This can happen when the batch of EBID is empty.
     * Use this event to show an status icon in your ui that the core is currently not able to
     * scan or broadcast any data. When the phone has no coverage for some time the internal
     * EBIDs can run out. After this time the core needs coverage again to retrieve a new
     * list of EBIDs.
     */
    CORE_IS_STALLED,

    /**
     * This event will be send if the core can proceed with its work after a
     * stall (CORE_IS_STALLED). This happens if the core received a new batch of EBIDs
     * from the backend. Use this event to show a ready/healty icon in your ui that the
     * app is running fine.
     */
    CORE_RESUMED_FROM_STALL,

    /**
     * This event will be send if the upload can start.
     */
    TAN_ACTIVATION_SUCCEDED,

    /**
     * These events will be send during the upload of the use.
     */

    //////////////////////////////////////////////////////////////////////////
    // Upload
    //////////////////////////////////////////////////////////////////////////

    UPLOAD_STARTED,
    UPLOAD_PROGRESS,        // args: UploadEventArgs: The current progress in percent.
    UPLOAD_FAILED,          // args: UploadEventArgs: The failed Message
    UPLOAD_FINISHED,
    UPLOAD_PAUSED,          // args: UploadEventArgs: The current progress in percent.

    //////////////////////////////////////////////////////////////////////////
    // Registration
    //////////////////////////////////////////////////////////////////////////

    /**
     * Events for initial device registration at the backend
     */
    REGISTRATION_SUCCESS,
    REGISTRATION_FAILED,

    //////////////////////////////////////////////////////////////////////////
    // TAN
    //////////////////////////////////////////////////////////////////////////

    /**
     * Will be send to the ui callback after calling ProximityTracingService.requestTan()
     * args: NewTanEventArgs
     */
    NEW_TAN,

    /**
     * Error while requesting the tan.
     */
    TAN_REQUEST_FAILED,

    /**
     * Error while activating the tan.
     */
    TAN_ACTIVATION_FAILED,

    //////////////////////////////////////////////////////////////////////////
    // Messaging
    //////////////////////////////////////////////////////////////////////////

    NEW_MESSAGES_ARRIVED,
    CHECK_MESSAGES_FAILED,
    NO_NEW_MESSAGES,

    LOAD_ALL_MESSAGES_SUCCESSFULLY,
    LOAD_ALL_MESSAGES_FAILED,

    MESSAGE_CONFIRMED,
    CONFIRM_MESSAGES_FAILED,

    LABTEST_RESULT_AVAILABLE,
    LABTEST_REQUEST_FAILED
}
