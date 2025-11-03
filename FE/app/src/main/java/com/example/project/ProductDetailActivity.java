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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize API service
        apiService = RetrofitClient.getInstance().getApiService();

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
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity_selector, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Initialize dialog views
        ImageView ivDialogProductImage = dialogView.findViewById(R.id.ivDialogProductImage);
        TextView tvDialogProductName = dialogView.findViewById(R.id.tvDialogProductName);
        TextView tvDialogProductPrice = dialogView.findViewById(R.id.tvDialogProductPrice);
        TextView tvDialogQuantity = dialogView.findViewById(R.id.tvDialogQuantity);
        CardView btnDialogDecrease = dialogView.findViewById(R.id.btnDialogDecrease);
        CardView btnDialogIncrease = dialogView.findViewById(R.id.btnDialogIncrease);
        CardView btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        CardView btnDialogConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        // Set product info
        ivDialogProductImage.setImageResource(productImageResId);
        tvDialogProductName.setText(productName);
        tvDialogProductPrice.setText(productPrice);

        // Quantity control
        final int[] quantity = {1};
        tvDialogQuantity.setText(String.valueOf(quantity[0]));

        btnDialogDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvDialogQuantity.setText(String.valueOf(quantity[0]));
            } else {
                Toast.makeText(this, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            }
        });

        btnDialogIncrease.setOnClickListener(v -> {
            if (quantity[0] < 99) {
                quantity[0]++;
                tvDialogQuantity.setText(String.valueOf(quantity[0]));
            } else {
                Toast.makeText(this, "Số lượng tối đa là 99", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button
        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());

        // Confirm button
        btnDialogConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (isBuyNow) {
                buyNow(quantity[0]);
            } else {
                addToCart(quantity[0]);
            }
        });

        dialog.show();
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            // Change icon color or style here if needed
        } else {
            Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToCart(int quantity) {
        // TODO: Add product to cart database
        String message = "Đã thêm " + quantity + " sản phẩm vào giỏ hàng";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
