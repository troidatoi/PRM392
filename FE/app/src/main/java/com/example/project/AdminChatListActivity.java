package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.example.project.models.ApiResponse;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatListActivity extends AppCompatActivity implements ChatUserAdapter.OnChatUserClickListener {

    private static final String TAG = "AdminChatListActivity";
    
    private RecyclerView rvChatUsers;
    private CardView btnBack;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private ChatUserAdapter chatUserAdapter;
    private List<ChatUser> chatUsers;
    private ApiService apiService;
    private String token;
    
    // Bottom navigation
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconChatManagement;
    private TextView tvChatManagement;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);
        
        // Check if logged in
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get token from AuthManager
        token = authManager.getAuthHeader(); // "Bearer token"
        
        apiService = RetrofitClient.getInstance().getApiService();

        initViews();
        setupRecyclerView();
        loadChatUsers();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        rvChatUsers = findViewById(R.id.rvChatUsers);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        
        // Bottom navigation
        navDashboard = findViewById(R.id.navDashboard);
        navUserManagement = findViewById(R.id.navUserManagement);
        navProductManagement = findViewById(R.id.navProductManagement);
        navStoreManagement = findViewById(R.id.navStoreManagement);
        navOrderManagement = findViewById(R.id.navOrderManagement);
        navChatManagement = findViewById(R.id.navChatManagement);
        iconChatManagement = findViewById(R.id.iconChatManagement);
        tvChatManagement = findViewById(R.id.tvChatManagement);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        chatUsers = new ArrayList<>();
        chatUserAdapter = new ChatUserAdapter(chatUsers, this);

        rvChatUsers.setLayoutManager(new LinearLayoutManager(this));
        rvChatUsers.setAdapter(chatUserAdapter);
        
        Log.d(TAG, "RecyclerView setup complete");
        Log.d(TAG, "RecyclerView: " + (rvChatUsers != null ? "not null" : "null"));
        Log.d(TAG, "Adapter: " + (chatUserAdapter != null ? "not null" : "null"));
        Log.d(TAG, "ChatUsers list: " + (chatUsers != null ? chatUsers.size() : "null"));
    }

    private void loadChatUsers() {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty!");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        
        Log.d(TAG, "=== Loading chat conversations ===");
        Log.d(TAG, "Token: " + token.substring(0, Math.min(20, token.length())) + "...");
        Log.d(TAG, "Token length: " + token.length());
        Log.d(TAG, "Is admin: " + authManager.isAdmin());
        if (authManager.getCurrentUser() != null) {
            Log.d(TAG, "Current user: " + authManager.getCurrentUser().getUsername());
            Log.d(TAG, "User role: " + authManager.getCurrentUser().getRole());
        }
        
        // token already has "Bearer " prefix from AuthManager.getAuthHeader()
        apiService.getChatConversations(token).enqueue(new Callback<ApiResponse<ChatConversation[]>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatConversation[]>> call, Response<ApiResponse<ChatConversation[]>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                Log.d(TAG, "=== API Response ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());
                Log.d(TAG, "Response message: " + response.message());
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Response body is not null");
                    Log.d(TAG, "Response body success: " + response.body().isSuccess());
                    
                    if (response.body().isSuccess()) {
                        ChatConversation[] conversations = response.body().getData();
                        chatUsers.clear();
                        
                        Log.d(TAG, "Conversations count: " + (conversations != null ? conversations.length : 0));
                        
                        if (conversations != null && conversations.length > 0) {
                            Log.d(TAG, "Processing conversations...");
                            
                            // Get current admin ID to filter out self
                            String currentAdminId = null;
                            if (authManager.getCurrentUser() != null) {
                                currentAdminId = authManager.getCurrentUser().getId();
                                Log.d(TAG, "Current admin ID: " + currentAdminId);
                            }
                            
                            for (int i = 0; i < conversations.length; i++) {
                                ChatConversation conv = conversations[i];
                                Log.d(TAG, "Conversation " + i + ":");
                                Log.d(TAG, "  - User: " + (conv.getUser() != null ? conv.getUser().getUsername() : "null"));
                                Log.d(TAG, "  - User ID: " + (conv.getUser() != null ? conv.getUser().getId() : "null"));
                                Log.d(TAG, "  - LastMessage: " + (conv.getLastMessage() != null ? conv.getLastMessage().getMessage() : "null"));
                                Log.d(TAG, "  - Unread: " + conv.getUnreadCount());
                                
                                if (conv.getUser() == null) {
                                    Log.e(TAG, "Conversation has null user, skipping");
                                    continue;
                                }
                                
                                ChatMessage.User user = conv.getUser();
                                
                                // Skip if this conversation is with self (admin chatting with themselves)
                                if (currentAdminId != null && user.getId() != null && user.getId().equals(currentAdminId)) {
                                    Log.d(TAG, "Skipping conversation with self (ID: " + user.getId() + ")");
                                    continue;
                                }
                                
                                ChatUser chatUser = new ChatUser(
                                    user.getId(),
                                    user.getUsername(),
                                    user.getAvatar(),  // ChatMessage.User has avatar field directly
                                    conv.getLastMessage() != null ? conv.getLastMessage().getMessage() : "",
                                    conv.getLastMessage() != null ? conv.getLastMessage().getSentAt() : System.currentTimeMillis(),
                                    conv.getUnreadCount()
                                );
                                Log.d(TAG, "Created ChatUser: " + chatUser.getUserName() + " (ID: " + chatUser.getUserId() + ")");
                                chatUsers.add(chatUser);
                            }
                            
                            Log.d(TAG, "Total chatUsers in list: " + chatUsers.size());
                            Log.d(TAG, "Calling notifyDataSetChanged()");
                            chatUserAdapter.notifyDataSetChanged();
                            
                            Log.d(TAG, "Adapter item count after notify: " + chatUserAdapter.getItemCount());
                            Log.d(TAG, "RecyclerView visibility: " + (rvChatUsers.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE"));
                            Log.d(TAG, "Loaded " + chatUsers.size() + " chat users");
                            
                            if (tvEmptyState != null) {
                                tvEmptyState.setVisibility(View.GONE);
                                Log.d(TAG, "Empty state hidden");
                            }
                        } else {
                            Log.d(TAG, "No conversations found");
                            if (tvEmptyState != null) {
                                tvEmptyState.setText("Chưa có tin nhắn nào");
                                tvEmptyState.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        Log.d(TAG, "Response body success is false");
                        Toast.makeText(AdminChatListActivity.this, "Không thể tải danh sách chat", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Response error";
                    Log.e(TAG, "=== API Error ===");
                    Log.e(TAG, "Failed to load conversations: " + response.code() + " - " + response.message());
                    
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            errorMsg = "Error: " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    
                    Toast.makeText(AdminChatListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    if (tvEmptyState != null) {
                        tvEmptyState.setText("Lỗi: " + response.code());
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChatConversation[]>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "=== API Call Failed ===");
                Log.e(TAG, "Error class: " + t.getClass().getName());
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Error", t);
                
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                Toast.makeText(AdminChatListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                
                if (tvEmptyState != null) {
                    tvEmptyState.setText("Lỗi kết nối");
                    tvEmptyState.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onChatUserClick(ChatUser chatUser) {
        // Open chat with specific user
        Intent intent = new Intent(this, AdminChatActivity.class);
        intent.putExtra("userId", chatUser.getUserId());
        intent.putExtra("userName", chatUser.getUserName());
        startActivity(intent);
    }
    
    private void setupBottomNavigation() {
        // Set Chat Management tab as active
        if (iconChatManagement != null) {
            iconChatManagement.setColorFilter(0xFF2196F3); // active blue
        }
        if (tvChatManagement != null) {
            tvChatManagement.setTextColor(0xFF2196F3); // active blue
        }
        
        // Bottom navigation click listeners
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminChatListActivity.this, AdminManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navUserManagement != null) {
            navUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminChatListActivity.this, UserManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navProductManagement != null) {
            navProductManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminChatListActivity.this, ProductManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navStoreManagement != null) {
            navStoreManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminChatListActivity.this, StoreManagementActivity.class);
                startActivity(intent);
            });
        }
        if (navOrderManagement != null) {
            navOrderManagement.setOnClickListener(v -> {
                Intent intent = new Intent(AdminChatListActivity.this, OrderManagementActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}

