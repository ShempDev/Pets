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
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetSchema;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;
    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;
    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;
    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;
    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;
    private Uri mPetUri;
    // Boolean variables to determine if activity is in insert or update mode.
    private boolean MODE;
    private final static boolean INSERT = false;
    private final static boolean UPDATE = true;
    // Variable to help watch if change is made in editor.
    private boolean mPetChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();

        // Get the intent uri data.
        Bundle extras = getIntent().getExtras();
        mPetUri = getIntent().getData();
        if (mPetUri == null) { //no uri so, go insert mode
            setTitle(R.string.insert_mode);
            MODE = INSERT;
        } else { // We have a pet uri so, we are updating an existing pet.
            setTitle(R.string.update_mode);
            MODE = UPDATE;
            getLoaderManager().initLoader(1, null, this);
        }
    }

    /*
    * Method to insert the pet data as entered by user in the edit text views.
     */
    private void insertPetData() {
        // First, validate editText entries.
        if (!validPetData ()) { //something is wrong with user entries.
            return; // bail out!
        }
        Uri uri;
        int newRowId;
        // Setup and input Value Key pairs for the pet data.
        ContentValues values = new ContentValues();
        values.put(PetContract.PetSchema.COLUMN_NAME, mNameEditText.getText().toString().trim());
        values.put(PetContract.PetSchema.COLUMN_BREED, mBreedEditText.getText().toString().trim());
        values.put(PetContract.PetSchema.COLUMN_GENDER, mGender);
        try {
            values.put(PetContract.PetSchema.COLUMN_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString().trim()));
        } catch (NumberFormatException ex) { // Value in Weight edit text is not a valid integer
            values.put(PetSchema.COLUMN_WEIGHT, 0);
        }
        // Call content provider insert method.
        if (MODE == INSERT) {
            uri = getContentResolver().insert(PetSchema.CONTENT_URI, values);
            newRowId = Integer.parseInt(uri.getLastPathSegment());
            if (newRowId == -1) { // Something went wrong
                Toast.makeText(this, getString(R.string.error_adding_pet), Toast.LENGTH_SHORT).show();
            } else { // No problems.
                Toast.makeText(this, getString(R.string.new_pet_added) + newRowId, Toast.LENGTH_SHORT).show();
                // Also, clear the edit text entries to allow for another entry.
                mNameEditText.setText("");
                mBreedEditText.setText("");
                mWeightEditText.setText("");
                mGenderSpinner.setSelection(0);
                // Clear the mPetChanged boolean from the input touch.
                mPetChanged = false;
            }
        } else { // Call content provider update method for selected pet _ID.
            String selection = PetSchema._ID + "=?";
            String[] selectionArgs = { mPetUri.getLastPathSegment() };
            newRowId = getContentResolver().update(PetSchema.CONTENT_URI, values, selection, selectionArgs);
            if (newRowId == -1) { // Something went wrong
                Toast.makeText(this, getString(R.string.error_adding_pet), Toast.LENGTH_SHORT).show();
            } else { // No problems.
                Toast.makeText(this, getString(R.string.pet_updated) + selectionArgs[0], Toast.LENGTH_SHORT).show();
                // Also, exit this activity and return to previous. Cannot add another in this mode.
                finish();
            }
        }
    }

    // This method will be called on selected pet to remove pet from database.
    private void deletePetData () {
        int newRowId;
        // Make sure we are in update mode.
        if (MODE == UPDATE) {
            // Setup to select pet with provided _ID.
            String selection = PetSchema._ID + "=?";
            String[] selectionArgs = { mPetUri.getLastPathSegment() };
            newRowId = getContentResolver().delete(PetSchema.CONTENT_URI, selection, selectionArgs);
            if (newRowId == -1) { // Something went wrong during delete
                Toast.makeText(this, getString(R.string.error_adding_pet), Toast.LENGTH_SHORT).show();
            } else { // No problems.
                Toast.makeText(this, getString(R.string.pet_deleted) + selectionArgs[0], Toast.LENGTH_SHORT).show();
                // Also, exit this activity and return to previous.
                finish();
            }
        } else { //Cannot delete pet from insert mode.
            Toast.makeText(this, getString(R.string.cannot_delete), Toast.LENGTH_LONG).show();
        }
    }

    // This method pops up an alert to warn/verify user that ALL pet data will be deleted.
    private void showDeletePetDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.delete_pets_warning);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick (DialogInterface dialog, int id) {
                // User verified action to delete ALL pet data.
                deletePetData();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick (DialogInterface dialog, int id) {
                // User clicked to cancel so just quit dialog and stay in activity.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        //Create and show the dialog box.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // This method performs checks to make sure inputs contain valid data.
    private boolean validPetData () {
        // make sure name and breed are not blank.
        if (TextUtils.isEmpty(mNameEditText.getText())) {
            Toast.makeText(this, getString(R.string.name_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(mWeightEditText.getText())) {
            // If Weight is empty then set to 0;
            mWeightEditText.setText("0");
            return false;
        }
        if (Integer.parseInt(mWeightEditText.getText().toString().trim()) > 100) {
            // Let's set a maximum weight for a "pet".
            Toast.makeText(this, getString(R.string.max_weight), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetSchema.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetSchema.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetSchema.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    // OnTouchListener to watch for editor activity before backing out.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetChanged = true;
            return false;
        }
    };

    // Method to popup a dialog to verify leaving activity w/o saving data.
    private void unsavedChangesDialog (DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick (DialogInterface dialog, int id) {
                // User clicked to cancel so just quit dialog and stay in activity.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        //Create and show the dialog box.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If pet data hasn't changed, just continue with back out.
        if (!mPetChanged) {
            super.onBackPressed();
            return;
        }
        // Else display alert dialog to ask user to verify quit w/o saving.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        unsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        if (MODE == INSERT) {
            menu.removeItem(R.id.action_delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert (save) the entered pet data or update selected pet.
                insertPetData();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Delete selected pet data.
                showDeletePetDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                // Check for changes to pet data.
                if (!mPetChanged) { // no changes; OK to go back.
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // User made changes to pet. Call alert dialog to verify action to take.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                unsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query. We need all columns.
        String[] projection = {
                PetContract.PetSchema._ID,
                PetContract.PetSchema.COLUMN_NAME,
                PetContract.PetSchema.COLUMN_BREED,
                PetContract.PetSchema.COLUMN_GENDER,
                PetContract.PetSchema.COLUMN_WEIGHT
        };
        // Filter results WHERE selection = _ID of selected pet only.
        String selection = PetSchema._ID + "=?";
        String[] selectionArgs = { mPetUri.getLastPathSegment() };

        return new CursorLoader(this, PetSchema.CONTENT_URI, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            // Set the edit texts from our selected pet.
            String name = data.getString(data.getColumnIndexOrThrow(PetSchema.COLUMN_NAME));
            mNameEditText.setText(name);
            String breed = data.getString(data.getColumnIndexOrThrow(PetSchema.COLUMN_BREED));
            mBreedEditText.setText(breed);
            int gender = data.getInt(data.getColumnIndexOrThrow(PetSchema.COLUMN_GENDER));
            mGenderSpinner.setSelection(gender);
            int weight = data.getInt(data.getColumnIndexOrThrow(PetSchema.COLUMN_WEIGHT));
            mWeightEditText.setText(Integer.toString(weight));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // clear the edit text entries.
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}