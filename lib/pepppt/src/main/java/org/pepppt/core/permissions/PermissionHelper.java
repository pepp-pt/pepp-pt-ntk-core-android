package org.pepppt.core.permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.content.ContextCompat;

import org.pepppt.core.telemetry.Telemetry;

import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PermissionHelper {

    /**
     * Returns a list of missing permissions to run the core-systems.
     *
     * @param context The context.
     * @return The list with missing permissions.
     */
    @NonNull
    public static ArrayList<String> checkAllPermissions(Context context) {
        ArrayList<String> insufficientpermissions = new ArrayList<>();
        if (checkPermissionFailed(context, Manifest.permission.BLUETOOTH))
            insufficientpermissions.add(Manifest.permission.BLUETOOTH);
        if (checkPermissionFailed(context, Manifest.permission.BLUETOOTH_ADMIN))
            insufficientpermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (checkPermissionFailed(context, Manifest.permission.ACCESS_COARSE_LOCATION))
            insufficientpermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        return insufficientpermissions;
    }

    private static boolean checkPermissionFailed(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

    @SuppressWarnings("CatchMayIgnoreException")
    public static List<String> getAllPermissions(final Context context, final String appPackage) {
        List<String> granted = new ArrayList<>();
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(appPackage, PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++) {
                if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    granted.add(pi.requestedPermissions[i]);
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            Telemetry.processException(ignored);
        }
        return granted;
    }

}
