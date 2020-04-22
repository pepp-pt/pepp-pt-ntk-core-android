# Known Issues for PEPP-PT-sample-android and PEPP-PT-core-android


[Known Issues](KNOWN-ISSUES.md)


## Scanning stopped over night
App has been installed and registered correctly. Phone has been put to standby mode and left untouched over night (7 hours). The next morning the app showed a significantly low number count for sent/received signals. All numbers besides activity counter were frozen. According to the numbers shown, the app must have stopped signal operation after less than half an hour.
Normal operation could be restored after cycling Bluetooth.
Phone model: Samsung A5 (SM-A510F)
Android version: 7.0

## No proximity signals registered
Initially the PEPP-PT NTK sample worked as expected. After a reinstall, there are no proximity signals counted after many hours of operation. The results seem inconclusive, since other devices receive hundreds of signals in the same time frame.
Android version: 7.1.1

#### Mitigation:
Restarting the app should be fixed. However, on some devices it seems as if the app does not restart.
This is an ongoing task.
Note: development was carried out from a team of remotely working developers, mostly working from Corona Home offices with limited access to physical devices.

## App turns on bluetooth without notification
Once user decides to switch off bluetooth while the app is running, the app reenables Bluetooth without any notification. The app should encourage the user to turn it back on himself by displaying a warning.

#### Mitigation:
The PEPP-PT core SDK now offers a control for enabling/disabling the Bluetooth activities. In addition, flight-mode is detected to make sure BLuetooth is kept off.
The app should not enable the user without notitication or hint in the UI.
Ongoing improvement in the PEPP-PT app.

## debug/test/release build settings still non-optimal
build settings should allow to eliminate all logging and other test/debug functionality from the final build.

#### Mitigation
Need to improve build configuration.


## check device for Google Play services APK before accessing Google Play services
Apps that rely on the Play Services SDK should always check the device for a compatible Google Play services APK before accessing Google Play services features. It is recommended to do this in two places: in the main activity's onCreate() method, and in its onResume() method. The check in onCreate() ensures that the app can't be used without a successful check. The check in onResume() ensures that if the user returns to the running app through some other means, such as through the back button, the check is still performed.
If the device doesn't have a compatible version of Google Play services, your app can call GoogleApiAvailability.makeGooglePlayServicesAvailable() to allow users to download Google Play services from the Play Store.

#### Mitigation:
Google Play Services are used for Firebase Messaging only.
Issue is fixed for now. If there is no Google Play Services available, the app will
show an unescapeable screen until the problem is fixed.
If the google play services were just deactivated, the user can easily activate them again.
If the google play services arent available at all, the user should get assistet to install them.

## [BSI] Issue with re-booting device
After re-starting a device with the app installed, there appears to be a functional problem:
First: The counters displayed by the app, showing time, advertisements sent and received, encounters, are reset to zero. This implies that the data, which was collected before, might also be lost. (This could not yet be verified in the database).

#### Comment:
These numbers in the UI are never stored; they serve for showing activity only. All proximity findings are stored in the local encrypted sqllite database.

## [BSI] Issue with re-booting device
Second: The app seems only to start working (counting) again, after activating it. So a user has to manually select the app again, after a re-boot. (This might be a security feature, but does not support an intuitive handling).

#### Mitigation
App shall restart when rebooting device. This needs to be fixed.

## [BSI] Issue with re-booting device
Third: If the app restarts after a re-boot, but the device is kept offline (i.e. without internet connection), the app does not start sending or receiving advertisements until the internet connection is re-established. This just seems a bit unusual from a user perspective. And a short information about this would probably help for a better acceptance.

#### Mitigation
The application needs to refresh the EBID pool; however, obviously the number of available EBIDs was not checked correctly.
Should be fixed already.
Note: The application does not need an Internet connection, except when refreshing the EBID pool for the Bluetooth LE advertisements and services.


## [BSI] App crashes by calling com.facebook.CustomTabActivity
Calling the com.facebook.CustomTabActivity (exported=true) leads to an app crash.
This may be exploited by other applications and can lead to a DoS-Attack.

#### Mitigation:
All occurences of any facebook libraries have been removed. Cleanup was required. Facebook must never be used.
Google Firebase Analytics, Crashlytics, Realtime Database, Firestore, Cloud Storage were removed from app depedencies.
CLOSED.

