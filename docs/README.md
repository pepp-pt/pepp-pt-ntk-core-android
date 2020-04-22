<img src="../pepp-pt.png" width="200">

# PEPP-PT core library

## How to get started

1.  Download the latest library from this project.
2.  Create an empty android project
3.  Add the library to your dependencies
4.  Create your application class

```
public class UIApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
}
```


5.  Add the following code to your application class onCreate():

```
    proximityservice = new ProximityTracingService();
    proximityservice.setCallback(new MyCallback());
    proximityservice.create(getContext());
    proximityservice.setApiEndpoints(endpoints);
    proximityservice.setClientIdAndSecret(api_id, api_secret);
    proximityservice.setCertificatePinnerInformation(host, certfp);
    proximityservice.setScannerProfile(ScannerProfile.SCANNER_PROFILE_LOWLATENCY);
    proximityservice.setAdvertiserProfile(AdvertiserProfile.ADVERTISIER_PROFILE_HIGHPOWER);
    proximityservice.setForegroundServiceNotification(new ServiceNotification(
            getString(R.string.appchannelid),
            getString(R.string.appchannelname),
            getString(R.string.appchanneldesc)).createNotification(this,
            getString(R.string.service_notification_title),
            getString(R.string.service_notification_text),
            R.mipmap.ic_launcher));
    proximityservice.startForegroundService();
    proximityservice.enableKeepBTOn(false);
    proximityservice.start();

```

6. Your callback will be called by the core:

```
class MyCallback extends CoreCallback {
    @Override
    public void onEvent(CoreEvent event, EventArgs args) {
        super.onEvent(event, args);
        Log.i(TAG, event.toString());

        switch (event) {
            case INSUFFICIENT_PERMISSIONS:
                handleInsufficientPermissions(args);
                break;
            case UPLOAD_STARTED:
                Log.i(TAG, "Great! The upload has begun.");
                break;
                .
                .
                .
            default:
                break;
        }
    }
```

7.  In order to communicate with backend you need to sign up with your firebase token:

