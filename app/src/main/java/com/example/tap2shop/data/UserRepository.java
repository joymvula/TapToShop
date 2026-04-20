package com.example.tap2shop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.tap2shop.model.User; // Make sure this matches your local model name

public class UserRepository {

    private final DatabaseHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Register a new user into the local SQLite database
    public long register(String name, String email, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.COL_USER_NAME, name);
        cv.put(DatabaseHelper.COL_USER_EMAIL, email);
        cv.put(DatabaseHelper.COL_USER_PASS, password);

        long id = db.insert(DatabaseHelper.TABLE_USERS, null, cv);
        db.close();
        return id;
    }

    // Login logic: Checks credentials and returns a User object if successful
    public User login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to find user with matching email and password
        Cursor c = db.rawQuery("SELECT " +
                        DatabaseHelper.COL_USER_ID + "," +
                        DatabaseHelper.COL_USER_NAME + "," +
                        DatabaseHelper.COL_USER_EMAIL + "," +
                        DatabaseHelper.COL_USER_PASS + " FROM " +
                        DatabaseHelper.TABLE_USERS +
                        " WHERE " + DatabaseHelper.COL_USER_EMAIL + "=? AND " +
                        DatabaseHelper.COL_USER_PASS + "=?",
                new String[]{email, password});

        User user = null;

        if (c.moveToFirst()) {
            // We create a new User object and fill it with data from the database
            user = new User();
            user.setId(c.getLong(0));
            user.setName(c.getString(1));
            user.setEmail(c.getString(2));
            user.setPassword(c.getString(3));
        }

        c.close();
        db.close();
        return user;
    }
}