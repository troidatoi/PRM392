package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.project.models.ApiResponse;

public class StoreDetailActivity extends AppCompatActivity {

    // Views
    private CardView btnBack, btnEdit, btnDelete, statusBadge;
    private TextView tvStoreName, tvStatus, tvAddress, tvPhone, tvEmail, tvLatitude, tvLongitude, tvDescription;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Data
    private String storeId;
    private Store currentStore;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        // Get store ID from intent
        storeId = getIntent().getStringExtra("storeId");
        if (storeId == null || storeId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID cửa hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        setupListeners();
        loadStoreDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        statusBadge = findViewById(R.id.statusBadge);
        tvStoreName = findViewById(R.id.tvStoreName);
        tvStatus = findViewById(R.id.tvStatus);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvDescription = findViewById(R.id.tvDescription);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            if (currentStore != null) {
                // TODO: Open edit store dialog/activity
                showEditStoreDialog();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (currentStore != null) {
                confirmDeleteStore();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadStoreDetails();
        });

        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
    }

    private void loadStoreDetails() {
        showLoading(true);
        swipeRefreshLayout.setRefreshing(true);

        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<Store>> call = apiService.getStoreById(storeId);
        
        call.enqueue(new Callback<ApiResponse<Store>>() {
            @Override
            public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Store store = response.body().getData();
                    if (store != null) {
                        currentStore = store;
                        displayStoreDetails(currentStore);
                    } else {
                        Toast.makeText(StoreDetailActivity.this, "Không tìm thấy cửa hàng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(StoreDetailActivity.this, "Không tìm thấy cửa hàng: " + response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(StoreDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStoreDetails(Store store) {
        // Store name
        tvStoreName.setText(store.getName());

        // Status
        tvStatus.setText(store.getDisplayStatus());
        if (store.isActive()) {
            statusBadge.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            statusBadge.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Address
        if (!TextUtils.isEmpty(store.getFullAddress())) {
            tvAddress.setText(store.getFullAddress());
        } else if (!TextUtils.isEmpty(store.getAddress())) {
            tvAddress.setText(store.getAddress());
        } else {
            tvAddress.setText("Chưa có địa chỉ");
        }

        // Phone
        if (!TextUtils.isEmpty(store.getPhone())) {
            tvPhone.setText(store.getPhone());
        } else {
            tvPhone.setText("Chưa có số điện thoại");
        }

        // Email
        if (!TextUtils.isEmpty(store.getEmail())) {
            tvEmail.setText(store.getEmail());
        } else {
            tvEmail.setText("Chưa có email");
        }

        // Location
        tvLatitude.setText(String.valueOf(store.getLatitude()));
        tvLongitude.setText(String.valueOf(store.getLongitude()));

        // Description
        if (!TextUtils.isEmpty(store.getDescription())) {
            tvDescription.setText(store.getDescription());
        } else {
            tvDescription.setText("Chưa có mô tả");
        }
    }

    private void showEditStoreDialog() {
        if (currentStore == null) {
            Toast.makeText(this, "Không có dữ liệu cửa hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        EditStoreDialog dialog = new EditStoreDialog(this, currentStore, new EditStoreDialog.OnStoreUpdatedListener() {
            @Override
            public void onStoreUpdated(Store store) {
                // Refresh the store details
                loadStoreDetails();
            }
        });
        dialog.show();
    }

    private void confirmDeleteStore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa Cửa Hàng");
        builder.setMessage("Bạn có chắc chắn muốn xóa cửa hàng: " + currentStore.getName() + "?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteStore();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteStore() {
        showLoading(true);

        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<Void>> call = apiService.deleteStore(authHeader, storeId);
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(StoreDetailActivity.this, "Xóa cửa hàng thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(StoreDetailActivity.this, "Lỗi xóa cửa hàng: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(StoreDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
