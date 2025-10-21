package com.example.project;

import android.content.Intent;
import android.os.Bundle;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        initViews();
        setupRecyclerView();
        loadChatUsers();
        setupClickListeners();
    }

    private void initViews() {
        rvChatUsers = findViewById(R.id.rvChatUsers);
        btnBack = findViewById(R.id.btnBack);
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
}

