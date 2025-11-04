package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private LinearLayout indicatorLayout;
    private ImageView ivFavorite;
    private TextView tvProductName, tvProductPrice, tvProductDescription;
    private CardView btnBack, btnFavorite, btnAddToCart, btnBuyNow;
    private RecyclerView rvStoreStock;

    private boolean isFavorite = false;
    private int currentImagePosition = 0;

    // Product data
    private String bikeId;
    private Bike bike;
    private String productName;
    private String productDescription;
    private String productPrice;
    private int productImageResId;
    private String productImageUrl;
    private List<Integer> productImages;
    private List<String> productImageUrls;
    private List<ProductStock> productStockList;
    private ProductStockAdapter stockAdapter;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize API service and Auth
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);

        initViews();
        loadProductData();
        setupStoreStock();
        setupClickListeners();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        indicatorLayout = findViewById(R.id.indicatorLayout);
        ivFavorite = findViewById(R.id.ivFavorite);

        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDescription = findViewById(R.id.tvProductDescription);

        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        rvStoreStock = findViewById(R.id.rvStoreStock);
    }

    private void loadProductData() {
        // Get product data from intent
        Intent intent = getIntent();
        bikeId = intent.getStringExtra("bikeId");
        productName = intent.getStringExtra("productName");
        productDescription = intent.getStringExtra("productDescription");
        productPrice = intent.getStringExtra("productPrice");
        productImageResId = intent.getIntExtra("productImage", R.drawable.splash_bike_background);
        productImageUrl = intent.getStringExtra("productImageUrl");

        // If bikeId is available, fetch from API
        if (bikeId != null && !bikeId.isEmpty()) {
            loadBikeFromApi(bikeId);
        } else {
            // Use fallback data from intent
            displayProductData();
        }
    }

    private void loadBikeFromApi(String bikeId) {
        apiService.getBikeById(bikeId).enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Bike bike = response.body().getData();
                    if (bike != null) {
                        // Update product data from API
                        productName = bike.getName();
                        productDescription = bike.getDescription();
                        productPrice = String.format("%.0f ₫", bike.getPrice());
                        
                        // Get images from API
                        productImageUrls = new ArrayList<>();
                        if (bike.getImages() != null && !bike.getImages().isEmpty()) {
                            for (Bike.BikeImage image : bike.getImages()) {
                                productImageUrls.add(image.getUrl());
                            }
                        }
                        
                        // Display the data
                        displayProductData();
                    } else {
                        // Fallback to intent data
                        displayProductData();
                    }
                } else {
                    // Fallback to intent data
                    Toast.makeText(ProductDetailActivity.this, 
                        "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    displayProductData();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                // Fallback to intent data
                Toast.makeText(ProductDetailActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                displayProductData();
            }
        });
    }

    private void displayProductData() {
        // Set default values if not provided
        if (productName == null || productName.isEmpty()) {
            productName = "Xe đạp điện VinFast Klara S";
        }
        if (productDescription == null || productDescription.isEmpty()) {
            productDescription = "Xe đạp điện thông minh với công nghệ tiên tiến, thiết kế hiện đại và tiết kiệm năng lượng. Pin lithium-ion dung lượng cao, quãng đường di chuyển lên đến 80km với một lần sạc.";
        }
        if (productPrice == null || productPrice.isEmpty()) {
            productPrice = "29.990.000 VNĐ";
        }

        // Display product data
        tvProductName.setText(productName);
        tvProductDescription.setText(productDescription);
        tvProductPrice.setText(productPrice);

        // Setup product images
        setupImageCarousel();
    }

    private void setupImageCarousel() {
        // Setup product images
        if (productImageUrls != null && !productImageUrls.isEmpty()) {
            // Use URL images from API
            ImageCarouselUrlAdapter carouselAdapter = new ImageCarouselUrlAdapter(productImageUrls);
            viewPagerImages.setAdapter(carouselAdapter);
            setupIndicators(productImageUrls.size());
        } else {
            // Use resource images as fallback
            productImages = new ArrayList<>();
            productImages.add(productImageResId);
            productImages.add(R.drawable.splash_bike_background);
            productImages.add(R.drawable.google_ai_studio);
            productImages.add(R.drawable.google_ai_studio_1);
            
            ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(productImages);
            viewPagerImages.setAdapter(carouselAdapter);
            setupIndicators(productImages.size());
        }

        setCurrentIndicator(0);

        // Listen to page changes
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentImagePosition = position;
                setCurrentIndicator(position);
            }
        });
    }

    private void setupIndicators(int count) {
        indicatorLayout.removeAllViews();
        ImageView[] indicators = new ImageView[count];

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageResource(R.drawable.ic_account);
            indicators[i].setLayoutParams(layoutParams);
            indicators[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
            indicators[i].setColorFilter(Color.parseColor("#80FFFFFF"));
            indicatorLayout.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int position) {
        int childCount = indicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) indicatorLayout.getChildAt(i);
            if (i == position) {
                imageView.setColorFilter(Color.parseColor("#FFFFFF"));
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = (int) (12 * getResources().getDisplayMetrics().density);
                params.height = (int) (12 * getResources().getDisplayMetrics().density);
                imageView.setLayoutParams(params);
            } else {
                imageView.setColorFilter(Color.parseColor("#80FFFFFF"));
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.width = (int) (8 * getResources().getDisplayMetrics().density);
                params.height = (int) (8 * getResources().getDisplayMetrics().density);
                imageView.setLayoutParams(params);
            }
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Favorite button
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Add to cart button
        btnAddToCart.setOnClickListener(v -> showQuantityDialog(false));

        // Buy now button
        btnBuyNow.setOnClickListener(v -> showQuantityDialog(true));
    }

    private void showQuantityDialog(boolean isBuyNow) {
        if (bike == null && bikeId == null) {
            Toast.makeText(this, "Không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            // Navigate to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        // If we don't have bike object yet, load it first
        if (bike == null && bikeId != null) {
            loadBikeAndShowDialog(isBuyNow);
            return;
        }

        // Show the beautiful modal
        showAddToCartDialog();
    }

    private void loadBikeAndShowDialog(boolean isBuyNow) {
        apiService.getBikeById(bikeId).enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bike = response.body().getData();
                    showAddToCartDialog();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddToCartDialog() {
        // Inflate dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
        
        // Initialize views
        ImageView ivDialogProductImage = dialogView.findViewById(R.id.ivDialogProductImage);
        TextView tvDialogProductName = dialogView.findViewById(R.id.tvDialogProductName);
        TextView tvDialogProductPrice = dialogView.findViewById(R.id.tvDialogProductPrice);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        TextView tvMaxStock = dialogView.findViewById(R.id.tvMaxStock);
        CardView btnDecreaseQty = dialogView.findViewById(R.id.btnDecreaseQty);
        CardView btnIncreaseQty = dialogView.findViewById(R.id.btnIncreaseQty);
        RecyclerView rvStoreList = dialogView.findViewById(R.id.rvStoreList);
        LinearLayout layoutEmptyStores = dialogView.findViewById(R.id.layoutEmptyStores);
        CardView btnCancel = dialogView.findViewById(R.id.btnCancel);
        CardView btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        // Create dialog with white background
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_Dialog)
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Set product info
        tvDialogProductName.setText(productName != null ? productName : (bike != null ? bike.getName() : ""));
        
        if (bike != null) {
            tvDialogProductPrice.setText(String.format("%,d VNĐ", (int)bike.getPrice()));
            
            // Load product image - Fix: Extract URL from BikeImage object
            if (bike.getImages() != null && !bike.getImages().isEmpty()) {
                String imageUrl = bike.getImages().get(0).getUrl(); // Get URL from BikeImage
                Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(ivDialogProductImage);
            }
        } else if (productPrice != null) {
            tvDialogProductPrice.setText(productPrice);
            
            // Load from productImageUrls if available
            if (productImageUrls != null && !productImageUrls.isEmpty()) {
                Glide.with(this)
                    .load(productImageUrls.get(0))
                    .centerCrop()
                    .into(ivDialogProductImage);
            } else if (productImageUrl != null) {
                Glide.with(this)
                    .load(productImageUrl)
                    .centerCrop()
                    .into(ivDialogProductImage);
            } else {
                ivDialogProductImage.setImageResource(productImageResId);
            }
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

        rvStoreList.setLayoutManager(new LinearLayoutManager(this));
        rvStoreList.setAdapter(adapter);

        // Load stores and their inventory
        String productIdToUse = (bike != null) ? bike.getId() : bikeId;
        
        apiService.getStores(null, null, null, true, 1, 100, null)
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
                            body.put("productId", productIdToUse);
                            body.put("storeId", store.getId());
                            body.put("quantity", 1);
                            
                            apiService.checkAvailability(authManager.getAuthHeader(), body)
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
                    Toast.makeText(ProductDetailActivity.this, "Lỗi tải danh sách cửa hàng", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ProductDetailActivity.this, "Vượt quá số lượng tồn kho", Toast.LENGTH_SHORT).show();
            }
        });

        // Dialog controls
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirm.setOnClickListener(v -> {
            com.example.project.adapters.StoreSelectionAdapter.StoreItem selectedStore = adapter.getSelectedStore();
            
            if (selectedStore == null) {
                Toast.makeText(ProductDetailActivity.this, "Vui lòng chọn cửa hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedStore.getStock() == 0) {
                Toast.makeText(ProductDetailActivity.this, "Cửa hàng này hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (quantity[0] > selectedStore.getStock()) {
                Toast.makeText(ProductDetailActivity.this, "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add to cart
            dialog.dismiss();
            addToCartWithStore(selectedStore.getId(), quantity[0]);
        });

        dialog.show();
    }

    private void addToCartWithStore(String storeId, int quantity) {
        String userId = authManager.getCurrentUser() != null ? authManager.getCurrentUser().getId() : null;
        String productIdToUse = (bike != null) ? bike.getId() : bikeId;
        
        // Show loading
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang thêm vào giỏ hàng...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Check availability first
        java.util.Map<String, Object> checkBody = new java.util.HashMap<>();
        checkBody.put("productId", productIdToUse);
        checkBody.put("storeId", storeId);
        checkBody.put("quantity", quantity);
        
        apiService.checkAvailability(authManager.getAuthHeader(), checkBody).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Create/get cart
                    java.util.Map<String, String> cartBody = new java.util.HashMap<>();
                    cartBody.put("userId", userId);
                    
                    apiService.createCart(authManager.getAuthHeader(), cartBody).enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                            // Add item to cart
                            java.util.Map<String, Object> addBody = new java.util.HashMap<>();
                            addBody.put("userId", userId);
                            addBody.put("productId", productIdToUse);
                            addBody.put("storeId", storeId);
                            addBody.put("quantity", quantity);
                            
                            apiService.addItemToCart(authManager.getAuthHeader(), addBody).enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                    progressDialog.dismiss();
                                    
                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                        Toast.makeText(ProductDetailActivity.this, "✓ Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                        
                                        // Send broadcast to update cart
                                        Intent intent = new Intent("com.example.project.CART_UPDATED");
                                        sendBroadcast(intent);
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
                                        
                                        Toast.makeText(ProductDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                                
                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(ProductDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ProductDetailActivity.this, "Số lượng vượt tồn kho", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProductDetailActivity.this, "Lỗi kiểm tra tồn kho: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(int quantity) {
        // This method is now replaced by showAddToCartDialog
        showAddToCartDialog();
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow(int quantity) {
        // TODO: Add product to cart and navigate to checkout
        String message = "Mua " + quantity + " sản phẩm - " + productName;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Navigate to checkout activity
        Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
        intent.putExtra("productName", productName);
        intent.putExtra("productPrice", productPrice);
        intent.putExtra("productImage", productImageResId);
        intent.putExtra("quantity", quantity);
        startActivity(intent);
    }

    private void setupStoreStock() {
        // TODO: Load real store stock data from database
        productStockList = new ArrayList<>();
        productStockList.add(new ProductStock(1, "Chi nhánh Quận 1", "123 Nguyễn Huệ, Q.1, TP.HCM", 24, "Còn hàng"));
        productStockList.add(new ProductStock(2, "Chi nhánh Quận 3", "456 Võ Văn Tần, Q.3, TP.HCM", 8, "Sắp hết"));
        productStockList.add(new ProductStock(3, "Chi nhánh Bình Thạnh", "789 Điện Biên Phủ, Q.Bình Thạnh", 15, "Còn hàng"));
        productStockList.add(new ProductStock(4, "Chi nhánh Quận 7", "321 Nguyễn Thị Thập, Q.7, TP.HCM", 0, "Hết hàng"));
        productStockList.add(new ProductStock(5, "Chi nhánh Thủ Đức", "555 Võ Văn Ngân, TP.Thủ Đức", 12, "Còn hàng"));

        stockAdapter = new ProductStockAdapter(productStockList);
        rvStoreStock.setLayoutManager(new LinearLayoutManager(this));
        rvStoreStock.setAdapter(stockAdapter);
    }
}
