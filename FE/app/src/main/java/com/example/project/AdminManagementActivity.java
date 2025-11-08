package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AdminNavHelper;
import com.example.project.utils.AuthManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManagementActivity extends AppCompatActivity {

    private TextView tvTotalSales, tvTotalSalesLabel, tvOrderUser, tvOrderUserLabel, tvTotalProducts, tvTotalProductsLabel, tvTotalUsers, tvTotalUsersLabel;
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconDashboard, iconUserManagement, iconProductManagement, iconStoreManagement, iconOrderManagement, iconChatManagement;
    private TextView tvDashboard, tvUserManagement, tvProductManagement, tvStoreManagement, tvOrderManagement, tvChatManagement;
    private CardView cardDashboard, cardUserManagement, cardProductManagement, cardStoreManagement, cardOrderManagement, cardChatManagement;
    
    // Chart views
    private TextView tvYAxisMax, tvYAxisMid2, tvYAxisMid1;
    private View barViewCN, barViewT2, barViewT3, barViewT4, barViewT5, barViewT6, barViewT7;
    private TextView tvXAxisCN, tvXAxisT2, tvXAxisT3, tvXAxisT4, tvXAxisT5, tvXAxisT6, tvXAxisT7;
    private LinearLayout barCN, barT2, barT3, barT4, barT5, barT6, barT7;
    
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        initViews();
        initData();
        loadStatistics();
        setupClickListeners();
    }
    
    private void initData() {
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void initViews() {
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvTotalSalesLabel = findViewById(R.id.tvTotalSalesLabel);
        tvOrderUser = findViewById(R.id.tvOrderUser);
        tvOrderUserLabel = findViewById(R.id.tvOrderUserLabel);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalProductsLabel = findViewById(R.id.tvTotalProductsLabel);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalUsersLabel = findViewById(R.id.tvTotalUsersLabel);
        
        // Ensure labels are visible and set text
        if (tvTotalSalesLabel != null) {
            tvTotalSalesLabel.setText("Tổng doanh thu");
            tvTotalSalesLabel.setVisibility(android.view.View.VISIBLE);
        }
        if (tvOrderUserLabel != null) {
            tvOrderUserLabel.setText("Tổng đơn hàng");
            tvOrderUserLabel.setVisibility(android.view.View.VISIBLE);
        }
        if (tvTotalProductsLabel != null) {
            tvTotalProductsLabel.setText("Tổng số xe");
            tvTotalProductsLabel.setVisibility(android.view.View.VISIBLE);
        }
        if (tvTotalUsersLabel != null) {
            tvTotalUsersLabel.setText("Tổng người dùng");
            tvTotalUsersLabel.setVisibility(android.view.View.VISIBLE);
        }

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

        cardDashboard = findViewById(R.id.cardDashboard);
        cardUserManagement = findViewById(R.id.cardUserManagement);
        cardProductManagement = findViewById(R.id.cardProductManagement);
        cardStoreManagement = findViewById(R.id.cardStoreManagement);
        cardOrderManagement = findViewById(R.id.cardOrderManagement);
        cardChatManagement = findViewById(R.id.cardChatManagement);
        
        // Initialize chart views
        initChartViews();

        // Mark current tab active with gradient background
        setActiveTab(cardDashboard, iconDashboard, tvDashboard);
    }
    
    private void initChartViews() {
        tvYAxisMax = findViewById(R.id.tvYAxisMax);
        tvYAxisMid2 = findViewById(R.id.tvYAxisMid2);
        tvYAxisMid1 = findViewById(R.id.tvYAxisMid1);
        
        barViewCN = findViewById(R.id.barViewCN);
        barViewT2 = findViewById(R.id.barViewT2);
        barViewT3 = findViewById(R.id.barViewT3);
        barViewT4 = findViewById(R.id.barViewT4);
        barViewT5 = findViewById(R.id.barViewT5);
        barViewT6 = findViewById(R.id.barViewT6);
        barViewT7 = findViewById(R.id.barViewT7);
        
        tvXAxisCN = findViewById(R.id.tvXAxisCN);
        tvXAxisT2 = findViewById(R.id.tvXAxisT2);
        tvXAxisT3 = findViewById(R.id.tvXAxisT3);
        tvXAxisT4 = findViewById(R.id.tvXAxisT4);
        tvXAxisT5 = findViewById(R.id.tvXAxisT5);
        tvXAxisT6 = findViewById(R.id.tvXAxisT6);
        tvXAxisT7 = findViewById(R.id.tvXAxisT7);
        
        barCN = findViewById(R.id.barCN);
        barT2 = findViewById(R.id.barT2);
        barT3 = findViewById(R.id.barT3);
        barT4 = findViewById(R.id.barT4);
        barT5 = findViewById(R.id.barT5);
        barT6 = findViewById(R.id.barT6);
        barT7 = findViewById(R.id.barT7);
    }

    private void setActiveTab(CardView activeCard, ImageView activeIcon, TextView activeText) {
        // Reset all tabs to inactive state
        AdminNavHelper.resetAllTabs(
                cardDashboard, iconDashboard, tvDashboard,
                cardUserManagement, iconUserManagement, tvUserManagement,
                cardProductManagement, iconProductManagement, tvProductManagement,
                cardStoreManagement, iconStoreManagement, tvStoreManagement,
                cardOrderManagement, iconOrderManagement, tvOrderManagement,
                cardChatManagement, iconChatManagement, tvChatManagement
        );

        // Set active tab
        AdminNavHelper.setActiveTab(activeCard, activeIcon, activeText);
    }

    private void resetTab(CardView card, ImageView icon, TextView text) {
        AdminNavHelper.resetTab(card, icon, text);
    }

    private void loadStatistics() {
        // Get auth token
        String token = authManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String authHeader = "Bearer " + token;
        
        // Call API to get total revenue
        Call<ApiService.RevenueResponse> call = apiService.getTotalRevenue(
            authHeader,
            null,  // startDate
            null,  // endDate
            null   // storeId
        );
        
        call.enqueue(new Callback<ApiService.RevenueResponse>() {
            @Override
            public void onResponse(Call<ApiService.RevenueResponse> call, Response<ApiService.RevenueResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.RevenueResponse revenueResponse = response.body();
                    
                    if (revenueResponse.isSuccess() && revenueResponse.getData() != null) {
                        ApiService.RevenueData revenueData = revenueResponse.getData();
                        
                        // Format and display total revenue
                        double totalRevenue = revenueData.getTotalRevenue();
                        String formattedRevenue = formatRevenue(totalRevenue);
                        tvTotalSales.setText(formattedRevenue);
                        
                        // Display total orders
                        int totalOrders = revenueData.getTotalOrders();
                        tvOrderUser.setText(String.valueOf(totalOrders));
                    } else {
                        // Show error message
                        showError("Không thể tải dữ liệu thống kê");
                    }
                } else {
                    showError("Lỗi kết nối server");
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.RevenueResponse> call, Throwable t) {
                showError("Lỗi kết nối: " + t.getMessage());
                // Show default values on error
                tvTotalSales.setText("0");
                tvOrderUser.setText("0");
            }
        });
        
        // Call API to get total users
        loadTotalUsers(authHeader);
        
        // Call API to get total bikes
        loadTotalBikes();
        
        // Call API to get orders by day of week
        loadOrdersByDayOfWeek(authHeader);
    }
    
    private void loadTotalUsers(String authHeader) {
        Call<ApiService.TotalUsersResponse> call = apiService.getTotalUsers(
            authHeader,
            null,  // role
            null   // isActive
        );
        
        call.enqueue(new Callback<ApiService.TotalUsersResponse>() {
            @Override
            public void onResponse(Call<ApiService.TotalUsersResponse> call, Response<ApiService.TotalUsersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.TotalUsersResponse usersResponse = response.body();
                    
                    if (usersResponse.isSuccess() && usersResponse.getData() != null) {
                        ApiService.TotalUsersData usersData = usersResponse.getData();
                        
                        // Display total users
                        int totalUsers = usersData.getTotalUsers();
                        tvTotalUsers.setText(String.valueOf(totalUsers));
                    } else {
                        // Show default value on error
                        tvTotalUsers.setText("0");
                    }
                } else {
                    // Show default value on error
                    tvTotalUsers.setText("0");
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.TotalUsersResponse> call, Throwable t) {
                // Show default value on error
                tvTotalUsers.setText("0");
            }
        });
    }
    
    private void loadTotalBikes() {
        Call<ApiService.TotalBikesResponse> call = apiService.getTotalBikes(
            null,  // status
            null,  // category
            null   // brand
        );
        
        call.enqueue(new Callback<ApiService.TotalBikesResponse>() {
            @Override
            public void onResponse(Call<ApiService.TotalBikesResponse> call, Response<ApiService.TotalBikesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.TotalBikesResponse bikesResponse = response.body();
                    
                    if (bikesResponse.isSuccess() && bikesResponse.getData() != null) {
                        ApiService.TotalBikesData bikesData = bikesResponse.getData();
                        
                        // Display total bikes
                        int totalBikes = bikesData.getTotalBikes();
                        tvTotalProducts.setText(String.valueOf(totalBikes));
                    } else {
                        // Show default value on error
                        tvTotalProducts.setText("0");
                    }
                } else {
                    // Show default value on error
                    tvTotalProducts.setText("0");
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.TotalBikesResponse> call, Throwable t) {
                // Show default value on error
                tvTotalProducts.setText("0");
            }
        });
    }
    
    private String formatRevenue(double revenue) {
        // Format VND with thousand separators
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        
        // If revenue is very large, use abbreviated format
        if (revenue >= 1_000_000_000) {
            // Format as billions (tỷ) - e.g., 1.5T VND
            double billions = revenue / 1_000_000_000;
            if (billions == (long) billions) {
                return String.format(Locale.getDefault(), "%.0fT", billions);
            } else {
                return String.format(Locale.getDefault(), "%.1fT", billions);
            }
        } else if (revenue >= 1_000_000) {
            // Format as millions (triệu) - e.g., 1.5M VND
            double millions = revenue / 1_000_000;
            if (millions == (long) millions) {
                return String.format(Locale.getDefault(), "%.0fM", millions);
            } else {
                return String.format(Locale.getDefault(), "%.1fM", millions);
            }
        } else if (revenue >= 1_000) {
            // Format as thousands (nghìn) - e.g., 1.5K VND
            double thousands = revenue / 1_000;
            if (thousands == (long) thousands) {
                return String.format(Locale.getDefault(), "%.0fK", thousands);
            } else {
                return String.format(Locale.getDefault(), "%.1fK", thousands);
            }
        } else {
            // Format with thousand separators for small amounts
            return formatter.format(revenue);
        }
    }
    
    private void loadOrdersByDayOfWeek(String authHeader) {
        Call<ApiService.OrdersByDayOfWeekResponse> call = apiService.getOrdersByDayOfWeek(
            authHeader,
            null,  // startDate
            null,  // endDate
            null,  // storeId
            null   // status
        );
        
        call.enqueue(new Callback<ApiService.OrdersByDayOfWeekResponse>() {
            @Override
            public void onResponse(Call<ApiService.OrdersByDayOfWeekResponse> call, Response<ApiService.OrdersByDayOfWeekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.OrdersByDayOfWeekResponse ordersResponse = response.body();
                    
                    if (ordersResponse.isSuccess() && ordersResponse.getData() != null) {
                        List<ApiService.OrderByDay> ordersByDay = ordersResponse.getData().getOrdersByDay();
                        if (ordersByDay != null && ordersByDay.size() == 7) {
                            // Run on UI thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateChart(ordersByDay);
                                }
                            });
                        } else {
                            android.util.Log.e("AdminDashboard", "OrdersByDay size is not 7: " + (ordersByDay != null ? ordersByDay.size() : "null"));
                        }
                    } else {
                        android.util.Log.e("AdminDashboard", "Response not successful or data is null");
                    }
                } else {
                    android.util.Log.e("AdminDashboard", "Response not successful: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.OrdersByDayOfWeekResponse> call, Throwable t) {
                android.util.Log.e("AdminDashboard", "API call failed: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }
    
    private void updateChart(List<ApiService.OrderByDay> ordersByDay) {
        if (ordersByDay == null || ordersByDay.size() != 7) {
            android.util.Log.e("AdminDashboard", "Invalid ordersByDay data");
            return;
        }
        
        // Find max value for scaling
        int maxOrders = 0;
        for (ApiService.OrderByDay day : ordersByDay) {
            int totalOrders = day.getTotalOrders();
            if (totalOrders > maxOrders) {
                maxOrders = totalOrders;
            }
        }
        
        // If all values are 0, set a default max for display
        if (maxOrders == 0) {
            maxOrders = 10; // Default max for empty chart
        }
        
        android.util.Log.d("AdminDashboard", "Max orders: " + maxOrders);
        for (int i = 0; i < ordersByDay.size(); i++) {
            android.util.Log.d("AdminDashboard", ordersByDay.get(i).getDayName() + ": " + ordersByDay.get(i).getTotalOrders());
        }
        
        // Update Y-axis labels
        if (tvYAxisMax != null) {
            tvYAxisMax.setText(String.valueOf(maxOrders));
        }
        if (tvYAxisMid2 != null) {
            tvYAxisMid2.setText(String.valueOf((int) (maxOrders * 0.75)));
        }
        if (tvYAxisMid1 != null) {
            tvYAxisMid1.setText(String.valueOf((int) (maxOrders * 0.5)));
        }
        
        // Update X-axis labels - fixed labels: CN T2 T3 T4 T5 T6 T7
        if (tvXAxisCN != null) {
            tvXAxisCN.setText("CN");
        }
        if (tvXAxisT2 != null) {
            tvXAxisT2.setText("T2");
        }
        if (tvXAxisT3 != null) {
            tvXAxisT3.setText("T3");
        }
        if (tvXAxisT4 != null) {
            tvXAxisT4.setText("T4");
        }
        if (tvXAxisT5 != null) {
            tvXAxisT5.setText("T5");
        }
        if (tvXAxisT6 != null) {
            tvXAxisT6.setText("T6");
        }
        if (tvXAxisT7 != null) {
            tvXAxisT7.setText("T7");
        }
        
        // Update bar heights
        // Wait for layout to be measured
        // Create final variables for inner class
        final int finalMaxOrders = maxOrders;
        final int ordersCN = ordersByDay.get(0).getTotalOrders();
        final int ordersT2 = ordersByDay.get(1).getTotalOrders();
        final int ordersT3 = ordersByDay.get(2).getTotalOrders();
        final int ordersT4 = ordersByDay.get(3).getTotalOrders();
        final int ordersT5 = ordersByDay.get(4).getTotalOrders();
        final int ordersT6 = ordersByDay.get(5).getTotalOrders();
        final int ordersT7 = ordersByDay.get(6).getTotalOrders();
        
        // Use ViewTreeObserver to wait for layout to be measured
        if (barCN != null) {
            barCN.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove listener to avoid multiple calls
                    barCN.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    
                    int containerHeight = barCN.getHeight();
                    android.util.Log.d("AdminDashboard", "Container height: " + containerHeight);
                    
                    if (containerHeight > 0) {
                        updateBarHeight(barViewCN, ordersCN, finalMaxOrders, containerHeight);
                        updateBarHeight(barViewT2, ordersT2, finalMaxOrders, containerHeight);
                        updateBarHeight(barViewT3, ordersT3, finalMaxOrders, containerHeight);
                        updateBarHeight(barViewT4, ordersT4, finalMaxOrders, containerHeight);
                        updateBarHeight(barViewT5, ordersT5, finalMaxOrders, containerHeight);
                        updateBarHeight(barViewT6, ordersT6, finalMaxOrders, containerHeight);
                        updateBarHeight(barViewT7, ordersT7, finalMaxOrders, containerHeight);
                    } else {
                        // If height is still 0, try again after a short delay
                        barCN.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int containerHeight = barCN.getHeight();
                                if (containerHeight > 0) {
                                    updateBarHeight(barViewCN, ordersCN, finalMaxOrders, containerHeight);
                                    updateBarHeight(barViewT2, ordersT2, finalMaxOrders, containerHeight);
                                    updateBarHeight(barViewT3, ordersT3, finalMaxOrders, containerHeight);
                                    updateBarHeight(barViewT4, ordersT4, finalMaxOrders, containerHeight);
                                    updateBarHeight(barViewT5, ordersT5, finalMaxOrders, containerHeight);
                                    updateBarHeight(barViewT6, ordersT6, finalMaxOrders, containerHeight);
                                    updateBarHeight(barViewT7, ordersT7, finalMaxOrders, containerHeight);
                                }
                            }
                        }, 100);
                    }
                }
            });
        }
    }
    
    private void updateBarHeight(View barView, int value, int maxValue, int containerHeight) {
        if (barView == null) {
            return;
        }
        
        if (maxValue == 0) {
            ViewGroup.LayoutParams params = barView.getLayoutParams();
            params.height = 0;
            barView.setLayoutParams(params);
            return;
        }
        
        // Calculate height as percentage of container
        float percentage = (float) value / maxValue;
        int height = (int) (containerHeight * percentage);
        
        // Ensure minimum height for visibility if value > 0
        if (value > 0 && height < 8) {
            height = 8;
        }
        
        // Ensure bar is visible even if value is 0 (show as very small bar)
        if (value == 0) {
            height = 0;
        }
        
        android.util.Log.d("AdminDashboard", "Updating bar: value=" + value + ", maxValue=" + maxValue + ", height=" + height + ", containerHeight=" + containerHeight);
        
        ViewGroup.LayoutParams params = barView.getLayoutParams();
        params.height = height;
        barView.setLayoutParams(params);
        barView.setVisibility(View.VISIBLE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(AdminManagementActivity.this, OrderManagementActivity.class);
                startActivity(intent);
                finish();
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
