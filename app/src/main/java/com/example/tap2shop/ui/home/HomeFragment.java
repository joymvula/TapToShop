package com.example.tap2shop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tap2shop.R;
import com.example.tap2shop.data.CartRepository;
import com.example.tap2shop.data.ProductRepository;
import com.example.tap2shop.model.Product;
import com.example.tap2shop.ui.product.ProductDetailsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Added
import com.google.android.material.snackbar.Snackbar; // Added

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private ProductAdapter adapter;
    private View emptyView;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerProducts);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.productProgressBar);

        productRepository = new ProductRepository(requireContext());
        cartRepository = new CartRepository(requireContext());

        adapter = new ProductAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadData();

        return view;
    }

    private void loadData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        List<Product> localProducts = productRepository.getAllProducts();
        if (!localProducts.isEmpty()) {
            adapter.setProducts(localProducts);
            emptyView.setVisibility(View.GONE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }

        productRepository.getProducts(progressBar, products -> {
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (products != null && !products.isEmpty()) {
                        adapter.setProducts(products);
                        emptyView.setVisibility(View.GONE);
                    } else if (adapter.getItemCount() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        // 1. Logic to add to cart repository
        cartRepository.addToCart(product.getId());

        // 2. Localized message with product name
        String message = getString(R.string.msg_added_to_cart, product.getName());

        // 3. Snackbar with 'VIEW' action to switch to Cart fragment
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.menu_cart), v -> { // Uses your existing "Cart" string
                    if (getActivity() != null) {
                        // Switch the Bottom Navigation selection to 'Cart'
                        // Inside onAddToCartClick method
                        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
                        if (bottomNav != null) {
                            bottomNav.setSelectedItemId(R.id.menu_cart);
                        }
                    }
                })
                .show();
    }
}