package com.example.project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.example.project.models.ApiResponse;
import com.example.project.models.User;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailActivity extends AppCompatActivity {

    private CardView btnBack, btnUpdateUser, btnDeleteUser;
    private TextView tvTitle, tvUsername, tvEmail, tvPhoneNumber, tvAddress, tvRole, tvStatus, tvCreatedAt, tvLastLogin;
    private ImageView imgAvatar, imgStatus;
    private ProgressBar progressBar;
    private CardView cardViewUserInfo;

    private String userId;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initData();
        loadUserDetail();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnUpdateUser = findViewById(R.id.btnUpdateUser);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
        tvTitle = findViewById(R.id.tvTitle);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        tvRole = findViewById(R.id.tvRole);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvLastLogin = findViewById(R.id.tvLastLogin);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgStatus = findViewById(R.id.imgStatus);
        progressBar = findViewById(R.id.progressBar);
        cardViewUserInfo = findViewById(R.id.cardViewUserInfo);

        // Policy: Admin cannot delete users, only disable → hide Delete button
        if (btnDeleteUser != null) {
            btnDeleteUser.setVisibility(View.GONE);
        }
    }

    private void initData() {
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);
    }

    private void loadUserDetail() {
        // Check if user has permission
        if (!authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền truy cập chức năng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<User>> call = apiService.getUser(authHeader, userId);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getUser() != null) {
                        displayUserDetail(apiResponse.getUser());
                    } else {
                        showError("Không thể tải thông tin người dùng");
                    }
                } else {
                    showError("Lỗi kết nối: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayUserDetail(User user) {
        // Username
        tvUsername.setText(user.getUsername());

        // Email
        tvEmail.setText(user.getEmail());

        // Phone Number
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            tvPhoneNumber.setText(user.getPhoneNumber());
            tvPhoneNumber.setVisibility(View.VISIBLE);
        } else {
            tvPhoneNumber.setText("");
            tvPhoneNumber.setVisibility(View.GONE);
        }

        // Address
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            tvAddress.setText(user.getAddress());
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            tvAddress.setText("");
            tvAddress.setVisibility(View.GONE);
        }

        // Role
        String role = user.getRole();
        tvRole.setText(getRoleDisplayName(role));

        // Status
        boolean isActive = user.isActive();
        tvStatus.setText(isActive ? "Active" : "Inactive");
        tvStatus.setTextColor(isActive ? 0xFF059669 : 0xFFDC2626);
        
        // Update status icon
        if (imgStatus != null) {
            imgStatus.setImageResource(isActive ? R.drawable.circle_green : R.drawable.circle_red);
        }

        // Last login
        if (user.getLastLogin() != null && !user.getLastLogin().isEmpty()) {
            tvLastLogin.setText(formatDate(user.getLastLogin()));
        } else {
            tvLastLogin.setText("Never logged in");
        }

        // Created date
        if (user.getCreatedAt() != null && !user.getCreatedAt().isEmpty()) {
            tvCreatedAt.setText(formatDate(user.getCreatedAt()));
        } else {
            tvCreatedAt.setText("");
        }

        // Show user info card
        cardViewUserInfo.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnUpdateUser.setOnClickListener(v -> {
            Toast.makeText(this, "Đang mở dialog chỉnh sửa...", Toast.LENGTH_SHORT).show();
            showEditUserDialog();
        });
        
        // Delete disabled by policy
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            cardViewUserInfo.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getRoleDisplayName(String role) {
        if (role == null) return "Customer";
        
        switch (role.toLowerCase()) {
            case "admin":
                return "Admin";
            case "staff":
                return "Staff";
            case "customer":
                return "Customer";
            default:
                return role;
        }
    }

    private String formatDate(String dateString) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    private void showEditUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_user, null);
        builder.setView(dialogView);

        // Get dialog components
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etPhoneNumber = dialogView.findViewById(R.id.etPhoneNumber);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        CardView btnCancel = dialogView.findViewById(R.id.btnCancel);
        CardView btnSave = dialogView.findViewById(R.id.btnSave);

        // Set current values
        etUsername.setText(tvUsername.getText().toString());
        etEmail.setText(tvEmail.getText().toString());
        etPhoneNumber.setText(tvPhoneNumber.getText().toString());
        etAddress.setText(tvAddress.getText().toString());

        // Setup role spinner
        String[] roles = {"customer", "staff", "admin"};
        String[] roleDisplayNames = {"Khách hàng", "Nhân viên", "Quản trị viên"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roleDisplayNames);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Set current role
        String currentRole = tvRole.getText().toString();
        for (int i = 0; i < roleDisplayNames.length; i++) {
            if (roleDisplayNames[i].equals(currentRole)) {
                spinnerRole.setSelection(i);
                break;
            }
        }

        // Setup status spinner
        String[] statuses = {"true", "false"};
        String[] statusDisplayNames = {"Hoạt động", "Tạm khóa"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusDisplayNames);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Set current status
        String currentStatus = tvStatus.getText().toString();
        spinnerStatus.setSelection(currentStatus.equals("Hoạt động") ? 0 : 1);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            // Clear previous errors
            etUsername.setError(null);
            etEmail.setError(null);
            etPhoneNumber.setError(null);
            etAddress.setError(null);

            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String role = roles[spinnerRole.getSelectedItemPosition()];
            boolean isActive = spinnerStatus.getSelectedItemPosition() == 0;

            if (username.isEmpty()) { etUsername.setError("Không được để trống"); return; }
            if (email.isEmpty()) { etEmail.setError("Không được để trống"); return; }

            // Build body here to show field-level errors on failure
            String token = authManager.getAuthHeader();
            if (token == null) { Toast.makeText(this, "Hết phiên đăng nhập", Toast.LENGTH_SHORT).show(); return; }

            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("username", username);
            body.put("email", email);
            if (!phoneNumber.isEmpty()) {
                String normalized = phoneNumber.replaceAll("[^0-9]", "");
                if (normalized.startsWith("84") && normalized.length() >= 11) normalized = "0" + normalized.substring(2);
                body.put("phoneNumber", normalized);
            }
            if (!address.isEmpty()) body.put("address", address);
            body.put("role", role);
            body.put("isActive", isActive);

            showLoading(true);
            apiService.updateUser(token, userId, body).enqueue(new retrofit2.Callback<ApiResponse<User>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<User>> call, retrofit2.Response<ApiResponse<User>> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(UserDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadUserDetail();
                        return;
                    }
                    // Parse body errors even when HTTP 200 but success=false
                    if (response.isSuccessful() && response.body() != null && !response.body().isSuccess()) {
                        try {
                            java.lang.reflect.Field f = response.body().getClass().getDeclaredField("errors");
                            f.setAccessible(true);
                            Object errs = f.get(response.body());
                            if (errs instanceof java.util.List) {
                                for (Object eo : (java.util.List) errs) {
                                    try {
                                        java.lang.reflect.Field fp = eo.getClass().getDeclaredField("param");
                                        fp.setAccessible(true);
                                        String param = String.valueOf(fp.get(eo));
                                        java.lang.reflect.Field fm = eo.getClass().getDeclaredField("msg");
                                        fm.setAccessible(true);
                                        String msg = String.valueOf(fm.get(eo));
                                        if ("email".equals(param)) etEmail.setError(msg);
                                        else if ("phoneNumber".equals(param)) etPhoneNumber.setError(msg);
                                        else if ("username".equals(param)) etUsername.setError(msg);
                                        else if ("address".equals(param)) etAddress.setError(msg);
                                    } catch (Exception ignored2) {}
                                }
                                return;
                            }
                        } catch (Exception ignored1) {}
                    }
                    // Parse error and map to fields (HTTP 4xx)
                    try {
                        String eb = response.errorBody() != null ? response.errorBody().string() : null;
                        if (eb != null) {
                            org.json.JSONObject obj = new org.json.JSONObject(eb);
                            if (obj.has("errors") && obj.get("errors") instanceof org.json.JSONArray) {
                                org.json.JSONArray arr = obj.getJSONArray("errors");
                                for (int i = 0; i < arr.length(); i++) {
                                    org.json.JSONObject e = arr.getJSONObject(i);
                                    String param = e.has("param") ? e.optString("param") : (e.has("path") ? e.optString("path") : e.optString("field"));
                                    String msg = e.has("msg") ? e.optString("msg") : e.optString("message", "Không hợp lệ");
                                    if ("email".equals(param)) etEmail.setError(msg);
                                    else if ("phoneNumber".equals(param)) etPhoneNumber.setError(msg);
                                    else if ("username".equals(param)) etUsername.setError(msg);
                                    else if ("address".equals(param)) etAddress.setError(msg);
                                }
                                return;
                            }
                            // Mongoose ValidationError shape: { errors: { field: { message: '...', path: 'field', value: '...' } } }
                            if (obj.has("errors") && obj.get("errors") instanceof org.json.JSONObject) {
                                org.json.JSONObject errs = obj.getJSONObject("errors");
                                java.util.Iterator<String> keys = errs.keys();
                                boolean mapped = false;
                                while (keys.hasNext()) {
                                    String field = keys.next();
                                    org.json.JSONObject e = errs.optJSONObject(field);
                                    if (e != null) {
                                        String msg = e.optString("message", "Không hợp lệ");
                                        if ("email".equals(field)) { etEmail.setError(msg); mapped = true; }
                                        else if ("phoneNumber".equals(field)) { etPhoneNumber.setError(msg); mapped = true; }
                                        else if ("username".equals(field)) { etUsername.setError(msg); mapped = true; }
                                        else if ("address".equals(field)) { etAddress.setError(msg); mapped = true; }
                                    }
                                }
                                if (mapped) return;
                            }
                            String message = obj.optString("message");
                            if (message != null && message.contains("duplicate key") ) {
                                if (message.contains("email")) etEmail.setError("Email đã tồn tại");
                                if (message.contains("username")) etUsername.setError("Tên đăng nhập đã tồn tại");
                                return;
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(UserDetailActivity.this, "Cập nhật thất bại ("+response.code()+")", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(retrofit2.Call<ApiResponse<User>> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(UserDetailActivity.this, "Lỗi mạng: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateUser(String username, String email, String phoneNumber, String address, String role, boolean isActive) {
        showLoading(true);

        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        if (username != null && !username.trim().isEmpty()) body.put("username", username.trim());
        if (email != null && !email.trim().isEmpty()) body.put("email", email.trim());
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            String normalized = phoneNumber.replaceAll("[^0-9]", "");
            // Convert +84xxxxxxxxx -> 0xxxxxxxxx when applicable
            if (normalized.startsWith("84") && normalized.length() >= 11) {
                normalized = "0" + normalized.substring(2);
            }
            body.put("phoneNumber", normalized);
        }
        if (address != null && !address.trim().isEmpty()) body.put("address", address.trim());
        if (role != null && !role.trim().isEmpty()) body.put("role", role.trim());
        body.put("isActive", isActive);

        Call<ApiResponse<User>> call = apiService.updateUser(authHeader, userId, body);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(UserDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        loadUserDetail(); // Refresh data
                    } else {
                        Toast.makeText(UserDetailActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String detail = String.valueOf(response.code());
                    try {
                        if (response.errorBody() != null) {
                            String eb = response.errorBody().string();
                            detail = detail + " - " + eb;
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(UserDetailActivity.this, "Lỗi cập nhật: " + detail, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(UserDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUser() {
        showLoading(true);

        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Void>> call = apiService.deleteUser(authHeader, userId);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(UserDetailActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to user list
                    } else {
                        Toast.makeText(UserDetailActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserDetailActivity.this, "Lỗi kết nối: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(UserDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
