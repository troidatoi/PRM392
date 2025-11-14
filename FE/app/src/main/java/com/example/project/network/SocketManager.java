package com.example.project.network;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.project.ChatMessage;
import com.example.project.utils.AuthManager;
import com.example.project.utils.ChatNotificationHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    
    private Socket socket;
    private Context context;
    private List<SocketListener> listeners = new ArrayList<>();
    private boolean isConnected = false;
    
    // Debug mode: Hiện notification ngay cả khi app foreground (để test)
    private static boolean FORCE_NOTIFICATION_FOR_TESTING = true;
    
    // Socket events
    public static final String EVENT_CONNECT = "connect";
    public static final String EVENT_DISCONNECT = "disconnect";
    public static final String EVENT_CONNECT_ERROR = "connect_error";
    public static final String EVENT_MESSAGE_RECEIVED = "message:received";
    public static final String EVENT_MESSAGE_READ = "message:read-status";
    public static final String EVENT_USER_TYPING = "user:typing";
    public static final String EVENT_USER_STOP_TYPING = "user:stop-typing";
    public static final String EVENT_USER_JOINED = "user:joined";
    public static final String EVENT_USER_LEFT = "user:left";
    public static final String EVENT_USERS_ONLINE = "users:online";
    public static final String EVENT_MESSAGE_EDITED = "message:edited";
    public static final String EVENT_MESSAGE_DELETED = "message:deleted";
    public static final String EVENT_MESSAGE_REACTION = "message:reaction";
    public static final String EVENT_SYSTEM_MESSAGE = "message:system";
    public static final String EVENT_CHAT_LIST_UPDATE = "chat-list:update";
    
    private SocketManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized SocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new SocketManager(context);
        }
        return instance;
    }
    
    public void connect() {
        if (socket != null && socket.connected()) {
            Log.d(TAG, "Socket already connected");
            return;
        }
        
        try {
            // Get token from AuthManager
            AuthManager authManager = AuthManager.getInstance(context);
            String token = authManager.getToken();
            
            if (token == null || token.isEmpty()) {
                Log.e(TAG, "No token found");
                return;
            }
            
            Log.d(TAG, "Connecting with token: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            // Socket server URL - using port 5001 (same as API server)
            String serverUrl = "http://10.0.2.2:5001"; // For Android emulator
            // String serverUrl = "http://YOUR_SERVER_IP:5001"; // For real device
            
            IO.Options options = new IO.Options();
            options.auth = new java.util.HashMap<String, String>() {{
                put("token", token);
            }};
            options.reconnection = true;
            options.reconnectionDelay = 1000;
            options.reconnectionDelayMax = 5000;
            options.reconnectionAttempts = 5;
            options.transports = new String[]{"websocket", "polling"};
            
            socket = IO.socket(serverUrl, options);
            setupSocketListeners();
            socket.connect();
            
            Log.d(TAG, "Connecting to socket server...");
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket connection error", e);
        }
    }
    
    private void setupSocketListeners() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = true;
                Log.d(TAG, "Socket connected");
                notifyListeners(EVENT_CONNECT, null);
            }
        });
        
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = false;
                Log.d(TAG, "Socket disconnected");
                notifyListeners(EVENT_DISCONNECT, null);
            }
        });
        
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket connection error: " + args[0]);
                notifyListeners(EVENT_CONNECT_ERROR, args[0]);
            }
        });
        
        socket.on(EVENT_MESSAGE_RECEIVED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Message received");
                
                // Show notification if app is in background
                if (args.length > 0 && args[0] instanceof JSONObject) {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        showNotificationIfBackground(data);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing notification", e);
                    }
                }
                
                notifyListeners(EVENT_MESSAGE_RECEIVED, args[0]);
            }
        });
        
        socket.on(EVENT_MESSAGE_READ, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_MESSAGE_READ, args[0]);
            }
        });
        
        socket.on(EVENT_USER_TYPING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_USER_TYPING, args[0]);
            }
        });
        
        socket.on(EVENT_USER_STOP_TYPING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_USER_STOP_TYPING, args[0]);
            }
        });
        
        socket.on(EVENT_USER_JOINED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_USER_JOINED, args[0]);
            }
        });
        
        socket.on(EVENT_USER_LEFT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_USER_LEFT, args[0]);
            }
        });
        
        socket.on(EVENT_USERS_ONLINE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_USERS_ONLINE, args[0]);
            }
        });
        
        socket.on(EVENT_MESSAGE_EDITED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_MESSAGE_EDITED, args[0]);
            }
        });
        
        socket.on(EVENT_MESSAGE_DELETED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_MESSAGE_DELETED, args[0]);
            }
        });
        
        socket.on(EVENT_MESSAGE_REACTION, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_MESSAGE_REACTION, args[0]);
            }
        });
        
        socket.on(EVENT_SYSTEM_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                notifyListeners(EVENT_SYSTEM_MESSAGE, args[0]);
            }
        });
        
        socket.on(EVENT_CHAT_LIST_UPDATE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "Chat list update event received");
                notifyListeners(EVENT_CHAT_LIST_UPDATE, args[0]);
            }
        });
    }
    
    public void joinRoom(String roomId) {
        if (!isConnected) {
            Log.e(TAG, "Socket not connected");
            return;
        }
        
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            socket.emit("chat:join", data);
            Log.d(TAG, "Joined room: " + roomId);
        } catch (JSONException e) {
            Log.e(TAG, "Error joining room", e);
        }
    }
    
    public void leaveRoom(String roomId) {
        if (!isConnected) return;
        
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            socket.emit("chat:leave", data);
            Log.d(TAG, "Left room: " + roomId);
        } catch (JSONException e) {
            Log.e(TAG, "Error leaving room", e);
        }
    }
    
    public void sendMessage(String roomId, String message, String messageType) {
        if (!isConnected) {
            Log.e(TAG, "Socket not connected");
            return;
        }
        
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            data.put("message", message);
            data.put("messageType", messageType);
            socket.emit("message:send", data);
            Log.d(TAG, "Message sent to room: " + roomId);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending message", e);
        }
    }
    
    public void markMessageAsRead(String messageId, String roomId) {
        if (!isConnected) return;
        
        try {
            JSONObject data = new JSONObject();
            data.put("messageId", messageId);
            data.put("roomId", roomId);
            socket.emit("message:read", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error marking message as read", e);
        }
    }
    
    public void startTyping(String roomId) {
        if (!isConnected) return;
        
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            socket.emit("typing:start", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending typing event", e);
        }
    }
    
    public void stopTyping(String roomId) {
        if (!isConnected) return;
        
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            socket.emit("typing:stop", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending stop typing event", e);
        }
    }
    
    public void editMessage(String messageId, String newMessage, String roomId) {
        if (!isConnected) return;
        
        try {
            JSONObject data = new JSONObject();
            data.put("messageId", messageId);
            data.put("newMessage", newMessage);
            data.put("roomId", roomId);
            socket.emit("message:edit", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error editing message", e);
        }
    }
    
    public void deleteMessage(String messageId, String roomId) {
        if (!isConnected) return;
        
        try {
            JSONObject data = new JSONObject();
            data.put("messageId", messageId);
            data.put("roomId", roomId);
            socket.emit("message:delete", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error deleting message", e);
        }
    }
    
    public void reactToMessage(String messageId, String emoji, String roomId) {
        if (!isConnected) return;
        
        try {
            JSONObject data = new JSONObject();
            data.put("messageId", messageId);
            data.put("emoji", emoji);
            data.put("roomId", roomId);
            socket.emit("message:react", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error reacting to message", e);
        }
    }
    
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
            socket = null;
            isConnected = false;
            Log.d(TAG, "Socket disconnected manually");
        }
    }
    
    public boolean isConnected() {
        return isConnected && socket != null && socket.connected();
    }
    
    // Listener management
    public void addListener(SocketListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(SocketListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners(String event, Object data) {
        for (SocketListener listener : listeners) {
            listener.onSocketEvent(event, data);
        }
    }
    
    /**
     * Hiển thị notification nếu app đang ở background
     */
    private void showNotificationIfBackground(JSONObject messageData) {
        try {
            Log.d(TAG, "=== NOTIFICATION DEBUG START ===");
            Log.d(TAG, "Received message data: " + messageData.toString());
            
            // Kiểm tra xem app có đang ở foreground không
            boolean isInForeground = isAppInForeground();
            Log.d(TAG, "App in foreground: " + isInForeground);
            Log.d(TAG, "Force notification mode: " + FORCE_NOTIFICATION_FOR_TESTING);
            
            if (isInForeground && !FORCE_NOTIFICATION_FOR_TESTING) {
                Log.d(TAG, "App is in foreground and force mode OFF, skipping notification");
                return;
            }
            
            if (FORCE_NOTIFICATION_FOR_TESTING) {
                Log.d(TAG, "⚠️ FORCE MODE ENABLED - Showing notification even in foreground");
            }
            
            // Parse message data
            String senderId = messageData.getJSONObject("user").getString("_id");
            String senderName = messageData.getJSONObject("user").getString("username");
            String message = messageData.getString("message");
            
            Log.d(TAG, "Parsed - senderId: " + senderId + ", senderName: " + senderName);
            
            // Lấy user ID hiện tại từ AuthManager
            AuthManager authManager = AuthManager.getInstance(context);
            com.example.project.models.User currentUser = authManager.getCurrentUser();
            String currentUserId = (currentUser != null) ? currentUser.getId() : null;
            
            Log.d(TAG, "Current user ID: " + currentUserId);
            Log.d(TAG, "Sender ID: " + senderId);
            
            // Chỉ hiện notification nếu không phải tin nhắn của mình
            if (currentUserId != null && !currentUserId.equals(senderId)) {
                // Kiểm tra xem người gửi có phải admin không
                String userRole = messageData.getJSONObject("user").optString("role", "customer");
                boolean isFromAdmin = "admin".equals(userRole) || "staff".equals(userRole);
                boolean isAdminChat = !isFromAdmin; // Nếu nhận từ admin thì đây là user chat
                
                Log.d(TAG, "User role: " + userRole + ", isFromAdmin: " + isFromAdmin);
                Log.d(TAG, "🔔 Showing notification - From: " + senderName + ", Message: " + message);
                
                // Hiển thị notification
                ChatNotificationHelper.showChatNotification(
                    context,
                    senderId,
                    senderName,
                    message,
                    isAdminChat
                );
                
                Log.d(TAG, "✅ Notification shown successfully");
            } else {
                Log.d(TAG, "❌ Skipping notification - Message from self or no current user");
            }
            
            Log.d(TAG, "=== NOTIFICATION DEBUG END ===");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing notification", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Kiểm tra xem app có đang ở foreground không
     */
    private boolean isAppInForeground() {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                Log.w(TAG, "ActivityManager is null");
                return false;
            }
            
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                Log.w(TAG, "Running app processes is null");
                return false;
            }
            
            final String packageName = context.getPackageName();
            Log.d(TAG, "Checking foreground for package: " + packageName);
            
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                Log.d(TAG, "Process: " + appProcess.processName + ", Importance: " + appProcess.importance);
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
                    && appProcess.processName.equals(packageName)) {
                    Log.d(TAG, "✅ App is in FOREGROUND");
                    return true;
                }
            }
            
            Log.d(TAG, "❌ App is in BACKGROUND");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking foreground status", e);
            return false;
        }
    }

    // Listener interface
    public interface SocketListener {
        void onSocketEvent(String event, Object data);
    }
}