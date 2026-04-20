package com.example.tap2shop.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tap2shop.R;
import com.example.tap2shop.data.CartRepository;
import com.example.tap2shop.model.CartItem;
import com.example.tap2shop.ui.BaseActivity;
import com.example.tap2shop.ui.checkout.CheckoutActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CartActivity extends BaseActivity implements CartAdapter.OnCartItemChangedListener {

    private CartRepository cartRepository;
    private RecyclerView recyclerCart;
    private TextView tvCartTotal, tvEmptyCart;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize Repository
        cartRepository = new CartRepository(this);

        // Initialize Views
        recyclerCart = findViewById(R.id.recyclerCart);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);
        View btnContinueShopping = findViewById(R.id.btnContinueShopping);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));

        // Load data and display
        updateCartDisplay();

        // Pass data to CheckoutActivity
        btnCheckout.setOnClickListener(v -> {
            List<CartItem> items = cartRepository.getCartItems();

            if (items.isEmpty()) {
                // Prevent going to checkout with an empty cart
                com.google.android.material.snackbar.Snackbar.make(v,
                        "Your cart is empty!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);

            // CRITICAL: Put the data into the intent so CheckoutActivity can see it
            intent.putExtra("cart_total", cartRepository.getCartTotal());
            intent.putExtra("item_count", items.size());

            startActivity(intent);
        });

        if (btnContinueShopping != null) {
            btnContinueShopping.setOnClickListener(v -> goToHome());
        }
    }

    private void updateCartDisplay() {
        List<CartItem> items = cartRepository.getCartItems();

        // Update adapter
        adapter = new CartAdapter(items, this, cartRepository);
        recyclerCart.setAdapter(adapter);

        // Update Total Text (formatted for Kwacha)
        double total = cartRepository.getCartTotal();
        tvCartTotal.setText(String.format("K %.2f", total));

        // Toggle Visibility based on content
        if (items.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            recyclerCart.setVisibility(View.GONE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            recyclerCart.setVisibility(View.VISIBLE);
        }
    }

    public void goToHome() {
        // finish() takes the user back to the previous screen (likely MainActivity/Home)
        finish();
    }

    @Override
    public void onCartUpdated() {
        // This is called by the Adapter when items are added/removed
        updateCartDisplay();
    }
}