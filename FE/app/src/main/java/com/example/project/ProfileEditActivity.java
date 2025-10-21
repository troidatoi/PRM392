package com.example.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Calendar;
import java.util.Locale;

public class ProfileEditActivity extends AppCompatActivity {

    private CardView btnBack, btnSaveProfile, btnChangePhoto;
    private ImageView ivProfilePicture;
    private EditText etFullName, etEmail, etPhone, etBirthday, etAddress, etCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etBirthday = findViewById(R.id.etBirthday);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
    }

    private void loadUserData() {
        // TODO: Load user data from database or shared preferences
        // For now, we'll set some sample data
        etFullName.setText("Nguyễn Văn A");
        etEmail.setText("nguyenvana@example.com");
        etPhone.setText("0123456789");
        etBirthday.setText("01/01/1990");
        etAddress.setText("123 Đường ABC");
        etCity.setText("Hồ Chí Minh");
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Change photo button
        btnChangePhoto.setOnClickListener(v -> {
            // TODO: Implement photo picker
            Toast.makeText(this, "Chức năng chọn ảnh đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        // Birthday picker
        etBirthday.setOnClickListener(v -> showDatePicker());

        // Save profile button
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etBirthday.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveProfile() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
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

        // TODO: Save to database or shared preferences
        Toast.makeText(this, "Đã lưu thông tin hồ sơ thành công!", Toast.LENGTH_SHORT).show();

        // Return to previous screen
        finish();
    }
}
