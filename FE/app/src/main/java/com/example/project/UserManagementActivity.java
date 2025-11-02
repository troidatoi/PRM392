package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.UserAdapter;
import com.example.project.models.ApiResponse;
import com.example.project.models.User;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManagementActivity extends AppCompatActivity {

    private CardView btnBack;
    private TextView tvTitle, tvUserCount;
    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconUserManagement;
    private TextView tvUserManagement;
    private TextView tabAll, tabCustomers, tabAdmins;

    private UserAdapter userAdapter;
    private List<User> userList;
    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        initViews();
        initData();
        loadUsers();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvUserCount = findViewById(R.id.tvUserCount);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Bottom nav (may be null depending on layout version)
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);
        iconUserManagement = findViewById(R.id.iconUserManagement);
        tvUserManagement = findViewById(R.id.tvUserManagement);
        
        // Set User Management tab as active
        if (iconUserManagement != null) {
            iconUserManagement.setColorFilter(0xFF2196F3); // active blue
        }
        if (tvUserManagement != null) {
            tvUserManagement.setTextColor(0xFF2196F3); // active blue
        }

        tabAll = findViewById(R.id.tabAll);
        tabCustomers = findViewById(R.id.tabCustomers);
        tabAdmins = findViewById(R.id.tabAdmins);
    }

    private void initData() {
        userList = new ArrayList<>();
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);

        // Setup RecyclerView
        userAdapter = new UserAdapter(userList, this);
        userAdapter.setOnUserClickListener(this::onUserClick);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void loadUsers() {
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

        Call<ApiResponse<User[]>> call = apiService.getUsers(authHeader);
        call.enqueue(new Callback<ApiResponse<User[]>>() {
            @Override
            public void onResponse(Call<ApiResponse<User[]>> call, Response<ApiResponse<User[]>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User[]> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getUsers() != null) {
                        userList.clear();
                        userList.addAll(java.util.Arrays.asList(apiResponse.getUsers()));
                        userAdapter.notifyDataSetChanged();
                        
                        // Update user count
                        tvUserCount.setText("Tổng: " + apiResponse.getCount() + " người dùng");
                        
                        // Show/hide empty state
                        if (userList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            recyclerViewUsers.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            recyclerViewUsers.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError("Không thể tải danh sách người dùng");
                    }
                } else {
                    showError("Lỗi kết nối: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User[]>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Bottom navigation
        if (navDashboard != null) navDashboard.setOnClickListener(v -> startActivity(new Intent(this, AdminManagementActivity.class)));
        if (navProductManagement != null) navProductManagement.setOnClickListener(v -> startActivity(new Intent(this, ProductManagementActivity.class)));
        if (navStoreManagement != null) navStoreManagement.setOnClickListener(v -> startActivity(new Intent(this, StoreManagementActivity.class)));
        if (navOrderManagement != null) navOrderManagement.setOnClickListener(v -> Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show());
        if (navChatManagement != null) navChatManagement.setOnClickListener(v -> startActivity(new Intent(this, AdminChatListActivity.class)));

        // Tabs (UI highlight only)
        if (tabAll != null && tabCustomers != null && tabAdmins != null) {
            View.OnClickListener reset = x -> {
                tabAll.setTextColor(0xFF6B7280);
                tabCustomers.setTextColor(0xFF6B7280);
                tabAdmins.setTextColor(0xFF6B7280);
            };
            tabAll.setOnClickListener(v -> { reset.onClick(v); tabAll.setTextColor(0xFF2196F3); });
            tabCustomers.setOnClickListener(v -> { reset.onClick(v); tabCustomers.setTextColor(0xFF2196F3); });
            tabAdmins.setOnClickListener(v -> { reset.onClick(v); tabAdmins.setTextColor(0xFF2196F3); });
        }
    }

    private void onUserClick(User user) {
        // Guard: require admin/staff and valid token before navigating
        if (authManager == null) authManager = AuthManager.getInstance(this);
        if (!authManager.isStaff()) {
            Toast.makeText(this, "Bạn không có quyền xem chi tiết người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        String header = authManager.getAuthHeader();
        if (header == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (user == null || user.getId() == null) {
            Toast.makeText(this, "Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("userId", user.getId());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewUsers.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvEmptyState.setText("Không thể tải dữ liệu\n" + message);
        tvEmptyState.setVisibility(View.VISIBLE);
        recyclerViewUsers.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadUsers();
    }
}
