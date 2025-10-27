package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ApiService apiService;

    // Bottom Navigation
    private View navHome, navProducts, navCart, navAccount;
    private View blurHome, blurProducts, blurCart, blurAccount;
    private ImageView iconHome, iconProducts, iconCart, iconAccount;
    private TextView tvHome, tvProducts, tvCart, tvAccount;

    // Search Box
    private EditText etSearch;
    private CardView searchCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        initViews();
        setupSearchBox();
        setupRecyclerView();
        setupBottomNavigation();
        
        // Initialize API service
        apiService = RetrofitClient.getInstance().getApiService();
        
        // Load bikes from API
        loadBikes();
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);

        // Search Box
        etSearch = findViewById(R.id.etSearch);
        searchCard = findViewById(R.id.searchCard);

        // Bottom Navigation
        navHome = findViewById(R.id.navHome);
        navProducts = findViewById(R.id.navProducts);
        navCart = findViewById(R.id.navCart);
        navAccount = findViewById(R.id.navAccount);

        blurHome = findViewById(R.id.blurHome);
        blurProducts = findViewById(R.id.blurProducts);
        blurCart = findViewById(R.id.blurCart);
        blurAccount = findViewById(R.id.blurAccount);

        iconHome = findViewById(R.id.iconHome);
        iconProducts = findViewById(R.id.iconProducts);
        iconCart = findViewById(R.id.iconCart);
        iconAccount = findViewById(R.id.iconAccount);

        tvHome = findViewById(R.id.tvHome);
        tvProducts = findViewById(R.id.tvProducts);
        tvCart = findViewById(R.id.tvCart);
        tvAccount = findViewById(R.id.tvAccount);
    }

    private void setupSearchBox() {
        // Khi bấm vào ô tìm kiếm, mở SearchActivity
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Khi bấm vào searchCard cũng mở SearchActivity
        searchCard.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Disable input trực tiếp ở ShopActivity
        etSearch.setFocusable(false);
        etSearch.setClickable(true);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);

        // Set GridLayoutManager with 2 columns for shop view
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(productAdapter);
    }

    private void setupBottomNavigation() {
        // Set Products as selected by default (we're on shop page)
        selectNavItem(blurProducts, iconProducts, tvProducts);

        navHome.setOnClickListener(v -> {
            finish(); // Go back to home
        });

        navProducts.setOnClickListener(v -> {
            selectNavItem(blurProducts, iconProducts, tvProducts);
            deselectNavItem(blurHome, iconHome, tvHome);
            deselectNavItem(blurCart, iconCart, tvCart);
            deselectNavItem(blurAccount, iconAccount, tvAccount);
        });

        navCart.setOnClickListener(v -> {
            // Open Cart Activity
            Intent intent = new Intent(ShopActivity.this, CartActivity.class);
            startActivity(intent);
        });

        navAccount.setOnClickListener(v -> {
            // Open Account Activity
            Intent intent = new Intent(ShopActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }

    private void selectNavItem(View blur, ImageView icon, TextView text) {
        blur.setVisibility(View.VISIBLE);
        icon.setColorFilter(Color.parseColor("#2196F3"));
        text.setTextColor(Color.parseColor("#2196F3"));
    }

    private void deselectNavItem(View blur, ImageView icon, TextView text) {
        blur.setVisibility(View.GONE);
        icon.setColorFilter(Color.parseColor("#666666"));
        text.setTextColor(Color.parseColor("#666666"));
    }

    private void loadBikes() {
        // Call API to get all bikes
        apiService.getBikes(1, 100, null, null, null, null, null, null, null)
            .enqueue(new Callback<ApiResponse<Bike[]>>() {
                @Override
                public void onResponse(Call<ApiResponse<Bike[]>> call, Response<ApiResponse<Bike[]>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Bike[] bikes = response.body().getData();
                        if (bikes != null && bikes.length > 0) {
                            productList.clear();
                            
                            // Convert bikes to products
                            for (Bike bike : bikes) {
                                String priceText = String.format("%.0f ₫", bike.getPrice());
                                productList.add(new Product(
                                    bike.getName(),
                                    bike.getDescription(),
                                    priceText,
                                    R.drawable.splash_bike_background
                                ));
                            }
                            
                            productAdapter.notifyDataSetChanged();
                        } else {
                            // If no bikes, use sample data
                            loadSampleProducts();
                        }
                    } else {
                        // Fallback to sample data if API fails
                        loadSampleProducts();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Bike[]>> call, Throwable t) {
                    // Fallback to sample data if API fails
                    loadSampleProducts();
                }
            });
    }
    
    private void loadSampleProducts() {
        // === XE ĐẠP ĐIỆN (8 sản phẩm) ===
        productList.add(new Product(
            "VinFast Klara S",
            "Pin Lithium 60V, tốc độ 50km/h",
            "29.990.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Yadea E3",
            "Động cơ 1200W, quãng đường 80km",
            "25.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Dibao Type R",
            "Phanh ABS, màn hình LCD",
            "32.000.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Pega Aska",
            "Vận hành êm, bảo hành 3 năm",
            "27.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Giant M133S",
            "Chất lượng cao, pin 72V",
            "35.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "HKbike Cap A+",
            "Tốc độ cao, phanh đĩa",
            "28.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Bluera 133E",
            "Cao cấp, sang trọng",
            "31.200.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Anbico Smart",
            "Thông minh, GPS tích hợp",
            "26.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // === XE ĐẠP THỂ THAO (4 sản phẩm) ===
        productList.add(new Product(
            "Road Bike Pro",
            "Khung carbon, siêu nhẹ",
            "15.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Giant TCR",
            "Khí động học, Shimano 105",
            "18.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Specialized Allez",
            "Xe đua tốc độ cao",
            "22.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Trek Emonda",
            "Siêu nhẹ, leo dốc tốt",
            "19.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        // === XE ĐẠP ĐỊA HÌNH (5 sản phẩm) ===
        productList.add(new Product(
            "MTB Pro 27.5",
            "Leo núi, phuộc 120mm",
            "8.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Trek Marlin 7",
            "Nhôm cao cấp, phanh thủy lực",
            "12.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Specialized Rock",
            "Địa hình phức tạp, 21 tốc độ",
            "11.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Giant Talon 29",
            "Bánh 29 inch, khung nhôm",
            "10.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Cannondale Trail",
            "Ổn định, bền bỉ",
            "13.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // === XE ĐẠP GẤP (5 sản phẩm) ===
        productList.add(new Product(
            "Dahon K3",
            "Siêu gọn, chỉ 7kg",
            "5.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Java Fit 16",
            "Gấp trong 10 giây",
            "6.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Fornix BF-300",
            "Phù hợp đi làm, học",
            "4.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Hachiko HA-02",
            "Gọn nhẹ, màu đẹp",
            "5.200.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Sava Z0 Carbon",
            "Xe gấp carbon cao cấp",
            "15.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // === XE ĐẠP TRẺ EM (5 sản phẩm) ===
        productList.add(new Product(
            "Disney Princess",
            "Cho bé gái 5-8 tuổi",
            "1.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Giant XTC Jr",
            "Thể thao, bánh 20 inch",
            "3.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "BMX Freestyle",
            "Biểu diễn từ 10 tuổi",
            "2.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Spider Man",
            "Cho bé trai 6-9 tuổi",
            "2.200.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Royal Baby",
            "An toàn, dễ sử dụng",
            "2.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // === XE ĐẠP TOURING (3 sản phẩm) ===
        productList.add(new Product(
            "Giant Escape 3",
            "Đi phố, 21 tốc độ",
            "9.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Trek FX 2",
            "Đa năng, hàng ngày",
            "10.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Momentum Street",
            "Thoải mái, phong cách",
            "8.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // === FIXED GEAR (3 sản phẩm) ===
        productList.add(new Product(
            "Fixed Gear Pista",
            "Thời trang, tối giản",
            "4.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Single Speed",
            "1 tốc độ, cổ điển",
            "3.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Fixie Retro",
            "Phong cách Vintage",
            "4.200.000 ₫",
            R.drawable.splash_bike_background
        ));

        productAdapter.notifyDataSetChanged();
    }
}
