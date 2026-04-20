// File: app/src/main/java/com/example/tap2shop/managers/UserManager.java
package com.example.tap2shop.firebase;

import android.util.Log;

import com.example.tap2shop.model.FirestoreUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserManager {
    private static final String TAG = "UserManager";
    private static UserManager instance;
    private final FirebaseFirestore firestore;
    private static final String USERS_COLLECTION = "users";

    private UserManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Sync user to Firestore
     * @param user The FirestoreUser object to sync
     * @return Task<Void> for completion handling
     */
    public Task<Void> syncUserToFirestore(FirestoreUser user) {
        Log.d(TAG, "Syncing user to Firestore: " + user.getUid());

        return firestore.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User synced successfully: " + user.getUid()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error syncing user: " + user.getUid(), e));
    }

    /**
     * Get user from Firestore by UID
     * @param uid The Firebase Auth UID
     * @return Task<FirestoreUser> containing the user data
     */
    public Task<FirestoreUser> getUserFromFirestore(String uid) {
        return firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(FirestoreUser.class);
                    }
                    return null;
                });
    }

    /**
     * Update user in Firestore
     * @param user The FirestoreUser object with updated data
     * @return Task<Void> for completion handling
     */
    public Task<Void> updateUserInFirestore(FirestoreUser user) {
        user.setUpdatedAt(com.google.firebase.Timestamp.now());

        return firestore.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User updated successfully: " + user.getUid()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating user: " + user.getUid(), e));
    }

    /**
     * Delete user from Firestore
     * @param uid The Firebase Auth UID
     * @return Task<Void> for completion handling
     */
    public Task<Void> deleteUserFromFirestore(String uid) {
        return firestore.collection(USERS_COLLECTION)
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User deleted successfully: " + uid))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error deleting user: " + uid, e));
    }
}