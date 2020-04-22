# pepp-pt core

## v0.2.17-beta
* removed debug telemetry implementation

## v0.2.15-beta
* CoreProfile has beend renamed to AdvertisementProfile and moved to ble
* DeviceModelData added
* Safe strings in the DeviceModelData
* Telemetry and exception collection add to all scheduler runnables
* Exception collector refined in Telemetry
* fix: out of bounds fix for the exception collector
* TelemetryRunnable added; setEnableTelemetry added to the ProximityTracingService
* incrementCall stripped
* Telemetry is now a static class; sendException() send the data to a server
* Metrics moved to root package => will be used for other metrics beside ble as well
* renamed BTCGAID to EBID
* fix: received id uuid fix
* ebid list will be stored in the database and will be used again after restart
* active ebid is now stored in the database and will be used after the restart
* ebid usage is now synchronized with the expiration time of each ebid
* preparations for a server time correction integrated
* SCrypt added
* annotations added
* ProximityTracingService.setServerTimeouts added

## v0.2.13-beta
* Deactivating fb analytics collection
* change Data Format Version: 0.9.3
* fix: sender id and receiver id
* receiver id removed from aggregation runnable
* fix: android-os-board
* added CORE_IS_STALLED, CORE_RESUMED_FROM_STALL
* fix: TAN expiry date
* Add CheckMessagesRunnable every 12Hours
* fix: datasource accesses coresystems too early

## v0.2.12-beta
* tan integration
* airplane mode will be handled now while keeping bluetooth on
* doc folder renamed to docs
* set SCAN_MODE_BALANCED for SCANNER_PROFILE_1
* integration of the Fraunhofer Aggregator
* add Tan Activation
* Add lastToken to DB init
* Keep alive improvements integrated from fabrice-gagneux
* decommission feature added
* BTCGAID must have exactly 16 bytes
* ComputeThread and ApiRequestThread has been replaced by LooperThread
* TanViewModel integrated into the core
* Core is now produces less log output
* received ids that are not exaclty 16 bytes long will now be logged
* live data for the tan added
* change data format version 0.9.2
* new aggregated data will be stored in the db
* raw live results no longer supported -> aggregation is the only mode now
* default scan mode set to ScanSettings.SCAN_MODE_LOW_POWER
* moved unittests
* removed unnecessary logging from runnables
* add int txCorrectionValue, int countOfObservation, int meanTx

## v0.2.4-beta
* moved ComputationThread to package org.pepppt.core.threads
* add ApiRequestThread for core web requests
* move callUI into the Callback class
* user callback will be called by the ui thread now
* reorganized the handlers and moved them to the runnables
* moved the current BTCGAID to the rotation manager
* renamed rotation manager
* implemented next profile mechanism

