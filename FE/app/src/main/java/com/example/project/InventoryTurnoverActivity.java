package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryTurnoverActivity extends AppCompatActivity implements InventoryTurnoverAdapter.OnProductClickListener {

    private CardView btnBack, btnSort, btnFilter;
    private EditText etSearch;
    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    
    // Summary cards
    private TextView tvAverageRatio, tvFastMoving, tvSlowMoving, tvOutOfStock;
    
    // Filter buttons
    private CardView btnFilterAll, btnFilterFast, btnFilterSlow, btnFilterOutOfStock, btnFilterLowStock;
    private TextView tvSortLabel;
    private ImageView ivSortDirection;
    
    // Data
    private InventoryTurnoverAdapter adapter;
    private ApiService apiService;
    private AuthManager authManager;
    
    // Filter & Sort state
    private String currentStatusFilter = "all";
    private String currentSortBy = "turnover";
    private boolean sortAscending = false;
    private String currentStoreId = null;
    private String startDate = null;
    private String endDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_turnover);

        initViews();
        initData();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        
        // Set default date range (last 30 days)
        Calendar calendar = Calendar.getInstance();
        endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSort = findViewById(R.id.btnSort);
        btnFilter = findViewById(R.id.btnFilter);
        etSearch = findViewById(R.id.etSearch);
        rvProducts = findViewById(R.id.rvProducts);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        
        // Summary cards
        tvAverageRatio = findViewById(R.id.tvAverageRatio);
        tvFastMoving = findViewById(R.id.tvFastMoving);
        tvSlowMoving = findViewById(R.id.tvSlowMoving);
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        
        // Filter buttons
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterFast = findViewById(R.id.btnFilterFast);
        btnFilterSlow = findViewById(R.id.btnFilterSlow);
        btnFilterOutOfStock = findViewById(R.id.btnFilterOutOfStock);
        btnFilterLowStock = findViewById(R.id.btnFilterLowStock);
        
        // Sort
        tvSortLabel = findViewById(R.id.tvSortLabel);
        ivSortDirection = findViewById(R.id.ivSortDirection);
    }

    private void initData() {
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void setupRecyclerView() {
        if (rvProducts != null) {
            adapter = new InventoryTurnoverAdapter(this);
            rvProducts.setLayoutManager(new LinearLayoutManager(this));
            rvProducts.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // Sort button
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortDialog());
        }
        
        // Filter button (for advanced filters)
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }
        
        // Status filter buttons
        if (btnFilterAll != null) {
            btnFilterAll.setOnClickListener(v -> setStatusFilter("all"));
        }
        if (btnFilterFast != null) {
            btnFilterFast.setOnClickListener(v -> setStatusFilter("fast"));
        }
        if (btnFilterSlow != null) {
            btnFilterSlow.setOnClickListener(v -> setStatusFilter("slow"));
        }
        if (btnFilterOutOfStock != null) {
            btnFilterOutOfStock.setOnClickListener(v -> setStatusFilter("out_of_stock"));
        }
        if (btnFilterLowStock != null) {
            btnFilterLowStock.setOnClickListener(v -> setStatusFilter("low_stock"));
        }
        
        // Search
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setStatusFilter(String status) {
        currentStatusFilter = status;
        updateFilterButtons();
        applyFilters();
    }

    private void updateFilterButtons() {
        // Reset all buttons
        if (btnFilterAll != null) resetFilterButton(btnFilterAll);
        if (btnFilterFast != null) resetFilterButton(btnFilterFast);
        if (btnFilterSlow != null) resetFilterButton(btnFilterSlow);
        if (btnFilterOutOfStock != null) resetFilterButton(btnFilterOutOfStock);
        if (btnFilterLowStock != null) resetFilterButton(btnFilterLowStock);
        
        // Set active button
        CardView activeButton = null;
        switch (currentStatusFilter) {
            case "all":
                activeButton = btnFilterAll;
                break;
            case "fast":
                activeButton = btnFilterFast;
                break;
            case "slow":
                activeButton = btnFilterSlow;
                break;
            case "out_of_stock":
                activeButton = btnFilterOutOfStock;
                break;
            case "low_stock":
                activeButton = btnFilterLowStock;
                break;
        }
        
        if (activeButton != null && activeButton.getChildCount() > 0) {
            try {
                activeButton.setCardBackgroundColor(getResources().getColor(R.color.primary_blue));
                View child = activeButton.getChildAt(0);
                if (child instanceof LinearLayout && ((LinearLayout) child).getChildCount() > 0) {
                    View textView = ((LinearLayout) child).getChildAt(0);
                    if (textView instanceof TextView) {
                        ((TextView) textView).setTextColor(getResources().getColor(R.color.white));
                    }
                }
            } catch (Exception e) {
                // Ignore if view structure is different
            }
        }
    }

    private void resetFilterButton(CardView button) {
        if (button == null || button.getChildCount() == 0) return;
        try {
            button.setCardBackgroundColor(getResources().getColor(R.color.background_light_gray));
            View child = button.getChildAt(0);
            if (child instanceof LinearLayout && ((LinearLayout) child).getChildCount() > 0) {
                View textView = ((LinearLayout) child).getChildAt(0);
                if (textView instanceof TextView) {
                    ((TextView) textView).setTextColor(getResources().getColor(R.color.text_secondary));
                }
            }
        } catch (Exception e) {
            // Ignore if view structure is different
        }
    }

    private void showSortDialog() {
        String[] sortOptions = {
            "Tỷ lệ quay vòng",
            "Tồn kho",
            "Số lượng bán",
            "Tên sản phẩm",
            "Doanh thu"
        };
        
        String[] sortValues = {"turnover", "stock", "sales", "name", "revenue"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sắp xếp theo");
        builder.setItems(sortOptions, (dialog, which) -> {
            currentSortBy = sortValues[which];
            updateSortLabel();
        });
        builder.show();
    }

    private void showFilterDialog() {
        // TODO: Implement advanced filter dialog (date range, store, category)
        Toast.makeText(this, "Bộ lọc nâng cao sẽ được thêm sau", Toast.LENGTH_SHORT).show();
    }

    private void updateSortLabel() {
        String[] sortLabels = {
            "Tỷ lệ",
            "Tồn kho",
            "Bán hàng",
            "Tên",
            "Doanh thu"
        };
        
        String[] sortValues = {"turnover", "stock", "sales", "name", "revenue"};
        
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentSortBy)) {
                if (tvSortLabel != null) {
                    tvSortLabel.setText("Sắp xếp: " + sortLabels[i]);
                }
                break;
            }
        }
        
        // Toggle sort direction
        sortAscending = !sortAscending;
        // Use available arrow icons or rotate existing one
        if (ivSortDirection != null) {
            ivSortDirection.setRotation(sortAscending ? 180 : 0);
        }
        
        // Update adapter sort
        if (adapter != null) {
            adapter.setSort(currentSortBy, sortAscending);
        }
    }

    private void applyFilters() {
        if (adapter != null && etSearch != null) {
            String query = etSearch.getText().toString().trim();
            adapter.filter(query, null, currentStatusFilter);
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (adapter != null) {
            if (adapter.getFilteredCount() == 0) {
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                if (rvProducts != null) rvProducts.setVisibility(View.GONE);
            } else {
                if (emptyState != null) emptyState.setVisibility(View.GONE);
                if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadData() {
        String token = authManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        Call<ApiService.InventoryTurnoverResponse> call = apiService.getInventoryTurnover(
            token,
            startDate,
            endDate,
            currentStoreId,
            null, // limit
            null, // minStock
            currentSortBy
        );
        
        call.enqueue(new Callback<ApiService.InventoryTurnoverResponse>() {
            @Override
            public void onResponse(Call<ApiService.InventoryTurnoverResponse> call, Response<ApiService.InventoryTurnoverResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.InventoryTurnoverResponse apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ApiService.InventoryTurnoverData data = apiResponse.getData();
                        
                        // Update summary
                        if (data.getSummary() != null) {
                            ApiService.Summary summary = data.getSummary();
                            if (tvAverageRatio != null) {
                                double avgRatio = summary.getAverageTurnoverRatio();
                                tvAverageRatio.setText(String.format(Locale.getDefault(), "%.2f", avgRatio));
                            }
                            if (tvFastMoving != null) {
                                tvFastMoving.setText(String.valueOf(summary.getFastMovingProducts()));
                            }
                            if (tvSlowMoving != null) {
                                tvSlowMoving.setText(String.valueOf(summary.getSlowMovingProducts()));
                            }
                            if (tvOutOfStock != null) {
                                tvOutOfStock.setText(String.valueOf(summary.getOutOfStockProducts()));
                            }
                        }
                        
                        // Update adapter
                        List<ApiService.ProductTurnover> products = data.getProducts();
                        if (products != null) {
                            adapter.setProducts(products);
                            applyFilters();
                        } else {
                            adapter.setProducts(new ArrayList<>());
                            updateEmptyState();
                        }
                    } else {
                        showError("Không thể tải dữ liệu");
                    }
                } else {
                    // Handle different error codes
                    int statusCode = response.code();
                    String errorMessage;
                    
                    if (statusCode == 404) {
                        errorMessage = "API endpoint chưa được implement trên backend.\nVui lòng liên hệ developer để thêm endpoint /api/inventory/turnover-ratio";
                        // Show mock data for demo purposes
                        showMockData();
                    } else if (statusCode == 401) {
                        errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
                        finish();
                    } else if (statusCode == 403) {
                        errorMessage = "Bạn không có quyền truy cập tính năng này.";
                    } else {
                        errorMessage = "Lỗi kết nối server (Code: " + statusCode + ")";
                    }
                    
                    showError(errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.InventoryTurnoverResponse> call, Throwable t) {
                showLoading(false);
                String errorMessage = "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
                showError(errorMessage);
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvProducts != null) {
            rvProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
            // Update empty state message if possible
            TextView emptyText = emptyState.findViewById(android.R.id.text1);
            if (emptyText == null) {
                // Try to find TextView in empty state
                for (int i = 0; i < emptyState.getChildCount(); i++) {
                    View child = emptyState.getChildAt(i);
                    if (child instanceof TextView) {
                        emptyText = (TextView) child;
                        break;
                    }
                }
            }
            if (emptyText != null && message.contains("404")) {
                emptyText.setText("API chưa sẵn sàng\n\nEndpoint: /api/inventory/turnover-ratio\n\nVui lòng implement API này trên backend.");
            }
        }
        if (rvProducts != null) {
            rvProducts.setVisibility(View.GONE);
        }
    }
    
    private void showMockData() {
        // Create mock data for demo purposes
        List<ApiService.ProductTurnover> mockProducts = new ArrayList<>();
        
        // Create a few mock products
        for (int i = 1; i <= 5; i++) {
            ApiService.ProductTurnover product = new ApiService.ProductTurnover();
            product.setProductId("mock_" + i);
            product.setProductName("Sản phẩm mẫu " + i);
            product.setProductBrand("Brand " + i);
            product.setProductCategory("city");
            product.setCurrentStock(10 + i * 5);
            product.setTotalQuantitySold(20 + i * 10);
            product.setTotalOrders(5 + i);
            product.setTurnoverRatio((double)(20 + i * 10) / (10 + i * 5));
            product.setTotalRevenue(10000000.0 * i);
            product.setAveragePrice(5000000.0);
            mockProducts.add(product);
        }
        
        // Update summary with mock data
        if (tvAverageRatio != null) {
            tvAverageRatio.setText("2.50");
        }
        if (tvFastMoving != null) {
            tvFastMoving.setText("2");
        }
        if (tvSlowMoving != null) {
            tvSlowMoving.setText("1");
        }
        if (tvOutOfStock != null) {
            tvOutOfStock.setText("0");
        }
        
        // Update adapter
        if (adapter != null) {
            adapter.setProducts(mockProducts);
            applyFilters();
        }
        
        // Show info message
        Toast.makeText(this, "Đang hiển thị dữ liệu mẫu. API endpoint chưa được implement.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProductClick(ApiService.ProductTurnover product) {
        // Navigate to product detail or show detail dialog
        Intent intent = new Intent(this, BikeDetailActivity.class);
        intent.putExtra("bike_id", product.getProductId());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        // Similar to other admin activities
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navUserManagement = findViewById(R.id.navUserManagement);
        LinearLayout navProductManagement = findViewById(R.id.navProductManagement);
        LinearLayout navStoreManagement = findViewById(R.id.navStoreManagement);
        LinearLayout navOrderManagement = findViewById(R.id.navOrderManagement);
        LinearLayout navChatManagement = findViewById(R.id.navChatManagement);
        
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navUserManagement != null) {
            navUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(this, UserManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navProductManagement != null) {
            navProductManagement.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navStoreManagement != null) {
            navStoreManagement.setOnClickListener(v -> {
                Intent intent = new Intent(this, StoreManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navOrderManagement != null) {
            navOrderManagement.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navChatManagement != null) {
            navChatManagement.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminChatListActivity.class);
                startActivity(intent);
            });
        }
    }
}

