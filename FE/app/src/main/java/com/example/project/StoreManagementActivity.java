package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.project.models.ApiResponse;

public class StoreManagementActivity extends AppCompatActivity implements StoreAdapter.OnStoreActionListener, StoreAdapter.OnStoreClickListener {

    private CardView btnBack, btnAddStore, btnShippingRates;
    private RecyclerView rvStores;
    private TextView tvTotalStores, tvActiveStores;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private StoreAdapter storeAdapter;
    private List<Store> storeList;
    
    // API related
    private ApiService apiService;
    private AuthManager authManager;
    
    // Bottom navigation
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconStoreManagement;
    private TextView tvStoreManagement;
    
    // Pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    
    // Filters
    private String currentCity = null;
    private Boolean currentIsActive = null;
    private String currentSort = "createdAt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_management);

        // Initialize API and Auth
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
        
        // Check authentication
        if (!authManager.isLoggedIn() || !authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadStores();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddStore = findViewById(R.id.btnAddStore);
        btnShippingRates = findViewById(R.id.btnShippingRates);
        rvStores = findViewById(R.id.rvStores);
        tvTotalStores = findViewById(R.id.tvTotalStores);
        tvActiveStores = findViewById(R.id.tvActiveStores);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        // Bottom navigation
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);
        iconStoreManagement = findViewById(R.id.iconStoreManagement);
        tvStoreManagement = findViewById(R.id.tvStoreManagement);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Add store button
        btnAddStore.setOnClickListener(v -> {
            showAddStoreDialog();
        });

        // Shipping rates button
        if (btnShippingRates != null) {
            btnShippingRates.setOnClickListener(v -> {
                Intent intent = new Intent(StoreManagementActivity.this, ShippingRateManagementActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerView() {
        storeList = new ArrayList<>();
        storeAdapter = new StoreAdapter(storeList, this);
        storeAdapter.setOnStoreClickListener(this);
        storeAdapter.setOnInventoryClickListener(store -> {
            Intent intent = new Intent(StoreManagementActivity.this, StoreInventoryActivity.class);
            intent.putExtra("storeId", store.getId());
            intent.putExtra("storeName", store.getName());
            startActivity(intent);
        });
        rvStores.setLayoutManager(new LinearLayoutManager(this));
        rvStores.setAdapter(storeAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshStores();
        });
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
    }

    private void loadStores() {
        if (isLoading) return;
        
        isLoading = true;
        showLoading(true);
        
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Call<ApiService.StoreResponse> call = apiService.getStores(
            currentCity,
            null, // district
            null, // storeType
            currentIsActive,
            currentPage,
            10, // limit
            currentSort
        );
        
        call.enqueue(new Callback<ApiService.StoreResponse>() {
            @Override
            public void onResponse(Call<ApiService.StoreResponse> call, Response<ApiService.StoreResponse> response) {
                isLoading = false;
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.StoreResponse storeResponse = response.body();
                    if (storeResponse.isSuccess()) {
                        handleStoreResponse(storeResponse);
                    } else {
                        Toast.makeText(StoreManagementActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StoreManagementActivity.this, "Lỗi tải dữ liệu: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.StoreResponse> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(StoreManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleStoreResponse(ApiService.StoreResponse response) {
        if (currentPage == 1) {
            storeList.clear();
        }
        
        if (response.getData() != null) {
            storeList.addAll(response.getData());
        }
        
        currentPage = response.getPage();
        totalPages = response.getPages();
        hasMoreData = currentPage < totalPages;
        
        storeAdapter.notifyDataSetChanged();
        updateStatistics();
        updateEmptyState();
    }
    
    private void refreshStores() {
        currentPage = 1;
        hasMoreData = true;
        loadStores();
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void updateEmptyState() {
        if (storeList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvStores.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvStores.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        int totalStores = storeList.size();
        int activeStores = 0;

        for (Store store : storeList) {
            if (store.isActive()) {
                activeStores++;
            }
        }

        tvTotalStores.setText(String.valueOf(totalStores));
        tvActiveStores.setText(String.valueOf(activeStores));
    }

    private void showAddStoreDialog() {
        AddStoreDialog dialog = new AddStoreDialog(this, new AddStoreDialog.OnStoreAddedListener() {
            @Override
            public void onStoreAdded(Store store) {
                // Refresh the store list
                refreshStores();
                
                // Update statistics
                updateStatistics();
            }
        });
        dialog.show();
    }

    @Override
    public void onEditStore(Store store) {
        // Handle edit store
        EditStoreDialog dialog = new EditStoreDialog(this, store, new EditStoreDialog.OnStoreUpdatedListener() {
            @Override
            public void onStoreUpdated(Store updatedStore) {
                // Refresh the store list
                refreshStores();
            }
        });
        dialog.show();
    }

    @Override
    public void onDeleteStore(Store store) {
        // Handle delete store with confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa Cửa Hàng");
        builder.setMessage("Bạn có chắc chắn muốn xóa cửa hàng: " + store.getName() + "?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteStoreFromAPI(store);
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    private void deleteStoreFromAPI(Store store) {
        // Get auth header
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        showLoading(true);
        
        // Call API to delete store
        Call<ApiResponse<Void>> call = apiService.deleteStore(authHeader, store.getId());
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Remove from local list
                    storeList.remove(store);
                    storeAdapter.notifyDataSetChanged();
                    updateStatistics();
                    
                    // Show/hide empty state
                    if (storeList.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        rvStores.setVisibility(View.GONE);
                    }
                    
                    Toast.makeText(StoreManagementActivity.this, "Đã xóa cửa hàng: " + store.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Lỗi không xác định";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage() != null ? response.body().getMessage() : "Lỗi xóa cửa hàng";
                    } else {
                        errorMsg = response.message();
                    }
                    Toast.makeText(StoreManagementActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(StoreManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStoreClick(Store store) {
        // Open store detail activity
        Intent intent = new Intent(this, StoreDetailActivity.class);
        intent.putExtra("storeId", store.getId());
        startActivity(intent);
    }
    
    private void setupBottomNavigation() {
        // Set Store Management tab as active
        if (iconStoreManagement != null) {
            iconStoreManagement.setColorFilter(0xFF2196F3); // active blue
        }
        if (tvStoreManagement != null) {
            tvStoreManagement.setTextColor(0xFF2196F3); // active blue
        }
        
        // Bottom navigation click listeners
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(StoreManagementActivity.this, AdminManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navUserManagement != null) {
            navUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(StoreManagementActivity.this, UserManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navProductManagement != null) {
            navProductManagement.setOnClickListener(v -> {
                Intent intent = new Intent(StoreManagementActivity.this, ProductManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navOrderManagement != null) {
            navOrderManagement.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        if (navChatManagement != null) {
            navChatManagement.setOnClickListener(v -> {
                Intent intent = new Intent(StoreManagementActivity.this, AdminChatListActivity.class);
                startActivity(intent);
            });
        }
    }
}
