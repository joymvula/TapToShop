package com.example.tap2shop.firebase;



import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tap2shop.data.DatabaseHelper;

import com.example.tap2shop.model.Product;

import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.EventListener;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.ListenerRegistration;

import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import java.util.List;



public class FirebaseManager {

    private static final String TAG = "FirebaseManager";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final CollectionReference productsRef = db.collection("products");

    private final CollectionReference usersRef = db.collection("users");



    public interface ProductsCallback {

        void onResult(List<Product> products);

    }



    /**

     * Starts a realtime listener.

     * Updates the local SQLite database whenever cloud data changes.

     */

    public ListenerRegistration startProductsRealtimeSync(final DatabaseHelper localDb, final ProductsCallback callback) {

        return productsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override

            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {

                    Log.w(TAG, "Listen failed.", e);

// If cloud fails, return whatever is in local storage

                    callback.onResult(localDb.getAllProducts());

                    return;

                }



                List<Product> list = new ArrayList<>();

                if (snapshots != null) {

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {

                        try {

                            Product p = doc.toObject(Product.class);

                            if (p != null) {

// Important: Set the Document ID as the Product ID

                                p.setId(doc.getId());



// Verify price is a number (debug log)

                                Log.d(TAG, "Product loaded: " + p.getName() +

                                        ", price: " + p.getPrice() +

                                        " (type: " + ((Object)p.getPrice()).getClass().getSimpleName() + ")");



// Sync to local SQLite

                                localDb.insertOrUpdateProduct(p);

                                list.add(p);

                            }

                        } catch (Exception ex) {

                            Log.e(TAG, "Error parsing product: " + doc.getId(), ex);

                            Log.e(TAG, "Document data: " + doc.getData());

                        }

                    }

                }

                callback.onResult(list);

            }

        });

    }



    /**

     * Fetches products once (e.g., on app startup).

     */

    public void fetchProductsOnce(final DatabaseHelper localDb, final ProductsCallback callback) {

        productsRef.get()

                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<Product> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                        try {

                            Product p = doc.toObject(Product.class);

                            if (p != null) {

                                p.setId(doc.getId());

                                localDb.insertOrUpdateProduct(p);

                                list.add(p);

                                Log.d(TAG, "✅ Fetched: " + p.getName() + " - K" + p.getPrice());

                            }

                        } catch (Exception ex) {

                            Log.e(TAG, "Error parsing product: " + doc.getId(), ex);

                        }

                    }

                    callback.onResult(list);

                })

                .addOnFailureListener(e -> {

                    Log.e(TAG, "Fetch failed, loading from local DB", e);

                    callback.onResult(localDb.getAllProducts());

                });

    }



    /**

     * Creates or updates a user document in Firestore.

     */

    public void createUserDocument(String uid, Object user) {

        if (uid == null) return;

        usersRef.document(uid).set(user)

                .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ User profile synced to Firebase: " + uid))

                .addOnFailureListener(e -> Log.e(TAG, "❌ User sync failed: " + uid, e));

    }



    /**

     * Helper method to add sample products to Firestore (run once)

     */

    public void addSampleProducts() {

        List<Product> products = new ArrayList<>();



        products.add(new Product(null, "Chikanda",

                "Traditional Zambian food made from wild tubers", 45.00, "chikanda.jpg"));

        products.add(new Product(null, "Ifinkubala",

                "Dried mopane worms, protein-rich snack", 60.00, "ifinkubala.jpg"));

        products.add(new Product(null, "Chibwantu",

                "Traditional Zambian fermented drink", 15.00, "chibwantu.jpg"));

        products.add(new Product(null, "Citenge Fabric",

                "Traditional 6-yard cotton print fabric", 180.00, "citenge.jpg"));

        products.add(new Product(null, "Mongu Rice",

                "High-quality rice from Western Province", 120.00, "mongu_rice.jpg"));



        for (Product product : products) {

            productsRef.add(product)

                    .addOnSuccessListener(ref ->

                            Log.d(TAG, "✅ Added: " + product.getName() + " with ID: " + ref.getId()))

                    .addOnFailureListener(e ->

                            Log.e(TAG, "❌ Failed to add: " + product.getName(), e));

        }

    }

}





