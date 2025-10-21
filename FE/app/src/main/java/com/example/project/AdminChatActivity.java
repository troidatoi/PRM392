package com.example.project;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private CardView btnBack, btnSend;
    private TextView tvUserNameHeader;

    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messages;

    private String userId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        // Get user info from intent
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        initViews();
        setupRecyclerView();
        loadMessages();
        setupClickListeners();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        tvUserNameHeader = findViewById(R.id.tvUserName);

        if (userName != null) {
            tvUserNameHeader.setText(userName);
        }
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(messages, false); // false = admin view

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        // TODO: Load messages for specific user from database
        // Sample messages
        messages.add(new ChatMessage("1", userId, userName, "admin",
                "Xin chào, tôi cần hỗ trợ về sản phẩm",
                System.currentTimeMillis() - 3600000, true));

        messages.add(new ChatMessage("2", "admin", "Admin", userId,
                "Chào bạn! Tôi có thể giúp gì cho bạn?",
                System.currentTimeMillis() - 3500000, false));

        messages.add(new ChatMessage("3", userId, userName, "admin",
                "Tôi muốn biết thông tin về xe đạp điện VinFast",
                System.currentTimeMillis() - 3400000, true));

        messageAdapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Send message to database
        ChatMessage newMessage = new ChatMessage(
                String.valueOf(System.currentTimeMillis()),
                "admin",
                "Admin",
                userId,
                messageText,
                System.currentTimeMillis(),
                false
        );

        messages.add(newMessage);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);

        etMessage.setText("");

        Toast.makeText(this, "Đã gửi tin nhắn", Toast.LENGTH_SHORT).show();
    }
}

