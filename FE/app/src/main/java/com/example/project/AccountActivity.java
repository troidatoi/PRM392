package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.project.utils.BottomNavigationHelper;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvPhone, tvAddress;
    private CardView btnEditProfile, btnOrders, btnStores, btnFavorites, btnSettings, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initViews();
        setupBottomNavigation();
        setupClickListeners();
        loadUserData();
    }

    private void initViews() {
        // User info
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);

        // Buttons
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnOrders = findViewById(R.id.btnOrders);
        btnStores = findViewById(R.id.btnStores);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupBottomNavigation() {
        // Setup bottom navigation with Account tab active (index 3)
        BottomNavigationHelper.setupBottomNavigation(this, 3);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnStores.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, StoreListActivity.class);
            startActivity(intent);
        });

        btnFavorites.setOnClickListener(v -> {
            Toast.makeText(this, "Sản phẩm yêu thích", Toast.LENGTH_SHORT).show();
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            // Navigate to Login Activity
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        // Load user data from preferences or database
        // For now, using sample data
        tvUserName.setText("Nguyễn Văn A");
        tvUserEmail.setText("nguyenvana@gmail.com");
        tvPhone.setText("0123 456 789");
        tvAddress.setText("123 Đường ABC, Quận 1, TP.HCM");
    }
}

