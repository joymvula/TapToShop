package com.example.tap2shop.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tap2shop.R;
import com.example.tap2shop.model.PaymentMethod;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentViewHolder> {

    private List<PaymentMethod> paymentMethods;
    private OnPaymentMethodSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnPaymentMethodSelectedListener {
        void onPaymentMethodSelected(PaymentMethod method);
    }

    public PaymentMethodAdapter(List<PaymentMethod> paymentMethods, OnPaymentMethodSelectedListener listener) {
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentMethod method = paymentMethods.get(position);
        holder.tvPaymentName.setText(method.getName());

        // Set icon based on payment method type
        String methodId = method.getId();
        if (PaymentMethod.METHOD_CARD.equals(methodId)) {
            holder.ivPaymentIcon.setImageResource(R.drawable.ic_credit_card);
        } else if (PaymentMethod.METHOD_MOBILE_MONEY.equals(methodId)) {
            holder.ivPaymentIcon.setImageResource(R.drawable.ic_mobile_money);
        } else if (PaymentMethod.METHOD_CASH.equals(methodId)) {
            holder.ivPaymentIcon.setImageResource(R.drawable.ic_cash);
        } else if (PaymentMethod.METHOD_PAYPAL.equals(methodId)) {
            holder.ivPaymentIcon.setImageResource(R.drawable.ic_paypal);
        } else {
            holder.ivPaymentIcon.setImageResource(R.drawable.ic_credit_card);
        }

        // Set radio button state based on selection
        holder.rbSelect.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int previous = selectedPosition;
            selectedPosition = adapterPosition;

            // Refresh the previous and new selected items to update RadioButton UI
            if (previous != -1) notifyItemChanged(previous);
            notifyItemChanged(adapterPosition);

            for (int i = 0; i < paymentMethods.size(); i++) {
                paymentMethods.get(i).setSelected(i == adapterPosition);
            }

            if (listener != null) {
                listener.onPaymentMethodSelected(paymentMethods.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods != null ? paymentMethods.size() : 0;
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPaymentIcon;
        TextView tvPaymentName;
        RadioButton rbSelect;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPaymentIcon = itemView.findViewById(R.id.ivPaymentIcon);
            tvPaymentName = itemView.findViewById(R.id.tvPaymentName);
            rbSelect = itemView.findViewById(R.id.rbSelect);

            // This makes sure the click passes through the radio button to the whole row
            // This is critical for a smooth user experience in Tap2Shop
            rbSelect.setClickable(false);
            rbSelect.setFocusable(false);
        }
    }
}