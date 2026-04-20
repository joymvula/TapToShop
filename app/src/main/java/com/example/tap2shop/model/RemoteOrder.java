package com.example.tap2shop.model;

public class RemoteOrder {
    private String id;          // Firebase key
    private long localOrderId;
    private double totalAmount;
    private String address;
    private String phone;
    private long timestamp;
    private String status;

    public RemoteOrder() {
        // Required for Firebase DataSnapshot.getValue(RemoteOrder.class)
    }

    // Getters
    public String getId() { return id; }
    public long getLocalOrderId() { return localOrderId; }
    public double getTotalAmount() { return totalAmount; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setLocalOrderId(long localOrderId) { this.localOrderId = localOrderId; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "RemoteOrder{" +
                "id='" + id + '\'' +
                ", localOrderId=" + localOrderId +
                ", totalAmount=" + totalAmount +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}