## [BSI] energy safety mode deactivates bluetooth broadcasts
As soon as the device enters energy safety mode, bluetooth broadcast stops. This will drastically reduce the effect of proximity tracing.

#### Mitigation
PEPP-PT service is kept alive, and by adjustung battery optimization (needs Android permission!). On some devices, service still faces sleep modes.
Ongoing improvements.


## [BSI] High battery drain due to high frequency bluetooth broadcasts
The current frequency of sending bluetooth broadcasts leads to quite a lot of battery drain. This might result in a loss of user acceptance. The frequency should be reconsidered to avoid deinstallation due to high battery drain.

This could indeed be a serious issue since user acceptance is key. Can you quantify what you observed with "quite a lot of battery drain"? This could also be useful in trying to specify what is an acceptable level of battery drain.

#### Mitigation:
The actual energy consumption is measured using cut-open phones; the observations were made when the app still used high power, low latency, max. settings for BLE.
Meanwhile, settings have bee lowered.

## [BSI] SQL-Injection vulnerability 
Java-Class database.DataSource.java contains code which is vulnerable to sql injection. Even though it is not easy to reach from outside the application, it is recommended to implement input validation and restructure database calls in a secure manner.

#### Mitigation
Ongoing improvements. Partially fixed.


## [BSI] unencrypted secrets disclosed in xml files
Amongst other things the device_secret is stored unencrypted in a xml file in shared_prefs. Just like client_id and client_secret it is used to authenticate to the backend. It is strongy adviced to NOT store ANY secrets unencrypted in java files.

#### Mitigation:
device_secret and other confidential data is now stored in encrypted app sqlite database. client_id and client_secret are now stored in sample/app/src/main/assets/app.properties

Plus, client_id and client_secret are considered *public* because these are required for one API endpoint only. Only after registration, the actual access credentials are created and stored by the client in the encrypted sqlite database of the application.

It's not obvious for a pen tester that the client_id and client_secret keys are non-private.

#### [BSI] comment:
It is still worth to mention that this is not according to security best practices. So in my opinion this issue retains its right to be present with a status of "best practice violation".