```
    ProximityTracingService.signUpUser(firebaseToken);
```
A basic way of getting the token is explained in the following snippet.
Note: You can only retrieve the token after a successful Firebase login.
``` 
FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
mUser.getIdToken(true)
    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
        public void onComplete(@NonNull Task<GetTokenResult> task) {
            if (task.isSuccessful()) {
                String idToken = task.getResult().getToken();
                // Send token to the core
                UIApplication.getApplication().getProximityService().signUpUser(firebaseToken);
            } else {
                // Handle error -> task.getException();
            }
        }
    });
```
See also the [Verify ID Tokens](https://firebase.google.com/docs/auth/admin/verify-id-tokens "Firebase Docs") section of 
the Google Firebase docs. <br />
The core will notify your application about the registration result via the following 
events: 

- **REGISTRATION_FAILED**
- **REGISTRATION_SUCCESS**

After that the core will retrieve the neccessary *BTCGAIDs* from the keyserver
and start scanning and advertising. Scanned advertisements from other phones
with the same app will be stored in the core database.

## Core Events

As described in step 6 of 'How to get started', the core will inform the hosting
application about certain events triggered by severeal things that can occur via the CoreCallback
function. The hosting application can react and perform nescessary steps. 



<table class="tg">
    <tr>
        <th>Name</th>
        <th>Description</th>
        <th>EventArgs</th>
    </tr>
    <tr>
        <td>BLUETOOTH_HAS_BEEN_TURNED_OFF</td>
        <td>This event will be send if bluetooth has been disabled by the user or some other instance.</td>
        <td>None</td>
    </tr>
    <tr>
        <td>BLUETOOTH_HAS_BEEN_TURNED_ON</td>
        <td>This event will be send if bluetooth has been enabled by the user or some other instance.</td>
        <td>None</td>
    </tr>
    <tr>
        <td>BLUETOOTH_HAS_BEEN_REENABLED</td>
        <td>This event can occur when the user switches off the bluetooth. The core will automatically turn it on again and then sends this event. The ui should then generate a notification that will inform the user about this automatic enabling and can provide further instructions and reasons why bluetooth is neccessary for the app when the user clicks on the notification.
        </td>
        <td>None</td>
    </tr>
    <tr>
        <td>INSUFFICIENT_PERMISSIONS</td>
        <td>This event will be send to the ui in case of missing permissions. The ui should then generate a notification. Clicking on the notification should provide informations on why he app needs these permissions.
        </td>
        <td>MissingPermissionsEventArgs</td>
    </tr>
    <tr>
        <td>MISSING_FEATURE_BLE</td>
        <td>This event will be send to the ui in case of missing permissions. The ui should then generate a notification. Clicking on the notification should provide informations on why the app needs these permissions.</td>
        <td>MissingPermissionsEventArgs</td>
    </tr>
    <tr>
        <td class="tg-0pky">APP_IS_NOT_IGNORING_BATTERY_OPTIMIZATION</td>
        <td class="tg-0pky">This event will be send if the app is not ignoring battery optimization. The app should then disable the battery optimization by its own.
        </td>
        <td class="tg-0pky">None</td>
    </tr>
    <tr>
        <td class="tg-0pky">ROTATE_ID</td>
        <td class="tg-0pky">This event will be send by the core when a new broadcast id has been set by the core. This will happen every 30min.</td>
        <td class="tg-0pky">None</td>
    </tr>
    <tr>
        <td class="tg-0pky">NEW_BATCH</td>
        <td class="tg-0pky">This event will be send by the core when it successfully receives a new batch of ids from the keyserver. This will happen every 24h</td>
        <td class="tg-0pky">None</td>
    </tr>
    <tr>
        <td class="tg-0pky">CORE_IS_READY</td>
        <td class="tg-0pky">Will be send after Core.create(). Signals that the core is running.</td>
        <td class="tg-0pky">None</td>
    </tr>
    <tr>
        <td class="tg-0pky">CORE_SERVICE_CREATED</td>
        <td class="tg-0pky">Will be send after Core.create(). Signals that the core is running.</td>
        <td class="tg-0pky">None</td>
    </tr>
    <tr>
        <td class="tg-0pky">UPLOAD_STARTED</td>
        <td class="tg-0pky">Will be triggered if data upload started</td>
        <td class="tg-0pky">None</td>
    </tr>
    <tr>
        <td class="tg-0pky">UPLOAD_PROGRESS</td>
        <td class="tg-0pky">Will be triggered to state the progress of the current data upload</td>
        <td class="tg-0pky">UploadEventArgs: The current progress in percent.</td>
    </tr>
    <tr>
        <td class="tg-0pky">UPLOAD_FAILED</td>
        <td class="tg-0pky">Will be triggered if the current data upload failed.</td>
        <td class="tg-0pky">UploadEventArgs: The failed Message.</td>
    </tr>
    <tr>
        <td class="tg-0pky">UPLOAD_FINISHED</td>
        <td class="tg-0pky">Will be triggered if the current data upload has finished.</td>
        <td class="tg-0pky">none</td>
    </tr>
    <tr>
        <td class="tg-0pky">UPLOAD_PAUSED</td>
        <td class="tg-0pky">Will be triggered if the current data upload is paused.</td>
        <td class="tg-0pky">none</td>
    </tr>
    <tr>
        <td class="tg-0pky">REGISTRATION_SUCCESS</td>
        <td class="tg-0pky">Indicates that the device registration was successful</td>
        <td class="tg-0pky">none</td>
    </tr>
    <tr>
        <td class="tg-0pky">REGISTRATION_FAILED</td>
        <td class="tg-0pky">Fired if the device registration has failed.</td>
        <td class="tg-0pky">RegistrationEvents: type = error, message = the reason why</td>
    </tr>
    <tr>
        <td class="tg-0pky">AUTHENTIFICATION_FAILED</td>
        <td class="tg-0pky">Is triggerd when the device is unable to receive a new token</td></td>
        <td class="tg-0pky">AuthentificationEvent: error: the error itself as String</td>
    </tr>    

</table>


