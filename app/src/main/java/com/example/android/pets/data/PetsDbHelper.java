package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetSchema;


/**
 * Created by jeremy on 4/19/17.
 */

public class PetsDbHelper extends SQLiteOpenHelper {
    // Defined constants for the database.
    private final static int DATABASE_VERSION = 1;
    private final static String DATABASE_NAME = "shelter.db";

    public PetsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + PetSchema.TABLE_NAME +
                        "(" + PetSchema._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PetSchema.COLUMN_NAME + " TEXT NOT NULL," +
                        PetSchema.COLUMN_BREED + " TEXT," +
                        PetSchema.COLUMN_GENDER + " INTEGER NOT NULL," +
                        PetSchema.COLUMN_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
