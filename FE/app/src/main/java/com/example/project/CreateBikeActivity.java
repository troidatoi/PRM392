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

import com.example.project.adapters.SelectedImageAdapter;
import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBikeActivity extends AppCompatActivity implements SelectedImageAdapter.OnImageClickListener {

    private static final int PICK_IMAGES_REQUEST = 1001;

    // Views
    private CardView btnBack;
    private EditText etName, etBrand, etModel, etPrice, etDescription, etStock;
    private EditText etBattery, etMotor, etRange, etMaxSpeed, etWeight, etChargingTime;
    private EditText etFeatures, etWarranty, etOriginalPrice, etTags;
    private Spinner spinnerCategory, spinnerStatus;
    private Button btnSelectImages, btnCreateBike;
    private TextView tvImageCount;
    private RecyclerView rvSelectedImages;
    private ProgressBar progressBar;

    // Data
    private List<Uri> selectedImages;
    private SelectedImageAdapter imageAdapter;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bike);

        initViews();
        initData();
        setupSpinners();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etStock = findViewById(R.id.etStock);
        etBattery = findViewById(R.id.etBattery);
        etMotor = findViewById(R.id.etMotor);
        etRange = findViewById(R.id.etRange);
        etMaxSpeed = findViewById(R.id.etMaxSpeed);
        etWeight = findViewById(R.id.etWeight);
        etChargingTime = findViewById(R.id.etChargingTime);
        etFeatures = findViewById(R.id.etFeatures);
        etWarranty = findViewById(R.id.etWarranty);
        etOriginalPrice = findViewById(R.id.etOriginalPrice);
        etTags = findViewById(R.id.etTags);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnCreateBike = findViewById(R.id.btnCreateBike);
        tvImageCount = findViewById(R.id.tvImageCount);
        rvSelectedImages = findViewById(R.id.rvSelectedImages);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initData() {
        selectedImages = new ArrayList<>();
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

        btnCreateBike.setOnClickListener(v -> createBike());
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
            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    if (selectedImages.size() < 5) {
                        selectedImages.add(imageUri);
                    }
                }
            } else if (data.getData() != null) {
                // Single image selected
                Uri imageUri = data.getData();
                if (selectedImages.size() < 5) {
                    selectedImages.add(imageUri);
                }
            }
            
            updateImageDisplay();
        }
    }

    private void updateImageDisplay() {
        tvImageCount.setText("Đã chọn: " + selectedImages.size() + "/5 ảnh");
        imageAdapter.notifyDataSetChanged();
        
        if (selectedImages.size() >= 5) {
            btnSelectImages.setEnabled(false);
            btnSelectImages.setText("Đã chọn tối đa 5 ảnh");
        } else {
            btnSelectImages.setEnabled(true);
            btnSelectImages.setText("Chọn ảnh (" + selectedImages.size() + "/5)");
        }
    }

    @Override
    public void onImageClick(int position) {
        // Show image in full screen (optional)
        Toast.makeText(this, "Xem ảnh " + (position + 1), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveImage(int position) {
        selectedImages.remove(position);
        updateImageDisplay();
    }

    private void createBike() {
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
        if (selectedImages.isEmpty()) {
            showToast("Vui lòng chọn ít nhất 1 ảnh");
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
        btnCreateBike.setEnabled(false);

        // Prepare form data
        String name = etName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString());
        String description = etDescription.getText().toString().trim();
        int stock = Integer.parseInt(etStock.getText().toString());
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
        
        // Original price
        double originalPrice = 0;
        String originalPriceText = etOriginalPrice.getText().toString().trim();
        if (!originalPriceText.isEmpty()) {
            try {
                originalPrice = Double.parseDouble(originalPriceText);
            } catch (NumberFormatException e) {
                showToast("Giá gốc không hợp lệ");
                progressBar.setVisibility(View.GONE);
                btnCreateBike.setEnabled(true);
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

        // Convert images to MultipartBody.Part
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (int i = 0; i < selectedImages.size(); i++) {
            Uri uri = selectedImages.get(i);
            try {
                // Get file name
                String fileName = "image_" + (i + 1) + ".jpg";
                
                // Get content type
                String contentType = getContentResolver().getType(uri);
                if (contentType == null) {
                    contentType = "image/jpeg";
                }
                
                // Create RequestBody from InputStream
                java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    throw new Exception("Không thể đọc file ảnh");
                }
                
                // Convert InputStream to byte array
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                inputStream.close();
                
                RequestBody requestFile = RequestBody.create(MediaType.parse(contentType), bytes);
                imageParts.add(MultipartBody.Part.createFormData("images", fileName, requestFile));
                
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Lỗi xử lý ảnh " + (i + 1) + ": " + e.getMessage());
                progressBar.setVisibility(View.GONE);
                btnCreateBike.setEnabled(true);
                return;
            }
        }

        // Create RequestBody for text fields
        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody brandPart = RequestBody.create(MediaType.parse("text/plain"), brand);
        RequestBody modelPart = RequestBody.create(MediaType.parse("text/plain"), model);
        RequestBody pricePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(price));
        RequestBody descriptionPart = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody stockPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(stock));
        RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), category);
        RequestBody statusPart = RequestBody.create(MediaType.parse("text/plain"), status);
        
        // Specifications
        RequestBody batteryPart = RequestBody.create(MediaType.parse("text/plain"), battery);
        RequestBody motorPart = RequestBody.create(MediaType.parse("text/plain"), motor);
        RequestBody rangePart = RequestBody.create(MediaType.parse("text/plain"), range);
        RequestBody maxSpeedPart = RequestBody.create(MediaType.parse("text/plain"), maxSpeed);
        RequestBody weightPart = RequestBody.create(MediaType.parse("text/plain"), weight);
        RequestBody chargingTimePart = RequestBody.create(MediaType.parse("text/plain"), chargingTime);
        
        // Features (join with newline)
        String featuresString = String.join("\n", features);
        RequestBody featuresPart = RequestBody.create(MediaType.parse("text/plain"), featuresString);
        
        // Warranty
        RequestBody warrantyPart = RequestBody.create(MediaType.parse("text/plain"), warranty);
        
        // Original price
        RequestBody originalPricePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(originalPrice));
        
        // Tags (join with comma)
        String tagsString = String.join(",", tags);
        RequestBody tagsPart = RequestBody.create(MediaType.parse("text/plain"), tagsString);

        // Make API call
        String token = "Bearer " + authManager.getToken();
        Call<ApiResponse<Bike>> call = apiService.createBike(
            token, namePart, brandPart, modelPart, pricePart, 
            descriptionPart, stockPart, categoryPart, statusPart,
            batteryPart, motorPart, rangePart, maxSpeedPart, weightPart, chargingTimePart,
            featuresPart, warrantyPart, originalPricePart, tagsPart, imageParts
        );

        call.enqueue(new Callback<ApiResponse<Bike>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike>> call, Response<ApiResponse<Bike>> response) {
                progressBar.setVisibility(View.GONE);
                btnCreateBike.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToast("Tạo xe thành công!");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "Lỗi tạo xe: " + (response.body() != null ? response.body().getMessage() : response.message());
                    showToast(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreateBike.setEnabled(true);
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private File getFileFromUri(Uri uri) throws Exception {
        // Get the file path from URI
        String filePath = null;
        
        if (uri.getScheme().equals("content")) {
            // For content URIs, we need to get the actual file path
            String[] projection = { android.provider.MediaStore.Images.Media.DATA };
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                filePath = cursor.getString(column_index);
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            filePath = uri.getPath();
        }
        
        if (filePath == null) {
            throw new Exception("Không thể lấy đường dẫn file từ URI");
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("File không tồn tại: " + filePath);
        }
        
        return file;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
