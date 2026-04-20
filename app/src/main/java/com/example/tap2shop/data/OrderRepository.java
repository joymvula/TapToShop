package com.example.tap2shop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class OrderRepository {

    private final DatabaseHelper dbHelper;

    public OrderRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long createOrder(double totalAmount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Using constants from DatabaseHelper
        cv.put(DatabaseHelper.COL_ORDER_TOTAL, totalAmount);
        cv.put(DatabaseHelper.COL_ORDER_TIME, System.currentTimeMillis());
        cv.put(DatabaseHelper.COL_ORDER_STATUS, "PENDING");

        long id = db.insert(DatabaseHelper.TABLE_ORDERS, null, cv);
        db.close();
        return id;
    }
}

