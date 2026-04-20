package com.example.tap2shop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.tap2shop.model.CartItem;
import com.example.tap2shop.model.Product;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    private final DatabaseHelper dbHelper;
    private final ProductRepository productRepository;

    public CartRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
        productRepository = new ProductRepository(context);
    }

    /**
     * Fixes: Cannot resolve method 'addToCart'
     * Uses String productId to match Firestore and updated Product model.
     */
    public void addToCart(String productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id, quantity FROM " + DatabaseHelper.TABLE_CART +
                " WHERE product_id=?", new String[]{productId});

        if (c.moveToFirst()) {
            long id = c.getLong(0);
            int qty = c.getInt(1) + 1;
            ContentValues cv = new ContentValues();
            cv.put("quantity", qty);
            db.update(DatabaseHelper.TABLE_CART, cv, "id=?", new String[]{String.valueOf(id)});
        } else {
            ContentValues cv = new ContentValues();
            cv.put("product_id", productId);
            cv.put("quantity", 1);
            db.insert(DatabaseHelper.TABLE_CART, null, cv);
        }
        c.close();
        db.close();
    }

    /**
     * Removes an item or decreases its quantity in the cart.
     */
    public void removeFromCart(String productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id, quantity FROM " + DatabaseHelper.TABLE_CART +
                " WHERE product_id=?", new String[]{productId});

        if (c.moveToFirst()) {
            long id = c.getLong(0);
            int qty = c.getInt(1) - 1;
            if (qty <= 0) {
                db.delete(DatabaseHelper.TABLE_CART, "id=?", new String[]{String.valueOf(id)});
            } else {
                ContentValues cv = new ContentValues();
                cv.put("quantity", qty);
                db.update(DatabaseHelper.TABLE_CART, cv, "id=?", new String[]{String.valueOf(id)});
            }
        }
        c.close();
        db.close();
    }

    public List<CartItem> getCartItems() {
        List<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, product_id, quantity FROM " + DatabaseHelper.TABLE_CART, null);

        if (c.moveToFirst()) {
            do {
                long id = c.getLong(0);
                String productId = c.getString(1);
                int qty = c.getInt(2);

                Product p = productRepository.getProductById(productId);
                if (p != null) {
                    items.add(new CartItem(id, p, qty));
                }
            } while (c.moveToNext());
        }
        c.close();
        return items;
    }

    public double getCartTotal() {
        double total = 0;
        List<CartItem> items = getCartItems();
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public void clearCart() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CART, null, null);
        db.close();
    }
}