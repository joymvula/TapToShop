// File: app/src/main/java/com/example/tap2shop/model/FirestoreUser.java
package com.example.tap2shop.model;

import com.google.firebase.Timestamp;

public class FirestoreUser {
    private String uid;           // Firebase Auth UID (document ID)
    private long localId;          // Your SQLite user ID
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public FirestoreUser() {}

    public FirestoreUser(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public long getLocalId() { return localId; }
    public void setLocalId(long localId) { this.localId = localId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Convert to local User
    public User toLocalUser() {
        User localUser = new User();
        localUser.setUid(this.uid);
        localUser.setName(this.name);
        localUser.setEmail(this.email);
        localUser.setPhone(this.phone);
        localUser.setProfileImage(this.profileImage);
        localUser.setCreatedAt(this.createdAt != null ? this.createdAt.toDate().getTime() : System.currentTimeMillis());
        localUser.setUpdatedAt(this.updatedAt != null ? this.updatedAt.toDate().getTime() : System.currentTimeMillis());
        return localUser;
    }

    // Convert from local User
    public static FirestoreUser fromLocalUser(User localUser) {
        FirestoreUser firestoreUser = new FirestoreUser();
        firestoreUser.setUid(localUser.getUid());
        firestoreUser.setLocalId(localUser.getId());
        firestoreUser.setName(localUser.getName());
        firestoreUser.setEmail(localUser.getEmail());
        firestoreUser.setPhone(localUser.getPhone());
        firestoreUser.setProfileImage(localUser.getProfileImage());
        firestoreUser.setCreatedAt(Timestamp.now());
        firestoreUser.setUpdatedAt(Timestamp.now());
        return firestoreUser;
    }
}