package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.example.project.utils.AdminNavHelper;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManagementActivity extends AppCompatActivity {

    private CardView btnAddNewUser, btnFilter;
    private TextView tvTitle, tvUserCount, tvEmptyState;
    private EditText etSearch;
    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private CardView emptyStateCard;
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconDashboard, iconUserManagement, iconProductManagement, iconStoreManagement, iconOrderManagement, iconChatManagement;
    private TextView tvDashboard, tvUserManagement, tvProductManagement, tvStoreManagement, tvOrderManagement, tvChatManagement;
    private CardView cardDashboard, cardUserManagement, cardProductManagement, cardStoreManagement, cardOrderManagement, cardChatManagement;
    private CardView chipAll, chipCustomers, chipAdmins, chipActive, chipInactive;
    private TextView tabAll, tabCustomers, tabAdmins;

    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredUserList;
    private ApiService apiService;
    private AuthManager authManager;
    private String currentFilter = "all";

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
        btnAddNewUser = findViewById(R.id.btnAddNewUser);
        btnFilter = findViewById(R.id.btnFilter);
        tvTitle = findViewById(R.id.tvTitle);
        tvUserCount = findViewById(R.id.tvUserCount);
        etSearch = findViewById(R.id.etSearch);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        // Bottom nav
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);
        
        iconDashboard = findViewById(R.id.iconDashboard);
        iconUserManagement = findViewById(R.id.iconUserManagement);
        iconProductManagement = findViewById(R.id.iconProductManagement);
        iconStoreManagement = findViewById(R.id.iconStoreManagement);
        iconOrderManagement = findViewById(R.id.iconOrderManagement);
        iconChatManagement = findViewById(R.id.iconChatManagement);
        
        tvDashboard = findViewById(R.id.tvDashboard);
        tvUserManagement = findViewById(R.id.tvUserManagement);
        tvProductManagement = findViewById(R.id.tvProductManagement);
        tvStoreManagement = findViewById(R.id.tvStoreManagement);
        tvOrderManagement = findViewById(R.id.tvOrderManagement);
        tvChatManagement = findViewById(R.id.tvChatManagement);
        
        cardDashboard = findViewById(R.id.cardDashboard);
        cardUserManagement = findViewById(R.id.cardUserManagement);
        cardProductManagement = findViewById(R.id.cardProductManagement);
        cardStoreManagement = findViewById(R.id.cardStoreManagement);
        cardOrderManagement = findViewById(R.id.cardOrderManagement);
        cardChatManagement = findViewById(R.id.cardChatManagement);

        // Filter chips
        chipAll = findViewById(R.id.chipAll);
        chipCustomers = findViewById(R.id.chipCustomers);
        chipAdmins = findViewById(R.id.chipAdmins);
        chipActive = findViewById(R.id.chipActive);
        chipInactive = findViewById(R.id.chipInactive);
        
        tabAll = findViewById(R.id.tabAll);
        tabCustomers = findViewById(R.id.tabCustomers);
        tabAdmins = findViewById(R.id.tabAdmins);

        // Set User Management tab as active
        AdminNavHelper.resetAllTabs(
                cardDashboard, iconDashboard, tvDashboard,
                cardUserManagement, iconUserManagement, tvUserManagement,
                cardProductManagement, iconProductManagement, tvProductManagement,
                cardStoreManagement, iconStoreManagement, tvStoreManagement,
                cardOrderManagement, iconOrderManagement, tvOrderManagement,
                cardChatManagement, iconChatManagement, tvChatManagement
        );
        AdminNavHelper.setActiveTab(cardUserManagement, iconUserManagement, tvUserManagement);
    }

    private void initData() {
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);

        // Setup RecyclerView
        userAdapter = new UserAdapter(filteredUserList, this);
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
                        
                        // Apply current filter
                        applyFilter();
                        
                        // Update user count
                        tvUserCount.setText("Total: " + userList.size() + " users");
                        
                        // Show/hide empty state
                        if (filteredUserList.isEmpty()) {
                            emptyStateCard.setVisibility(View.VISIBLE);
                            recyclerViewUsers.setVisibility(View.GONE);
                        } else {
                            emptyStateCard.setVisibility(View.GONE);
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
        // Add new user button
        if (btnAddNewUser != null) {
            btnAddNewUser.setOnClickListener(v -> {
                Toast.makeText(this, "Add new user feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Search functionality
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterUsers(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Bottom navigation
        if (navDashboard != null) navDashboard.setOnClickListener(v -> startActivity(new Intent(this, AdminManagementActivity.class)));
        if (navProductManagement != null) navProductManagement.setOnClickListener(v -> startActivity(new Intent(this, ProductManagementActivity.class)));
        if (navStoreManagement != null) navStoreManagement.setOnClickListener(v -> startActivity(new Intent(this, StoreManagementActivity.class)));
        if (navOrderManagement != null) navOrderManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderManagementActivity.class));
            finish();
        });
        if (navChatManagement != null) navChatManagement.setOnClickListener(v -> startActivity(new Intent(this, AdminChatListActivity.class)));

        // Filter chips
        setupFilterChips();
    }

    private void setupFilterChips() {
        if (chipAll != null && tabAll != null) {
            chipAll.setOnClickListener(v -> {
                currentFilter = "all";
                setActiveChip(chipAll, tabAll);
                applyFilter();
            });
        }

        if (chipCustomers != null && tabCustomers != null) {
            chipCustomers.setOnClickListener(v -> {
                currentFilter = "customer";
                setActiveChip(chipCustomers, tabCustomers);
                applyFilter();
            });
        }

        if (chipAdmins != null && tabAdmins != null) {
            chipAdmins.setOnClickListener(v -> {
                currentFilter = "admin";
                setActiveChip(chipAdmins, tabAdmins);
                applyFilter();
            });
        }

        if (chipActive != null) {
            chipActive.setOnClickListener(v -> {
                currentFilter = "active";
                setActiveChip(chipActive, null);
                applyFilter();
            });
        }

        if (chipInactive != null) {
            chipInactive.setOnClickListener(v -> {
                currentFilter = "inactive";
                setActiveChip(chipInactive, null);
                applyFilter();
            });
        }
    }

    private void setActiveChip(CardView activeChip, TextView activeText) {
        // Reset all chips
        resetChip(chipAll, tabAll);
        resetChip(chipCustomers, tabCustomers);
        resetChip(chipAdmins, tabAdmins);
        resetChip(chipActive, null);
        resetChip(chipInactive, null);

        // Set active chip
        if (activeChip != null) {
            activeChip.setCardBackgroundColor(0xFF2196F3);
        }
        if (activeText != null) {
            activeText.setTextColor(0xFFFFFFFF);
        }
    }

    private void resetChip(CardView chip, TextView text) {
        if (chip != null) {
            chip.setCardBackgroundColor(0xFFF1F5F9);
        }
        if (text != null) {
            text.setTextColor(0xFF64748B);
        }
    }

    private void applyFilter() {
        filteredUserList.clear();

        for (User user : userList) {
            boolean matches = false;

            switch (currentFilter) {
                case "all":
                    matches = true;
                    break;
                case "customer":
                    matches = user.getRole() != null && user.getRole().equalsIgnoreCase("customer");
                    break;
                case "admin":
                    matches = user.getRole() != null && (user.getRole().equalsIgnoreCase("admin") || user.getRole().equalsIgnoreCase("staff"));
                    break;
                case "active":
                    // Assume user is active if no specific field is available
                    matches = true;
                    break;
                case "inactive":
                    // Cannot determine inactive status without field
                    matches = false;
                    break;
            }

            if (matches) {
                filteredUserList.add(user);
            }
        }

        userAdapter.notifyDataSetChanged();

        // Update UI
        if (filteredUserList.isEmpty()) {
            emptyStateCard.setVisibility(View.VISIBLE);
            recyclerViewUsers.setVisibility(View.GONE);
        } else {
            emptyStateCard.setVisibility(View.GONE);
            recyclerViewUsers.setVisibility(View.VISIBLE);
        }
    }

    private void filterUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            applyFilter();
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        filteredUserList.clear();

        for (User user : userList) {
            boolean matchesSearch = false;

            if (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerQuery)) {
                matchesSearch = true;
            } else if (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)) {
                matchesSearch = true;
            }

            if (matchesSearch) {
                filteredUserList.add(user);
            }
        }

        userAdapter.notifyDataSetChanged();

        // Update UI
        if (filteredUserList.isEmpty()) {
            emptyStateCard.setVisibility(View.VISIBLE);
            recyclerViewUsers.setVisibility(View.GONE);
            if (tvEmptyState != null) {
                tvEmptyState.setText("No users found for \"" + query + "\"");
            }
        } else {
            emptyStateCard.setVisibility(View.GONE);
            recyclerViewUsers.setVisibility(View.VISIBLE);
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
            emptyStateCard.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (tvEmptyState != null) {
            tvEmptyState.setText("Cannot load data\n" + message);
        }
        emptyStateCard.setVisibility(View.VISIBLE);
        recyclerViewUsers.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadUsers();
    }
}
