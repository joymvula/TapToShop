package com.example.tap2shop.data;



import android.content.ContentValues;

import android.content.Context;

import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;

import com.example.tap2shop.model.Product;

import com.example.tap2shop.model.User;

import java.util.ArrayList;

import java.util.List;



public class DatabaseHelper extends SQLiteOpenHelper {



// Database Info

    public static final String DB_NAME = "tap2shop.db";

    public static final int DB_VERSION = 10; // Incremented to add user table with all columns



// --- PRODUCT TABLE ---

    public static final String TABLE_PRODUCTS = "products";

    public static final String COL_PROD_ID = "id";

    public static final String COL_PROD_NAME = "name";

    public static final String COL_PROD_DESC = "description";

    public static final String COL_PROD_PRICE = "price";

    public static final String COL_PROD_IMAGE = "image";



// --- CART TABLE ---

    public static final String TABLE_CART = "cart";

    public static final String COL_CART_ID = "id";

    public static final String COL_CART_PROD_ID = "product_id";

    public static final String COL_CART_QTY = "quantity";



// --- ORDER TABLE ---

    public static final String TABLE_ORDERS = "orders";

    public static final String COL_ORDER_ID = "id";

    public static final String COL_ORDER_TOTAL = "total_amount";

    public static final String COL_ORDER_TIME = "timestamp";

    public static final String COL_ORDER_STATUS = "status";



// --- USER TABLE ---

    public static final String TABLE_USERS = "users";

    public static final String COL_USER_ID = "id";

    public static final String COL_USER_UID = "uid"; // Firebase UID / Firestore document ID

    public static final String COL_USER_NAME = "name";

    public static final String COL_USER_EMAIL = "email";

    public static final String COL_USER_PASS = "password";

    public static final String COL_USER_PHONE = "phone";

    public static final String COL_USER_PROFILE_IMAGE = "profile_image";

    public static final String COL_USER_CREATED_AT = "created_at";

    public static final String COL_USER_UPDATED_AT = "updated_at";



    public DatabaseHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);

    }



    @Override

    public void onCreate(SQLiteDatabase db) {

// 1. Create Products table

        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +

                COL_PROD_ID + " TEXT PRIMARY KEY, " +

                COL_PROD_NAME + " TEXT, " +

                COL_PROD_DESC + " TEXT, " +

                COL_PROD_PRICE + " REAL, " +

                COL_PROD_IMAGE + " TEXT)");



// 2. Create Cart table

        db.execSQL("CREATE TABLE " + TABLE_CART + " (" +

                COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_CART_PROD_ID + " TEXT, " +

                COL_CART_QTY + " INTEGER)");



// 3. Create Orders table

        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +

                COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_ORDER_TOTAL + " REAL, " +

                COL_ORDER_TIME + " INTEGER, " +

                COL_ORDER_STATUS + " TEXT)");



// 4. Create Users table with all columns

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +

                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                COL_USER_UID + " TEXT, " +

                COL_USER_NAME + " TEXT, " +

                COL_USER_EMAIL + " TEXT UNIQUE, " +

                COL_USER_PASS + " TEXT, " +

                COL_USER_PHONE + " TEXT, " +

                COL_USER_PROFILE_IMAGE + " TEXT, " +

                COL_USER_CREATED_AT + " INTEGER, " +

                COL_USER_UPDATED_AT + " INTEGER)");

    }



    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

// Drop existing tables

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);



// Recreate tables

        onCreate(db);

    }



// ==================== PRODUCT METHODS ====================



    /**

     * Insert or update a product in the local database

     */

    public void insertOrUpdateProduct(Product product) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_PROD_ID, product.getId());

        values.put(COL_PROD_NAME, product.getName());

        values.put(COL_PROD_DESC, product.getDescription());

        values.put(COL_PROD_PRICE, product.getPrice());

        values.put(COL_PROD_IMAGE, product.getImage());



        db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();

    }



    /**

     * Get all products from local database

     */

    public List<Product> getAllProducts() {

        List<Product> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);



        if (cursor.moveToFirst()) {

            do {

                Product p = new Product();

                p.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_ID)));

                p.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME)));

                p.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_DESC)));

                p.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PROD_PRICE)));

                p.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_IMAGE)));

                list.add(p);

            } while (cursor.moveToNext());

        }

        cursor.close();

        db.close();

        return list;

    }



    /**

     * Get a single product by ID

     */

    public Product getProductById(String productId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PRODUCTS, null, COL_PROD_ID + " = ?",

                new String[]{productId}, null, null, null);



        Product product = null;

        if (cursor.moveToFirst()) {

            product = new Product();

            product.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_ID)));

            product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME)));

            product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_DESC)));

            product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PROD_PRICE)));

            product.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_IMAGE)));

        }

        cursor.close();

        db.close();

        return product;

    }



    /**

     * Delete a product

     */

    public void deleteProduct(String productId) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_PRODUCTS, COL_PROD_ID + " = ?", new String[]{productId});

        db.close();

    }



