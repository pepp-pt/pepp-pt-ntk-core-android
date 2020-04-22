package org.pepppt.core.database.models;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.RestrictTo;

import org.jetbrains.annotations.NotNull;

/**
 * Inteface for DataModels
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface IMasterDataModel {

    String getTableName();

    ContentValues toContentValues();

    @NotNull
    String toString();

    String[] getAllColumns();

    IMasterDataModel fromCursor(Cursor cursor);

    String getDefaultOrder();

}
