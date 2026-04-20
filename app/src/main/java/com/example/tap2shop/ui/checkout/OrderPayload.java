package com.example.tap2shop.ui.checkout;
public class OrderPayload {
    public long localOrderId;
    public double totalAmount;
    public String address;
    public String phone;
    public long timestamp;
    public String status;

    public OrderPayload() {
        // Default constructor required for calls to DataSnapshot.getValue(OrderPayload.class)
    }

    public OrderPayload(long localOrderId, double totalAmount, String address,
                        String phone, long timestamp, String status) {
        this.localOrderId = localOrderId;
        this.totalAmount = totalAmount;
        this.address = address;
        this.phone = phone;
        this.timestamp = timestamp;
        this.status = status;
    }
}