// ==================== CART METHODS ====================



    /**

     * Add item to cart

     */

    public void addToCart(String productId, int quantity) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_CART_PROD_ID, productId);

        values.put(COL_CART_QTY, quantity);



        db.insert(TABLE_CART, null, values);

        db.close();

    }



    /**

     * Update cart item quantity

     */

    public void updateCartItem(long cartId, int quantity) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_CART_QTY, quantity);



        db.update(TABLE_CART, values, COL_CART_ID + " = ?",

                new String[]{String.valueOf(cartId)});

        db.close();

    }



    /**

     * Remove item from cart

     */

    public void removeFromCart(long cartId) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_CART, COL_CART_ID + " = ?", new String[]{String.valueOf(cartId)});

        db.close();

    }



    /**

     * Clear entire cart

     */

    public void clearCart() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_CART, null, null);

        db.close();

    }



    /**

     * Get all cart items

     */

    public Cursor getCartItems() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("SELECT c.*, p." + COL_PROD_NAME + ", p." + COL_PROD_PRICE +

                ", p." + COL_PROD_IMAGE + " FROM " + TABLE_CART + " c " +

                "JOIN " + TABLE_PRODUCTS + " p ON c." + COL_CART_PROD_ID + " = p." + COL_PROD_ID, null);

    }



// ==================== ORDER METHODS ====================



    /**

     * Insert a new order

     */

    public long insertOrder(double totalAmount, long timestamp, String status) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_ORDER_TOTAL, totalAmount);

        values.put(COL_ORDER_TIME, timestamp);

        values.put(COL_ORDER_STATUS, status);



        long id = db.insert(TABLE_ORDERS, null, values);

        db.close();

        return id;

    }



    /**

     * Update order status

     */

    public void updateOrderStatus(long orderId, String status) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_ORDER_STATUS, status);



        db.update(TABLE_ORDERS, values, COL_ORDER_ID + " = ?",

                new String[]{String.valueOf(orderId)});

        db.close();

    }



    /**

     * Get all orders

     */

    public List<Order> getAllOrders() {

        List<Order> orderList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " ORDER BY " +

                COL_ORDER_TIME + " DESC", null);



        if (cursor.moveToFirst()) {

            do {

                Order order = new Order();

                order.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ORDER_ID)));

                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL)));

                order.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ORDER_TIME)));

                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS)));

                orderList.add(order);

            } while (cursor.moveToNext());

        }

        cursor.close();

        db.close();

        return orderList;

    }



