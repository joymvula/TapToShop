package com.example.tap2shop.ui.orders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tap2shop.R;
import com.example.tap2shop.model.RemoteOrder;
import com.example.tap2shop.ui.BaseActivity;
import com.example.tap2shop.ui.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends BaseActivity implements MyOrdersAdapter.OnOrderClickListener {

    private RecyclerView recyclerOrders;
    private LinearLayout layoutEmptyOrders;
    private TextView tvEmptyOrders;
    private MyOrdersAdapter adapter;
    private final List<RemoteOrder> orderList = new ArrayList<>();
    private static final String TAG = "MyOrdersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initializeViews();
        setupClickListeners();
        setupRecyclerView();

        // Add test button (remove after testing)
        // addTestButton();

        loadOrdersFromFirestore();
    }

    private void initializeViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders);
        layoutEmptyOrders = findViewById(R.id.layoutEmptyOrders);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
    }

    private void setupClickListeners() {
        Button btnShopNow = findViewById(R.id.btnShopNow);
        TextView tvTapToShop = findViewById(R.id.tvTapToShop);
        Button btnBackToHomeHeader = findViewById(R.id.btnBackToHomeHeader);

        if (tvTapToShop != null) tvTapToShop.setOnClickListener(v -> navigateToHome());
        if (btnBackToHomeHeader != null) btnBackToHomeHeader.setOnClickListener(v -> navigateToHome());
        if (btnShopNow != null) btnShopNow.setOnClickListener(v -> navigateToHome());
    }

    private void setupRecyclerView() {
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyOrdersAdapter(orderList, this, this);
        recyclerOrders.setAdapter(adapter);
    }

    // Temporary test button (remove after testing)
    private void addTestButton() {
        Button btnTest = new Button(this);
        btnTest.setText("ADD TEST ORDER");
        btnTest.setOnClickListener(v -> addTestOrder());
        ((LinearLayout) findViewById(R.id.layoutEmptyOrders)).addView(btnTest);
    }

    private void addTestOrder() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Login first", Toast.LENGTH_SHORT).show();
            return;
        }

        RemoteOrder testOrder = new RemoteOrder();
        testOrder.setLocalOrderId(System.currentTimeMillis());
        testOrder.setTotalAmount(99.99);
        testOrder.setAddress("Test Address, Lilongwe");
        testOrder.setPhone("0999123456");
        testOrder.setTimestamp(System.currentTimeMillis());
        testOrder.setStatus("TEST_ORDER");

        FirebaseFirestore.getInstance()
                .collection("orders")
                .document(user.getUid())
                .collection("user_orders")
                .add(testOrder)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "✅ TEST ORDER ADDED: " + ref.getId());
                    Toast.makeText(this, "Test order added!", Toast.LENGTH_SHORT).show();
                    loadOrdersFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ TEST ORDER FAILED: " + e.getMessage());
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MyOrdersActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadOrdersFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e(TAG, "User is null - not logged in");
            showEmptyState(true, R.string.error_not_logged_in);
            return;
        }

        Log.d(TAG, "========== LOADING ORDERS FROM FIRESTORE ==========");
        Log.d(TAG, "Loading orders for user: " + user.getUid());
        Log.d(TAG, "User email: " + user.getEmail());

        CollectionReference ordersCollection = FirebaseFirestore.getInstance()
                .collection("orders")
                .document(user.getUid())
                .collection("user_orders");

        Query ordersQuery = ordersCollection.orderBy("timestamp", Query.Direction.DESCENDING);

        ordersQuery.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "❌ Firestore error: " + error.getMessage());
                showEmptyState(true, R.string.error_load_orders);
                return;
            }

            orderList.clear();

            if (snapshots == null || snapshots.isEmpty()) {
                Log.d(TAG, "No orders found in Firestore");
                showEmptyState(true, R.string.no_orders_message);
                return;
            }

            Log.d(TAG, "Found " + snapshots.size() + " orders in Firestore");

            for (QueryDocumentSnapshot document : snapshots) {
                try {
                    RemoteOrder order = document.toObject(RemoteOrder.class);
                    order.setId(document.getId());
                    orderList.add(order);
                    Log.d(TAG, "✅ Order added - ID: " + order.getId() +
                            ", LocalID: " + order.getLocalOrderId() +
                            ", Total: K" + order.getTotalAmount());
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing order: " + document.getId(), e);
                }
            }

            if (!orderList.isEmpty()) {
                showEmptyState(false, 0);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "✅ Displaying " + orderList.size() + " orders");
            } else {
                showEmptyState(true, R.string.no_orders_message);
            }
        });
    }

    private void showEmptyState(boolean show, int messageResId) {
        if (show) {
            layoutEmptyOrders.setVisibility(View.VISIBLE);
            recyclerOrders.setVisibility(View.GONE);
            if (messageResId != 0) tvEmptyOrders.setText(messageResId);
        } else {
            layoutEmptyOrders.setVisibility(View.GONE);
            recyclerOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOrderClick(RemoteOrder order) {
        String details = getString(R.string.label_status) + ": " + order.getStatus() + "\n" +
                getString(R.string.label_address) + ": " + order.getAddress() + "\n" +
                getString(R.string.label_phone) + ": " + order.getPhone();

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.label_order_id, order.getLocalOrderId()))
                .setMessage(details)
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }

    @Override
    public void onBackToHomeClick() {
        navigateToHome();
    }
}