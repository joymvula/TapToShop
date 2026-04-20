package com.example.tap2shop.ui.checkout;


import android.os.Bundle;
import android.widget.TextView;

import com.example.tap2shop.R;
import com.example.tap2shop.ui.BaseActivity;

public class OrderConfirmationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        long orderId = getIntent().getLongExtra("order_id", -1);
        double total = getIntent().getDoubleExtra("order_total", 0);

        TextView tvOrderId = findViewById(R.id.tvOrderId);
        TextView tvOrderTotal = findViewById(R.id.tvOrderTotal);

        tvOrderId.setText(getString(R.string.label_order_id, orderId));
        tvOrderTotal.setText(String.format("K %.2f", total));
    }
}
