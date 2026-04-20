package com.example.tap2shop.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater; // Added
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // Added
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tap2shop.R;
import com.example.tap2shop.model.PaymentMethod;
import com.example.tap2shop.model.RemoteOrder;
import com.example.tap2shop.ui.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends BaseActivity {

    private EditText etAddress, etPhone;
    private TextView tvOrderSummary, tvTotalAmount;
    private Button btnPlaceOrder;
    private RecyclerView recyclerPaymentMethods;
    private LinearLayout layoutDynamicInputs; // Added
    private PaymentMethodAdapter paymentAdapter;
    private List<PaymentMethod> paymentMethodList = new ArrayList<>();
    private PaymentMethod selectedPaymentMethod;

    private double cartTotal = 0.0;
    private long localOrderId;
    private static final String TAG = "CheckoutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartTotal = getIntent().getDoubleExtra("cart_total", 0.0);
        localOrderId = System.currentTimeMillis();

        initializeViews();
        setupPaymentMethods();
        setupClickListeners();
        updateOrderSummary();
    }

    private void initializeViews() {
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        tvOrderSummary = findViewById(R.id.tvOrderSummary);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        recyclerPaymentMethods = findViewById(R.id.recyclerPaymentMethods);
        layoutDynamicInputs = findViewById(R.id.layoutDynamicInputs); // Initialize dynamic container

        recyclerPaymentMethods.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupPaymentMethods() {
        paymentMethodList.add(new PaymentMethod(
                PaymentMethod.METHOD_CARD, "Credit / Debit Card", "ic_credit_card"));
        paymentMethodList.add(new PaymentMethod(
                PaymentMethod.METHOD_MOBILE_MONEY, "Mobile Money (Airtel, TNM)", "ic_mobile_money"));
        paymentMethodList.add(new PaymentMethod(
                PaymentMethod.METHOD_CASH, "Cash on Delivery", "ic_cash"));
        paymentMethodList.add(new PaymentMethod(
                PaymentMethod.METHOD_PAYPAL, "PayPal", "ic_paypal"));

        // Updated Adapter setup with showSpecificPaymentInputs
        paymentAdapter = new PaymentMethodAdapter(paymentMethodList, method -> {
            selectedPaymentMethod = method;
            enablePlaceOrderButton();
            showSpecificPaymentInputs(method); // Trigger the dynamic fields
        });

        recyclerPaymentMethods.setAdapter(paymentAdapter);
    }

    // New Method to inflate dynamic layouts based on selection
    private void showSpecificPaymentInputs(PaymentMethod method) {
        layoutDynamicInputs.removeAllViews(); // Clear previous inputs
        layoutDynamicInputs.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        if (PaymentMethod.METHOD_CARD.equals(method.getId())) {
            inflater.inflate(R.layout.layout_input_card, layoutDynamicInputs);
        }
        else if (PaymentMethod.METHOD_MOBILE_MONEY.equals(method.getId())) {
            inflater.inflate(R.layout.layout_input_mobile_money, layoutDynamicInputs);
        }
        else if (PaymentMethod.METHOD_CASH.equals(method.getId())) {
            TextView tv = new TextView(this);
            tv.setText("You can pay when your order arrives.");
            tv.setPadding(0, 20, 0, 20);
            layoutDynamicInputs.addView(tv);
        }
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(v -> {
            if (validateInputs()) {
                processOrder();
            }
        });
    }

    private void updateOrderSummary() {
        String summary = "Order #" + localOrderId + "\n" +
                "Date: " + getCurrentDateTime() + "\n" +
                "Items: " + getIntent().getIntExtra("item_count", 0) + " items";
        tvOrderSummary.setText(summary);
        tvTotalAmount.setText(String.format(Locale.getDefault(), "K %.2f", cartTotal));
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean validateInputs() {
        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("Address is required");
            return false;
        }
        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Phone number is required");
            return false;
        }
        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Note: You can add extra validation here for the dynamic fields if needed
        return true;
    }

    private void enablePlaceOrderButton() {
        btnPlaceOrder.setEnabled(true);
        btnPlaceOrder.setAlpha(1.0f);
    }

    private void processOrder() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to place order", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setTitle("Processing Order")
                .setMessage("Please wait...")
                .setCancelable(false)
                .show();

        RemoteOrder order = new RemoteOrder();
        order.setLocalOrderId(localOrderId);
        order.setTotalAmount(cartTotal);
        order.setAddress(etAddress.getText().toString().trim());
        order.setPhone(etPhone.getText().toString().trim());
        order.setTimestamp(System.currentTimeMillis());
        order.setStatus("PENDING");

        CollectionReference ordersRef = FirebaseFirestore.getInstance()
                .collection("orders")
                .document(user.getUid())
                .collection("user_orders");

        ordersRef.add(order)
                .addOnSuccessListener(documentReference -> {
                    loadingDialog.dismiss();
                    showPaymentProcessingDialog();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showPaymentProcessingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment");

        String message;
        switch (selectedPaymentMethod.getId()) {
            case PaymentMethod.METHOD_CARD:
                message = "Enter card details to complete payment";
                break;
            case PaymentMethod.METHOD_MOBILE_MONEY:
                message = "You will receive a prompt on your phone to complete the transaction.";
                break;
            case PaymentMethod.METHOD_CASH:
                message = "Order confirmed! Please have the exact amount ready for delivery.";
                break;
            case PaymentMethod.METHOD_PAYPAL:
                message = "Redirecting to PayPal secure checkout...";
                break;
            default:
                message = "Processing payment...";
        }

        builder.setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> navigateToOrderConfirmation())
                .setCancelable(false)
                .show();
    }

    private void navigateToOrderConfirmation() {
        Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
        intent.putExtra("order_id", localOrderId);
        intent.putExtra("total_amount", cartTotal);
        intent.putExtra("payment_method", selectedPaymentMethod.getName());
        startActivity(intent);
        finish();
    }
}