package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private EditText etName, etEmail, etPhone, etAddress;
    private CardView btnBack, btnChangeAvatar, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadCurrentData();
        setupClickListeners();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        btnBack = findViewById(R.id.btnBack);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadCurrentData() {
        // Load current user data from preferences or database
        // For now, using sample data from AccountActivity
        etName.setText("Nguyễn Văn A");
        etEmail.setText("nguyenvana@gmail.com");
        etPhone.setText("0123 456 789");
        etAddress.setText("123 Đường ABC, Quận 1, TP.HCM");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            finish(); // Go back to previous screen
        });

        btnChangeAvatar.setOnClickListener(v -> {
            // TODO: Implement image picker to change avatar
            Toast.makeText(this, "Chức năng thay đổi ảnh đại diện", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            saveProfile();
        });
    }

    private void saveProfile() {
        // Get values from input fields
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập họ và tên");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            etAddress.requestFocus();
            return;
        }

        // Save to database or preferences
        // TODO: Implement actual save logic

        Toast.makeText(this, "Đã lưu thay đổi thành công!", Toast.LENGTH_SHORT).show();

        // Go back to account screen
        finish();
    }
}

