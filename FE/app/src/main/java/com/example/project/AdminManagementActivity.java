package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminManagementActivity extends AppCompatActivity {

    private CardView btnBack, btnNotifications;
    private CardView btnManageProducts, btnManageStores, btnManageOrders, btnManageUsers, btnManageChat;
    private TextView tvTotalProducts, tvTotalOrders, tvTotalRevenue, tvTotalUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        initViews();
        loadStatistics();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotifications = findViewById(R.id.btnNotifications);

        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnManageStores = findViewById(R.id.btnManageStores);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageChat = findViewById(R.id.btnManageChat);

        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
    }

    private void loadStatistics() {
        // TODO: Load real statistics from database
        tvTotalProducts.setText("245");
        tvTotalOrders.setText("1,234");
        tvTotalRevenue.setText("15.5M");
        tvTotalUsers.setText("856");
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Notifications button
        btnNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show();
        });

        // Manage Products
        btnManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManagementActivity.this, ProductManagementActivity.class);
            startActivity(intent);
        });

        // Manage Stores/Branches
        btnManageStores.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManagementActivity.this, StoreManagementActivity.class);
            startActivity(intent);
        });

        // Manage Orders
        btnManageOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        // Manage Users
        btnManageUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        // Manage Chat - NEW
        btnManageChat.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManagementActivity.this, AdminChatListActivity.class);
            startActivity(intent);
        });
    }
}
