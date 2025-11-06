package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

public class OrderManagementActivity extends AppCompatActivity {

    private CardView btnBack;
    private RecyclerView rvOrders;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private TextView tvTotalOrders, tvPendingOrders, tvCompletedOrders;
    private CardView btnFilterAll, btnFilterPending, btnFilterProcessing;
    private CardView btnFilterShipping, btnFilterCompleted, btnFilterCancelled;
    
    private ApiService apiService;
    private AuthManager authManager;
    
    // Bottom navigation
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconOrderManagement;
    private TextView tvOrderManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        initViews();
        initData();
        setupClickListeners();
        setupBottomNavigation();
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrders);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPending = findViewById(R.id.btnFilterPending);
        btnFilterProcessing = findViewById(R.id.btnFilterProcessing);
        btnFilterShipping = findViewById(R.id.btnFilterShipping);
        btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        btnFilterCancelled = findViewById(R.id.btnFilterCancelled);
        
        // Bottom navigation
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);
        iconOrderManagement = findViewById(R.id.iconOrderManagement);
        tvOrderManagement = findViewById(R.id.tvOrderManagement);
    }

    private void initData() {
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // Filter buttons
        btnFilterAll.setOnClickListener(v -> filterOrders("all"));
        btnFilterPending.setOnClickListener(v -> filterOrders("pending"));
        btnFilterProcessing.setOnClickListener(v -> filterOrders("processing"));
        btnFilterShipping.setOnClickListener(v -> filterOrders("shipping"));
        btnFilterCompleted.setOnClickListener(v -> filterOrders("completed"));
        btnFilterCancelled.setOnClickListener(v -> filterOrders("cancelled"));
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        rvOrders.setVisibility(View.GONE);

        // Check if user is admin or staff
        if (!authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Simulate loading orders (replace with actual API call)
        progressBar.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            
            // For now, show empty state
            // TODO: Implement actual order loading from API
            emptyState.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
            
            // Set sample stats
            tvTotalOrders.setText("0");
            tvPendingOrders.setText("0");
            tvCompletedOrders.setText("0");
        }, 1000);
    }

    private void filterOrders(String filter) {
        // Reset all filter buttons
        resetFilterButtons();
        
        // Highlight selected filter
        CardView selectedButton = null;
        switch (filter) {
            case "all":
                selectedButton = btnFilterAll;
                break;
            case "pending":
                selectedButton = btnFilterPending;
                break;
            case "processing":
                selectedButton = btnFilterProcessing;
                break;
            case "shipping":
                selectedButton = btnFilterShipping;
                break;
            case "completed":
                selectedButton = btnFilterCompleted;
                break;
            case "cancelled":
                selectedButton = btnFilterCancelled;
                break;
        }
        
        if (selectedButton != null) {
            selectedButton.setCardBackgroundColor(getResources().getColor(R.color.primary_blue));
            ((TextView) selectedButton.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
        }
        
        // TODO: Implement actual filtering logic
        Toast.makeText(this, "Lọc: " + filter, Toast.LENGTH_SHORT).show();
    }

    private void resetFilterButtons() {
        CardView[] buttons = {btnFilterAll, btnFilterPending, btnFilterProcessing, 
                              btnFilterShipping, btnFilterCompleted, btnFilterCancelled};
        
        for (CardView button : buttons) {
            button.setCardBackgroundColor(getResources().getColor(R.color.background_light_gray));
            ((TextView) button.getChildAt(0)).setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    private void setupBottomNavigation() {
        // Set Order Management tab as active
        if (iconOrderManagement != null) {
            iconOrderManagement.setColorFilter(0xFF2196F3); // active blue
        }
        if (tvOrderManagement != null) {
            tvOrderManagement.setTextColor(0xFF2196F3); // active blue
        }

        // Navigation click listeners
        navDashboard.setOnClickListener(v -> {
            startActivity(new Intent(OrderManagementActivity.this, AdminManagementActivity.class));
            finish();
        });

        navUserManagement.setOnClickListener(v -> {
            startActivity(new Intent(OrderManagementActivity.this, UserManagementActivity.class));
            finish();
        });

        navProductManagement.setOnClickListener(v -> {
            startActivity(new Intent(OrderManagementActivity.this, ProductManagementActivity.class));
            finish();
        });

        navStoreManagement.setOnClickListener(v -> {
            startActivity(new Intent(OrderManagementActivity.this, StoreManagementActivity.class));
            finish();
        });

        navOrderManagement.setOnClickListener(v -> {
            // Already on this page
        });

        navChatManagement.setOnClickListener(v -> {
            startActivity(new Intent(OrderManagementActivity.this, AdminChatListActivity.class));
            finish();
        });
    }
}
