package com.example.tap2shop.ui.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tap2shop.R;
import com.example.tap2shop.model.RemoteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.OrderViewHolder> {

    private List<RemoteOrder> orders;
    private OnOrderClickListener listener;
    private Context context;

    public interface OnOrderClickListener {
        void onOrderClick(RemoteOrder order);
        void onBackToHomeClick();
    }

    public MyOrdersAdapter(List<RemoteOrder> orders, OnOrderClickListener listener, Context context) {
        this.orders = orders;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        RemoteOrder order = orders.get(position);

        // Set order ID - use localOrderId for display
        holder.tvOrderId.setText("Nambala ya kugula #" + order.getLocalOrderId());

        // Format and set date from timestamp
        if (order.getTimestamp() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(order.getTimestamp()));
            holder.tvOrderDate.setText(formattedDate);
        } else {
            holder.tvOrderDate.setText("Date not available");
        }

        // Set total amount
        holder.tvOrderTotal.setText(String.format(Locale.getDefault(), "K %.2f", order.getTotalAmount()));

        // Set status
        if (order.getStatus() != null && !order.getStatus().isEmpty()) {
            holder.tvOrderStatus.setText(order.getStatus());
        } else {
            holder.tvOrderStatus.setText("PENDING");
        }

        // Click listener for entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });

        // View details button
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });

        // Back to home button
        holder.btnBackToHome.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBackToHomeClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId;
        TextView tvOrderDate;
        TextView tvOrderTotal;
        TextView tvOrderStatus;
        Button btnBackToHome;
        Button btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnBackToHome = itemView.findViewById(R.id.btnBackToHome);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}