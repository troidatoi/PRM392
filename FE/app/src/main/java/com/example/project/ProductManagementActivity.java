package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.ProductCardAdapter;
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

    private RecyclerView rvProducts;
    private LinearLayout emptyState;
    private TextView tvBikeCount;
    private TextView btnCategory, btnPrice, btnStock, btnEditDelete;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAddProduct;
    private ProductCardAdapter productAdapter;
    private List<Bike> bikeList;
    private ApiService apiService;
    private AuthManager authManager;
    
    // Bottom navigation
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconProductManagement;
    private TextView tvProductManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        initViews();
        initData();
        setupRecyclerView();
        loadBikes();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        emptyState = findViewById(R.id.emptyState);
        tvBikeCount = findViewById(R.id.tvBikeCount);
        
        // Filter buttons
        btnCategory = findViewById(R.id.btnCategory);
        btnPrice = findViewById(R.id.btnPrice);
        btnStock = findViewById(R.id.btnStock);
        btnEditDelete = findViewById(R.id.btnEditDelete);
        
        // FAB
        fabAddProduct = findViewById(R.id.fabAddProduct);
        
        // Bottom navigation
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);
        iconProductManagement = findViewById(R.id.iconProductManagement);
        tvProductManagement = findViewById(R.id.tvProductManagement);
    }

    private void initData() {
        bikeList = new ArrayList<>();
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductCardAdapter(bikeList);
        productAdapter.setOnProductClickListener(new ProductCardAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Bike product) {
                // Open product detail
                Intent intent = new Intent(ProductManagementActivity.this, BikeDetailActivity.class);
                intent.putExtra("bike_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onProductEdit(Bike product) {
                // Open edit product
                Intent intent = new Intent(ProductManagementActivity.this, UpdateBikeActivity.class);
                intent.putExtra("bike_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onProductDelete(Bike product) {
                // Show delete confirmation
                Toast.makeText(ProductManagementActivity.this, "Delete: " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(productAdapter);
    }

    private void loadBikes() {
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
                            productAdapter.notifyDataSetChanged();
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
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }


    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        emptyState.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        // Filter buttons
        btnCategory.setOnClickListener(v -> {
            // TODO: Implement category filter
            Toast.makeText(this, "Category filter", Toast.LENGTH_SHORT).show();
        });

        btnPrice.setOnClickListener(v -> {
            // TODO: Implement price filter
            Toast.makeText(this, "Price filter", Toast.LENGTH_SHORT).show();
        });

        btnStock.setOnClickListener(v -> {
            // TODO: Implement stock filter
            Toast.makeText(this, "Stock filter", Toast.LENGTH_SHORT).show();
        });

        btnEditDelete.setOnClickListener(v -> {
            // TODO: Implement edit/delete mode
            Toast.makeText(this, "Edit/Delete mode", Toast.LENGTH_SHORT).show();
        });

        // FAB -> navigate to CreateBikeActivity (full screen)
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductManagementActivity.this, CreateBikeActivity.class);
            startActivity(intent);
        });
    }
    
    private void setupBottomNavigation() {
        // Set Product Management tab as active
        if (iconProductManagement != null) {
            iconProductManagement.setColorFilter(0xFF2196F3); // active blue
        }
        if (tvProductManagement != null) {
            tvProductManagement.setTextColor(0xFF2196F3); // active blue
        }
        
        // Bottom navigation click listeners
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(ProductManagementActivity.this, AdminManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navUserManagement != null) {
            navUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(ProductManagementActivity.this, UserManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navStoreManagement != null) {
            navStoreManagement.setOnClickListener(v -> {
                Intent intent = new Intent(ProductManagementActivity.this, StoreManagementActivity.class);
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
                Intent intent = new Intent(ProductManagementActivity.this, AdminChatListActivity.class);
                startActivity(intent);
            });
        }
    }

    // Không cần onActivityResult nữa vì đã chuyển sang startActivity

    @Override
    protected void onResume() {
        super.onResume();
        // Reload bikes when returning from add product screen
        loadBikes();
    }
}
