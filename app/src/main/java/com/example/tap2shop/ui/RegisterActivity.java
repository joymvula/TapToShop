package com.example.tap2shop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tap2shop.R;
import com.example.tap2shop.data.UserRepository;
import com.example.tap2shop.firebase.UserManager;
import com.example.tap2shop.model.FirestoreUser;
import com.example.tap2shop.model.User;
import com.example.tap2shop.util.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterActivity";

    // UI Components
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnGoLogin;

    // Data and Auth
    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Initialize Tools
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);

        // 2. Initialize Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        // 3. Set Listeners
        btnRegister.setOnClickListener(v -> handleRegistration());
        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Snackbar.make(btnRegister, "Please fill all fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        // Create user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Update display name in Firebase
                            firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build());

                            // Save locally via UserRepository
                            saveToLocalAndCloud(firebaseUser, name, email, password);
                        }
                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Register");
                        String error = task.getException() != null ? task.getException().getMessage() : "Auth Failed";
                        Snackbar.make(btnRegister, "Error: " + error, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void saveToLocalAndCloud(FirebaseUser firebaseUser, String name, String email, String password) {
        try {
            // Save to SQLite
            long localId = userRepository.register(name, email, password);

            // Prepare Firestore Model
            FirestoreUser firestoreUser = new FirestoreUser(firebaseUser.getUid(), name, email);
            firestoreUser.setLocalId(localId);

            // Sync to Firestore
            UserManager.getInstance().syncUserToFirestore(firestoreUser)
                    .addOnCompleteListener(task -> {
                        // Save session details
                        sessionManager.saveUserId(localId);
                        sessionManager.setUserEmail(email);
                        sessionManager.setUserName(name);
                        sessionManager.setUserUid(firebaseUser.getUid());

                        Toast.makeText(this, "Welcome to Tap2Shop!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Database error", e);
            btnRegister.setEnabled(true);
            btnRegister.setText("Register");
        }
    }
}