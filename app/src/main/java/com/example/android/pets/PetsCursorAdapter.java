package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

/**
 * Created by jeremy on 5/18/17.
 */

public class PetsCursorAdapter extends CursorAdapter {

    // Public constructor for our Pets custom CursorAdapter
    public PetsCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate and return the list_item view for Pets cursor.
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get the text views to populate with the cursor data
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        // Get the data from the cursor.
        String name = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetSchema.COLUMN_NAME));
        String breed = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetSchema.COLUMN_BREED));
        // Set the text in the list_view text items.
        nameTextView.setText(name);
        summaryTextView.setText(breed);
    }
}
