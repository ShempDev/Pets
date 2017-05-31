package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by jeremy on 5/3/17.
 */

public class PetProvider extends ContentProvider {
    private PetsDbHelper mDbHelper;
    /** URI matcher constants for the pets table */
    private static final int PETS = 100; // entire table
    private static final int PET_ID = 101; //specific row
    //** UriMatcher for no match instances */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PetsDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        // Get readable database from DbHelper
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        //** Uri Matcher statements to verify query uri **/
        int match = sUriMatcher.match(uri);
       switch (match) {
            case PETS:
                // perform query on entire database
                cursor = database.query(uri.getLastPathSegment(), projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                // perform query on single row of database
                selection = PetContract.PetSchema._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(uri.getLastPathSegment(), projection, selection,
                         selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unkown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //** Uri Matcher statements to verify MIME type in the uri **/
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // return MIME type for table directory
                return PetContract.PetSchema.CONTENT_LIST_TYPE;
            case PET_ID:
                // return MIME type for single pet item
                return PetContract.PetSchema.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unkown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        //** Uri Matcher statements to verify insert uri **/
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS: // perform insert on entire database ONLY.
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Cannot query unkown uri: " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        //** Sanity checks: verify nonNull values in pet data **//
        Boolean sanityCheck = (values.getAsString(PetContract.PetSchema.COLUMN_NAME) != null) //name not null
                && (values.getAsString(PetContract.PetSchema.COLUMN_BREED) != null) //breed not null
                && (values.getAsInteger(PetContract.PetSchema.COLUMN_GENDER) >= 0) //gender 0,1 or 2
                && (values.getAsInteger(PetContract.PetSchema.COLUMN_GENDER) <= 2)
                && (values.getAsInteger(PetContract.PetSchema.COLUMN_WEIGHT) >= 0); //weight is positive
        if (!sanityCheck) { // A sanity check failed above.
            throw new IllegalArgumentException("Invalid pet data entered.");
        }
        // Get writable database from DbHelper
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert data values
        long newRowId = database.insert(uri.getLastPathSegment(), null, values);
        if (newRowId > 0) { //update was successful. Notify application of change.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // return uri with row _ID appended
        return Uri.withAppendedPath(uri, Long.toString(newRowId));

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database from DbHelper
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //** Uri Matcher statements to verify query uri **/
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // perform delete based on SQL query on entire database.
                return deletePet(uri, selection, selectionArgs);

            case PET_ID:
                // Perform delete base on specified row _ID.
                // perform update on single, specified row of database.
                selection = PetContract.PetSchema._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return deletePet(uri, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Cannot query unkown uri: " + uri);
        }
    }

    private int deletePet(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database from DbHelper
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int result = database.delete(uri.getPathSegments().get(0), selection, selectionArgs);
        if (result > 0) { //update was successful. Notify application of change.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        //** Uri Matcher statements to verify query uri **/
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // perform update based on SQL query on entire database.
                return updatePet(uri, values, selection, selectionArgs);

            case PET_ID:
                Log.i("INFO", uri.getPathSegments().get(0));
                // perform update on single, specified row of database.
                selection = PetContract.PetSchema._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Cannot query unkown uri: " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Get writable database from DbHelper
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int result = database.update(uri.getPathSegments().get(0), values, selection, selectionArgs);
        if (result > 0) { //update was successful. Notify application of change.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }
}
