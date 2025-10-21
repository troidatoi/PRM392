package com.example.project;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AddProductActivity extends AppCompatActivity {

    private CardView btnBack, btnUploadImage, btnCancel, btnSave;
    private ImageView ivProductPreview;
    private EditText etProductName, etProductDescription, etProductPrice, etProductCategory, etProductStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Buttons
        btnBack = findViewById(R.id.btnBack);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // Image Preview
        ivProductPreview = findViewById(R.id.ivProductPreview);

        // Input Fields
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductCategory = findViewById(R.id.etProductCategory);
        etProductStock = findViewById(R.id.etProductStock);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Upload image button
        btnUploadImage.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng upload ảnh đang phát triển", Toast.LENGTH_SHORT).show();
            // TODO: Implement image picker
            // For now, set a placeholder image
            ivProductPreview.setImageResource(R.drawable.splash_bike_background);
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveProduct();
            }
        });
    }

    private boolean validateInputs() {
        String name = etProductName.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String price = etProductPrice.getText().toString().trim();
        String category = etProductCategory.getText().toString().trim();
        String stock = etProductStock.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etProductName.setError("Vui lòng nhập tên sản phẩm");
            etProductName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            etProductDescription.setError("Vui lòng nhập mô tả sản phẩm");
            etProductDescription.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(price)) {
            etProductPrice.setError("Vui lòng nhập giá sản phẩm");
            etProductPrice.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(category)) {
            etProductCategory.setError("Vui lòng nhập danh mục");
            etProductCategory.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(stock)) {
            etProductStock.setError("Vui lòng nhập số lượng tồn kho");
            etProductStock.requestFocus();
            return false;
        }

        try {
            Double.parseDouble(price);
        } catch (NumberFormatException e) {
            etProductPrice.setError("Giá không hợp lệ");
            etProductPrice.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(stock);
        } catch (NumberFormatException e) {
            etProductStock.setError("Số lượng không hợp lệ");
            etProductStock.requestFocus();
            return false;
        }

        return true;
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String price = etProductPrice.getText().toString().trim();
        String category = etProductCategory.getText().toString().trim();
        String stock = etProductStock.getText().toString().trim();

        // TODO: Save product to database
        // For now, just show success message

        Toast.makeText(this, "Đã thêm sản phẩm: " + name, Toast.LENGTH_LONG).show();

        // Clear form
        clearForm();

        // Optionally, finish activity and return to previous screen
        // finish();
    }

    private void clearForm() {
        etProductName.setText("");
        etProductDescription.setText("");
        etProductPrice.setText("");
        etProductCategory.setText("");
        etProductStock.setText("");
        ivProductPreview.setImageResource(0);
    }
}

