package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.project.adapters.BikeImageAdapter;
import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.models.User;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;
import com.example.project.utils.NotificationHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BikeDetailActivity extends AppCompatActivity {

    // Views
    private CardView btnBack, btnEdit, btnDelete;
    private ImageView ivMainImage;
    private RecyclerView rvImageGallery;
    private TextView tvBikeName, tvBikeBrand, tvBikeModel, tvBikePrice, tvOriginalPrice;
    private TextView tvBikeDescription, tvBikeCategory, tvBikeStatus;
    private TextView tvBikeWarranty, tvBikeRating, tvBikeFeatures;
    private LinearLayout llSpecifications, llErrorState;
    private ProgressBar progressBar;

    // Data
    private String bikeId;
    private Bike bike;
    private BikeImageAdapter imageAdapter;
    private List<String> imageUrls;
    private ApiService apiService;
    private AuthManager authManager;
    
    // Modern add-to-cart dialog
    private void openSelectStoreQtyDialog() {
        if (bike == null) { 
            Toast.makeText(this, "Không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show(); 
            return; 
        }
        if (!authManager.isLoggedIn()) { 
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show(); 
            return; 
        }

        // Inflate dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
        
        // Initialize views
        ImageView ivDialogProductImage = dialogView.findViewById(R.id.ivDialogProductImage);
        TextView tvDialogProductName = dialogView.findViewById(R.id.tvDialogProductName);
        TextView tvDialogProductPrice = dialogView.findViewById(R.id.tvDialogProductPrice);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        TextView tvMaxStock = dialogView.findViewById(R.id.tvMaxStock);
        androidx.cardview.widget.CardView btnDecreaseQty = dialogView.findViewById(R.id.btnDecreaseQty);
        androidx.cardview.widget.CardView btnIncreaseQty = dialogView.findViewById(R.id.btnIncreaseQty);
        androidx.recyclerview.widget.RecyclerView rvStoreList = dialogView.findViewById(R.id.rvStoreList);
        LinearLayout layoutEmptyStores = dialogView.findViewById(R.id.layoutEmptyStores);
        androidx.cardview.widget.CardView btnCancel = dialogView.findViewById(R.id.btnCancel);
        androidx.cardview.widget.CardView btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        // Create dialog with white background (no liquid glass effect)
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this, android.R.style.Theme_Dialog)
                .setView(dialogView)
                .create();
        
        // Set dialog window properties
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Set product info
        tvDialogProductName.setText(bike.getName());
        tvDialogProductPrice.setText(String.format("%,d VNĐ", (int)bike.getPrice()));
        
        // Load product image
        if (bike.getImages() != null && !bike.getImages().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(bike.getImages().get(0))
                .centerCrop()
                .into(ivDialogProductImage);
        }

        // Quantity management
        final int[] quantity = {1};
        final int[] maxStock = {0};
        tvQuantity.setText(String.valueOf(quantity[0]));

        // Store list management
        final List<com.example.project.adapters.StoreSelectionAdapter.StoreItem> storeItems = new ArrayList<>();
        final com.example.project.adapters.StoreSelectionAdapter adapter = 
            new com.example.project.adapters.StoreSelectionAdapter(storeItems, (store, position) -> {
                maxStock[0] = store.getStock();
                tvMaxStock.setText("Còn: " + maxStock[0]);
                
                // Reset quantity if it exceeds new max stock
                if (quantity[0] > maxStock[0] && maxStock[0] > 0) {
                    quantity[0] = maxStock[0];
                    tvQuantity.setText(String.valueOf(quantity[0]));
                }
            });

        rvStoreList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvStoreList.setAdapter(adapter);

        // Load stores and their inventory
        RetrofitClient.getInstance().getApiService().getStores(null, null, null, true, 1, 100, null)
            .enqueue(new Callback<ApiService.StoreResponse>() {
                @Override 
                public void onResponse(Call<ApiService.StoreResponse> call, Response<ApiService.StoreResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        List<Store> stores = response.body().getData();
                        
                        if (stores.isEmpty()) {
                            layoutEmptyStores.setVisibility(View.VISIBLE);
                            rvStoreList.setVisibility(View.GONE);
                            return;
                        }

                        // Check inventory for each store
                        final int[] loadedCount = {0};
                        for (Store store : stores) {
                            java.util.Map<String, Object> body = new java.util.HashMap<>();
                            body.put("productId", bike.getId());
                            body.put("storeId", store.getId());
                            body.put("quantity", 1);
                            
                            RetrofitClient.getInstance().getApiService()
                                .checkAvailability(authManager.getAuthHeader(), body)
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call1, Response<ApiResponse<Object>> res1) {
                                        int stock = 0;
                                        if (res1.isSuccessful() && res1.body() != null && 
                                            res1.body().isSuccess() && res1.body().getData() instanceof java.util.Map) {
                                            Object cur = ((java.util.Map) res1.body().getData()).get("currentStock");
                                            if (cur instanceof Number) {
                                                stock = ((Number) cur).intValue();
                                            }
                                        }
                                        
                                        storeItems.add(new com.example.project.adapters.StoreSelectionAdapter.StoreItem(
                                            store.getId(),
                                            store.getName(),
                                            store.getAddress() + ", " + store.getCity(),
                                            stock
                                        ));
                                        
                                        loadedCount[0]++;
                                        if (loadedCount[0] == stores.size()) {
                                            adapter.notifyDataSetChanged();
                                            
                                            // Auto-select first available store
                                            for (int i = 0; i < storeItems.size(); i++) {
                                                if (storeItems.get(i).getStock() > 0) {
                                                    adapter.setSelectedPosition(i);
                                                    maxStock[0] = storeItems.get(i).getStock();
                                                    tvMaxStock.setText("Còn: " + maxStock[0]);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    
                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call1, Throwable t) {
                                        loadedCount[0]++;
                                    }
                                });
                        }
                    } else {
                        layoutEmptyStores.setVisibility(View.VISIBLE);
                        rvStoreList.setVisibility(View.GONE);
                    }
                }
                
                @Override
                public void onFailure(Call<ApiService.StoreResponse> call, Throwable t) {
                    Toast.makeText(BikeDetailActivity.this, "Lỗi tải danh sách cửa hàng", Toast.LENGTH_SHORT).show();
                    layoutEmptyStores.setVisibility(View.VISIBLE);
                    rvStoreList.setVisibility(View.GONE);
                }
            });

        // Quantity controls
        btnDecreaseQty.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        btnIncreaseQty.setOnClickListener(v -> {
            int limit = maxStock[0] == 0 ? 99 : maxStock[0];
            if (quantity[0] < limit) {
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));
            } else {
                Toast.makeText(BikeDetailActivity.this, "Vượt quá số lượng tồn kho", Toast.LENGTH_SHORT).show();
            }
        });

        // Dialog controls
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirm.setOnClickListener(v -> {
            com.example.project.adapters.StoreSelectionAdapter.StoreItem selectedStore = adapter.getSelectedStore();
            
            if (selectedStore == null) {
                Toast.makeText(BikeDetailActivity.this, "Vui lòng chọn cửa hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedStore.getStock() == 0) {
                Toast.makeText(BikeDetailActivity.this, "Cửa hàng này hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (quantity[0] > selectedStore.getStock()) {
                Toast.makeText(BikeDetailActivity.this, "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add to cart
            dialog.dismiss();
            addToCart(selectedStore.getId(), quantity[0]);
        });

        dialog.show();
    }

    private void addToCart(String storeId, int quantity) {
        String userId = authManager.getCurrentUser() != null ? authManager.getCurrentUser().getId() : null;
        ApiService api = RetrofitClient.getInstance().getApiService();
        
        // Show loading
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang thêm vào giỏ hàng...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Check availability first
        java.util.Map<String, Object> checkBody = new java.util.HashMap<>();
        checkBody.put("productId", bike.getId());
        checkBody.put("storeId", storeId);
        checkBody.put("quantity", quantity);
        
        api.checkAvailability(authManager.getAuthHeader(), checkBody).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Create/get cart
                    java.util.Map<String, String> cartBody = new java.util.HashMap<>();
                    cartBody.put("userId", userId);
                    
                    api.createCart(authManager.getAuthHeader(), cartBody).enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                            // Add item to cart
                            java.util.Map<String, Object> addBody = new java.util.HashMap<>();
                            addBody.put("userId", userId);
                            addBody.put("productId", bike.getId());
                            addBody.put("storeId", storeId);
                            addBody.put("quantity", quantity);
                            
                            api.addItemToCart(authManager.getAuthHeader(), addBody).enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                    progressDialog.dismiss();
                                    
                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                        Toast.makeText(BikeDetailActivity.this, "✓ Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                        
                                        // Send broadcast to update cart
                                        Intent intent = new Intent("com.example.project.CART_UPDATED");
                                        sendBroadcast(intent);
                                        
                                        // Cập nhật badge ngay lập tức
                                        updateCartBadgeAfterAdd();
                                    } else {
                                        // Show detailed error message from backend
                                        String errorMessage = "Thêm thất bại";
                                        
                                        // Try to parse error from response body
                                        if (!response.isSuccessful() && response.errorBody() != null) {
                                            try {
                                                String errorBodyString = response.errorBody().string();
                                                // Parse JSON to get error message
                                                if (errorBodyString.contains("\"error\"")) {
                                                    int startIndex = errorBodyString.indexOf("\"error\":\"") + 9;
                                                    int endIndex = errorBodyString.indexOf("\"", startIndex);
                                                    if (startIndex > 8 && endIndex > startIndex) {
                                                        errorMessage = errorBodyString.substring(startIndex, endIndex);
                                                    }
                                                } else if (errorBodyString.contains("\"message\"")) {
                                                    int startIndex = errorBodyString.indexOf("\"message\":\"") + 11;
                                                    int endIndex = errorBodyString.indexOf("\"", startIndex);
                                                    if (startIndex > 10 && endIndex > startIndex) {
                                                        errorMessage = errorBodyString.substring(startIndex, endIndex);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else if (response.body() != null && response.body().getMessage() != null) {
                                            errorMessage = response.body().getMessage();
                                        }
                                        
                                        Toast.makeText(BikeDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                                
                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(BikeDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(BikeDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(BikeDetailActivity.this, "Số lượng vượt tồn kho", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(BikeDetailActivity.this, "Lỗi kiểm tra tồn kho: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_detail);

        // Get bike ID from intent
        bikeId = getIntent().getStringExtra("bike_id");
        if (bikeId == null) {
            Toast.makeText(this, "Không tìm thấy ID xe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initData();
        setupRecyclerView();
        setupClickListeners();
        loadBikeDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        ivMainImage = findViewById(R.id.ivMainImage);
        rvImageGallery = findViewById(R.id.rvImageGallery);
        tvBikeName = findViewById(R.id.tvBikeName);
        tvBikeBrand = findViewById(R.id.tvBikeBrand);
        tvBikeModel = findViewById(R.id.tvBikeModel);
        tvBikePrice = findViewById(R.id.tvBikePrice);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvBikeDescription = findViewById(R.id.tvBikeDescription);
        tvBikeCategory = findViewById(R.id.tvBikeCategory);
        tvBikeStatus = findViewById(R.id.tvBikeStatus);
        // Stock label removed from layout
        tvBikeWarranty = findViewById(R.id.tvBikeWarranty);
        tvBikeRating = findViewById(R.id.tvBikeRating);
        tvBikeFeatures = findViewById(R.id.tvBikeFeatures);
        llSpecifications = findViewById(R.id.llSpecifications);
        llErrorState = findViewById(R.id.llErrorState);
        progressBar = findViewById(R.id.progressBar);
        // Add to cart button removed from layout
    }

    private void initData() {
        imageUrls = new ArrayList<>();
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void setupRecyclerView() {
        imageAdapter = new BikeImageAdapter(imageUrls, this::onImageClick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvImageGallery.setLayoutManager(layoutManager);
        rvImageGallery.setAdapter(imageAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnEdit.setOnClickListener(v -> {
            if (authManager.isStaff()) {
                Intent intent = new Intent(BikeDetailActivity.this, UpdateBikeActivity.class);
                intent.putExtra("bike_id", bikeId);
                // Use Activity Result API replacement
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bạn không có quyền chỉnh sửa", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (authManager.isStaff()) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "Bạn không có quyền xóa", Toast.LENGTH_SHORT).show();
            }
        });

        // No add-to-cart action on admin bike detail
    }

    private void onImageClick(int position) {
        // Set main image when gallery image is clicked
        if (position < imageUrls.size()) {
            Glide.with(this)
                .load(imageUrls.get(position))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_bike_placeholder)
                .error(R.drawable.ic_bike_placeholder)
                .centerCrop()
                .into(ivMainImage);
        }
    }

    private void loadBikeDetail() {
        progressBar.setVisibility(View.VISIBLE);
        llErrorState.setVisibility(View.GONE);

        Call<ApiResponse<Bike>> call = apiService.getBikeById(bikeId);
        call.enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    bike = response.body().getData();
                    if (bike != null) {
                        displayBikeDetail();
                    } else {
                        showError("Không tìm thấy thông tin xe");
                    }
                } else {
                    showError("Lỗi tải thông tin xe: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayBikeDetail() {
        // Set basic info
        tvBikeName.setText(bike.getName());
        tvBikeBrand.setText(bike.getBrand());
        tvBikeModel.setText(bike.getModel());
        tvBikeDescription.setText(bike.getDescription());
        tvBikeCategory.setText(getCategoryDisplayName(bike.getCategory()));
        tvBikeStatus.setText(bike.getStatus());
        // Stock row removed
        tvBikeWarranty.setText(bike.getWarranty() != null ? bike.getWarranty() : "12 tháng");

        // Format and set price
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedPrice = formatter.format(bike.getPrice()) + " ₫";
        tvBikePrice.setText(formattedPrice);

        // Set original price if available
        if (bike.getOriginalPrice() > 0 && bike.getOriginalPrice() > bike.getPrice()) {
            String formattedOriginalPrice = formatter.format(bike.getOriginalPrice()) + " ₫";
            tvOriginalPrice.setText(formattedOriginalPrice);
            tvOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
        }

        // Set rating
        if (bike.getRating() != null) {
            tvBikeRating.setText("Đánh giá: " + bike.getRating().getAverage() + "/5 (" + bike.getRating().getCount() + " đánh giá)");
        } else {
            tvBikeRating.setText("Chưa có đánh giá");
        }

        // Set features
        if (bike.getFeatures() != null && !bike.getFeatures().isEmpty()) {
            StringBuilder featuresText = new StringBuilder();
            for (int i = 0; i < bike.getFeatures().size(); i++) {
                featuresText.append("• ").append(bike.getFeatures().get(i));
                if (i < bike.getFeatures().size() - 1) {
                    featuresText.append("\n");
                }
            }
            tvBikeFeatures.setText(featuresText.toString());
        } else {
            tvBikeFeatures.setText("Không có thông tin đặc điểm");
        }

        // Set status color
        switch (bike.getStatus().toLowerCase()) {
            case "available":
                tvBikeStatus.setTextColor(getColor(R.color.green));
                break;
            case "out_of_stock":
                tvBikeStatus.setTextColor(getColor(R.color.red));
                break;
            case "discontinued":
                tvBikeStatus.setTextColor(getColor(R.color.orange));
                break;
            default:
                tvBikeStatus.setTextColor(getColor(R.color.gray));
                break;
        }

        // Load images
        loadBikeImages();

        // Load specifications
        loadSpecifications();
    }

    private void loadBikeImages() {
        imageUrls.clear();
        
        if (bike.getImages() != null && !bike.getImages().isEmpty()) {
            for (Bike.BikeImage image : bike.getImages()) {
                imageUrls.add(image.getUrl());
            }
            
            // Set main image
            Glide.with(this)
                .load(imageUrls.get(0))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_bike_placeholder)
                .error(R.drawable.ic_bike_placeholder)
                .centerCrop()
                .into(ivMainImage);
        } else {
            // Set placeholder
            ivMainImage.setImageResource(R.drawable.ic_bike_placeholder);
        }
        
        imageAdapter.notifyDataSetChanged();
    }

    private void loadSpecifications() {
        llSpecifications.removeAllViews();
        
        if (bike.getSpecifications() != null) {
            Bike.Specifications specs = bike.getSpecifications();
            
            if (specs.getBattery() != null && !specs.getBattery().isEmpty()) {
                addSpecificationItem("Pin", specs.getBattery());
            }
            if (specs.getMotor() != null && !specs.getMotor().isEmpty()) {
                addSpecificationItem("Động cơ", specs.getMotor());
            }
            if (specs.getRange() != null && !specs.getRange().isEmpty()) {
                addSpecificationItem("Quãng đường", specs.getRange());
            }
            if (specs.getMaxSpeed() != null && !specs.getMaxSpeed().isEmpty()) {
                addSpecificationItem("Tốc độ tối đa", specs.getMaxSpeed());
            }
            if (specs.getWeight() != null && !specs.getWeight().isEmpty()) {
                addSpecificationItem("Trọng lượng", specs.getWeight());
            }
            if (specs.getChargingTime() != null && !specs.getChargingTime().isEmpty()) {
                addSpecificationItem("Thời gian sạc", specs.getChargingTime());
            }
        }
        
        if (llSpecifications.getChildCount() == 0) {
            TextView noSpecs = new TextView(this);
            noSpecs.setText("Không có thông tin kỹ thuật");
            noSpecs.setTextColor(getColor(R.color.gray));
            noSpecs.setPadding(16, 16, 16, 16);
            llSpecifications.addView(noSpecs);
        }
    }

    private void addSpecificationItem(String label, String value) {
        LinearLayout specItem = new LinearLayout(this);
        specItem.setOrientation(LinearLayout.HORIZONTAL);
        specItem.setPadding(16, 8, 16, 8);

        TextView labelView = new TextView(this);
        labelView.setText(label + ": ");
        labelView.setTextColor(getColor(R.color.gray));
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f));

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(getColor(R.color.black));
        valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));

        specItem.addView(labelView);
        specItem.addView(valueView);
        llSpecifications.addView(specItem);
    }

    private String getCategoryDisplayName(String category) {
        switch (category) {
            case "city":
                return "Xe đạp điện thành phố";
            case "mountain":
                return "Xe đạp điện leo núi";
            case "folding":
                return "Xe đạp điện gấp";
            case "cargo":
                return "Xe đạp điện chở hàng";
            case "sport":
                return "Xe đạp điện thể thao";
            case "other":
                return "Khác";
            default:
                return category;
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        llErrorState.setVisibility(View.VISIBLE);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa xe")
            .setMessage("Bạn có chắc chắn muốn xóa xe \"" + (bike != null ? bike.getName() : "này") + "\"?\n\nHành động này không thể hoàn tác!")
            .setPositiveButton("Xóa", (dialog, which) -> deleteBike())
            .setNegativeButton("Hủy", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void deleteBike() {
        progressBar.setVisibility(View.VISIBLE);

        String token = "Bearer " + authManager.getToken();
        Call<ApiResponse<Void>> call = apiService.deleteBike(token, bikeId);
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BikeDetailActivity.this, "Xóa xe thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Lỗi xóa xe: " + (response.body() != null ? response.body().getMessage() : response.message());
                    Toast.makeText(BikeDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BikeDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Reload bike data when bike is updated successfully
            loadBikeDetail();
        }
    }
    
    /**
     * Cập nhật badge sau khi thêm sản phẩm vào giỏ hàng
     */
    private void updateCartBadgeAfterAdd() {
        AuthManager auth = AuthManager.getInstance(this);
        User user = auth.getCurrentUser();
        if (user == null) return;
        
        ApiService api = RetrofitClient.getInstance().getApiService();
        api.getCartByUser(auth.getAuthHeader(), user.getId())
            .enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        try {
                            Object data = response.body().getData();
                            java.util.Map dataMap = (java.util.Map) data;
                            java.util.Map cartMap = (java.util.Map) dataMap.get("cart");
                            
                            int itemCount = 0;
                            if (cartMap != null && cartMap.get("itemCount") instanceof Number) {
                                itemCount = ((Number) cartMap.get("itemCount")).intValue();
                            }
                            
                            NotificationHelper.updateCartBadge(BikeDetailActivity.this, itemCount);
                        } catch (Exception e) {
                            // Ignore error, badge will be updated when CartActivity loads
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    // Ignore error, badge will be updated when CartActivity loads
                }
            });
    }
}
