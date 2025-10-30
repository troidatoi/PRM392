package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OrderDetailActivity extends AppCompatActivity {

    private CardView btnBack, btnContactSupport;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvPaymentMethod;
    private TextView tvReceiverName, tvReceiverPhone, tvShippingAddress;
    private TextView tvSubtotal, tvShippingFee, tvTotal;
    private RecyclerView rvOrderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_new);

        initViews();
        setupListeners();
        loadOrderData();
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        btnContactSupport = findViewById(R.id.btnContactSupport);

        // Order Info
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);

        // Shipping Address
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);

        // Order Summary
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);

        // RecyclerView
        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement contact support functionality
                // For example: open chat, call support, or send email
            }
        });
    }

    private void loadOrderData() {
        // Get order ID from intent
        String orderId = getIntent().getStringExtra("ORDER_ID");

        if (orderId == null) {
            orderId = "12345";
        }

        // Mock data - Replace with actual API call
        tvOrderId.setText("#" + orderId);
        tvOrderDate.setText("30/10/2025 14:30");
        tvOrderStatus.setText("✓ Đã giao");
        tvPaymentMethod.setText("💵 COD");

        tvReceiverName.setText("Nguyễn Văn A");
        tvReceiverPhone.setText("0912345678");
        tvShippingAddress.setText("123 Đường ABC, Phường XYZ, Quận 1, TP.HCM");

        tvSubtotal.setText("30.000.000 ₫");
        tvShippingFee.setText("✓ Miễn phí");
        tvTotal.setText("30.000.000 ₫");

        // TODO: Setup RecyclerView adapter with order items
        // You can reuse CheckoutProductAdapter or create a new one
        // CheckoutProductAdapter adapter = new CheckoutProductAdapter(orderItems);
        // rvOrderItems.setAdapter(adapter);
    }
}

