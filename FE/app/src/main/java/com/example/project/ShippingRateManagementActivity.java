package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.models.ApiResponse;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShippingRateManagementActivity extends AppCompatActivity implements ShippingRateAdapter.OnRateActionListener {

    private CardView btnBack;
    private RecyclerView rvShippingRates;
    private TextView tvTotalRates;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ShippingRateAdapter adapter;
    private List<ApiService.ShippingRate> rateList;

    private ApiService apiService;
    private AuthManager authManager;

    // Bottom navigation
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconStoreManagement;
    private TextView tvStoreManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_rate_management);

        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn() || !authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadShippingRates();
        setupBottomNavigation();
    }

    private void initViews() {
        try {
            btnBack = findViewById(R.id.btnBack);
            rvShippingRates = findViewById(R.id.rvShippingRates);
            emptyState = findViewById(R.id.emptyState);
            progressBar = findViewById(R.id.progressBar);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            android.util.Log.e("ShippingRateManagement", "Error initializing views", e);
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        rateList = new ArrayList<>();
        adapter = new ShippingRateAdapter(rateList, this);
        rvShippingRates.setLayoutManager(new LinearLayoutManager(this));
        rvShippingRates.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadShippingRates();
        });
    }

    private void loadShippingRates() {
        showLoading(true);
        apiService.getShippingRates(null).enqueue(new Callback<ApiResponse<ApiService.ShippingRate[]>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.ShippingRate[]>> call, Response<ApiResponse<ApiService.ShippingRate[]>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiService.ShippingRate[] rates = response.body().getData();
                    if (rates != null) {
                        rateList.clear();
                        for (ApiService.ShippingRate rate : rates) {
                            rateList.add(rate);
                        }
                        adapter.updateList(rateList);
                        updateEmptyState();
                    }
                } else {
                    Toast.makeText(ShippingRateManagementActivity.this, "Không thể tải danh sách bảng giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiService.ShippingRate[]>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ShippingRateManagementActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditRateDialog(ApiService.ShippingRate rate) {
        if (rate == null) {
            Toast.makeText(this, "Không thể chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_shipping_rate, null);
        TextView tvMinDistance = dialogView.findViewById(R.id.tvMinDistance);
        TextView tvMaxDistance = dialogView.findViewById(R.id.tvMaxDistance);
        EditText etPricePerKm = dialogView.findViewById(R.id.etPricePerKm);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        Switch switchIsActive = dialogView.findViewById(R.id.switchIsActive);

        // Set read-only distance values
        tvMinDistance.setText(String.valueOf(rate.getMinDistance()));
        if (rate.getMaxDistance() != null) {
            tvMaxDistance.setText(String.valueOf(rate.getMaxDistance()));
        } else {
            tvMaxDistance.setText("Không giới hạn");
        }
        
        // Set editable fields
        etPricePerKm.setText(String.valueOf((long)rate.getPricePerKm()));
        if (rate.getNote() != null) {
            etNote.setText(rate.getNote());
        }
        switchIsActive.setChecked(rate.isActive());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa Giá Ship");
        builder.setView(dialogView);
        builder.setPositiveButton("Cập nhật", null); // Set null to prevent auto-dismiss
        builder.setNegativeButton("Hủy", null);
        
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String priceStr = etPricePerKm.getText().toString().trim();
                String noteStr = etNote.getText().toString().trim();

                if (TextUtils.isEmpty(priceStr)) {
                    etPricePerKm.setError("Vui lòng nhập đơn giá");
                    return; // Don't close dialog if there's an error
                }

                try {
                    double pricePerKm = Double.parseDouble(priceStr);
                    
                    if (pricePerKm < 0) {
                        etPricePerKm.setError("Đơn giá không được âm");
                        return;
                    }

                    dialog.dismiss(); // Close dialog only when validation passes
                    
                    // Only update price, note, and isActive - keep original distances
                    updateShippingRate(rate.getId(), rate.getMinDistance(), rate.getMaxDistance(), pricePerKm, noteStr, rate.getOrder(), switchIsActive.isChecked());
                } catch (NumberFormatException e) {
                    Toast.makeText(ShippingRateManagementActivity.this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }


    private void updateShippingRate(String id, double minDistance, Double maxDistance, double pricePerKm, String note, int order, boolean isActive) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "ID bảng giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            showLoading(false);
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create rate object - only update price, note, and isActive
        // Keep original minDistance, maxDistance, and order
        ApiService.ShippingRate rate = new ApiService.ShippingRate();
        rate.setId(id);
        rate.setPricePerKm(pricePerKm);
        rate.setNote(note);
        rate.setActive(isActive);
        // Don't set minDistance, maxDistance, order - keep original values
        
        android.util.Log.d("ShippingRate", "Updating rate: id=" + id + ", min=" + minDistance + ", max=" + maxDistance + ", price=" + pricePerKm);
        
        apiService.updateShippingRate(authHeader, id, rate).enqueue(new Callback<ApiResponse<ApiService.ShippingRate>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.ShippingRate>> call, Response<ApiResponse<ApiService.ShippingRate>> response) {
                showLoading(false);
                android.util.Log.d("ShippingRate", "Update response: code=" + response.code() + ", success=" + (response.isSuccessful()));
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ShippingRateManagementActivity.this, "Đã cập nhật bảng giá thành công", Toast.LENGTH_SHORT).show();
                    loadShippingRates();
                } else {
                    String message = "Không thể cập nhật bảng giá";
                    if (response.body() != null) {
                        message = response.body().getMessage() != null ? response.body().getMessage() : message;
                        android.util.Log.e("ShippingRate", "Update failed: " + message);
                    } else if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("ShippingRate", "Error body: " + errorBody);
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            ApiResponse errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                message = errorResponse.getMessage();
                            }
                        } catch (Exception e) {
                            android.util.Log.e("ShippingRate", "Error parsing error response", e);
                        }
                    }
                    Toast.makeText(ShippingRateManagementActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiService.ShippingRate>> call, Throwable t) {
                showLoading(false);
                android.util.Log.e("ShippingRate", "Update failed", t);
                Toast.makeText(ShippingRateManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onEditRate(ApiService.ShippingRate rate) {
        showEditRateDialog(rate);
    }

    @Override
    public void onDeleteRate(ApiService.ShippingRate rate) {
        // Delete is disabled - rates are fixed
        Toast.makeText(this, "Không thể xóa bảng giá. Chỉ có thể chỉnh sửa giá.", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState() {
        emptyState.setVisibility(rateList.isEmpty() ? View.VISIBLE : View.GONE);
        rvShippingRates.setVisibility(rateList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupBottomNavigation() {
        try {
            navDashboard = findViewById(R.id.navDashboard);
            navUserManagement = findViewById(R.id.navUserManagement);
            navProductManagement = findViewById(R.id.navProductManagement);
            navStoreManagement = findViewById(R.id.navStoreManagement);
            navOrderManagement = findViewById(R.id.navOrderManagement);
            navChatManagement = findViewById(R.id.navChatManagement);

            if (navDashboard != null) {
                navDashboard.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AdminManagementActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            if (navUserManagement != null) {
                navUserManagement.setOnClickListener(v -> {
                    Intent intent = new Intent(this, UserManagementActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            if (navProductManagement != null) {
                navProductManagement.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProductManagementActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            if (navStoreManagement != null) {
                navStoreManagement.setOnClickListener(v -> {
                    Intent intent = new Intent(this, StoreManagementActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            if (navOrderManagement != null) {
                navOrderManagement.setOnClickListener(v -> {
                    Intent intent = new Intent(this, OrderManagementActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            if (navChatManagement != null) {
                navChatManagement.setOnClickListener(v -> {
                    Intent intent = new Intent(this, AdminChatListActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        } catch (Exception e) {
            android.util.Log.e("ShippingRateManagement", "Error setting up bottom navigation", e);
        }
    }
}

