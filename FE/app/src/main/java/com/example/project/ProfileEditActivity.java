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
import com.example.project.models.ChangePasswordRequest;
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
    private Button btnSaveProfile;
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

        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        etBirthday.setOnClickListener(v -> showDatePicker());
        btnSaveProfile.setOnClickListener(v -> saveProfile());

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
        android.util.Log.d("ProfileEdit", "Fetching locations from API...");
        
        apiService.getLocations().enqueue(new Callback<ApiResponse<List<Location>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Location>>> call, Response<ApiResponse<List<Location>>> response) {
                android.util.Log.d("ProfileEdit", "API Response: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allLocations = response.body().getData();
                    android.util.Log.d("ProfileEdit", "Loaded " + allLocations.size() + " locations");
                    
                    if (allLocations == null || allLocations.isEmpty()) {
                        android.util.Log.e("ProfileEdit", "Location data is empty!");
                        handleLocationLoadError("Không có dữ liệu địa chỉ");
                        return;
                    }
                    
                    List<String> cityNames = allLocations.stream().map(Location::getName).collect(Collectors.toList()); 
                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(ProfileEditActivity.this, android.R.layout.simple_dropdown_item_1line, cityNames);
                    actvCity.setAdapter(cityAdapter);
                    
                    android.util.Log.d("ProfileEdit", "City adapter set with " + cityNames.size() + " cities");
                    
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
                    String errorMsg = "API Error - Code: " + response.code();
                    if (response.body() != null) {
                        errorMsg += ", Message: " + response.body().getMessage();
                    }
                    android.util.Log.e("ProfileEdit", errorMsg);
                    handleLocationLoadError("Không thể tải danh sách địa chỉ");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Location>>> call, Throwable t) {
                android.util.Log.e("ProfileEdit", "Network error: " + t.getMessage(), t);
                handleLocationLoadError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
    
    private void handleLocationLoadError(String error) {
        Toast.makeText(this, error + "\nSử dụng chế độ nhập thủ công.", Toast.LENGTH_LONG).show();
        
        // Enable manual input mode
        actvCity.setAdapter(null);
        actvCity.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        actvCity.setFocusable(true);
        actvCity.setClickable(true);
        
        actvDistrict.setAdapter(null);
        actvDistrict.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        actvDistrict.setFocusable(true);
        actvDistrict.setClickable(true);
        actvDistrict.setEnabled(true);
        
        android.util.Log.i("ProfileEdit", "Switched to manual input mode");
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
                    android.util.Log.d("DATA_SYNC", "[FE DEBUG] ProfileEditActivity: Update successful. Server responded with user data: " + new com.google.gson.Gson().toJson(response.body().getUser()));
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
