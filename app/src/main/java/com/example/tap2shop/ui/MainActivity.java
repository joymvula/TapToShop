package com.example.tap2shop.ui;


import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.widget.Toast;



import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;



import com.example.tap2shop.R;

import com.example.tap2shop.data.DatabaseHelper;

import com.example.tap2shop.firebase.FirebaseManager;

import com.example.tap2shop.model.FirestoreUser;

import com.example.tap2shop.model.Product;

import com.example.tap2shop.model.User;

import com.example.tap2shop.ui.cart.CartActivity;

import com.example.tap2shop.ui.home.HomeFragment;

import com.example.tap2shop.ui.profile.ProfileFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.FirebaseFirestore;



import java.util.ArrayList;

import java.util.List;



public class MainActivity extends BaseActivity {



    private static final String TAG = "MainActivity";

    private DatabaseHelper dbHelper;

    private FirebaseManager firebaseManager;



// Flag to control sample products addition

    private static final boolean ADD_SAMPLE_PRODUCTS = false; // Set to false after first run



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



// Initialize DatabaseHelper

        dbHelper = new DatabaseHelper(this);



// Initialize FirebaseManager

        firebaseManager = new FirebaseManager();



// Check Firebase Authentication status

        checkFirebaseAuth();



// Sync Firebase user with local database

        syncFirebaseUserToLocal();



// Check if product data exists; if not, populate it

        if (dbHelper.getAllProducts().isEmpty()) {

            seedZambianProducts(dbHelper);



// ADD SAMPLE PRODUCTS TO FIRESTORE (RUN ONLY ONCE)

            if (ADD_SAMPLE_PRODUCTS) {

                addSampleProductsToFirestore();

            }

        } else {

// If products exist locally, sync them to Firestore

            syncProductsToFirestore();

        }



