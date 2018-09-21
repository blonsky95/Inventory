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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;
import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

public class ItemCursorAdapter extends CursorAdapter {

    final static int UNIT_SOLD = 1;

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        int id = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemPriceFormatted = "€" + itemPrice;
        String itemQuantity = cursor.getString(quantityColumnIndex);
        String itemQuantityFormatted = "In stock: " + itemQuantity;

        final Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);


        nameTextView.setText(itemName);
        priceTextView.setText(itemPriceFormatted);
        quantityTextView.setText(itemQuantityFormatted);

        TextView saleButton = (TextView) view.findViewById(R.id.sale_button);

        final int quantity = cursor.getInt(quantityColumnIndex);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    Log.d("INVENTORY CHECK", "before: " + quantity);
                    int updatedQuan = quantity - UNIT_SOLD;
                    Log.d("INVENTORY CHECK", "after: " + updatedQuan);
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, updatedQuan);
                    resolver.update(currentItemUri, values, null, null);
                    context.getContentResolver().notifyChange(currentItemUri, null);
                } else {
                    Toast.makeText(context, "Item sold out", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
