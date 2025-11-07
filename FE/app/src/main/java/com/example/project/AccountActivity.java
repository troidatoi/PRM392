package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.example.project.models.User;
import com.example.project.utils.AuthManager;
import com.example.project.utils.BottomNavigationHelper;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvPhone, tvAddress;
    private CardView btnEditProfile, btnOrders, btnStores, btnFavorites, btnSettings, btnChangePassword, btnLogout;
    private ImageView ivAvatar;
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
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupBottomNavigation() {
        // Setup bottom navigation with Account tab active (index 3)
        BottomNavigationHelper.setupBottomNavigation(this, 3);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
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
        android.util.Log.d("DATA_SYNC", "[FE DEBUG] AccountActivity.onResume: Loading user data. Current user: " + new com.google.gson.Gson().toJson(currentUser));
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
