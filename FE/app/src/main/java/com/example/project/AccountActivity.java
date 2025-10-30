package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvPhone, tvAddress;
    private CardView btnEditProfile, btnOrders, btnFavorites, btnSettings, btnLogout;

    // Bottom Navigation
    private View navHome, navProducts, navCart, navAccount;
    private ImageView iconHome, iconProducts, iconCart, iconAccount;
    private TextView tvHome, tvProducts, tvCart, tvAccountNav;

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
        btnFavorites = findViewById(R.id.btnFavorites);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);

        // Bottom Navigation
        navHome = findViewById(R.id.navHome);
        navProducts = findViewById(R.id.navProducts);
        navCart = findViewById(R.id.navCart);
        navAccount = findViewById(R.id.navAccount);


        iconHome = findViewById(R.id.iconHome);
        iconProducts = findViewById(R.id.iconProducts);
        iconCart = findViewById(R.id.iconCart);
        iconAccount = findViewById(R.id.iconAccount);

        tvHome = findViewById(R.id.tvHome);
        tvProducts = findViewById(R.id.tvProducts);
        tvCart = findViewById(R.id.tvCart);
        tvAccountNav = findViewById(R.id.tvAccount);
    }

    private void setupBottomNavigation() {
        // Set Account as selected by default
        selectNavItem(iconAccount, tvAccountNav);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navProducts.setOnClickListener(v -> {
            iconProducts.setColorFilter(android.graphics.Color.parseColor("#7B6047"));
            tvProducts.setTextColor(android.graphics.Color.parseColor("#CEB797"));
            iconHome.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"));
            iconCart.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"));
            iconAccount.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"));
            tvHome.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
            tvCart.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
            tvAccountNav.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
            Intent intent = new Intent(AccountActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        navCart.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, CartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navAccount.setOnClickListener(v -> {
            selectNavItem(iconAccount, tvAccountNav);
            deselectNavItem(iconHome, tvHome);
            deselectNavItem(iconProducts, tvProducts);
            deselectNavItem(iconCart, tvCart);
        });
    }

    private void selectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#2196F3"));
        text.setTextColor(Color.parseColor("#2196F3"));
    }

    private void deselectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#666666"));
        text.setTextColor(Color.parseColor("#666666"));
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
