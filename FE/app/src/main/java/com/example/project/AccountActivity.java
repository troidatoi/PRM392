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
import com.bumptech.glide.Glide;
import com.example.project.models.User;
import com.example.project.utils.AuthManager;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvPhone, tvAddress;
    private CardView btnEditProfile, btnOrders, btnStores, btnFavorites, btnSettings, btnLogout;
    private ImageView ivAvatar;

    private View navHome, navProducts, navCart, navAccount;
    private ImageView iconHome, iconProducts, iconCart, iconAccount;
    private TextView tvHome, tvProducts, tvCart, tvAccountNav;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        authManager = AuthManager.getInstance(this);

        initViews();
        setupBottomNavigation();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Load or refresh user data every time the activity is shown
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        ivAvatar = findViewById(R.id.ivAvatar);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnOrders = findViewById(R.id.btnOrders);
        btnStores = findViewById(R.id.btnStores);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);

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
        selectNavItem(blurAccount, iconAccount, tvAccountNav);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ShopActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navCart.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, CartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navAccount.setOnClickListener(v -> {
            selectNavItem(blurAccount, iconAccount, tvAccountNav);
            deselectNavItem(blurHome, iconHome, tvHome);
            deselectNavItem(blurProducts, iconProducts, tvProducts);
            deselectNavItem(blurCart, iconCart, tvCart);
        });
    }

    private void selectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#2196F3"));
        text.setTextColor(Color.parseColor("#2196F3"));
    }

    private void deselectNavItem(View blur, ImageView icon, TextView text) {
        blur.setVisibility(View.GONE);
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
            authManager.clearAuthData();
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        User currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            String fullName = "";
            if (currentUser.getProfile() != null) {
                fullName = (currentUser.getProfile().getFirstName() != null ? currentUser.getProfile().getFirstName() : "") + " " + 
                           (currentUser.getProfile().getLastName() != null ? currentUser.getProfile().getLastName() : "");
                Glide.with(this)
                     .load(currentUser.getProfile().getAvatar())
                     .placeholder(R.drawable.ic_person)
                     .error(R.drawable.ic_person)
                     .into(ivAvatar);
            }
            tvUserName.setText(fullName.trim().isEmpty() ? currentUser.getUsername() : fullName.trim());
            tvUserEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "N/A");
            tvPhone.setText(currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().isEmpty() ? currentUser.getPhoneNumber() : "Chưa cập nhật");
            tvAddress.setText(currentUser.getAddress() != null && !currentUser.getAddress().isEmpty() ? currentUser.getAddress() : "Chưa cập nhật");
        } else {
            // Handle case where user is null, maybe redirect to login
            Toast.makeText(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            authManager.clearAuthData();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}