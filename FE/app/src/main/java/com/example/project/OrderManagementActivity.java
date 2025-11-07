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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter orderAdapter;
    private String currentFilter = null;
    
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
        
        // Setup RecyclerView
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(orderList, this);
        rvOrders.setAdapter(orderAdapter);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // Filter buttons
        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> filterOrders("all"));
        }
        if (btnFilterPending != null) {
            btnFilterPending.setOnClickListener(v -> filterOrders("pending"));
        }
        if (btnFilterProcessing != null) {
            btnFilterProcessing.setOnClickListener(v -> filterOrders("processing"));
        }
        if (btnFilterShipping != null) {
            btnFilterShipping.setOnClickListener(v -> filterOrders("shipping"));
        }
        if (btnFilterCompleted != null) {
            btnFilterCompleted.setOnClickListener(v -> filterOrders("completed"));
        }
        if (btnFilterCancelled != null) {
            btnFilterCancelled.setOnClickListener(v -> filterOrders("cancelled"));
        }
    }

    private void loadOrders() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (rvOrders != null) {
            rvOrders.setVisibility(View.GONE);
        }

        // Check if user is admin or staff
        if (!authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Call API to get orders
        Call<ApiService.OrderResponse> call = apiService.getAllOrders(
            authHeader,
            currentFilter,  // status filter
            1,              // page
            100             // limit
        );

        call.enqueue(new Callback<ApiService.OrderResponse>() {
            @Override
            public void onResponse(Call<ApiService.OrderResponse> call, Response<ApiService.OrderResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.OrderResponse orderResponse = response.body();
                    
                    if (orderResponse.isSuccess() && orderResponse.getData() != null) {
                        orderList.clear();
                        
                        // Convert API order data to Order objects
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        
                        for (ApiService.OrderData orderData : orderResponse.getData()) {
                            String orderId = orderData.getId();
                            String orderNumber = orderData.getOrderNumber() != null ? orderData.getOrderNumber() : orderId;
                            
                            // Format date
                            String orderDate = orderData.getCreatedAt();
                            try {
                                Date date = inputFormat.parse(orderData.getCreatedAt());
                                orderDate = outputFormat.format(date);
                            } catch (Exception e) {
                                // Keep original date if parsing fails
                            }
                            
                            String status = Order.mapStatusText(orderData.getStatus());
                            
                            // Format items
                            String items = "";
                            if (orderData.getItems() != null && !orderData.getItems().isEmpty()) {
                                items = orderData.getItems().size() + " sản phẩm";
                            }
                            
                            // Format total amount
                            String totalAmount = currencyFormat.format(orderData.getTotalAmount());
                            
                            String statusColor = Order.mapStatusColor(orderData.getStatus());
                            
                            Order order = new Order(orderId, orderNumber, orderDate, status, items, totalAmount, statusColor);
                            orderList.add(order);
                        }
                        
                        orderAdapter.notifyDataSetChanged();
                        
                        // Update statistics
                        updateStatistics(orderResponse.getData());
                        
                        // Show/hide empty state
                        if (orderList.isEmpty()) {
                            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                            if (rvOrders != null) rvOrders.setVisibility(View.GONE);
                        } else {
                            if (emptyState != null) emptyState.setVisibility(View.GONE);
                            if (rvOrders != null) rvOrders.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(OrderManagementActivity.this, "Không có đơn hàng", Toast.LENGTH_SHORT).show();
                        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                        updateStatistics(new ArrayList<>());
                    }
                } else {
                    Toast.makeText(OrderManagementActivity.this, "Lỗi tải dữ liệu: " + response.message(), Toast.LENGTH_SHORT).show();
                    if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiService.OrderResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void updateStatistics(List<ApiService.OrderData> orders) {
        int totalCount = orders.size();
        int pendingCount = 0;
        int completedCount = 0;
        
        for (ApiService.OrderData order : orders) {
            String status = order.getStatus();
            if (status != null) {
                status = status.toLowerCase();
                if (status.equals("pending")) {
                    pendingCount++;
                } else if (status.equals("delivered") || status.equals("completed")) {
                    completedCount++;
                }
            }
        }
        
        if (tvTotalOrders != null) tvTotalOrders.setText(String.valueOf(totalCount));
        if (tvPendingOrders != null) tvPendingOrders.setText(String.valueOf(pendingCount));
        if (tvCompletedOrders != null) tvCompletedOrders.setText(String.valueOf(completedCount));
    }

    private void filterOrders(String filter) {
        // Reset all filter buttons
        resetFilterButtons();
        
        // Highlight selected filter
        CardView selectedButton = null;
        switch (filter) {
            case "all":
                selectedButton = btnFilterAll;
                currentFilter = null;  // null means get all
                break;
            case "pending":
                selectedButton = btnFilterPending;
                currentFilter = "pending";
                break;
            case "processing":
                selectedButton = btnFilterProcessing;
                currentFilter = "processing";
                break;
            case "shipping":
                selectedButton = btnFilterShipping;
                currentFilter = "shipping";
                break;
            case "completed":
                selectedButton = btnFilterCompleted;
                currentFilter = "delivered";  // API uses 'delivered' for completed
                break;
            case "cancelled":
                selectedButton = btnFilterCancelled;
                currentFilter = "cancelled";
                break;
        }
        
        if (selectedButton != null) {
            selectedButton.setCardBackgroundColor(getResources().getColor(R.color.primary_blue));
            if (selectedButton.getChildAt(0) instanceof TextView) {
                ((TextView) selectedButton.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
            }
        }
        
        // Reload orders with filter
        loadOrders();
    }

    private void resetFilterButtons() {
        CardView[] buttons = {btnFilterAll, btnFilterPending, btnFilterProcessing, 
                              btnFilterShipping, btnFilterCompleted, btnFilterCancelled};
        
        for (CardView button : buttons) {
            if (button != null) {
                button.setCardBackgroundColor(getResources().getColor(R.color.background_light_gray));
                if (button.getChildAt(0) instanceof TextView) {
                    ((TextView) button.getChildAt(0)).setTextColor(getResources().getColor(R.color.text_secondary));
                }
            }
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
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                startActivity(new Intent(OrderManagementActivity.this, AdminManagementActivity.class));
                finish();
            });
        }

        if (navUserManagement != null) {
            navUserManagement.setOnClickListener(v -> {
                startActivity(new Intent(OrderManagementActivity.this, UserManagementActivity.class));
                finish();
            });
        }

        if (navProductManagement != null) {
            navProductManagement.setOnClickListener(v -> {
                startActivity(new Intent(OrderManagementActivity.this, ProductManagementActivity.class));
                finish();
            });
        }

        if (navStoreManagement != null) {
            navStoreManagement.setOnClickListener(v -> {
                startActivity(new Intent(OrderManagementActivity.this, StoreManagementActivity.class));
                finish();
            });
        }

        if (navOrderManagement != null) {
            navOrderManagement.setOnClickListener(v -> {
                // Already on this page
            });
        }

        if (navChatManagement != null) {
            navChatManagement.setOnClickListener(v -> {
                startActivity(new Intent(OrderManagementActivity.this, AdminChatListActivity.class));
                finish();
            });
        }
    }
}
