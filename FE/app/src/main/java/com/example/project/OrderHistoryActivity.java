package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private CardView btnBack;
    private RecyclerView rvOrders;
    private LinearLayout emptyState;

    private OrderAdapter orderAdapter;
    private List<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        setupRecyclerView();
        loadOrders();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrders);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        orders = new ArrayList<>();
        orderAdapter = new OrderAdapter(orders, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOrders.setLayoutManager(layoutManager);
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        // TODO: Load real orders from database
        // Sample orders
        orders.add(new Order(
                "12345",
                "20/10/2025",
                "Đã giao",
                "Xe đạp điện VinFast Klara S x1\nPhụ kiện bảo vệ xe x2",
                "30.500.000 ₫",
                "#4CAF50"
        ));

        orders.add(new Order(
                "12344",
                "18/10/2025",
                "Đang giao",
                "Xe đạp địa hình MTB Pro x1",
                "8.500.000 ₫",
                "#2196F3"
        ));

        orders.add(new Order(
                "12343",
                "15/10/2025",
                "Đã giao",
                "Xe đạp thể thao Road Bike x1\nMũ bảo hiểm x1",
                "15.200.000 ₫",
                "#4CAF50"
        ));

        orders.add(new Order(
                "12342",
                "12/10/2025",
                "Đã hủy",
                "Xe đạp gấp Mini x1",
                "5.800.000 ₫",
                "#F44336"
        ));

        orders.add(new Order(
                "12341",
                "10/10/2025",
                "Đã giao",
                "Xe đạp trẻ em Disney x1\nBình nước x1",
                "3.500.000 ₫",
                "#4CAF50"
        ));

        if (orders.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }

        orderAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
