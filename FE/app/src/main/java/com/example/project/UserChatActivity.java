package com.example.project;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private CardView btnBack, btnSend;

    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

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
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(messages, true); // true = user view

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom

        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        // TODO: Load real messages from database
        // Sample messages
        messages.add(new ChatMessage("1", "user1", "User", "admin",
                "Xin chào, tôi cần hỗ trợ về sản phẩm",
                System.currentTimeMillis() - 3600000, true));

        messages.add(new ChatMessage("2", "admin", "Admin", "user1",
                "Chào bạn! Tôi có thể giúp gì cho bạn?",
                System.currentTimeMillis() - 3500000, false));

        messages.add(new ChatMessage("3", "user1", "User", "admin",
                "Tôi muốn biết thông tin về xe đạp điện VinFast",
                System.currentTimeMillis() - 3400000, true));

        messages.add(new ChatMessage("4", "admin", "Admin", "user1",
                "Xe đạp điện VinFast Klara S có giá 29.990.000 VNĐ với pin lithium-ion dung lượng cao",
                System.currentTimeMillis() - 3300000, false));

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
                "user1",
                "User",
                "admin",
                messageText,
                System.currentTimeMillis(),
                true
        );

        messages.add(newMessage);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);

        etMessage.setText("");

        // Simulate admin reply after 2 seconds
        simulateAdminReply();
    }

    private void simulateAdminReply() {
        new android.os.Handler().postDelayed(() -> {
            ChatMessage adminReply = new ChatMessage(
                    String.valueOf(System.currentTimeMillis()),
                    "admin",
                    "Admin",
                    "user1",
                    "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ hỗ trợ bạn ngay.",
                    System.currentTimeMillis(),
                    false
            );

            messages.add(adminReply);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            rvMessages.scrollToPosition(messages.size() - 1);
        }, 2000);
    }
}

