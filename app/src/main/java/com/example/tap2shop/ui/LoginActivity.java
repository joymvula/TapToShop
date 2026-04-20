// File: app/src/main/java/com/example/tap2shop/ui/LoginActivity.java
package com.example.tap2shop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tap2shop.R;
import com.example.tap2shop.data.DatabaseHelper;
import com.example.tap2shop.firebase.UserManager;
import com.example.tap2shop.model.FirestoreUser;
import com.example.tap2shop.model.User;
import com.example.tap2shop.util.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    // Material Design components
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoRegister;

    // Firebase and database
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase and database
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        checkCurrentUser();

        // Initialize Material Design views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        // Set click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void checkCurrentUser() {
        // Check if user is already logged in via Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already logged in: " + currentUser.getUid());

            // Check if user exists in local database
            User localUser = dbHelper.getUserByUid(currentUser.getUid());

            if (localUser != null) {
                // User exists locally, save session and go to MainActivity
                sessionManager.saveUserId(localUser.getId());
                sessionManager.setUserUid(currentUser.getUid());
                sessionManager.setUserEmail(currentUser.getEmail());
                sessionManager.setUserName(currentUser.getDisplayName() != null ?
                        currentUser.getDisplayName() : getString(R.string.label_name));

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                // User exists in Firebase but not locally, fetch from Firestore
                syncUserFromFirestore(currentUser);
            }
        }
    }

    private void loginUser() {
        // Get text from Material Design TextInputEditText
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(etEmail, R.string.error_fill_all_fields, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double submission
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.logging_in);

        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login success
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                Log.d(TAG, "✅ Firebase Auth login successful: " + firebaseUser.getUid());

                                // Check if user exists in local database
                                handleUserLogin(firebaseUser);
                            } else {
                                resetLoginButton();
                                Snackbar.make(etEmail, R.string.error_firebase_user_null, Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            // Login fails
                            Log.e(TAG, "❌ Firebase Auth login failed", task.getException());
                            resetLoginButton();

                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : getString(R.string.error_general);
                            Snackbar.make(etEmail, getString(R.string.error_invalid_credentials) + ": " + errorMessage,
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText(R.string.btn_login);
    }

    private void handleUserLogin(FirebaseUser firebaseUser) {
        // Check if user exists in local database by Firebase UID
        User existingUser = dbHelper.getUserByUid(firebaseUser.getUid());

        if (existingUser == null) {
            // User doesn't exist locally - check by email or fetch from Firestore
            Log.d(TAG, "User not found locally by UID, checking by email or Firestore");

            // Try to get user by email
            existingUser = dbHelper.getUserByEmail(firebaseUser.getEmail());

            if (existingUser != null) {
                // User exists by email, update their UID
                Log.d(TAG, "User found by email, updating UID");
                dbHelper.updateUserUid(existingUser.getId(), firebaseUser.getUid());

                // Update session and proceed
                saveUserSession(existingUser, firebaseUser);
                navigateToMain();
            } else {
                // User doesn't exist locally at all, fetch from Firestore
                Log.d(TAG, "User not found locally, fetching from Firestore");
                syncUserFromFirestore(firebaseUser);
            }
        } else {
            // User exists locally
            Log.d(TAG, "User found locally with ID: " + existingUser.getId());

            // Update session and proceed
            saveUserSession(existingUser, firebaseUser);
            navigateToMain();
        }
    }

    private void syncUserFromFirestore(FirebaseUser firebaseUser) {
        Log.d(TAG, "Syncing user from Firestore for UID: " + firebaseUser.getUid());

        UserManager.getInstance().getUserFromFirestore(firebaseUser.getUid())
                .addOnCompleteListener(new OnCompleteListener<FirestoreUser>() {
                    @Override
                    public void onComplete(@NonNull Task<FirestoreUser> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // User found in Firestore
                            FirestoreUser firestoreUser = task.getResult();
                            Log.d(TAG, "✅ User retrieved from Firestore: " + firestoreUser.getName());

                            // Create local user from Firestore data
                            User newLocalUser = new User(
                                    firestoreUser.getName(),
                                    firestoreUser.getEmail(),
                                    "" // Password not stored locally for security
                            );
                            newLocalUser.setUid(firestoreUser.getUid());
                            newLocalUser.setPhone(firestoreUser.getPhone());
                            newLocalUser.setProfileImage(firestoreUser.getProfileImage());

                            // Insert into local database
                            long localId = dbHelper.insertUser(newLocalUser);
                            Log.d(TAG, "✅ Created local user from Firestore with ID: " + localId);

                            // Get the inserted user
                            User savedUser = dbHelper.getUserByLocalId(localId);

                            // Save session and proceed
                            saveUserSession(savedUser, firebaseUser);
                            navigateToMain();

                        } else {
                            // User not found in Firestore, create minimal local user
                            Log.d(TAG, "User not found in Firestore, creating minimal local user");

                            String name = firebaseUser.getDisplayName();
                            if (name == null || name.isEmpty()) {
                                String email = firebaseUser.getEmail();
                                name = email.substring(0, email.indexOf('@'));
                            }

                            User minimalUser = new User(name, firebaseUser.getEmail(), "");
                            minimalUser.setUid(firebaseUser.getUid());

                            long localId = dbHelper.insertUser(minimalUser);
                            Log.d(TAG, "✅ Created minimal local user with ID: " + localId);

                            User savedUser = dbHelper.getUserByLocalId(localId);

                            // Save session and proceed
                            saveUserSession(savedUser, firebaseUser);

                            // Also try to sync this user to Firestore
                            FirestoreUser newFirestoreUser = new FirestoreUser(
                                    firebaseUser.getUid(),
                                    name,
                                    firebaseUser.getEmail()
                            );
                            newFirestoreUser.setLocalId(localId);

                            UserManager.getInstance().syncUserToFirestore(newFirestoreUser)
                                    .addOnCompleteListener(syncTask -> {
                                        if (syncTask.isSuccessful()) {
                                            Log.d(TAG, "✅ Synced minimal user to Firestore");
                                        }
                                    });

                            navigateToMain();
                        }

                        resetLoginButton();
                    }
                });
    }

    private void saveUserSession(User user, FirebaseUser firebaseUser) {
        // Save all user data to SessionManager
        sessionManager.saveUserId(user.getId());
        sessionManager.setUserUid(firebaseUser.getUid());
        sessionManager.setUserEmail(firebaseUser.getEmail());

        String displayName = firebaseUser.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            sessionManager.setUserName(displayName);
        } else {
            sessionManager.setUserName(user.getName());
        }

        Log.d(TAG, "✅ User session saved: " + sessionManager.getUserInfo());
    }

    private void navigateToMain() {
        resetLoginButton();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-trivial) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in onStart: " + currentUser.getUid());
        }
    }
}


