package com.example.tap2shop.model;

public class PaymentMethod {
    private String id;
    private String name;
    private String icon;
    private boolean isSelected;

    // Payment method types
    public static final String METHOD_CARD = "card";
    public static final String METHOD_MOBILE_MONEY = "mobile_money";
    public static final String METHOD_CASH = "cash";
    public static final String METHOD_PAYPAL = "paypal";

    public PaymentMethod() {
        // Required for Firebase
    }

    public PaymentMethod(String id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}