        setupNavigation();

    }



    /**

     * Add sample products to Firestore using FirebaseManager

     * Run this ONCE, then set ADD_SAMPLE_PRODUCTS to false

     */

    private void addSampleProductsToFirestore() {

        Log.d(TAG, "========== ADDING SAMPLE PRODUCTS TO FIRESTORE ==========");



// Using FirebaseManager to add products

        firebaseManager.addSampleProducts();



// Also sync any existing local products

        syncProductsToFirestore();



        Log.d(TAG, "==========================================================");

        Log.d(TAG, "✅ IMPORTANT: Set ADD_SAMPLE_PRODUCTS = false after confirming products in Firebase Console");

    }



    /**

     * Alternative method to add products directly (if FirebaseManager doesn't have addSampleProducts)

     */

    private void addProductsDirectly() {

        Log.d(TAG, "Adding sample products directly to Firestore...");



        List<Product> products = new ArrayList<>();



// Create products with correct data types (price as double)

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



        CollectionReference productsRef = FirebaseFirestore.getInstance()

                .collection("products");



        for (Product product : products) {

            productsRef.add(product)

                    .addOnSuccessListener(documentReference -> {

                        String productId = documentReference.getId();

                        Log.d(TAG, "✅ Added: " + product.getName() +

                                " with ID: " + productId);



// Update local product with Firestore ID

                        product.setId(productId);

                        dbHelper.insertOrUpdateProduct(product);

                    })

                    .addOnFailureListener(e -> {

                        Log.e(TAG, "❌ Failed to add: " + product.getName(), e);

                    });

        }

    }



    /**

     * Sync existing local products to Firestore

     */

    private void syncProductsToFirestore() {

        List<Product> localProducts = dbHelper.getAllProducts();

        if (localProducts.isEmpty()) {

            Log.d(TAG, "No local products to sync");

            return;

        }



        Log.d(TAG, "Syncing " + localProducts.size() + " local products to Firestore...");

        CollectionReference productsRef = FirebaseFirestore.getInstance()

                .collection("products");



        for (Product product : localProducts) {

// If product doesn't have Firestore ID or ID starts with ZAM (local ID)

            if (product.getId() == null || product.getId().startsWith("ZAM")) {

// Add to Firestore with auto-ID

                productsRef.add(product)

                        .addOnSuccessListener(documentReference -> {

                            String firestoreId = documentReference.getId();

                            product.setId(firestoreId);

                            dbHelper.insertOrUpdateProduct(product);

                            Log.d(TAG, "✅ Synced local product to Firestore: " +

                                    product.getName() + " (ID: " + firestoreId + ")");

                        })

                        .addOnFailureListener(e ->

                                Log.e(TAG, "❌ Failed to sync: " + product.getName(), e));

            } else {

// Update existing Firestore document

                productsRef.document(product.getId())

                        .set(product)

                        .addOnSuccessListener(aVoid ->

                                Log.d(TAG, "✅ Updated Firestore product: " + product.getName()))

                        .addOnFailureListener(e ->

                                Log.e(TAG, "❌ Failed to update: " + product.getName(), e));

            }

        }

    }



    private void checkFirebaseAuth() {

        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            String uid = user.getUid();

            String email = user.getEmail();

            Log.d("FIREBASE_AUTH", "✅ User UID: " + uid);

            Log.d("FIREBASE_AUTH", "✅ User Email: " + email);

            Toast.makeText(this, "Logged in as: " + email, Toast.LENGTH_LONG).show();

        } else {

            Log.d("FIREBASE_AUTH", "❌ No user logged in");

            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();

        }

    }



    /**

     * Sync Firebase authenticated user with local SQLite database

     */

    private void syncFirebaseUserToLocal() {

        com.google.firebase.auth.FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();



        if (firebaseUser == null) {

            Log.d(TAG, "No Firebase user logged in");

            return;

        }



        String email = firebaseUser.getEmail();

        String name = firebaseUser.getDisplayName();

        String firestoreUid = firebaseUser.getUid();



// Check if user exists in local database by email

        User existingUser = dbHelper.getUserByEmail(email);



        if (existingUser == null) {

// User doesn't exist locally - create new user

            Log.d(TAG, "Creating new local user for Firebase user: " + email);



            if (name == null || name.isEmpty()) {

                name = email.substring(0, email.indexOf('@'));

            }



// Create new local user

            User newLocalUser = new User(name, email, "firebase_managed");

            newLocalUser.setUid(firestoreUid); // Set Firebase UID



// Insert into local database and get SQLite ID

            long localId = dbHelper.insertUser(newLocalUser);

            Log.d(TAG, "✅ Created local user with SQLite ID: " + localId);



// Create Firestore user from local user

            FirestoreUser firestoreUser = FirestoreUser.fromLocalUser(newLocalUser);

            firestoreUser.setLocalId(localId);



// Now sync to Firestore

            FirebaseFirestore.getInstance()

                    .collection("users")

                    .document(firestoreUid)

                    .set(firestoreUser)

                    .addOnSuccessListener(aVoid -> {

                        Log.d(TAG, "✅ User synced to Firestore with ID: " + firestoreUid);



                        User verifiedUser = dbHelper.getUserByLocalId(localId);

                        if (verifiedUser != null) {

                            Log.d(TAG, "Verified user - Local ID: " + verifiedUser.getId() +

                                    ", UID: " + verifiedUser.getUid());

                        }

                    })

                    .addOnFailureListener(e -> {

                        Log.e(TAG, "❌ Failed to sync user to Firestore", e);

                    });



        } else {

            Log.d(TAG, "User already exists locally with ID: " + existingUser.getId() +

                    ", UID: " + existingUser.getUid());



// Check if we need to update Firestore ID

            if (existingUser.getUid() == null || existingUser.getUid().isEmpty()) {

// Update local user with Firebase UID

                dbHelper.updateUserUid(existingUser.getId(), firestoreUid);

                existingUser.setUid(firestoreUid);

                Log.d(TAG, "Updated existing user with UID: " + firestoreUid);



// Convert to Firestore user and sync

                FirestoreUser firestoreUser = FirestoreUser.fromLocalUser(existingUser);



                FirebaseFirestore.getInstance()

                        .collection("users")

                        .document(firestoreUid)

                        .set(firestoreUser)

                        .addOnSuccessListener(aVoid ->

                                Log.d(TAG, "✅ Synced existing user to Firestore"))

                        .addOnFailureListener(e ->

                                Log.e(TAG, "❌ Failed to sync existing user", e));

            } else {

// User already has UID, just ensure Firestore is up to date

                FirestoreUser firestoreUser = FirestoreUser.fromLocalUser(existingUser);



                FirebaseFirestore.getInstance()

                        .collection("users")

                        .document(firestoreUid)

                        .set(firestoreUser)

                        .addOnSuccessListener(aVoid ->

                                Log.d(TAG, "✅ Updated existing user in Firestore"))

                        .addOnFailureListener(e ->

                                Log.e(TAG, "❌ Failed to update user in Firestore", e));

            }

        }



        demonstrateUserRetrieval();

    }



    /**

     * Example method to demonstrate user retrieval

     */

    private void demonstrateUserRetrieval() {

        Log.d(TAG, "========== USER RETRIEVAL DEMO ==========");



        com.google.firebase.auth.FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser == null) return;



