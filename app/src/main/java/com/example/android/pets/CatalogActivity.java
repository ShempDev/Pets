/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetsDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {
    private PetsDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        // Initialize the database helper.
        mDbHelper = new PetsDbHelper(this);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Setup and input Value Key pairs to test update/delete.
        ContentValues values = new ContentValues();
        values.put(PetContract.PetSchema.COLUMN_NAME, "Scotia"); //change name
        values.put(PetContract.PetSchema.COLUMN_BREED, "Scottish Terrier");
        String selection = PetContract.PetSchema.COLUMN_NAME + "=? AND " + "breed=?";
        String [] selectionArgs = {"Terry", "Terrier"};
        Uri uri = PetContract.PetSchema.CONTENT_URI;
        uri = ContentUris.withAppendedId(uri, 8);
        getContentResolver().update(uri, values, selection, selectionArgs);

        displayDatabaseInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PetContract.PetSchema._ID,
                PetContract.PetSchema.COLUMN_NAME,
                PetContract.PetSchema.COLUMN_BREED,
                PetContract.PetSchema.COLUMN_GENDER,
                PetContract.PetSchema.COLUMN_WEIGHT
                };
        // Filter results WHERE pet weight less than 30 kg.
        // String selection = PetContract.PetSchema.COLUMN_WEIGHT + " <= 30";
        // String[] selectionArgs = { "< 30" };

        // Perform this SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = getContentResolver().query(
                PetContract.PetSchema.CONTENT_URI,  //table to query
                projection,  //columns to include
                null,  //WHERE clause columns
                null, //WHERE clause value
                null  //no filters or sort options
        );
        // Lets build a string from our cursor data
        String cursorString = "_ID   NAME   BREED   GENDER   WEIGHT\n===   ====   =====   ======   =====";

        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount()
                    + "\n\n" + cursorString);
            // Display each row of pet data in the cursor
            while (cursor.moveToNext()) {
                cursorString = cursor.getInt(0) + " | " + cursor.getString(1)
                        + " | " + cursor.getString(2) + " | " + cursor.getInt(3)
                        + " | " + cursor.getInt(4) + "Kg";
                displayView.append("\n" + cursorString);
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    private void insertPet() {
        // Setup and input Value Key pairs for the pet data.
        ContentValues values = new ContentValues();
        values.put(PetContract.PetSchema.COLUMN_NAME, "Terry");
        values.put(PetContract.PetSchema.COLUMN_BREED, "Terrier");
        values.put(PetContract.PetSchema.COLUMN_GENDER, PetContract.PetSchema.GENDER_FEMALE);
        values.put(PetContract.PetSchema.COLUMN_WEIGHT, 11);
        // Call content provider insert method.
        Uri uri = getContentResolver().insert(PetContract.PetSchema.CONTENT_URI, values);
        int newRowId = Integer.parseInt(uri.getLastPathSegment());
        if (uri == null || newRowId == -1) { // Something went wrong
            Toast.makeText(this, getString(R.string.error_adding_pet), Toast.LENGTH_SHORT).show();
        } else { // No problems.
            Toast.makeText(this, getString(R.string.new_pet_added) + newRowId, Toast.LENGTH_SHORT).show();
        }
            displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Insert dummy data for a new pet into database.
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
