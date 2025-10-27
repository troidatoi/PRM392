package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class StoreManagementActivity extends AppCompatActivity implements StoreAdapter.OnStoreActionListener {

    private CardView btnBack, btnAddStore, btnSearch;
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
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddStore = findViewById(R.id.btnAddStore);
        btnSearch = findViewById(R.id.btnSearch);
        rvStores = findViewById(R.id.rvStores);
        tvTotalStores = findViewById(R.id.tvTotalStores);
        tvActiveStores = findViewById(R.id.tvActiveStores);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Add store button
        btnAddStore.setOnClickListener(v -> {
            // TODO: Open Add Store Dialog or Activity
            showAddStoreDialog();
        });

        // Search button
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng tìm kiếm - Coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        storeList = new ArrayList<>();
        storeAdapter = new StoreAdapter(storeList, this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Cửa Hàng Mới");
        builder.setMessage("Chức năng thêm cửa hàng sẽ được cập nhật trong phiên bản tiếp theo.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onEditStore(Store store) {
        // Handle edit store
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh Sửa Cửa Hàng");
        builder.setMessage("Bạn muốn chỉnh sửa: " + store.getName() + "?");
        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            Toast.makeText(this, "Chức năng chỉnh sửa - Coming soon", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDeleteStore(Store store) {
        // Handle delete store
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa Cửa Hàng");
        builder.setMessage("Bạn có chắc chắn muốn xóa cửa hàng: " + store.getName() + "?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            storeList.remove(store);
            storeAdapter.notifyDataSetChanged();
            updateStatistics();

            // Show/hide empty state
            if (storeList.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rvStores.setVisibility(View.GONE);
            }

            Toast.makeText(this, "Đã xóa cửa hàng: " + store.getName(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
