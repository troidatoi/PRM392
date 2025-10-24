package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.BikeAdapter;
import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductManagementActivity extends AppCompatActivity {

    private CardView btnBack, btnAddProduct;
    private RecyclerView rvProducts;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private TextView tvBikeCount;
    private BikeAdapter bikeAdapter;
    private List<Bike> bikeList;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        initViews();
        initData();
        setupRecyclerView();
        loadBikes();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        rvProducts = findViewById(R.id.rvProducts);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        tvBikeCount = findViewById(R.id.tvBikeCount);
    }

    private void initData() {
        bikeList = new ArrayList<>();
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void setupRecyclerView() {
        bikeAdapter = new BikeAdapter(bikeList);
        bikeAdapter.setOnBikeClickListener(this::onBikeClick);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(bikeAdapter);
    }

    private void loadBikes() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        rvProducts.setVisibility(View.GONE);

        // Check if user is admin or staff
        if (!authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<Bike[]>> call = apiService.getBikes(1, 50, null, null, null, null, null, null, null);
        call.enqueue(new Callback<ApiResponse<Bike[]>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike[]>> call, Response<ApiResponse<Bike[]>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Bike[]> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        bikeList.clear();
                        bikeList.addAll(Arrays.asList(apiResponse.getData()));
                        
                        // Update bike count from pagination
                        if (apiResponse.getPagination() != null) {
                            tvBikeCount.setText("Tổng số xe: " + apiResponse.getPagination().getTotalItems());
                        }
                        
                        if (bikeList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            rvProducts.setVisibility(View.GONE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                            rvProducts.setVisibility(View.VISIBLE);
                            bikeAdapter.notifyDataSetChanged();
                        }
                    } else {
                        showError("Không thể tải danh sách xe");
                    }
                } else {
                    showError("Lỗi tải dữ liệu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike[]>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void onBikeClick(Bike bike) {
        // TODO: Navigate to bike detail page
        Toast.makeText(this, "Xem chi tiết xe: " + bike.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        emptyState.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductManagementActivity.this, CreateBikeActivity.class);
            startActivityForResult(intent, 1001);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Reload bikes when bike is created successfully
            loadBikes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload bikes when returning from add product screen
        loadBikes();
    }
}
