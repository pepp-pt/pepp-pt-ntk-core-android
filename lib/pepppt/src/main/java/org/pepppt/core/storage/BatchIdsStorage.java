package org.pepppt.core.storage;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.pepppt.core.util.Units;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * This is a list of EBIDs. It has an active EBID. It sort and remove
 * the existing EBIDs in the list.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class BatchIdsStorage {
    private static final String TAG = BatchIdsStorage.class.getSimpleName();
    private ArrayList<EBID> ids;
    private EBID activeEBID;
    private static final long eliminationPeriod = 3 * Units.MINUTES;

    public BatchIdsStorage() {
        ids = new ArrayList<>();
    }

    public ArrayList<EBID> getIds() {
        return ids;
    }

    /**
     * Gets the ebid at a specific position.
     *
     * @param index index.
     * @return The ebid at position index.
     */
    public EBID getAt(int index) {
        return ids.get(index);
    }

    /**
     * Returns true if the EBID is not set.
     *
     * @return Returns true if the EBID is not set.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean emptyEBID() {
        return activeEBID == null ||
                activeEBID.getId() == null ||
                activeEBID.getId().toString().isEmpty();
    }

    /**
     * Sets the active ebid
     *
     * @param newid The new active ebid
     */
    public void setActiveEbid(EBID newid) {
        Log.i(TAG, "set active ebid " + newid.toString());
        activeEBID = newid;
    }

    /**
     * Returns the active ebid.
     *
     * @return Returns the active ebid.
     */
    public EBID getActiveEBID() {
        return activeEBID;
    }


    /**
     * Removes all ebids that are expired and only keep
     * those that expire in 30 minutes or more.
     */
    public void removeExpiredIds() {
        final long now = System.currentTimeMillis();
        Iterator<EBID> it = ids.iterator();
        int beforecount = ids.size();
        while (it.hasNext()) {
            EBID id = it.next();
            if (id.getExpired() - now < eliminationPeriod) {
                it.remove();
            }
        }
        Log.i(TAG, beforecount - ids.size() + " expired ids found");
    }

    /**
     * Sort the list of ebids.
     */
    public void sort() {
        Collections.sort(ids, new EBID.EBIDComparator());
    }

    /**
     * Returns the count of the list.
     *
     * @return Returns the count of the list.
     */
    public int count() {
        return ids.size();
    }

    /**
     * Adds a new ebid to the list.
     *
     * @param id the new ebid.
     */
    public void add(EBID id) {
        ids.add(id);
    }

    /**
     * Consumes the first ebid in the list and
     * returns it.
     *
     * @return the first ebid in the list.
     */
    public EBID takeFirstId() {
        if (ids.isEmpty())
            return null;
        return ids.remove(0);
    }
}
