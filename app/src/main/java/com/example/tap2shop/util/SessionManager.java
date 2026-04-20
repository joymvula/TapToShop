package com.example.tap2shop.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "Tap2ShopPrefs";

    // Keys for User Data
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_UID = "user_uid";  // Firebase UID
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Key for Language Settings
    private static final String KEY_SELECTED_LANGUAGE = "selected_language";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // --- Language Methods (Fixes BaseActivity Error) ---

    /**
     * Saves the user's language preference.
     * @param lang The language code (e.g., "en", "ny", "bem")
     */
    public void setLanguage(String lang) {
        editor.putString(KEY_SELECTED_LANGUAGE, lang);
        editor.apply();
    }

    /**
     * Retrieves the saved language preference.
     * Defaults to English ("en") if no preference is found.
     */
    public String getLanguage() {
        return pref.getString(KEY_SELECTED_LANGUAGE, "en");
    }

    // --- User Session Methods ---

    // Save user ID (SQLite local ID)
    public void saveUserId(long userId) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Get user ID
    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    // Save Firebase UID
    public void setUserUid(String uid) {
        editor.putString(KEY_USER_UID, uid);
        editor.apply();
    }

    // Get Firebase UID
    public String getUserUid() {
        return pref.getString(KEY_USER_UID, null);
    }

    // Set user email
    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    // Get user email
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    // Set user name
    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    // Get user name
    public String getUserName() {
        return pref.getString(KEY_USER_NAME, null);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Clear session (logout)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // Get all user info as a formatted string (for debugging)
    public String getUserInfo() {
        return "User ID: " + getUserId() +
                "\nFirebase UID: " + getUserUid() +
                "\nName: " + getUserName() +
                "\nEmail: " + getUserEmail() +
                "\nLanguage: " + getLanguage();
    }
}