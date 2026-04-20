package com.example.tap2shop.ui.product;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.tap2shop.R;
import com.example.tap2shop.data.CartRepository;
import com.example.tap2shop.data.ProductRepository;
import com.example.tap2shop.model.Product;
import com.example.tap2shop.ui.BaseActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class ProductDetailsActivity extends BaseActivity {

    private CartRepository cartRepository;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // FIX 1: Get String instead of long from Intent
        String productId = getIntent().getStringExtra("product_id");

        // FIX 2: productRepository converted to local variable (cleans up line 17 warning)
        ProductRepository productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);

        // FIX 3: Now matches the String signature in ProductRepository
        if (productId != null) {
            product = productRepository.getProductById(productId);
        }

        TextView tvName = findViewById(R.id.tvProductName);
        TextView tvDesc = findViewById(R.id.tvProductDescription);
        TextView tvPrice = findViewById(R.id.tvProductPrice);
        Button btnAddToCart = findViewById(R.id.btnAddToCart);

        if (product != null) {
            tvName.setText(product.getName());
            tvDesc.setText(product.getDescription());
            // FIX 4: Added Locale.getDefault() to solve the lint warning
            tvPrice.setText(String.format(Locale.getDefault(), "K %.2f", product.getPrice()));
        }

        btnAddToCart.setOnClickListener(v -> {
            if (product != null) {
                cartRepository.addToCart(product.getId());
                Snackbar.make(v, R.string.message_added_to_cart, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