## [BSI] Some devices do not collect data
Some Test-Devices are not able to collect data from other broadcasting devices (just sending works). These devices are in particular: Huawei P10 Lite with Android 8.0.0 and Huawei P20lite (ANE-LX1) with Android 8.0.0.
This post on stack overflow (https://stackoverflow.com/questions/51203419/bluetooth-low-energy-ble-devices-connection-issue-with-huawei-phone)  mentions poor BLE stability on some Huawei phones.

#### Mitigation:
Test real devices, which were not available in Corona Home Offices yet.


## [BSI] No State of the Art Certificate Pinning
Certificate Pinning based on okHttp3 is documented and declared as bypassable. Therefore, it is no longer State of Art.
https://medium.com/@hojat.sajadinia/bypass-okhttp-certificatepinner-on-android-9a45ad80a58b

#### Mitigation
By using a xml file that contains the reuqired pins. To allow dynamic exchange a pin-expiration time mus be provided as well. 
Current status: sha256 fingerprint values and host read from secrects.xml file.
pin-expiration time is not implemented yet.


## [BSI] Missing Tapjacking Protection
A developer can create an app that tricks users into tapping a specifically-crafted app popup window (called toast view), making it a gateway for varied threats.
The malicious application presents an unreal user interface in order to obtain user events for a hidden action in the background. When protection is not used inside an exported activity another application is able to redirect touch events to the exported activity without the users consent.
Recommendation:
The flag android:filterTouchesWhenObscured should be set to true.

#### Mitigation:
Must set android:filterTouchesWhenObscured to true.


## [BSI] App is marked debuggle (reminder for release)
An Android app can be marked as debuggable in the Android manifest. This is automatically set in apks built as debug version. The flag is helpful when developing
the application but should not be used in the release version of the app as
it impacts security. Private data of the app can be leaked to an adversary as the
data directory of the app can be accessed by anyone and not only the app itself.
Decompilation of the app is unproblematic as no function or symbol name is
obfuscated. 
#### Recommendation
In the release version, the debuggable flag must not be set.

## [BSI] Quality of Code
Source Code quality not ready for release. Several indications exist, that several features may be incomplete or that the code was not professionally written and audited.
e.g. String comparison using the == operator, incomplete/missing exception handling, unused functions.

#### Mitigation
Ongoing improvement. Finding from early versions.

## [BSI] ECB based encryption
Usage of ECB-based encryption is against best practices in IT security. But so far this could not be exploited.

#### Mitigation
Keep issue open until decided if scheme must be replaced.


## [BSI] Used library versions slightly outdated 
Used Library Versions Slightly Outdated. The following libraries are used in the PEPPPT core:
Library with used version Status
* com.squareup.okhttp3:mockwebserver:4.4.0 newest is 4.5.0
* junit:junit:4.12 newest is 4.13
* androidx.sqlite:sqlite:2.0.1 newest is 2.1.0
* com.google.code.gson:gson:2.8.6 latest
* androidx.appcompat:appcompat:1.1.0 latest
* androidx.work:workruntime: 2.3.4 latest
* androidx.test.ext:junit:1.1.1 latest
* androidx.test.espresso:espressocore: 3.2.0 latest
* net.zetetic:androiddatabasesqlcipher: 4.3.0@aar latest


PEPPPT Sample App:
* Library with used version Status
* androidx.sqlite:sqlite:2.0.1 newest is 2.1.0 
* junit:junit:4.12 newest is 4.13
* com.google.code.gson:gson:2.8.6 latest
* com.google.android.material:material:1.1.0 latest
* androidx.appcompat:appcompat:1.1.0 latest
* androidx.constraintlayout:constraintlayout:1.1.3 latest
* com.android.volley:volley:1.1.1 latest
* androidx.test.ext:junit:1.1.1 latest
* androidx.test.espresso:espressocore: 3.2.0 latest
* net.zetetic:androiddatabasesqlcipher: 4.3.0@aar latest


#### Mitigation
- Added to app's manifest: <meta-data android:name="firebase_analytics_collection_deactivated" android:value="true" />
- okhttp was updated to 4.5.0
- update and eliminate unnecessary inclusions


# Closed Issues

## [BSI] Arbitrary data as BTCGAID accepted
App accepts arbitrary data as BTCGAID transmitted in Bluetooth LE Advertisements:
BLE advertisments are identified by a constant service UUID, therefore the app accepts all data which is transmitted under this Service UUID und interprets it as valid BTCGAID. The app cannot fully verfiy if received BTCGAIDs are valid (as they are encrypted), but it can at least verify the length of data (Full validation must be perfomed by backend). By changing the transmitter ID randomly, it might be possible to flood the database on the receiving side.

#### Closed:
Only data with exactly 16 bytes will be accepted now.
Its difficult to differentiate a possible attack - sending many BTCGAIDs with different data - from a place with many phones nearby (running the the app).
Fraunhofer Institute may need to investigate what sideeffects this attack may have on the aggregation system that was developed.
However, it is part of the analysis later in the backend to filter out all bogus EBIDs that where collected by the app.


## [BSI] hard coded serialnumber
In core/util/SharedPrefUtil an App Serialnumber (1337) is hard coded. All keys for all installed "PEPP-PT Sample" app will have the same serialnumber. Additionally the used serialnumber is the sample serialnumber from the Android docs
(https://developer.android.com/reference/android/security/KeyPairGeneratorSpec.Builder)

#### Closed:
changed Serialnumber from 1337 to Math.abs(ALIAS.hashCode())


## [BSI] Password temporarily stored as string
In io.fabric.sdk.android.services.network.SystemKeyStore the keystore password is temporarily stored as string. It is not recommended to store passwords as strings in Java.
https://github.com/Anonymous-App/HappyFresh.Android/blob/master/io/fabric/sdk/android/services/network/SystemKeyStore.java

#### Closed:
removed in Class org.pepppt.core.database.DataSource


## [BSI] Logfiles are to verbose
Logfiles are to verbose and disclose sensitive data. (e.g. all generated bluetooth ids, "add database entry" commands with all data in plain text)

#### Mitigtion (closed):
Removed most of the logging in the corelib to a minimum.

## [BSI] Bluetooth deactivation bug
Bluetooth can be disabled and now does not turn on again automatically. When disabled the "advertisements sent" counter stops counting, but the "Your scanner is active" info stays green. Also the notification "Proximity Sensor aktiv" stays active.

#### Mitigation:
Ongoing. UI needs to be improved. 
Bluetooth activation / deactivation and handling of flight mode was already addressed and fixed.


## [BSI] Secure Random 
Even though SecureRandom is used to generate random numbers, this does not enforce the random generator to really create secure random numbers. SecureRandom.getInstanceStrong() should be used instead.

#### Closed:
The affected class was removed.

