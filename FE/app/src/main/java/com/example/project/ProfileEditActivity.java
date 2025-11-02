package com.example.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.models.ApiResponse;
import com.example.project.models.Location;
import com.example.project.models.User;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;
import com.google.android.material.textfield.TextInputEditText;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etBirthday, etEmail, etPhone, etAddress;
    private AutoCompleteTextView actvCity, actvDistrict;
    private Button btnSaveProfile, btnChangePassword;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private ApiService apiService;
    private User currentUser;
    private List<Location> allLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        authManager = AuthManager.getInstance(this);
        apiService = RetrofitClient.getInstance().getApiService();

        initViews();
        setupClickListeners();
        loadInitialData();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthday = findViewById(R.id.etBirthday);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        actvCity = findViewById(R.id.actvCity);
        actvDistrict = findViewById(R.id.actvDistrict);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        etBirthday.setOnClickListener(v -> showDatePicker());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        actvCity.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCityName = (String) parent.getItemAtPosition(position);
            Location selectedLocation = allLocations.stream().filter(l -> l.getName().equals(selectedCityName)).findFirst().orElse(null);
            if (selectedLocation != null) {
                updateDistrictAdapter(selectedLocation.getDistricts());
            }
        });
    }

    private void loadInitialData() {
        currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        populateUserData();
        fetchLocations();
    }

    private void populateUserData() {
        User.Profile profile = currentUser.getProfile();
        if (profile != null) {
            etFirstName.setText(profile.getFirstName());
            etLastName.setText(profile.getLastName());
            etBirthday.setText(formatDate(profile.getDateOfBirth()));
        }
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhoneNumber());
        etAddress.setText(currentUser.getAddress());
        // City and District will be populated after locations are fetched
    }

    private void fetchLocations() {
        apiService.getLocations().enqueue(new Callback<ApiResponse<List<Location>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Location>>> call, Response<ApiResponse<List<Location>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allLocations = response.body().getData();
                    List<String> cityNames = allLocations.stream().map(Location::getName).collect(Collectors.toList());
                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(ProfileEditActivity.this, android.R.layout.simple_dropdown_item_1line, cityNames);
                    actvCity.setAdapter(cityAdapter);
                    
                    // Now set user's saved city and district
                    if (currentUser.getCity() != null && !currentUser.getCity().isEmpty()) {
                        actvCity.setText(currentUser.getCity(), false);
                        Location savedLocation = allLocations.stream().filter(l -> l.getName().equals(currentUser.getCity())).findFirst().orElse(null);
                        if (savedLocation != null) {
                            updateDistrictAdapter(savedLocation.getDistricts());
                            if (currentUser.getDistrict() != null && !currentUser.getDistrict().isEmpty()) {
                                actvDistrict.setText(currentUser.getDistrict(), false);
                            }
                        }
                    }
                } else {
                    Toast.makeText(ProfileEditActivity.this, "Không thể tải danh sách địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Location>>> call, Throwable t) {
                Toast.makeText(ProfileEditActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDistrictAdapter(List<Location.District> districts) {
        List<String> districtNames = districts.stream().map(Location.District::getName).collect(Collectors.toList());
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districtNames);
        actvDistrict.setAdapter(districtAdapter);
        actvDistrict.setEnabled(true);
        actvDistrict.setText("", false); // Clear previous selection
    }

    private void saveProfile() {
        showLoading(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", etFirstName.getText().toString().trim());
        updates.put("lastName", etLastName.getText().toString().trim());
        updates.put("dateOfBirth", etBirthday.getText().toString().trim());
        updates.put("phoneNumber", etPhone.getText().toString().trim());
        updates.put("address", etAddress.getText().toString().trim());
        updates.put("city", actvCity.getText().toString().trim());
        updates.put("district", actvDistrict.getText().toString().trim());

        apiService.updateProfile("Bearer " + authManager.getToken(), updates).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    authManager.updateUser(response.body().getUser());
                    Toast.makeText(ProfileEditActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "Cập nhật thất bại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        final EditText etNewPasswordDialog = dialogView.findViewById(R.id.etNewPassword);
        final EditText etConfirmPasswordDialog = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setTitle("Đổi Mật Khẩu");
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            // This is overridden below to prevent dialog from closing on validation error
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPasswordDialog.getText().toString().trim();
            String confirmPassword = etConfirmPasswordDialog.getText().toString().trim();

            if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Vui lòng điền đầy đủ các trường", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService.ChangePasswordRequest request = new ApiService.ChangePasswordRequest(currentPassword, newPassword);
            String token = "Bearer " + authManager.getToken();

            apiService.changePassword(token, request).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ProfileEditActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Mật khẩu hiện tại không đúng.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    Toast.makeText(ProfileEditActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            String currentBday = etBirthday.getText().toString();
            if (!currentBday.isEmpty()) {
                Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentBday);
                if (date != null) {
                    calendar.setTime(date);
                }
            }
        } catch (ParseException e) {
            // Ignore if parsing fails, use current date
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
                    etBirthday.setText(date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            // Assuming the date from backend is in ISO 8601 format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : "";
        } catch (ParseException e) {
            // If parsing fails, it might already be in the correct format or a different one.
            // Return original string as a fallback.
            return dateString;
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSaveProfile.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSaveProfile.setEnabled(true);
        }
    }
}
