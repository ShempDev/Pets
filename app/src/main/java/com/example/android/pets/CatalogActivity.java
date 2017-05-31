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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetsDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private PetsDbHelper mDbHelper;
    private PetsCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        // Initialize the database helper.
        mDbHelper = new PetsDbHelper(this);

        // Setup FAB to open EditorActivity in insert mode.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Get the listview and set the cursorAdapter on the view.
        ListView listView = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view); // get our empty view
        listView.setEmptyView(emptyView); // display empty view if listView is empty.
        mAdapter = new PetsCursorAdapter(this, null); // empty adapter
        listView.setAdapter(mAdapter);

        // Setup a click listener for the listview items to open EditorActivity in edit mode.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                // Send uri with item _ID selected to the EditorActivity.
                Uri uri = ContentUris.withAppendedId(PetContract.PetSchema.CONTENT_URI, id);
                intent.setData(uri);
                //intent.putExtra("petsUri", uri.toString());
                startActivity(intent);
            }
        });
        // Initialize the cursor loader for the adapter
        getLoaderManager().initLoader(0, null, this);


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
            getContentResolver().notifyChange(uri, null);
        }
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

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                PetContract.PetSchema._ID,
                PetContract.PetSchema.COLUMN_NAME,
                PetContract.PetSchema.COLUMN_BREED,
                //PetContract.PetSchema.COLUMN_GENDER,
                //PetContract.PetSchema.COLUMN_WEIGHT
        };
        // Filter results WHERE pet weight less than 30 kg.
        // String selection = PetContract.PetSchema.COLUMN_WEIGHT + " <= 30";
        // String[] selectionArgs = { "< 30" };

        return new CursorLoader(this, PetContract.PetSchema.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the cursor data once the Loader is finished.
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear the cursor data when no longer valid.
        mAdapter.swapCursor(null);
    }
}
