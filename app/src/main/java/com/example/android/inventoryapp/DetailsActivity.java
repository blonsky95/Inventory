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
package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;
import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

import java.util.ArrayList;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;

    private TextView mNameTV;

    private TextView mPriceTV;

    private TextView mQuantityTV;

    private TextView mSupplierNameTV;

    private TextView mSupplierEmailTV;

    private TextView mSupplierPhoneTV;

    private boolean mItemHasChanged = false;

    final static int ADD_STOCK = 1;

    final static int REDUCE_STOCK = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        TextView quantityDisplay = (TextView) findViewById(R.id.q_display);
        quantityDisplay.setText("0");
            setTitle(getString(R.string.item_details));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);


        mNameTV = (TextView) findViewById(R.id.edit_item_name);
        mPriceTV = (TextView) findViewById(R.id.edit_item_price);
        mQuantityTV = (TextView) findViewById(R.id.edit_item_quantity);
        mSupplierNameTV = (TextView) findViewById(R.id.edit_item_supplier_name);
        mSupplierEmailTV = (TextView) findViewById(R.id.edit_item_supplier_email);
        mSupplierPhoneTV = (TextView) findViewById(R.id.edit_item_supplier_phone);

        TextView plusStock = (TextView) findViewById(R.id.plus_btn);
        TextView minusStock = (TextView) findViewById(R.id.minus_btn);
        TextView callButton = (TextView) findViewById(R.id.supplier_call);
        plusStock.setOnClickListener(increaseStock);
        minusStock.setOnClickListener(decreaseStock);
        callButton.setOnClickListener(dialSupplier);

    }

    private View.OnClickListener increaseStock = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentItemUri != null) {
                Log.e("URI_CHECK", "display" + mCurrentItemUri);
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                TextView q_display = (TextView) findViewById(R.id.q_display);
                String quantityString = q_display.getText().toString();
                int quantityInt = Integer.parseInt(quantityString);
                if (quantityInt >= 0) {
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityInt + ADD_STOCK);
                    resolver.update(mCurrentItemUri, values, null, null);
                    getApplicationContext().getContentResolver().notifyChange(mCurrentItemUri, null);

                } else {
                    Toast.makeText(getApplicationContext(), "Error in stock logs", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Save the new item first", Toast.LENGTH_SHORT).show();

            }
        }


    };

    private View.OnClickListener decreaseStock = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentItemUri != null) {
                ContentResolver resolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                TextView q_display = (TextView) findViewById(R.id.q_display);
                String quantityString = q_display.getText().toString();
                int quantityInt = Integer.parseInt(quantityString);
                if (quantityInt >= 1) {
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityInt - REDUCE_STOCK);
                    resolver.update(mCurrentItemUri, values, null, null);
                    getApplicationContext().getContentResolver().notifyChange(mCurrentItemUri, null);

                } else {
                    Toast.makeText(getApplicationContext(), "Item sold out", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Save the new item first", Toast.LENGTH_SHORT).show();

            }

        }
    };

    private View.OnClickListener dialSupplier = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentItemUri != null) {

                String supplierPhoneString= mSupplierPhoneTV.getText().toString();

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", supplierPhoneString, null));
                startActivity(intent);

            } else {
                Toast.makeText(getApplicationContext(), "Save the new item first", Toast.LENGTH_SHORT).show();

            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
       //delete always displayed
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:

                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(mCurrentItemUri);
                startActivity(intent);
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:

                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER_NAME,
                ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE};

        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_EMAIL);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE);

            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String suppName = cursor.getString(supplierNameColumnIndex);
            String suppEmail = cursor.getString(supplierEmailColumnIndex);
            String suppPhone = cursor.getString(supplierPhoneColumnIndex);

            int quantity = cursor.getInt(quantityColumnIndex);

            mNameTV.setText(name);
            mPriceTV.setText(price);
            mQuantityTV.setText(Integer.toString(quantity));
            mSupplierNameTV.setText(suppName);
            mSupplierEmailTV.setText(suppEmail);
            mSupplierPhoneTV.setText(suppPhone);

            String quantityString = cursor.getString(quantityColumnIndex);
            int quantityInt = Integer.parseInt(quantityString);
            TextView q_display = (TextView) findViewById(R.id.q_display);

            Log.e("CHECK", "display: " + quantityInt);
            q_display.setText(quantityString);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTV.setText("");
        mPriceTV.setText("");
        mQuantityTV.setText("");
        mSupplierNameTV.setText("");
        mSupplierEmailTV.setText("");
        mSupplierPhoneTV.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

}