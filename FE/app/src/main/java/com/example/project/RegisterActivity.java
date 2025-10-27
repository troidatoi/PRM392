package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.models.ApiResponse;
import com.example.project.models.RegisterRequest;
import com.example.project.models.User;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private TextInputEditText etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private AuthManager authManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        apiService = RetrofitClient.getInstance().getApiService();

        // Initialize views
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        // Register button click
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                performRegister();
            }
        });

        // Login link click
        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Reset errors
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return false;
        }

        if (!android.util.Patterns.PHONE.matcher(phone).matches() || phone.length() < 10) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            etPhone.requestFocus();
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegister() {
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Show loading
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest(
            email.split("@")[0], // Use email prefix as username
            email,
            password,
            phone,
            "", // address - empty for now
            "User", // firstName - default
            "" // lastName - empty
        );

        // Make API call
        apiService.register(registerRequest).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Save auth data
                        authManager.saveAuthData(apiResponse.getToken(), apiResponse.getUser());
                        
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to home
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

