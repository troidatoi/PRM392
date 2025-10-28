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
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BikeDetailActivity extends AppCompatActivity {

    // Views
    private CardView btnBack, btnEdit, btnDelete, btnAddToCart;
    private ImageView ivMainImage;
    private RecyclerView rvImageGallery;
    private TextView tvBikeName, tvBikeBrand, tvBikeModel, tvBikePrice, tvOriginalPrice;
    private TextView tvBikeDescription, tvBikeCategory, tvBikeStatus, tvBikeStock;
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
    
    private void openSelectStoreQtyDialog() {
        if (bike == null) { Toast.makeText(this, "Không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show(); return; }
        if (!authManager.isLoggedIn()) { Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show(); return; }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_store_qty, null);
        android.widget.Spinner sp = dialogView.findViewById(R.id.spStores);
        android.widget.TextView tvQty = dialogView.findViewById(R.id.tvQty);
        android.widget.TextView tvStockInfo = dialogView.findViewById(R.id.tvStockInfo);
        androidx.cardview.widget.CardView btnDec = dialogView.findViewById(R.id.btnDec);
        androidx.cardview.widget.CardView btnInc = dialogView.findViewById(R.id.btnInc);

        final List<Store> stores = new ArrayList<>();
        final List<String> display = new ArrayList<>();
        final java.util.Map<String, Integer> storeIdToStock = new java.util.HashMap<>();
        final int[] maxStock = {0};
        final String[] selectedStoreId = {null};
        final int[] qty = {1};

        tvQty.setText("1");

        // Load stores active
        RetrofitClient.getInstance().getApiService().getStores(null, null, null, true, 1, 100, null)
            .enqueue(new Callback<ApiService.StoreResponse>() {
                @Override public void onResponse(Call<ApiService.StoreResponse> call, Response<ApiService.StoreResponse> response) {
                    if (response.isSuccessful() && response.body()!=null && response.body().getData()!=null) {
                        stores.addAll(response.body().getData());
                        ApiService api = RetrofitClient.getInstance().getApiService();
                        for (Store s : stores) {
                            java.util.Map<String,Object> body = new java.util.HashMap<>();
                            body.put("productId", bike.getId());
                            body.put("storeId", s.getId());
                            body.put("quantity", 1);
                            api.checkAvailability(authManager.getAuthHeader(), body).enqueue(new Callback<ApiResponse<Object>>() {
                                @Override public void onResponse(Call<ApiResponse<Object>> call1, Response<ApiResponse<Object>> res1) {
                                    int stock = 0;
                                    if (res1.isSuccessful() && res1.body()!=null && res1.body().isSuccess() && res1.body().getData() instanceof java.util.Map) {
                                        Object cur = ((java.util.Map)res1.body().getData()).get("currentStock");
                                        if (cur instanceof Number) stock = ((Number)cur).intValue();
                                    }
                                    storeIdToStock.put(s.getId(), stock);
                                    String row = s.getName() + " — còn " + stock;
                                    display.add(row);
                                    if (display.size()==stores.size()) {
                                        android.widget.ArrayAdapter<String> ad = new android.widget.ArrayAdapter<>(BikeDetailActivity.this, android.R.layout.simple_spinner_item, display);
                                        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        sp.setAdapter(ad);
                                        if (!stores.isEmpty()) {
                                            selectedStoreId[0] = stores.get(0).getId();
                                            maxStock[0] = storeIdToStock.getOrDefault(selectedStoreId[0], 0);
                                            tvStockInfo.setText("Kho: " + maxStock[0]);
                                        }
                                    }
                                }
                                @Override public void onFailure(Call<ApiResponse<Object>> call1, Throwable t) { }
                            });
                        }
                    }
                }
                @Override public void onFailure(Call<ApiService.StoreResponse> call, Throwable t) { }
            });

        sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position>=0 && position<stores.size()) {
                    selectedStoreId[0] = stores.get(position).getId();
                    maxStock[0] = storeIdToStock.getOrDefault(selectedStoreId[0], 0);
                    tvStockInfo.setText("Kho: " + maxStock[0]);
                    if (qty[0] > maxStock[0] && maxStock[0] > 0) { qty[0] = maxStock[0]; tvQty.setText(String.valueOf(qty[0])); }
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        btnDec.setOnClickListener(v -> { if (qty[0] > 1) { qty[0]--; tvQty.setText(String.valueOf(qty[0])); } });
        btnInc.setOnClickListener(v -> { int limit = maxStock[0] == 0 ? 99 : maxStock[0]; if (qty[0] < limit) { qty[0]++; tvQty.setText(String.valueOf(qty[0])); } });

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Thêm vào giỏ", (d, w) -> {
                if (selectedStoreId[0] == null) { Toast.makeText(this, "Vui lòng chọn cửa hàng", Toast.LENGTH_SHORT).show(); return; }
                String userId = authManager.getCurrentUser()!=null ? authManager.getCurrentUser().getId() : null;
                ApiService api = RetrofitClient.getInstance().getApiService();
                // Bước 5: kiểm tra tồn kho với số lượng đã chọn
                java.util.Map<String,Object> check = new java.util.HashMap<>();
                check.put("productId", bike.getId());
                check.put("storeId", selectedStoreId[0]);
                check.put("quantity", qty[0]);
                api.checkAvailability(authManager.getAuthHeader(), check).enqueue(new Callback<ApiResponse<Object>>() {
                    @Override public void onResponse(Call<ApiResponse<Object>> callCk, Response<ApiResponse<Object>> resCk) {
                        if (resCk.isSuccessful() && resCk.body()!=null && resCk.body().isSuccess()) {
                            // Bước 4: đảm bảo cart
                            java.util.Map<String,String> c = new java.util.HashMap<>(); c.put("userId", userId);
                            api.createCart(authManager.getAuthHeader(), c).enqueue(new Callback<ApiResponse<Object>>() {
                                @Override public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> resp) {
                                    // Bước 6: add item
                                    java.util.Map<String,Object> add = new java.util.HashMap<>();
                                    add.put("userId", userId); add.put("productId", bike.getId()); add.put("storeId", selectedStoreId[0]); add.put("quantity", qty[0]);
                                    api.addItemToCart(authManager.getAuthHeader(), add).enqueue(new Callback<ApiResponse<Object>>() {
                                        @Override public void onResponse(Call<ApiResponse<Object>> call2, Response<ApiResponse<Object>> r2) {
                                            Toast.makeText(BikeDetailActivity.this, r2.isSuccessful()? "Đã thêm vào giỏ" : "Thêm thất bại", Toast.LENGTH_SHORT).show();
                                        }
                                        @Override public void onFailure(Call<ApiResponse<Object>> call2, Throwable t2) { Toast.makeText(BikeDetailActivity.this, "Lỗi: "+t2.getMessage(), Toast.LENGTH_SHORT).show(); }
                                    });
                                }
                                @Override public void onFailure(Call<ApiResponse<Object>> call, Throwable t) { Toast.makeText(BikeDetailActivity.this, "Lỗi: "+t.getMessage(), Toast.LENGTH_SHORT).show(); }
                            });
                        } else {
                            Toast.makeText(BikeDetailActivity.this, "Số lượng vượt tồn kho", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<ApiResponse<Object>> callCk, Throwable t) { Toast.makeText(BikeDetailActivity.this, "Lỗi kiểm tra tồn kho: "+t.getMessage(), Toast.LENGTH_SHORT).show(); }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
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
        tvBikeStock = findViewById(R.id.tvBikeStock);
        tvBikeWarranty = findViewById(R.id.tvBikeWarranty);
        tvBikeRating = findViewById(R.id.tvBikeRating);
        tvBikeFeatures = findViewById(R.id.tvBikeFeatures);
        llSpecifications = findViewById(R.id.llSpecifications);
        llErrorState = findViewById(R.id.llErrorState);
        progressBar = findViewById(R.id.progressBar);
        btnAddToCart = findViewById(R.id.btnAddToCart);
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

        btnAddToCart.setOnClickListener(v -> openSelectStoreQtyDialog());
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
        tvBikeStock.setText("Kho: " + bike.getStock());
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
}
