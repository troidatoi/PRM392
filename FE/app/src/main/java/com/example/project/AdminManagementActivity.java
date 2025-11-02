package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminManagementActivity extends AppCompatActivity {

    private TextView tvTotalSales, tvOrderUser, tvTotalProducts, tvTotalUsers;
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconDashboard, iconUserManagement, iconProductManagement, iconStoreManagement, iconOrderManagement, iconChatManagement;
    private TextView tvDashboard, tvUserManagement, tvProductManagement, tvStoreManagement, tvOrderManagement, tvChatManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        initViews();
        loadStatistics();
        setupClickListeners();
    }

    private void initViews() {
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvOrderUser = findViewById(R.id.tvOrderUser);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);

        // Bottom navigation views
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);

        iconDashboard = findViewById(R.id.iconDashboard);
        iconUserManagement = findViewById(R.id.iconUserManagement);
        iconProductManagement = findViewById(R.id.iconProductManagement);
        iconStoreManagement = findViewById(R.id.iconStoreManagement);
        iconOrderManagement = findViewById(R.id.iconOrderManagement);
        iconChatManagement = findViewById(R.id.iconChatManagement);

        tvDashboard = findViewById(R.id.tvDashboard);
        tvUserManagement = findViewById(R.id.tvUserManagement);
        tvProductManagement = findViewById(R.id.tvProductManagement);
        tvStoreManagement = findViewById(R.id.tvStoreManagement);
        tvOrderManagement = findViewById(R.id.tvOrderManagement);
        tvChatManagement = findViewById(R.id.tvChatManagement);

        // Mark current tab active
        if (iconDashboard != null) {
            iconDashboard.setColorFilter(0xFF2196F3); // active blue
        }
        if (tvDashboard != null) {
            tvDashboard.setTextColor(0xFF2196F3); // active blue
        }
    }

    private void loadStatistics() {
        // TODO: Load real statistics from database
        tvTotalSales.setText("#1F2937");
        tvOrderUser.setText("#PB2F0C9");
        tvTotalProducts.setText("7FE3D1");
        tvTotalUsers.setText("#BB2F0C9");
    }

    private void setupClickListeners() {
        // No management buttons in new design - only bottom navigation

        // Bottom navigation actions
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                // Already here
            });
        }
        if (navUserManagement != null) {
            navUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, UserManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navProductManagement != null) {
            navProductManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, ProductManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navStoreManagement != null) {
            navStoreManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, StoreManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navOrderManagement != null) {
            navOrderManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, AdminOrderListActivity.class);
                startActivity(intent);
            });
        }
        if (navChatManagement != null) {
            navChatManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminManagementActivity.this, AdminChatListActivity.class);
                startActivity(intent);
            });
        }
    }
}
