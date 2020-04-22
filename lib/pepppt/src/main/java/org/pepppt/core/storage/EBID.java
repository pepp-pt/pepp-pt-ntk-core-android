package org.pepppt.core.storage;

import android.text.format.DateFormat;

import androidx.annotation.RestrictTo;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

/**
 * This is the EBID.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EBID {
    private UUID id;
    private long expired;

    public static class EBIDComparator implements Comparator<EBID> {
        @Override
        public int compare(EBID o1, EBID o2) {
            return Long.compare(o1.getExpired(), o2.getExpired());
        }
    }

    public UUID getId() {
        return id;
    }

    public long getExpired() {
        return expired;
    }

    public EBID(UUID id, long expires) {
        this.id = id;
        this.expired = expires;
    }

    @NotNull
    @Override
    public String toString() {
        return "id=" + id + ", expired=" + DateFormat.format("dd.MM.yyyy HH:mm:ss", new Date(expired)).toString();
    }
}
