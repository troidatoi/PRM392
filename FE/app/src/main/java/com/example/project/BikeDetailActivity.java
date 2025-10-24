package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BikeDetailActivity extends AppCompatActivity {

    // Views
    private CardView btnBack;
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
    }

    private void initData() {
        imageUrls = new ArrayList<>();
        apiService = RetrofitClient.getInstance().getApiService();
    }

    private void setupRecyclerView() {
        imageAdapter = new BikeImageAdapter(imageUrls, this::onImageClick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvImageGallery.setLayoutManager(layoutManager);
        rvImageGallery.setAdapter(imageAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
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
}
