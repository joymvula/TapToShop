package com.example.tap2shop.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tap2shop.R;
import com.example.tap2shop.data.CartRepository;
import com.example.tap2shop.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemChangedListener {
        void onCartUpdated();
    }

    private final List<CartItem> items;
    private final OnCartItemChangedListener listener;
    private final CartRepository cartRepository;

    public CartAdapter(List<CartItem> items,
                       OnCartItemChangedListener listener,
                       CartRepository cartRepository) {
        this.items = items;
        this.listener = listener;
        this.cartRepository = cartRepository;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);

        // Display product details
        holder.tvName.setText(item.getProduct().getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText(String.format("K %.2f", item.getTotalPrice()));

        // Increase Quantity Button
        holder.btnPlus.setOnClickListener(v -> {
            // item.getProduct().getId() now returns a String,
            // matching the updated CartRepository.addToCart(String productId)
            cartRepository.addToCart(item.getProduct().getId());
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(holder.getAdapterPosition());
            listener.onCartUpdated();
        });

        // Decrease Quantity Button
        holder.btnMinus.setOnClickListener(v -> {
            // item.getProduct().getId() now returns a String,
            // matching the updated CartRepository.removeFromCart(String productId)
            cartRepository.removeFromCart(item.getProduct().getId());
            int newQty = item.getQuantity() - 1;

            if (newQty <= 0) {
                int pos = holder.getAdapterPosition();
                items.remove(pos);
                notifyItemRemoved(pos);
            } else {
                item.setQuantity(newQty);
                notifyItemChanged(holder.getAdapterPosition());
            }
            listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice;
        ImageButton btnPlus, btnMinus;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            btnPlus = itemView.findViewById(R.id.btnIncrease);
            btnMinus = itemView.findViewById(R.id.btnDecrease);
        }
    }
}