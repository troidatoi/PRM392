package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.models.ApiResponse;
import com.example.project.models.LoginRequest;
import com.example.project.models.User;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;
import com.example.project.utils.NetworkTest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private AuthManager authManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        apiService = RetrofitClient.getInstance().getApiService();
        
        // Test network connection
        NetworkTest.testConnection("http://10.0.2.2:5000/api/health");

        // Initialize views
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Login button click
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                performLogin();
            }
        });

        // Register link click
        findViewById(R.id.tvRegister).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot password click
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs() {
        String usernameOrEmail = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate username or email
        if (TextUtils.isEmpty(usernameOrEmail)) {
            tilEmail.setError("Vui lòng nhập tên đăng nhập hoặc email");
            etEmail.requestFocus();
            return false;
        }

        // Check if it's an email format, if not, treat as username
        boolean isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches();
        if (isEmail) {
            // Valid email format
        } else {
            // Username - check minimum length
            if (usernameOrEmail.length() < 3) {
                tilEmail.setError("Tên đăng nhập phải có ít nhất 3 ký tự");
                etEmail.requestFocus();
                return false;
            }
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

        return true;
    }

    private void performLogin() {
        String usernameOrEmail = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Show loading
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // Create login request
        LoginRequest loginRequest = new LoginRequest(usernameOrEmail, password);

        // Make API call
        apiService.login(loginRequest).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Save auth data
                        authManager.saveAuthData(apiResponse.getToken(), apiResponse.getUser());
                        
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate based on user role
                        Intent intent;
                        if (authManager.isAdmin()) {
                            intent = new Intent(LoginActivity.this, AdminManagementActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, HomeActivity.class);
                        }
                        
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = apiResponse.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "Đăng nhập thất bại. Vui lòng thử lại.";
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    String errorMsg = "Đăng nhập thất bại. Vui lòng thử lại.";
                    if (response.errorBody() != null) {
                        try {
                            // Try to parse error response
                            Gson gson = new Gson();
                            String errorBodyString = response.errorBody().string();
                            ApiResponse<?> errorResponse = gson.fromJson(errorBodyString, ApiResponse.class);
                            if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                                errorMsg = errorResponse.getMessage();
                            } else {
                                errorMsg = "Lỗi: " + response.code() + " - " + response.message();
                            }
                        } catch (IOException e) {
                            // If parsing fails, use default message
                            errorMsg = "Lỗi: " + response.code() + " - " + response.message();
                        }
                    } else {
                        errorMsg = "Lỗi: " + response.code() + " - " + response.message();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                if (t.getMessage() == null || t.getMessage().contains("Failed to connect")) {
                    errorMsg = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng và đảm bảo backend đang chạy.";
                }
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

}
