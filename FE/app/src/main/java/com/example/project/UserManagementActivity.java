package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        btnBack.setOnClickListener(v -> finish());
    }

    private void onUserClick(User user) {
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
