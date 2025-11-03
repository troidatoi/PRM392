package com.example.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.project.adapters.SelectedImageAdapter;
import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateBikeActivity extends AppCompatActivity implements SelectedImageAdapter.OnImageClickListener {

    private static final int PICK_IMAGES_REQUEST = 1001;

    // Views
    private CardView btnBack;
    private EditText etName, etBrand, etModel, etPrice, etDescription, etColor;
    private EditText etBattery, etMotor, etRange, etMaxSpeed, etWeight, etChargingTime;
    private EditText etFeatures, etWarranty, etOriginalPrice, etTags;
    private Spinner spinnerCategory, spinnerStatus;
    private Button btnSelectImages, btnUpdateBike;
    private TextView tvImageCount;
    private RecyclerView rvSelectedImages;
    private ProgressBar progressBar;

    // Data
    private String bikeId;
    private Bike originalBike;
    private List<Uri> selectedImages;
    private List<String> existingImageUrls;
    private SelectedImageAdapter imageAdapter;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bike);

        // Get bike ID from intent
        bikeId = getIntent().getStringExtra("bike_id");
        if (bikeId == null) {
            Toast.makeText(this, "Không tìm thấy ID xe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initData();
        setupSpinners();
        setupRecyclerView();
        setupClickListeners();
        loadBikeData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etColor = findViewById(R.id.etColor);
        // Specifications fields
        etBattery = findViewById(R.id.etBattery);
        etMotor = findViewById(R.id.etMotor);
        etRange = findViewById(R.id.etRange);
        etMaxSpeed = findViewById(R.id.etMaxSpeed);
        etWeight = findViewById(R.id.etWeight);
        etChargingTime = findViewById(R.id.etChargingTime);
        // Additional fields
        etFeatures = findViewById(R.id.etFeatures);
        etWarranty = findViewById(R.id.etWarranty);
        etOriginalPrice = findViewById(R.id.etOriginalPrice);
        etTags = findViewById(R.id.etTags);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnUpdateBike = findViewById(R.id.btnUpdateBike);
        tvImageCount = findViewById(R.id.tvImageCount);
        rvSelectedImages = findViewById(R.id.rvSelectedImages);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initData() {
        selectedImages = new ArrayList<>();
        existingImageUrls = new ArrayList<>();
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void setupSpinners() {
        // Category spinner
        String[] categories = {"city", "mountain", "folding", "cargo", "sport", "other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Status spinner
        String[] statuses = {"available", "out_of_stock", "discontinued"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupRecyclerView() {
        imageAdapter = new SelectedImageAdapter(selectedImages, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSelectedImages.setLayoutManager(layoutManager);
        rvSelectedImages.setAdapter(imageAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSelectImages.setOnClickListener(v -> selectImages());

        btnUpdateBike.setOnClickListener(v -> updateBike());
    }

    private void loadBikeData() {
        progressBar.setVisibility(View.VISIBLE);

        Call<ApiResponse<Bike>> call = apiService.getBikeById(bikeId);
        call.enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    originalBike = response.body().getData();
                    if (originalBike != null) {
                        populateForm();
                    } else {
                        showToast("Không tìm thấy thông tin xe");
                        finish();
                    }
                } else {
                    showToast("Lỗi tải thông tin xe: " + response.code());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showToast("Lỗi kết nối: " + t.getMessage());
                finish();
            }
        });
    }

    private void populateForm() {
        // Fill form with existing data
        etName.setText(originalBike.getName());
        etBrand.setText(originalBike.getBrand());
        etModel.setText(originalBike.getModel());
        // Format price to avoid scientific notation
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setGroupingUsed(false); // Remove commas for EditText input
        etPrice.setText(formatter.format(originalBike.getPrice()));
        etDescription.setText(originalBike.getDescription());
        
        // Fill color
        if (originalBike.getColor() != null && !originalBike.getColor().isEmpty()) {
            etColor.setText(originalBike.getColor());
        }

        // Fill specifications
        if (originalBike.getSpecifications() != null) {
            Bike.Specifications specs = originalBike.getSpecifications();
            if (specs.getBattery() != null) etBattery.setText(specs.getBattery());
            if (specs.getMotor() != null) etMotor.setText(specs.getMotor());
            if (specs.getRange() != null) etRange.setText(specs.getRange());
            if (specs.getMaxSpeed() != null) etMaxSpeed.setText(specs.getMaxSpeed());
            if (specs.getWeight() != null) etWeight.setText(specs.getWeight());
            if (specs.getChargingTime() != null) etChargingTime.setText(specs.getChargingTime());
        }

        // Fill features (join with newline)
        if (originalBike.getFeatures() != null && !originalBike.getFeatures().isEmpty()) {
            etFeatures.setText(String.join("\n", originalBike.getFeatures()));
        }

        // Fill warranty
        if (originalBike.getWarranty() != null) {
            etWarranty.setText(originalBike.getWarranty());
        } else {
            etWarranty.setText("12 tháng");
        }

        // Fill original price (format to avoid scientific notation)
        if (originalBike.getOriginalPrice() > 0) {
            formatter.setGroupingUsed(false); // Remove commas for EditText input
            etOriginalPrice.setText(formatter.format(originalBike.getOriginalPrice()));
        }

        // Fill tags (join with comma)
        if (originalBike.getTags() != null && !originalBike.getTags().isEmpty()) {
            etTags.setText(String.join(", ", originalBike.getTags()));
        }

        // Set spinner selections
        String[] categories = {"city", "mountain", "folding", "cargo", "sport", "other"};
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(originalBike.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        String[] statuses = {"available", "out_of_stock", "discontinued"};
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(originalBike.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        // Load existing images
        if (originalBike.getImages() != null && !originalBike.getImages().isEmpty()) {
            existingImageUrls.clear();
            for (Bike.BikeImage image : originalBike.getImages()) {
                existingImageUrls.add(image.getUrl());
            }
            // Add existing image URLs to adapter
            imageAdapter.addImageUrls(existingImageUrls);
            updateImageDisplay();
        }
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    // Multiple images selected
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count && selectedImages.size() + existingImageUrls.size() < 5; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(imageUri);
                        imageAdapter.addImageUri(imageUri);
                    }
                } else if (data.getData() != null) {
                    // Single image selected
                    Uri imageUri = data.getData();
                    if (selectedImages.size() + existingImageUrls.size() < 5) {
                        selectedImages.add(imageUri);
                        imageAdapter.addImageUri(imageUri);
                    }
                }
                updateImageDisplay();
            }
        }
    }

    private void updateImageDisplay() {
        int totalImages = existingImageUrls.size() + selectedImages.size();
        tvImageCount.setText("Đã chọn: " + totalImages + "/5 ảnh");
        imageAdapter.notifyDataSetChanged();
        
        if (totalImages >= 5) {
            btnSelectImages.setEnabled(false);
            btnSelectImages.setText("Đã chọn tối đa 5 ảnh");
        } else {
            btnSelectImages.setEnabled(true);
            btnSelectImages.setText("Chọn ảnh (" + totalImages + "/5)");
        }
    }

    @Override
    public void onImageClick(int position) {
        // Show image in full screen (optional)
        Toast.makeText(this, "Xem ảnh " + (position + 1), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveImage(int position) {
        // Check if it's a new selected image or existing image
        // Existing images come first, then new selected images
        if (position < existingImageUrls.size()) {
            // Remove existing image
            existingImageUrls.remove(position);
        } else {
            // Remove new selected image
            int selectedIndex = position - existingImageUrls.size();
            if (selectedIndex >= 0 && selectedIndex < selectedImages.size()) {
                selectedImages.remove(selectedIndex);
            }
        }
        // Remove from adapter
        imageAdapter.removeItem(position);
        updateImageDisplay();
    }

    private void updateBike() {
        // Validation
        if (etName.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập tên xe");
            return;
        }
        if (etBrand.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập thương hiệu");
            return;
        }
        if (etModel.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập model");
            return;
        }
        if (etPrice.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập giá");
            return;
        }
        if (etDescription.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập mô tả");
            return;
        }

        if (etColor.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập màu sắc");
            return;
        }

        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            
            if (price <= 0) {
                showToast("Giá phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            showToast("Giá phải là số hợp lệ");
            return;
        }

        // Check authentication
        if (!authManager.isLoggedIn()) {
            showToast("Vui lòng đăng nhập");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdateBike.setEnabled(false);

        // Prepare form data
        String name = etName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        
        // Parse and format price to avoid scientific notation
        double price;
        try {
            String priceText = etPrice.getText().toString().trim().replaceAll(",", "");
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showToast("Giá không hợp lệ");
            progressBar.setVisibility(View.GONE);
            btnUpdateBike.setEnabled(true);
            return;
        }
        
        String description = etDescription.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

        // Specifications
        String battery = etBattery.getText().toString().trim();
        String motor = etMotor.getText().toString().trim();
        String range = etRange.getText().toString().trim();
        String maxSpeed = etMaxSpeed.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String chargingTime = etChargingTime.getText().toString().trim();

        // Features (split by newline)
        String featuresText = etFeatures.getText().toString().trim();
        List<String> features = new ArrayList<>();
        if (!featuresText.isEmpty()) {
            String[] featureArray = featuresText.split("\n");
            for (String feature : featureArray) {
                if (!feature.trim().isEmpty()) {
                    features.add(feature.trim());
                }
            }
        }

        // Warranty
        String warranty = etWarranty.getText().toString().trim();
        if (warranty.isEmpty()) {
            warranty = "12 tháng";
        }

        // Original price (parse and format to avoid scientific notation)
        double originalPrice = 0;
        String originalPriceText = etOriginalPrice.getText().toString().trim();
        if (!originalPriceText.isEmpty()) {
            try {
                originalPriceText = originalPriceText.replaceAll(",", "");
                originalPrice = Double.parseDouble(originalPriceText);
            } catch (NumberFormatException e) {
                showToast("Giá gốc không hợp lệ");
                progressBar.setVisibility(View.GONE);
                btnUpdateBike.setEnabled(true);
                return;
            }
        }

        // Tags (split by comma)
        String tagsText = etTags.getText().toString().trim();
        List<String> tags = new ArrayList<>();
        if (!tagsText.isEmpty()) {
            String[] tagArray = tagsText.split(",");
            for (String tag : tagArray) {
                if (!tag.trim().isEmpty()) {
                    tags.add(tag.trim());
                }
            }
        }

        // Convert new images to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImages) {
            try {
                // Get file name
                String fileName = "image_" + System.currentTimeMillis() + ".jpg";
                
                // Get content type
                String contentType = getContentResolver().getType(uri);
                if (contentType == null) {
                    contentType = "image/jpeg";
                }
                
                // Create RequestBody from InputStream
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    throw new Exception("Không thể đọc file ảnh");
                }
                
                // Convert InputStream to byte array
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                inputStream.close();
                
                RequestBody requestFile = RequestBody.create(bytes, MediaType.get(contentType));
                imageParts.add(MultipartBody.Part.createFormData("images", fileName, requestFile));
                
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi xử lý ảnh: " + e.getMessage());
                progressBar.setVisibility(View.GONE);
                btnUpdateBike.setEnabled(true);
                return;
            }
        }

        // Create RequestBody for text fields
        RequestBody namePart = RequestBody.create(name, MediaType.get("text/plain"));
        RequestBody brandPart = RequestBody.create(brand, MediaType.get("text/plain"));
        RequestBody modelPart = RequestBody.create(model, MediaType.get("text/plain"));
        
        // Format price as string without scientific notation
        NumberFormat priceFormatter = NumberFormat.getNumberInstance(Locale.US);
        priceFormatter.setGroupingUsed(false);
        priceFormatter.setMaximumFractionDigits(0);
        String priceString = priceFormatter.format(price);
        RequestBody pricePart = RequestBody.create(priceString, MediaType.get("text/plain"));
        
        RequestBody descriptionPart = RequestBody.create(description, MediaType.get("text/plain"));
        RequestBody colorPart = RequestBody.create(color, MediaType.get("text/plain"));
        RequestBody categoryPart = RequestBody.create(category, MediaType.get("text/plain"));
        RequestBody statusPart = RequestBody.create(status, MediaType.get("text/plain"));

        // Specifications
        RequestBody batteryPart = RequestBody.create(battery, MediaType.get("text/plain"));
        RequestBody motorPart = RequestBody.create(motor, MediaType.get("text/plain"));
        RequestBody rangePart = RequestBody.create(range, MediaType.get("text/plain"));
        RequestBody maxSpeedPart = RequestBody.create(maxSpeed, MediaType.get("text/plain"));
        RequestBody weightPart = RequestBody.create(weight, MediaType.get("text/plain"));
        RequestBody chargingTimePart = RequestBody.create(chargingTime, MediaType.get("text/plain"));

        // Features (join with newline)
        String featuresString = String.join("\n", features);
        RequestBody featuresPart = RequestBody.create(featuresString, MediaType.get("text/plain"));

        // Warranty
        RequestBody warrantyPart = RequestBody.create(warranty, MediaType.get("text/plain"));

        // Original price (format without scientific notation)
        String originalPriceString = "0";
        if (originalPrice > 0) {
            NumberFormat originalPriceFormatter = NumberFormat.getNumberInstance(Locale.US);
            originalPriceFormatter.setGroupingUsed(false);
            originalPriceFormatter.setMaximumFractionDigits(0);
            originalPriceString = originalPriceFormatter.format(originalPrice);
        }
        RequestBody originalPricePart = RequestBody.create(originalPriceString, MediaType.get("text/plain"));

        // Tags (join with comma)
        String tagsString = String.join(",", tags);
        RequestBody tagsPart = RequestBody.create(tagsString, MediaType.get("text/plain"));

        // Make API call
        String token = "Bearer " + authManager.getToken();
        Call<ApiResponse<Bike>> call = apiService.updateBike(
            token, bikeId, namePart, brandPart, modelPart, pricePart, 
            descriptionPart, colorPart, categoryPart, statusPart,
            batteryPart, motorPart, rangePart, maxSpeedPart, weightPart, chargingTimePart,
            featuresPart, warrantyPart, originalPricePart, tagsPart, imageParts
        );

        call.enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                progressBar.setVisibility(View.GONE);
                btnUpdateBike.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToast("Cập nhật xe thành công!");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Handle error response - parse JSON error message
                    String errorMessage = parseErrorMessage(response);
                    showToast(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnUpdateBike.setEnabled(true);
                String errorMsg = t.getMessage();
                if (errorMsg != null && (errorMsg.contains("File quá lớn") || errorMsg.contains("5MB"))) {
                    showToast("File quá lớn. Kích thước tối đa 5MB");
                } else {
                    showToast("Lỗi kết nối: " + (errorMsg != null ? errorMsg : "Không thể kết nối đến server"));
                }
            }
        });
    }

    private String parseErrorMessage(retrofit2.Response<ApiResponse<Bike>> response) {
        String defaultMessage = "Lỗi cập nhật xe";
        
        // Try to get message from response body
        if (response.body() != null) {
            try {
                String message = response.body().getMessage();
                if (message != null && !message.isEmpty()) {
                    return message;
                }
            } catch (Exception e) {
                // Continue to try errorBody
            }
        }
        
        // Try to parse from errorBody JSON
        try {
            okhttp3.ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                String errorString = errorBody.string();
                if (errorString != null && !errorString.isEmpty()) {
                    // Check for file size error
                    if (errorString.contains("File quá lớn") || errorString.contains("5MB") || 
                        errorString.contains("Kích thước tối đa")) {
                        return "File quá lớn. Kích thước tối đa 5MB";
                    }
                    
                    // Try to extract message from JSON: {"message":"..."}
                    if (errorString.contains("\"message\"")) {
                        int messageIndex = errorString.indexOf("\"message\":\"");
                        if (messageIndex >= 0) {
                            int start = messageIndex + 10;
                            int end = errorString.indexOf("\"", start);
                            if (end > start) {
                                String message = errorString.substring(start, end);
                                return message;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to default
        }
        
        return defaultMessage + " (Mã lỗi: " + response.code() + ")";
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
