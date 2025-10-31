# Hệ thống Chat Realtime - Quick Start Guide

## ✅ Đã Hoàn Thành

### Backend (Node.js + Socket.IO)
1. ✅ Cài đặt Socket.IO
2. ✅ Tích hợp WebSocket vào Express server
3. ✅ Tạo Socket event handlers (`socket/chatSocket.js`)
4. ✅ Tạo REST API endpoints (`routes/chatRoutes.js`)
5. ✅ Tạo Chat Controller với các chức năng đầy đủ

### Frontend (Android)
1. ✅ Thêm Socket.IO client dependency
2. ✅ Tạo SocketManager để quản lý WebSocket
3. ✅ Cập nhật AdminChatActivity với realtime features
4. ✅ Cập nhật UserChatActivity với realtime features

## 🚀 Cách Sử Dụng

### 1. Start Backend Server

```bash
cd /Users/mycomputer/Documents/StudioProjects/project_main/PRM392/BE
npm start
```

Server sẽ chạy tại `http://localhost:5000`

### 2. Chạy Android App

1. Mở project trong Android Studio
2. Sync Gradle để tải Socket.IO dependency
3. Build và run app trên emulator hoặc device

### 3. Test Chat

**User Flow:**
1. Đăng nhập vào app
2. Mở UserChatActivity
3. Gửi tin nhắn cho admin

**Admin Flow:**
1. Đăng nhập với admin account
2. Mở AdminChatListActivity để xem danh sách users
3. Click vào user để mở AdminChatActivity
4. Chat realtime với user

## 🎯 Tính Năng

### Realtime Features
- ✅ **Instant Messaging** - Tin nhắn được gửi/nhận ngay lập tức
- ✅ **Typing Indicator** - Hiển thị khi người dùng đang nhập
- ✅ **Read Receipts** - Đánh dấu tin nhắn đã đọc
- ✅ **Online Status** - Theo dõi users đang online
- ✅ **Auto Reconnect** - Tự động kết nối lại khi mất kết nối

### Advanced Features (Đã implement)
- ✅ **Message Edit** - Chỉnh sửa tin nhắn
- ✅ **Message Delete** - Xóa tin nhắn
- ✅ **Message Reactions** - Thêm emoji reactions
- ✅ **Room-based Chat** - Chat theo rooms
- ✅ **JWT Authentication** - Xác thực an toàn

## 📁 Files Đã Tạo/Cập Nhật

### Backend
```
BE/
├── socket/chatSocket.js           [NEW] WebSocket handlers
├── controllers/chatController.js  [NEW] Chat API controller
├── routes/chatRoutes.js           [NEW] Chat routes
├── server.js                      [UPDATED] Added Socket.IO
├── package.json                   [UPDATED] Added socket.io
└── docs/
    └── CHAT_REALTIME_GUIDE.md     [NEW] Documentation
```

### Frontend
```
FE/app/
├── build.gradle.kts               [UPDATED] Added socket.io-client
└── src/main/java/com/example/project/
    ├── network/
    │   └── SocketManager.java     [NEW] WebSocket manager
    ├── AdminChatActivity.java     [UPDATED] Added realtime
    └── UserChatActivity.java      [UPDATED] Added realtime
```

## ⚙️ Configuration

### Backend - Server URL
File: `BE/server.js`
```javascript
// CORS configuration
origin: ['http://localhost:3000', 'http://10.0.2.2:5000']
```

### Frontend - Server URL
File: `FE/.../network/SocketManager.java`
```java
// For Android Emulator
String serverUrl = "http://10.0.2.2:5000";

// For Real Device - thay YOUR_IP
String serverUrl = "http://YOUR_IP:5000";
```

## 🔧 Troubleshooting

### Backend Issues

**Lỗi: "Socket connection error"**
```bash
# Kiểm tra port đã được sử dụng chưa
lsof -i :5000

# Restart server
npm start
```

**Lỗi: "Authentication error"**
- Kiểm tra JWT token trong request
- Verify JWT_SECRET trong .env file

### Android Issues

**Lỗi: "Cannot connect to server"**
1. Kiểm tra server đang chạy
2. Verify server URL trong SocketManager.java
3. Check INTERNET permission trong AndroidManifest.xml (đã có)

**Messages không hiển thị:**
1. Check logcat với filter "SocketManager"
2. Verify JSON parsing trong Activities
3. Enable debug logs

## 📝 API Documentation

### Socket Events

**Client → Server:**
- `chat:join` - Join room
- `message:send` - Send message
- `typing:start` - Start typing
- `typing:stop` - Stop typing
- `message:read` - Mark as read

**Server → Client:**
- `message:received` - New message
- `user:typing` - User typing
- `user:stop-typing` - User stopped
- `message:read-status` - Read status update

### REST Endpoints

```
GET    /api/chat/messages          - Get chat history
GET    /api/chat/conversations     - Get conversations list
PUT    /api/chat/read-all          - Mark all as read
GET    /api/chat/online-users      - Get online users
```

## 🎨 UI Updates Needed (Optional)

Nếu muốn thêm typing indicator UI, thêm vào layout:

```xml
<TextView
    android:id="@+id/tvTypingIndicator"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Đang nhập..."
    android:visibility="gone"
    android:textStyle="italic"
    android:textColor="@color/gray" />
```

## 📚 Documentation

Chi tiết đầy đủ xem tại:
- `BE/docs/CHAT_REALTIME_GUIDE.md`

## 🎓 Next Steps

1. **Test toàn bộ flow:**
   - User gửi tin nhắn → Admin nhận realtime
   - Admin reply → User nhận realtime
   - Typing indicators hoạt động
   - Read receipts được cập nhật

2. **Optional enhancements:**
   - Thêm file/image attachments
   - Push notifications khi có tin nhắn mới
   - Message search functionality
   - Chat history pagination

3. **Production deployment:**
   - Setup proper CORS origins
   - Enable SSL/TLS
   - Configure Redis for Socket.IO scaling
   - Setup monitoring and logging

## 💡 Tips

1. **Debug mode:**
   - Backend: Check terminal logs
   - Android: Use Logcat with "SocketManager" filter

2. **Testing:**
   - Sử dụng 2 devices/emulators để test realtime
   - Hoặc test với web client và mobile app

3. **Performance:**
   - Socket connections được reused
   - Messages được lưu vào MongoDB
   - Auto reconnection được enable

---

Chúc bạn thành công! 🎉
