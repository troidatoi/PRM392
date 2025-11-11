package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

    private TextInputLayout tilUsername, tilEmail, tilPhone, tilAddress, tilFirstName, tilLastName, tilPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etPhone, etAddress, etFirstName, etLastName, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private AuthManager authManager;
    private ApiService apiService;
    
    // Handler for debouncing validation
    private Handler validationHandler = new Handler(Looper.getMainLooper());
    private Runnable usernameValidationRunnable;
    private Runnable emailValidationRunnable;
    private Runnable phoneValidationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        apiService = RetrofitClient.getInstance().getApiService();

        // Initialize views
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
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

        // Setup real-time validation
        setupRealTimeValidation();
    }
    
    private void setupRealTimeValidation() {
        // Username validation with debounce
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove previous validation call
                if (usernameValidationRunnable != null) {
                    validationHandler.removeCallbacks(usernameValidationRunnable);
                }
                
                // Clear error when user starts typing
                String username = s.toString().trim();
                if (username.isEmpty()) {
                    tilUsername.setError(null);
                    return;
                }
                
                // Validate username length first
                if (username.length() < 3) {
                    tilUsername.setError("Tên đăng nhập phải có ít nhất 3 ký tự");
                    return;
                }
                
                // Debounce: wait 800ms after user stops typing
                usernameValidationRunnable = () -> checkUsernameDuplicate(username);
                validationHandler.postDelayed(usernameValidationRunnable, 800);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Email validation with debounce
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove previous validation call
                if (emailValidationRunnable != null) {
                    validationHandler.removeCallbacks(emailValidationRunnable);
                }
                
                // Clear error when user starts typing
                String email = s.toString().trim();
                if (email.isEmpty()) {
                    tilEmail.setError(null);
                    return;
                }
                
                // Validate email format first
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.setError("Email không hợp lệ");
                    return;
                }
                
                // Debounce: wait 800ms after user stops typing
                emailValidationRunnable = () -> checkEmailDuplicate(email);
                validationHandler.postDelayed(emailValidationRunnable, 800);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Phone validation with debounce
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove previous validation call
                if (phoneValidationRunnable != null) {
                    validationHandler.removeCallbacks(phoneValidationRunnable);
                }
                
                // Clear error when user starts typing
                String phone = s.toString().trim();
                if (phone.isEmpty()) {
                    tilPhone.setError(null);
                    return;
                }
                
                // Validate phone format first
                if (phone.length() < 10 || !android.util.Patterns.PHONE.matcher(phone).matches()) {
                    tilPhone.setError("Số điện thoại không hợp lệ");
                    return;
                }
                
                // Debounce: wait 800ms after user stops typing
                phoneValidationRunnable = () -> checkPhoneDuplicate(phone);
                validationHandler.postDelayed(phoneValidationRunnable, 800);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void checkUsernameDuplicate(String username) {
        if (TextUtils.isEmpty(username) || username.length() < 3) {
            return;
        }
        
        apiService.checkDuplicate(username, null, null).enqueue(new Callback<ApiResponse<ApiService.CheckDuplicateResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.CheckDuplicateResponse>> call, 
                                 Response<ApiResponse<ApiService.CheckDuplicateResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiService.CheckDuplicateResponse data = response.body().getData();
                    if (data != null && data.isUsernameExists()) {
                        tilUsername.setError("Tên đăng nhập đã được sử dụng");
                    } else {
                        tilUsername.setError(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiService.CheckDuplicateResponse>> call, Throwable t) {
                // Silently fail - don't show error for network issues during typing
            }
        });
    }
    
    private void checkEmailDuplicate(String email) {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return;
        }
        
        apiService.checkDuplicate(null, email, null).enqueue(new Callback<ApiResponse<ApiService.CheckDuplicateResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.CheckDuplicateResponse>> call, 
                                 Response<ApiResponse<ApiService.CheckDuplicateResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiService.CheckDuplicateResponse data = response.body().getData();
                    if (data != null && data.isEmailExists()) {
                        tilEmail.setError("Email đã được đăng ký");
                    } else {
                        tilEmail.setError(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiService.CheckDuplicateResponse>> call, Throwable t) {
                // Silently fail - don't show error for network issues during typing
            }
        });
    }
    
    private void checkPhoneDuplicate(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            return;
        }
        
        apiService.checkDuplicate(null, null, phone).enqueue(new Callback<ApiResponse<ApiService.CheckDuplicateResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.CheckDuplicateResponse>> call, 
                                 Response<ApiResponse<ApiService.CheckDuplicateResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiService.CheckDuplicateResponse data = response.body().getData();
                    if (data != null && data.isPhoneExists()) {
                        tilPhone.setError("Số điện thoại đã được đăng ký");
                    } else {
                        tilPhone.setError(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiService.CheckDuplicateResponse>> call, Throwable t) {
                // Silently fail - don't show error for network issues during typing
            }
        });
    }

    private boolean validateInputs() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Reset errors
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            tilUsername.setError("Tên đăng nhập phải có ít nhất 3 ký tự");
            etUsername.requestFocus();
            return false;
        }

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

        // Validate phone (optional but validate format if provided)
        if (!TextUtils.isEmpty(phone) && (phone.length() < 10 || !android.util.Patterns.PHONE.matcher(phone).matches())) {
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
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Show loading
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest(
            username,
            email,
            password,
            phone.isEmpty() ? null : phone,
            address.isEmpty() ? null : address,
            firstName.isEmpty() ? null : firstName,
            lastName.isEmpty() ? null : lastName
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
                        // Hiển thị message lỗi từ server
                        String errorMsg = apiResponse.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "Đăng ký thất bại. Vui lòng thử lại.";
                        }
                        Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Xử lý lỗi HTTP khác (400, 409, etc.)
                    String errorMsg = "Đăng ký thất bại. Vui lòng thử lại.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            // Parse error message from server
                            if (errorBody.contains("message")) {
                                org.json.JSONObject jsonError = new org.json.JSONObject(errorBody);
                                errorMsg = jsonError.optString("message", errorMsg);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

