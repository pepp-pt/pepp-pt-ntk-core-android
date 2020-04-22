package org.pepppt.core.ble;

import android.bluetooth.le.AdvertiseData;
import android.os.ParcelUuid;

import androidx.annotation.RestrictTo;

import org.pepppt.core.ble.profiles.AdvertisementProfile;
import org.pepppt.core.CoreContext;
import org.pepppt.core.util.UuidHelper;

import java.util.UUID;

/**
 * Helper class to construct the advertise data.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class AdvertiserDataHelper {
    static AdvertiseData createData() {
        final AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        final ParcelUuid serviceid = ParcelUuid.fromString(AdvertisementProfile.ServiceDataUUIDString);
        final byte[] bytesMan = new byte[]{1};
        dataBuilder.addManufacturerData(AdvertisementProfile.ManufacturerID, bytesMan);
        String ebid = CoreContext.getInstance().getKeystoreRunnable().getStorage().getActiveEBID().getId().toString();
        final UUID uuid = UUID.fromString(ebid);
        final byte[] bytesData = UuidHelper.getBytesFromUUID(uuid);
        dataBuilder.addServiceData(serviceid, bytesData);
        dataBuilder.setIncludeTxPowerLevel(true);
        return dataBuilder.build();
    }
}
