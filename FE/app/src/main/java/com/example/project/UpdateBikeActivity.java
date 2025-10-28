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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    private EditText etName, etBrand, etModel, etPrice, etDescription, etStock;
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
        etStock = findViewById(R.id.etStock);
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
        etPrice.setText(String.valueOf(originalBike.getPrice()));
        etDescription.setText(originalBike.getDescription());
        etStock.setText(String.valueOf(originalBike.getStock()));

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
            updateImageDisplay();
        }
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivity(Intent.createChooser(intent, "Chọn ảnh"));
    }

    // Bỏ onActivityResult để tránh API deprecated. Khi cần chọn ảnh đa luồng, sẽ dùng Activity Result API.

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
        if (position < selectedImages.size()) {
            selectedImages.remove(position);
        } else {
            // Remove from existing images
            int existingIndex = position - selectedImages.size();
            if (existingIndex < existingImageUrls.size()) {
                existingImageUrls.remove(existingIndex);
            }
        }
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
        if (etStock.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập số lượng tồn kho");
            return;
        }

        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            int stock = Integer.parseInt(etStock.getText().toString());
            
            if (price <= 0) {
                showToast("Giá phải lớn hơn 0");
                return;
            }
            if (stock < 0) {
                showToast("Số lượng tồn kho không được âm");
                return;
            }
        } catch (NumberFormatException e) {
            showToast("Giá và số lượng tồn kho phải là số hợp lệ");
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
        double price = Double.parseDouble(etPrice.getText().toString());
        String description = etDescription.getText().toString().trim();
        int stock = Integer.parseInt(etStock.getText().toString());
        String category = spinnerCategory.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

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
        RequestBody pricePart = RequestBody.create(String.valueOf(price), MediaType.get("text/plain"));
        RequestBody descriptionPart = RequestBody.create(description, MediaType.get("text/plain"));
        RequestBody stockPart = RequestBody.create(String.valueOf(stock), MediaType.get("text/plain"));
        RequestBody categoryPart = RequestBody.create(category, MediaType.get("text/plain"));
        RequestBody statusPart = RequestBody.create(status, MediaType.get("text/plain"));

        // Make API call
        String token = "Bearer " + authManager.getToken();
        Call<ApiResponse<Bike>> call = apiService.updateBike(
            token, bikeId, namePart, brandPart, modelPart, pricePart, 
            descriptionPart, stockPart, categoryPart, statusPart, imageParts
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
                    String errorMessage = "Lỗi cập nhật xe: " + (response.body() != null ? response.body().getMessage() : response.message());
                    showToast(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnUpdateBike.setEnabled(true);
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
