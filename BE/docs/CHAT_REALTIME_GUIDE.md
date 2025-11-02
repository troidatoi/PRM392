# Hệ thống Chat Realtime với WebSocket

## Tổng quan

Hệ thống chat realtime sử dụng Socket.IO cho phép người dùng và admin trao đổi tin nhắn trực tiếp với các tính năng:

- ✅ Gửi/nhận tin nhắn realtime
- ✅ Typing indicator (hiển thị khi đang nhập)
- ✅ Read receipts (đánh dấu đã đọc)
- ✅ Online users tracking
- ✅ Message reactions
- ✅ Edit/Delete messages
- ✅ Room-based chat

## Backend Setup

### 1. Cài đặt Dependencies

```bash
cd BE
npm install socket.io
```

### 2. Cấu trúc Files

```
BE/
├── socket/
│   └── chatSocket.js       # WebSocket event handlers
├── controllers/
│   └── chatController.js   # REST API controllers
├── routes/
│   └── chatRoutes.js       # API routes
└── server.js               # Main server with Socket.IO
```

### 3. Socket Events

#### Client → Server Events:
- `chat:join` - Join a chat room
- `chat:leave` - Leave a chat room
- `message:send` - Send a message
- `message:read` - Mark message as read
- `message:edit` - Edit a message
- `message:delete` - Delete a message
- `message:react` - Add reaction to message
- `typing:start` - Start typing
- `typing:stop` - Stop typing

#### Server → Client Events:
- `message:received` - New message received
- `message:read-status` - Message read status updated
- `message:edited` - Message edited
- `message:deleted` - Message deleted
- `message:reaction` - New reaction added
- `user:typing` - User is typing
- `user:stop-typing` - User stopped typing
- `user:joined` - User joined room
- `user:left` - User left room
- `users:online` - List of online users
- `message:system` - System message

### 4. REST API Endpoints

```
GET    /api/chat/messages          # Lấy lịch sử chat
GET    /api/chat/conversations     # Lấy danh sách conversations
PUT    /api/chat/read-all          # Đánh dấu tất cả đã đọc
DELETE /api/chat/conversation/:id  # Xóa conversation
GET    /api/chat/search            # Tìm kiếm tin nhắn
GET    /api/chat/online-users      # Lấy danh sách users online
POST   /api/chat/system-message    # Gửi tin nhắn hệ thống (Admin)
```

### 5. Authentication

WebSocket sử dụng JWT token để xác thực:

```javascript
// Client gửi token khi connect
socket.handshake.auth.token = "your-jwt-token"
```

## Frontend (Android) Setup

### 1. Thêm Dependencies

Trong `app/build.gradle.kts`:

```kotlin
dependencies {
    // Socket.IO client
    implementation("io.socket:socket.io-client:2.1.0")
}
```

### 2. Cấu trúc Classes

```
app/src/main/java/com/example/project/
├── network/
│   └── SocketManager.java          # Quản lý WebSocket connection
├── AdminChatActivity.java          # Chat UI cho Admin
├── UserChatActivity.java           # Chat UI cho User
├── ChatMessageAdapter.java         # RecyclerView adapter
└── ChatMessage.java                # Message model
```

### 3. SocketManager Usage

#### Khởi tạo và kết nối:

```java
SocketManager socketManager = SocketManager.getInstance(context);
socketManager.addListener(this);
socketManager.connect();
```

#### Join room:

```java
String roomId = "admin_" + userId;
socketManager.joinRoom(roomId);
```

#### Gửi tin nhắn:

```java
socketManager.sendMessage(roomId, messageText, "text");
```

#### Xử lý events:

```java
@Override
public void onSocketEvent(String event, Object data) {
    switch (event) {
        case SocketManager.EVENT_MESSAGE_RECEIVED:
            handleMessageReceived((JSONObject) data);
            break;
        case SocketManager.EVENT_USER_TYPING:
            handleUserTyping((JSONObject) data);
            break;
    }
}
```

### 4. Configuration

Trong `SocketManager.java`, cập nhật server URL:

```java
// For Android Emulator
String serverUrl = "http://10.0.2.2:5000";

// For Real Device (thay YOUR_SERVER_IP)
String serverUrl = "http://YOUR_SERVER_IP:5000";
```

## Testing

### 1. Start Backend Server

```bash
cd BE
npm start
```

Server chạy tại: `http://localhost:5000`

### 2. Test WebSocket Connection

Sử dụng Socket.IO client hoặc Postman:

```javascript
const socket = io('http://localhost:5000', {
  auth: {
    token: 'your-jwt-token'
  }
});

socket.on('connect', () => {
  console.log('Connected!');
});
```

### 3. Test trên Android

1. Build và chạy app
2. Đăng nhập với user account
3. Mở Chat activity
4. Gửi tin nhắn và kiểm tra realtime updates

## Room ID Format

Để đảm bảo admin và user chat đúng với nhau, sử dụng format:

```
roomId = "admin_" + userId
```

- Admin join room: `admin_user123`
- User join room: `admin_user123` (cùng room)

## Troubleshooting

### Backend Issues

1. **Socket không connect được:**
   - Kiểm tra CORS settings trong `server.js`
   - Verify JWT token đúng format
   - Check server logs

2. **Messages không gửi được:**
   - Verify user đã join room
   - Check MongoDB connection
   - Xem console logs

### Android Issues

1. **Cannot connect to server:**
   - Kiểm tra INTERNET permission trong AndroidManifest.xml:
     ```xml
     <uses-permission android:name="android.permission.INTERNET" />
     ```
   - Verify server URL đúng (10.0.2.2 cho emulator)
   - Check token được lưu trong SharedPreferences

2. **Messages không hiển thị:**
   - Verify JSON parsing trong `handleMessageReceived()`
   - Check RecyclerView adapter updates
   - Enable debug logs

## Security Considerations

1. **JWT Authentication:**
   - Token được verify ở mỗi socket connection
   - Expired tokens sẽ bị reject

2. **Message Validation:**
   - Max message length: 1000 characters
   - XSS protection trong frontend

3. **Rate Limiting:**
   - Có thể thêm rate limiting cho message sending
   - Prevent spam attacks

## Future Enhancements

- [ ] File/Image attachments
- [ ] Voice messages
- [ ] Video calls
- [ ] Message encryption
- [ ] Push notifications
- [ ] Message search
- [ ] Chat history pagination
- [ ] Multi-user group chat

## Support

Nếu gặp vấn đề, kiểm tra:
1. Server logs: `npm start` output
2. Android logcat: Filter by "SocketManager" or "ChatActivity"
3. Browser console (nếu test với web client)
