package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by jeremy on 4/18/17.
 */

public final class PetContract {

    /* Non-instantiable class */
    private PetContract() {}

    /*
     Inner class to define the schema for the pets table entries.
     */
    public final class PetSchema implements BaseColumns {
        // define our constants for the table
        public static final String TABLE_NAME = "pets";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BREED =  "breed";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_WEIGHT = "weight";

        // define constant values for gender entries.
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}