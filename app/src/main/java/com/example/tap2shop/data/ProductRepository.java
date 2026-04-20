package com.example.tap2shop.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ProgressBar;

import com.example.tap2shop.firebase.FirebaseManager;
import com.example.tap2shop.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final DatabaseHelper dbHelper;
    private final FirebaseManager firebaseManager;

    public ProductRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.firebaseManager = new FirebaseManager(); // Initialize Firebase link
    }

    /**
     * FIX for HomeFragment: This handles the UI state (ProgressBar)
     * and triggers the Firebase Realtime Sync.
     */
    public void getProducts(ProgressBar progressBar, FirebaseManager.ProductsCallback callback) {
        // Show progress bar on the UI thread
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Start the sync process
        firebaseManager.startProductsRealtimeSync(dbHelper, products -> {
            // Hide progress bar once data is received
            if (progressBar != null) {
                progressBar.post(() -> progressBar.setVisibility(View.GONE));
            }
            // Pass the list back to the HomeFragment
            callback.onResult(products);
        });
    }

    /**
     * Local lookup: Useful for offline mode.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, description, price, image FROM " +
                DatabaseHelper.TABLE_PRODUCTS, null);
        if (c.moveToFirst()) {
            do {
                Product p = new Product();
                p.setId(c.getString(0));
                p.setName(c.getString(1));
                p.setDescription(c.getString(2));
                p.setPrice(c.getDouble(3));
                p.setImage(c.getString(4));
                products.add(p);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return products;
    }

    /**
     * Fetches a single product by its String ID (Firestore UID).
     */
    public Product getProductById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, description, price, image FROM " +
                        DatabaseHelper.TABLE_PRODUCTS + " WHERE id=?",
                new String[]{id});
        Product p = null;
        if (c.moveToFirst()) {
            p = new Product();
            p.setId(c.getString(0));
            p.setName(c.getString(1));
            p.setDescription(c.getString(2));
            p.setPrice(c.getDouble(3));
            p.setImage(c.getString(4));
        }
        c.close();
        db.close();
        return p;
    }
}