// ==================== USER METHODS ====================



    /**

     * Insert a new user into local database

     * @param user The user object (without ID)

     * @return The SQLite local ID of the inserted user

     */

    public long insertUser(User user) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_USER_UID, user.getUid());

        values.put(COL_USER_NAME, user.getName());

        values.put(COL_USER_EMAIL, user.getEmail());

        values.put(COL_USER_PASS, user.getPassword());

        values.put(COL_USER_PHONE, user.getPhone());

        values.put(COL_USER_PROFILE_IMAGE, user.getProfileImage());

        values.put(COL_USER_CREATED_AT, user.getCreatedAt());

        values.put(COL_USER_UPDATED_AT, user.getUpdatedAt());



        long id = db.insert(TABLE_USERS, null, values);

        db.close();

        return id;

    }



    /**

     * Update existing user

     * @return number of rows affected

     */

    public int updateUser(User user) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_USER_UID, user.getUid());

        values.put(COL_USER_NAME, user.getName());

        values.put(COL_USER_EMAIL, user.getEmail());

        values.put(COL_USER_PASS, user.getPassword());

        values.put(COL_USER_PHONE, user.getPhone());

        values.put(COL_USER_PROFILE_IMAGE, user.getProfileImage());

        values.put(COL_USER_UPDATED_AT, System.currentTimeMillis());



        int rowsAffected = db.update(TABLE_USERS, values, COL_USER_ID + " = ?",

                new String[]{String.valueOf(user.getId())});

        db.close();

        return rowsAffected;

    }



    /**

     * Get user by local SQLite ID

     */

    public User getUserByLocalId(long localId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null, COL_USER_ID + " = ?",

                new String[]{String.valueOf(localId)},

                null, null, null);



        User user = null;

        if (cursor.moveToFirst()) {

            user = cursorToUser(cursor);

        }

        cursor.close();

        db.close();

        return user;

    }



    /**

     * Get user by Firebase UID

     */

    public User getUserByUid(String uid) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null, COL_USER_UID + " = ?",

                new String[]{uid},

                null, null, null);



        User user = null;

        if (cursor.moveToFirst()) {

            user = cursorToUser(cursor);

        }

        cursor.close();

        db.close();

        return user;

    }



    /**

     * Get user by email (for login)

     */

    public User getUserByEmail(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null, COL_USER_EMAIL + " = ?",

                new String[]{email},

                null, null, null);



        User user = null;

        if (cursor.moveToFirst()) {

            user = cursorToUser(cursor);

        }

        cursor.close();

        db.close();

        return user;

    }



    /**

     * Convert cursor to User object with safe column handling

     */

    private User cursorToUser(Cursor cursor) {

        User user = new User();



// Safely get each column index and check if it exists

        int idIndex = cursor.getColumnIndex(COL_USER_ID);

        if (idIndex >= 0) {

            user.setId(cursor.getLong(idIndex));

        }



        int uidIndex = cursor.getColumnIndex(COL_USER_UID);

        if (uidIndex >= 0) {

            user.setUid(cursor.getString(uidIndex));

        }



        int nameIndex = cursor.getColumnIndex(COL_USER_NAME);

        if (nameIndex >= 0) {

            user.setName(cursor.getString(nameIndex));

        }



        int emailIndex = cursor.getColumnIndex(COL_USER_EMAIL);

        if (emailIndex >= 0) {

            user.setEmail(cursor.getString(emailIndex));

        }



        int passIndex = cursor.getColumnIndex(COL_USER_PASS);

        if (passIndex >= 0) {

            user.setPassword(cursor.getString(passIndex));

        }



        int phoneIndex = cursor.getColumnIndex(COL_USER_PHONE);

        if (phoneIndex >= 0) {

            user.setPhone(cursor.getString(phoneIndex));

        }



        int profileIndex = cursor.getColumnIndex(COL_USER_PROFILE_IMAGE);

        if (profileIndex >= 0) {

            user.setProfileImage(cursor.getString(profileIndex));

        }



        int createdAtIndex = cursor.getColumnIndex(COL_USER_CREATED_AT);

        if (createdAtIndex >= 0) {

            user.setCreatedAt(cursor.getLong(createdAtIndex));

        }



        int updatedAtIndex = cursor.getColumnIndex(COL_USER_UPDATED_AT);

        if (updatedAtIndex >= 0) {

            user.setUpdatedAt(cursor.getLong(updatedAtIndex));

        }



        return user;

    }



    /**

     * Get all users (for debugging)

     */

    public List<User> getAllUsers() {

        List<User> userList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);



        if (cursor.moveToFirst()) {

            do {

                userList.add(cursorToUser(cursor));

            } while (cursor.moveToNext());

        }

        cursor.close();

        db.close();

        return userList;

    }



    /**

     * Update user's Firebase UID (Firestore ID)

     */

    public void updateUserUid(long localId, String uid) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_USER_UID, uid);

        values.put(COL_USER_UPDATED_AT, System.currentTimeMillis());



        db.update(TABLE_USERS, values, COL_USER_ID + " = ?",

                new String[]{String.valueOf(localId)});

        db.close();

    }



    /**

     * Update user's profile information

     */

    public void updateUserProfile(long localId, String name, String phone, String profileImage) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_USER_NAME, name);

        values.put(COL_USER_PHONE, phone);

        values.put(COL_USER_PROFILE_IMAGE, profileImage);

        values.put(COL_USER_UPDATED_AT, System.currentTimeMillis());



        db.update(TABLE_USERS, values, COL_USER_ID + " = ?",

                new String[]{String.valueOf(localId)});

        db.close();

    }



    /**

     * Delete user by ID

     */

    public void deleteUser(long localId) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USERS, COL_USER_ID + " = ?", new String[]{String.valueOf(localId)});

        db.close();

    }



    /**

     * Check if user exists by email

     */

    public boolean userExists(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},

                COL_USER_EMAIL + " = ?", new String[]{email},

                null, null, null);

        boolean exists = cursor.getCount() > 0;

        cursor.close();

        db.close();

        return exists;

    }



    /**

     * Check if user exists by Firebase UID

     */

    public boolean userExistsByUid(String uid) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},

                COL_USER_UID + " = ?", new String[]{uid},

                null, null, null);

        boolean exists = cursor.getCount() > 0;

        cursor.close();

        db.close();

        return exists;

    }

}