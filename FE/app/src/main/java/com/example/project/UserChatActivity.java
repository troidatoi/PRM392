package com.example.project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import android.widget.EditText;
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
import com.example.project.network.SocketManager;
import com.example.project.utils.AuthManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserChatActivity extends AppCompatActivity implements SocketManager.SocketListener {

    private static final String TAG = "UserChatActivity";
    
    private RecyclerView rvMessages;
    private EditText etMessage;
    private CardView btnBack, btnSend;
    private TextView tvTypingIndicator;
    private ProgressBar progressBar;

    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messages;
    
    private String userId;
    private String roomId;
    private String token;
    
    private ApiService apiService;
    private SocketManager socketManager;
    private Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable typingTimeout;
    private boolean isTyping = false;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(this);
        
        // Get user ID and token from AuthManager
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        userId = authManager.getCurrentUser().getId();
        token = authManager.getAuthHeader(); // "Bearer token"
        roomId = "admin_" + userId; // Same room format as admin
        
        apiService = RetrofitClient.getInstance().getApiService();

        initViews();
        setupRecyclerView();
        setupSocketConnection();
        loadMessagesFromAPI(); // Load messages from API
        setupClickListeners();
        setupTypingDetection();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);
        // tvTypingIndicator = findViewById(R.id.tvTypingIndicator); // Add this to layout if needed
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
        loadMessagesFromAPI();
    }
    
    private void loadMessagesFromAPI() {
        if (token == null || token.isEmpty() || userId == null || userId.isEmpty()) {
            Log.e(TAG, "Token or userId is missing");
            Toast.makeText(this, "Lỗi: Không có thông tin đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "Loading messages for userId: " + userId);
        
        apiService.getChatMessages(token, userId, null, null).enqueue(new Callback<ApiResponse<ChatMessage[]>>() {
                @Override
                public void onResponse(Call<ApiResponse<ChatMessage[]>> call, Response<ApiResponse<ChatMessage[]>> response) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        ChatMessage[] messagesArray = response.body().getData();
                        
                        if (messagesArray != null && messagesArray.length > 0) {
                            messages.clear();
                            
                            Log.d(TAG, "Current userId: " + userId);

                            for (ChatMessage msg : messagesArray) {
                                // Set isFromUser based on sender
                                // Null-safe comparison
                                String senderId = msg.getSenderId();

                                Log.d(TAG, "Message senderId: " + senderId + ", userId: " + userId);
                                Log.d(TAG, "Message content: " + msg.getMessage());

                                if (senderId != null && userId != null) {
                                    boolean isFromUser = senderId.equals(userId);
                                    msg.setFromUser(isFromUser);
                                    Log.d(TAG, "isFromUser set to: " + isFromUser);
                                } else {
                                    // Default: if we can't determine, treat as not from user
                                    msg.setFromUser(false);
                                    Log.d(TAG, "isFromUser set to false (senderId or userId is null)");
                                }
                                messages.add(msg);
                            }
                            
                            messageAdapter.notifyDataSetChanged();
                            if (messages.size() > 0) {
                                rvMessages.scrollToPosition(messages.size() - 1);
                            }
                            
                            Log.d(TAG, "Loaded " + messages.size() + " messages");
                        }
                    } else {
                        Log.e(TAG, "Failed to load messages: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ChatMessage[]>> call, Throwable t) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading messages", t);
                    Toast.makeText(UserChatActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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

        // Send through socket
        if (socketManager != null && socketManager.isConnected()) {
            socketManager.sendMessage(roomId, messageText, "text");
            etMessage.setText("");
            
            // Stop typing indicator
            if (isTyping) {
                socketManager.stopTyping(roomId);
                isTyping = false;
            }
        } else {
            Toast.makeText(this, "Không thể kết nối đến server", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupSocketConnection() {
        socketManager = SocketManager.getInstance(this);
        socketManager.addListener(this);
        socketManager.connect();
        
        // Wait a bit for connection then join room
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (socketManager.isConnected()) {
                socketManager.joinRoom(roomId);
                Log.d(TAG, "Joined room: " + roomId);
            }
        }, 1000);
    }
    
    private void setupTypingDetection() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !isTyping) {
                    isTyping = true;
                    socketManager.startTyping(roomId);
                }
                
                // Reset timeout
                if (typingTimeout != null) {
                    typingHandler.removeCallbacks(typingTimeout);
                }
                
                typingTimeout = () -> {
                    if (isTyping) {
                        isTyping = false;
                        socketManager.stopTyping(roomId);
                    }
                };
                
                typingHandler.postDelayed(typingTimeout, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    @Override
    public void onSocketEvent(String event, Object data) {
        runOnUiThread(() -> {
            try {
                switch (event) {
                    case SocketManager.EVENT_CONNECT:
                        Log.d(TAG, "Socket connected");
                        socketManager.joinRoom(roomId);
                        break;
                        
                    case SocketManager.EVENT_DISCONNECT:
                        Log.d(TAG, "Socket disconnected");
                        Toast.makeText(this, "Mất kết nối", Toast.LENGTH_SHORT).show();
                        break;
                        
                    case SocketManager.EVENT_MESSAGE_RECEIVED:
                        handleMessageReceived((JSONObject) data);
                        break;
                        
                    case SocketManager.EVENT_USER_TYPING:
                        handleUserTyping((JSONObject) data);
                        break;
                        
                    case SocketManager.EVENT_USER_STOP_TYPING:
                        handleUserStopTyping();
                        break;
                        
                    case SocketManager.EVENT_MESSAGE_READ:
                        handleMessageRead((JSONObject) data);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling socket event", e);
            }
        });
    }
    
    private void handleMessageReceived(JSONObject data) {
        try {
            String messageId = data.getString("_id");
            String message = data.getString("message");
            String messageType = data.optString("messageType", "text");
            
            // Parse sentAt - handle both timestamp and ISO string
            long sentAt;
            if (data.get("sentAt") instanceof Long) {
                sentAt = data.getLong("sentAt");
            } else {
                String sentAtStr = data.getString("sentAt");
                sentAt = parseISODate(sentAtStr);
            }
            
            JSONObject userObj = data.getJSONObject("user");
            String senderId = userObj.getString("_id");
            String senderName = userObj.getString("username");
            
            // Null-safe comparison for isFromUser
            boolean isFromCurrentUser = (senderId != null && userId != null && senderId.equals(userId));

            ChatMessage chatMessage = new ChatMessage(
                messageId,
                senderId,
                senderName,
                "admin",
                message,
                sentAt,
                isFromCurrentUser
            );
            
            messages.add(chatMessage);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            rvMessages.scrollToPosition(messages.size() - 1);
            
            // Mark admin messages as read (if not from current user)
            if (!isFromCurrentUser && senderId != null) {
                socketManager.markMessageAsRead(messageId, roomId);
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing message", e);
        }
    }
    
    private void handleUserTyping(JSONObject data) {
        try {
            String typingUserId = data.getString("userId");
            if (typingUserId != null && userId != null && !typingUserId.equals(userId) && tvTypingIndicator != null) {
                tvTypingIndicator.setText("Admin đang nhập...");
                tvTypingIndicator.setVisibility(TextView.VISIBLE);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing typing event", e);
        }
    }
    
    private void handleUserStopTyping() {
        if (tvTypingIndicator != null) {
            tvTypingIndicator.setVisibility(TextView.GONE);
        }
    }
    
    private void handleMessageRead(JSONObject data) {
        try {
            String messageId = data.getString("messageId");
            // Update message read status in list
            for (ChatMessage msg : messages) {
                String msgId = msg.getMessageId();
                if (msgId != null && msgId.equals(messageId)) {
                    msg.setRead(true);
                    messageAdapter.notifyDataSetChanged();
                    break;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing read status", e);
        }
    }
    
    // Helper method to parse ISO 8601 date string to timestamp
    private long parseISODate(String isoDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(isoDate);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing ISO date: " + isoDate, e);
            return System.currentTimeMillis();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketManager != null) {
            socketManager.leaveRoom(roomId);
            socketManager.removeListener(this);
        }
        if (typingHandler != null && typingTimeout != null) {
            typingHandler.removeCallbacks(typingTimeout);
        }
    }
}

