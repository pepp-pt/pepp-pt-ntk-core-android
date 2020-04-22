package org.pepppt.core.ble.profiles;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AdvertisementProfile {
    // Fraunhofer IIS Company Identifier
    // https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/
    public static final int ManufacturerID = 0x08A9;

    // This payload can be used for auxiliary information
    public static final int ManufacturerData = 0x81;

    // This ID stores the payload-data
    public static final String ServiceDataUUIDString = "00008385-0000-1000-8000-00805F9B34FB";
}
