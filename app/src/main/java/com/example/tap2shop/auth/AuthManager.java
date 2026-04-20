package com.example.tap2shop.auth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private FirebaseAuth auth;

    public interface AuthCallback { void onSuccess(String uid); void onFailure(Exception e); }

    public AuthManager() { auth = FirebaseAuth.getInstance(); }

    public void signUp(final String email, final String password, final AuthCallback callback, final Context ctx) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String uid = task.getResult().getUser().getUid();
                            callback.onSuccess(uid);
                        } else {
                            Exception e = task.getException();
                            Log.e(TAG, "signUp failed", e);
                            if (ctx != null && e != null) Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                            callback.onFailure(e);
                        }
                    }
                });
    }

    public void signIn(final String email, final String password, final AuthCallback callback, final Context ctx) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String uid = task.getResult().getUser().getUid();
                            callback.onSuccess(uid);
                        } else {
                            Exception e = task.getException();
                            Log.e(TAG, "signIn failed", e);
                            if (ctx != null && e != null) Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                            callback.onFailure(e);
                        }
                    }
                });
    }
}
