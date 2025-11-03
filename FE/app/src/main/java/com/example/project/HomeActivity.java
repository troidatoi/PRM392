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
import androidx.recyclerview.widget.LinearLayoutManager;
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

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ApiService apiService;

    // Bottom Navigation
    private View navHome, navProducts, navCart, navAccount;
    private ImageView iconHome, iconProducts, iconCart, iconAccount;
    private TextView tvHome, tvProducts, tvCart, tvAccount;

    // Floating Chat Button
    private CardView btnFloatingChat;

    // Search Box
    private EditText etSearch;
    private CardView searchCard;

    private static final String COLOR_BROWN = "#7B6047";
    private static final String COLOR_BE = "#CEB797";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupSearchBox();
        setupRecyclerView();
        setupBottomNavigation();
        setupChatButton();
        
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

        iconHome = findViewById(R.id.iconHome);
        iconProducts = findViewById(R.id.iconProducts);
        iconCart = findViewById(R.id.iconCart);
        iconAccount = findViewById(R.id.iconAccount);

        tvHome = findViewById(R.id.tvHome);
        tvProducts = findViewById(R.id.tvProducts);
        tvCart = findViewById(R.id.tvCart);
        tvAccount = findViewById(R.id.tvAccount);

        // Chat Button
        btnFloatingChat = findViewById(R.id.btnFloatingChat);
    }

    private void setupSearchBox() {
        // Khi bấm vào ô tìm kiếm, mở SearchActivity
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Khi bấm vào searchCard cũng mở SearchActivity
        searchCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Disable input trực tiếp ở HomeActivity
        etSearch.setFocusable(false);
        etSearch.setClickable(true);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);

        // Set LinearLayoutManager with vertical orientation
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(productAdapter);
    }

    private void setupBottomNavigation() {
        // Đặt mặc định: TẤT CẢ tab be, chỉ tab hiện tại là nâu đậm
        iconHome.setColorFilter(Color.parseColor(COLOR_BROWN));
        tvHome.setTextColor(Color.parseColor(COLOR_BROWN));
        iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
        tvProducts.setTextColor(Color.parseColor(COLOR_BE));
        iconCart.setColorFilter(Color.parseColor(COLOR_BE));
        tvCart.setTextColor(Color.parseColor(COLOR_BE));
        iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
        tvAccount.setTextColor(Color.parseColor(COLOR_BE));

        navHome.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvHome.setTextColor(Color.parseColor(COLOR_BROWN));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
            tvProducts.setTextColor(Color.parseColor(COLOR_BE));
            iconCart.setColorFilter(Color.parseColor(COLOR_BE));
            tvCart.setTextColor(Color.parseColor(COLOR_BE));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
            tvAccount.setTextColor(Color.parseColor(COLOR_BE));
            // Không cần mở lại Home nếu đã ở Home
        });
        navProducts.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BE));
            tvHome.setTextColor(Color.parseColor(COLOR_BE));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvProducts.setTextColor(Color.parseColor(COLOR_BROWN));
            iconCart.setColorFilter(Color.parseColor(COLOR_BE));
            tvCart.setTextColor(Color.parseColor(COLOR_BE));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
            tvAccount.setTextColor(Color.parseColor(COLOR_BE));
            Intent intent = new Intent(HomeActivity.this, ShopActivity.class);
            startActivity(intent);
        });
        navCart.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BE));
            tvHome.setTextColor(Color.parseColor(COLOR_BE));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
            tvProducts.setTextColor(Color.parseColor(COLOR_BE));
            iconCart.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvCart.setTextColor(Color.parseColor(COLOR_BROWN));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
            tvAccount.setTextColor(Color.parseColor(COLOR_BE));
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });
        navAccount.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BE));
            tvHome.setTextColor(Color.parseColor(COLOR_BE));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
            tvProducts.setTextColor(Color.parseColor(COLOR_BE));
            iconCart.setColorFilter(Color.parseColor(COLOR_BE));
            tvCart.setTextColor(Color.parseColor(COLOR_BE));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvAccount.setTextColor(Color.parseColor(COLOR_BROWN));
            Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }

    private void selectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#2196F3"));
        text.setTextColor(Color.parseColor("#2196F3"));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void deselectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#666666"));
        text.setTextColor(Color.parseColor("#666666"));
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void loadBikes() {
        // Call API to get all bikes
        apiService.getBikes(1, 100, null, null, null, null, null, null, null).enqueue(new Callback<ApiResponse<Bike[]>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike[]>> call, Response<ApiResponse<Bike[]>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Bike[] bikes = response.body().getData();
                    if (bikes != null && bikes.length > 0) {
                        productList.clear();
                        
                        // Convert bikes to products
                        for (Bike bike : bikes) {
                            String priceText = String.format("%.0f ₫", bike.getPrice());
                            String imageUrl = (bike.getImages() != null && !bike.getImages().isEmpty()) 
                                ? bike.getImages().get(0).getUrl() 
                                : null;
                            
                            Product product = new Product(
                                bike.getName(),
                                bike.getDescription(),
                                priceText,
                                R.drawable.splash_bike_background,
                                imageUrl
                            );
                            product.setBikeId(bike.getId());
                            productList.add(product);
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
        // Xe đạp điện cao cấp
        productList.add(new Product(
            "Xe đạp điện VinFast Klara S",
            "Xe đạp điện thông minh, thiết kế hiện đại, pin Lithium 60V",
            "29.990.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Yadea E3",
            "Công nghệ Nhật Bản, động cơ 1200W, quãng đường 80km",
            "25.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Dibao Type R",
            "Thiết kế thể thao, phanh ABS, màn hình LCD",
            "32.000.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Pega Aska",
            "Phong cách trẻ trung, vận hành êm ái, bảo hành 3 năm",
            "27.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Giant M133S",
            "Thương hiệu uy tín, chất lượng cao, pin Lithium 72V",
            "35.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện HKbike Cap A+",
            "Pin lithium 60V, tốc độ cao, phanh đĩa trước sau",
            "28.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Bluera 133E",
            "Xe đạp điện cao cấp, sang trọng, động cơ mạnh mẽ",
            "31.200.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Anbico Smart",
            "Công nghệ thông minh, tiết kiệm, GPS tích hợp",
            "26.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp thể thao
        productList.add(new Product(
            "Xe đạp thể thao Road Bike Pro",
            "Xe đạp đua chuyên nghiệp, khung carbon",
            "15.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp thể thao Giant TCR",
            "Thiết kế khí động học, Shimano 105",
            "18.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp địa hình
        productList.add(new Product(
            "Xe đạp địa hình MTB Pro",
            "Xe đạp leo núi chuyên dụng, phuộc 120mm",
            "8.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp địa hình Trek Marlin",
            "Khung nhôm cao cấp, phanh đĩa thủy lực",
            "12.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp địa hình Specialized",
            "Thiết kế bền bỉ, địa hình phức tạp",
            "11.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp gấp
        productList.add(new Product(
            "Xe đạp gấp Dahon K3",
            "Siêu nhỏ gọn, dễ mang theo, 7kg",
            "5.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp gấp Java Fit 16",
            "Xe gấp cao cấp, gấp trong 10 giây",
            "6.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp gấp Fornix BF-300",
            "Gọn nhẹ, phù hợp đi làm, đi học",
            "4.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp trẻ em
        productList.add(new Product(
            "Xe đạp trẻ em Disney Princess",
            "Cho bé gái 5-8 tuổi, có bánh phụ",
            "1.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp trẻ em Giant XTC Jr",
            "Xe đạp thể thao cho trẻ em, bánh 20 inch",
            "3.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp trẻ em BMX Freestyle",
            "Xe đạp biểu diễn cho trẻ từ 10 tuổi",
            "2.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp touring
        productList.add(new Product(
            "Xe đạp Touring Giant Escape",
            "Xe đạp đi phố và đường dài, 21 tốc độ",
            "9.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp Touring Trek FX 2",
            "Thiết kế đa năng, di chuyển hàng ngày",
            "10.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Fixed Gear
        productList.add(new Product(
            "Xe đạp Fixed Gear Pista",
            "Xe cào cào thời trang, phong cách tối giản",
            "4.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp Single Speed Classic",
            "1 tốc độ, thiết kế cổ điển",
            "3.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productAdapter.notifyDataSetChanged();
    }

    private void setupChatButton() {
        btnFloatingChat.setOnClickListener(v -> {
            // Open User Chat Activity
            Intent intent = new Intent(HomeActivity.this, UserChatActivity.class);
            startActivity(intent);
        });
    }
}
