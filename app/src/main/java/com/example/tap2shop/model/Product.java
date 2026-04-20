package com.example.tap2shop.model;

/**
 * Model representing a Product.
 * The ID is a String to support alphanumeric IDs from Firebase.
 */
public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String image;

    // Required empty constructor for Firebase/Firestore
    public Product() {}

    // Constructor with parameters (useful for manual creation)
    public Product(String id, String name, String description, double price, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImage() { return image; }

    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImage(String image) { this.image = image; }
}