// Get by email

        User userByEmail = dbHelper.getUserByEmail(currentFirebaseUser.getEmail());

        if (userByEmail != null) {

            Log.d(TAG, "By email: " + userByEmail.getName() +

                    " (Local ID: " + userByEmail.getId() +

                    ", UID: " + userByEmail.getUid() + ")");

        }



// Get by UID

        User userByUid = dbHelper.getUserByUid(currentFirebaseUser.getUid());

        if (userByUid != null) {

            Log.d(TAG, "By UID: " + userByUid.getName() +

                    " (Local ID: " + userByUid.getId() + ")");

        }



// Get by local ID

        if (userByEmail != null) {

            User userByLocalId = dbHelper.getUserByLocalId(userByEmail.getId());

            if (userByLocalId != null) {

                Log.d(TAG, "By local ID: " + userByLocalId.getName() +

                        " (UID: " + userByLocalId.getUid() + ")");

            }

        }



// Get all users

        List<User> allUsers = dbHelper.getAllUsers();

        Log.d(TAG, "Total users in local database: " + allUsers.size());

        for (User user : allUsers) {

            Log.d(TAG, " - " + user.getName() + " (Email: " + user.getEmail() +

                    ", Local ID: " + user.getId() +

                    ", UID: " + user.getUid() + ")");

        }



        Log.d(TAG, "==========================================");

    }



    public void seedZambianProducts(DatabaseHelper dbHelper) {

        List<Product> products = new ArrayList<>();



        products.add(new Product("ZAM001", "Chibwantu", "Traditional fermented maize drink", 15.00, "https://example.com/chibwantu.jpg"));

        products.add(new Product("ZAM002", "Chikanda", "Traditional African Polony snack", 45.00, "https://example.com/chikanda.jpg"));

        products.add(new Product("ZAM003", "Ifinkubala", "High-protein dried mopane worms", 60.00, "https://example.com/worms.jpg"));

        products.add(new Product("ZAM004", "Citenge Fabric", "Traditional 6-yard cotton print", 180.00, "https://example.com/citenge.jpg"));

        products.add(new Product("ZAM005", "Mongu Rice", "Premium aromatic Barotse plains rice", 120.00, "https://example.com/rice.jpg"));



        for (Product p : products) {

            dbHelper.insertOrUpdateProduct(p);

        }



        Log.d(TAG, "✅ Seeded " + products.size() + " Zambian products to local database");

    }



    private void setupNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menu_home) {

                openFragment(new HomeFragment());

                return true;

            } else if (id == R.id.menu_cart) {

                startActivity(new Intent(MainActivity.this, CartActivity.class));

                return true;

            } else if (id == R.id.menu_profile) {

                openFragment(new ProfileFragment());

                return true;

            }

            return false;

        });



        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) == null) {

            bottomNav.setSelectedItemId(R.id.menu_home);

        }

    }



    private void openFragment(@NonNull Fragment fragment) {

        getSupportFragmentManager()

                .beginTransaction()

                .replace(R.id.fragmentContainer, fragment)

                .commit();

    }

}


