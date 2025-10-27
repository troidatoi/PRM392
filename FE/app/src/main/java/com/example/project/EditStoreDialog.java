package com.example.project;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.project.models.ApiResponse;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditStoreDialog extends Dialog {

    private Context context;
    private Store currentStore;
    private OnStoreUpdatedListener listener;
    private ApiService apiService;
    private AuthManager authManager;

    // Views
    private TextInputEditText etStoreName, etAddress, etCity, etLatitude, etLongitude;
    private TextInputEditText etPhone, etEmail, etDescription;
    private Switch switchIsActive;
    private Button btnCancel, btnUpdate;

    public interface OnStoreUpdatedListener {
        void onStoreUpdated(Store store);
    }

    public EditStoreDialog(@NonNull Context context, Store store, OnStoreUpdatedListener listener) {
        super(context);
        this.context = context;
        this.currentStore = store;
        this.listener = listener;
        
        // Initialize API and Auth
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_store); // Reuse add store layout
        setCancelable(true);

        initViews();
        populateFields();
        setupListeners();
    }

    private void initViews() {
        etStoreName = findViewById(R.id.etStoreName);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etDescription = findViewById(R.id.etDescription);
        switchIsActive = findViewById(R.id.switchIsActive);
        btnCancel = findViewById(R.id.btnCancel);
        btnUpdate = findViewById(R.id.btnCreate); // Reuse btnCreate button
        btnUpdate.setText("Cập Nhật");
    }

    private void populateFields() {
        if (currentStore != null) {
            etStoreName.setText(currentStore.getName());
            etAddress.setText(currentStore.getAddress());
            etCity.setText(currentStore.getCity());
            etLatitude.setText(String.valueOf(currentStore.getLatitude()));
            etLongitude.setText(String.valueOf(currentStore.getLongitude()));
            
            if (currentStore.getPhone() != null) {
                etPhone.setText(currentStore.getPhone());
            }
            
            if (currentStore.getEmail() != null) {
                etEmail.setText(currentStore.getEmail());
            }
            
            if (currentStore.getDescription() != null) {
                etDescription.setText(currentStore.getDescription());
            }
            
            switchIsActive.setChecked(currentStore.isActive());
        }
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnUpdate.setOnClickListener(v -> {
            if (validateInput()) {
                updateStore();
            }
        });
    }

    private boolean validateInput() {
        // Check required fields
        if (TextUtils.isEmpty(etStoreName.getText())) {
            Toast.makeText(context, "Vui lòng nhập tên cửa hàng", Toast.LENGTH_SHORT).show();
            etStoreName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etAddress.getText())) {
            Toast.makeText(context, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            etAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(etCity.getText())) {
            Toast.makeText(context, "Vui lòng nhập thành phố", Toast.LENGTH_SHORT).show();
            etCity.requestFocus();
            return false;
        }

        // Check latitude
        String latStr = etLatitude.getText().toString();
        if (!TextUtils.isEmpty(latStr)) {
            try {
                double lat = Double.parseDouble(latStr);
                if (lat < -90 || lat > 90) {
                    Toast.makeText(context, "Vĩ độ phải từ -90 đến 90", Toast.LENGTH_SHORT).show();
                    etLatitude.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Vĩ độ không hợp lệ", Toast.LENGTH_SHORT).show();
                etLatitude.requestFocus();
                return false;
            }
        } else {
            Toast.makeText(context, "Vui lòng nhập vĩ độ", Toast.LENGTH_SHORT).show();
            etLatitude.requestFocus();
            return false;
        }

        // Check longitude
        String lngStr = etLongitude.getText().toString();
        if (!TextUtils.isEmpty(lngStr)) {
            try {
                double lng = Double.parseDouble(lngStr);
                if (lng < -180 || lng > 180) {
                    Toast.makeText(context, "Kinh độ phải từ -180 đến 180", Toast.LENGTH_SHORT).show();
                    etLongitude.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Kinh độ không hợp lệ", Toast.LENGTH_SHORT).show();
                etLongitude.requestFocus();
                return false;
            }
        } else {
            Toast.makeText(context, "Vui lòng nhập kinh độ", Toast.LENGTH_SHORT).show();
            etLongitude.requestFocus();
            return false;
        }

        // Check email format if provided
        String email = etEmail.getText().toString();
        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        // Check phone format if provided
        String phone = etPhone.getText().toString();
        if (!TextUtils.isEmpty(phone) && (phone.length() < 10 || phone.length() > 11)) {
            Toast.makeText(context, "Số điện thoại không hợp lệ (10-11 số)", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void updateStore() {
        // Update Store object
        currentStore.setName(etStoreName.getText().toString());
        currentStore.setAddress(etAddress.getText().toString());
        currentStore.setCity(etCity.getText().toString());
        currentStore.setLatitude(Double.parseDouble(etLatitude.getText().toString()));
        currentStore.setLongitude(Double.parseDouble(etLongitude.getText().toString()));
        currentStore.setIsActive(switchIsActive.isChecked());
        
        // Optional fields - set only if provided
        if (!TextUtils.isEmpty(etPhone.getText())) {
            currentStore.setPhone(etPhone.getText().toString());
        } else {
            currentStore.setPhone(null);
        }
        
        if (!TextUtils.isEmpty(etEmail.getText())) {
            currentStore.setEmail(etEmail.getText().toString());
        } else {
            currentStore.setEmail(null);
        }
        
        if (!TextUtils.isEmpty(etDescription.getText())) {
            currentStore.setDescription(etDescription.getText().toString());
        } else {
            currentStore.setDescription(null);
        }

        // Get auth header
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            Toast.makeText(context, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Call API
        btnUpdate.setEnabled(false);
        Call<ApiResponse<Store>> call = apiService.updateStore(authHeader, currentStore.getId(), currentStore);
        
        call.enqueue(new Callback<ApiResponse<Store>>() {
            @Override
            public void onResponse(Call<ApiResponse<Store>> call, Response<ApiResponse<Store>> response) {
                btnUpdate.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Store updatedStore = response.body().getData();
                    
                    if (updatedStore != null) {
                        Toast.makeText(context, "Cập nhật cửa hàng thành công!", Toast.LENGTH_SHORT).show();
                        
                        if (listener != null) {
                            listener.onStoreUpdated(updatedStore);
                        }
                        
                        dismiss();
                    } else {
                        Toast.makeText(context, "Cập nhật cửa hàng thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Lỗi không xác định";
                    if (response.body() != null) {
                        errorMsg = response.body().getMessage() != null ? response.body().getMessage() : "Bad Request";
                    } else {
                        errorMsg = response.message();
                    }
                    Toast.makeText(context, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Store>> call, Throwable t) {
                btnUpdate.setEnabled(true);
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
