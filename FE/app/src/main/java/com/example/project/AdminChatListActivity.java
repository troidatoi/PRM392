package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminChatListActivity extends AppCompatActivity implements ChatUserAdapter.OnChatUserClickListener {

    private RecyclerView rvChatUsers;
    private CardView btnBack;

    private ChatUserAdapter chatUserAdapter;
    private List<ChatUser> chatUsers;
    
    // Bottom navigation
    private LinearLayout navDashboard, navUserManagement, navProductManagement, navStoreManagement, navOrderManagement, navChatManagement;
    private ImageView iconChatManagement;
    private TextView tvChatManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        initViews();
        setupRecyclerView();
        loadChatUsers();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        rvChatUsers = findViewById(R.id.rvChatUsers);
        btnBack = findViewById(R.id.btnBack);
        
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
        chatUsers = new ArrayList<>();
        chatUserAdapter = new ChatUserAdapter(chatUsers, this);

        rvChatUsers.setLayoutManager(new LinearLayoutManager(this));
        rvChatUsers.setAdapter(chatUserAdapter);
    }

    private void loadChatUsers() {
        // TODO: Load real chat users from database
        // Sample data
        chatUsers.add(new ChatUser(
                "user1",
                "Nguyễn Văn A",
                "",
                "Xin chào, tôi cần hỗ trợ...",
                System.currentTimeMillis() - 600000,
                3
        ));

        chatUsers.add(new ChatUser(
                "user2",
                "Trần Thị B",
                "",
                "Sản phẩm này còn hàng không?",
                System.currentTimeMillis() - 1200000,
                1
        ));

        chatUsers.add(new ChatUser(
                "user3",
                "Lê Văn C",
                "",
                "Cảm ơn bạn!",
                System.currentTimeMillis() - 3600000,
                0
        ));

        chatUsers.add(new ChatUser(
                "user4",
                "Phạm Thị D",
                "",
                "Tôi muốn đặt hàng",
                System.currentTimeMillis() - 7200000,
                5
        ));

        chatUsers.add(new ChatUser(
                "user5",
                "Hoàng Văn E",
                "",
                "Giá sản phẩm là bao nhiêu?",
                System.currentTimeMillis() - 10800000,
                0
        ));

        chatUserAdapter.notifyDataSetChanged();
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
                Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }
    }
}

