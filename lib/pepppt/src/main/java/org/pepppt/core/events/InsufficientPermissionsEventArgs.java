package org.pepppt.core.events;

import java.util.ArrayList;

/**
 * Events for the INSUFFICIENT_PERMISSIONS event.
 */
public class InsufficientPermissionsEventArgs extends EventArgs {
    private ArrayList<String> missingPermissions;

    public InsufficientPermissionsEventArgs(ArrayList<String> missingPermissions) {
        this.missingPermissions = missingPermissions;
    }

    public ArrayList<String> getMissingPermissions() {
        return missingPermissions;
    }
}
