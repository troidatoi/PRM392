# Hướng dẫn khởi động lại server Chat

## Thay đổi mới nhất:
- ✅ Đổi cổng server từ 5000 sang **5002**
- ✅ Sửa lỗi "No token found" trong SocketManager
- ✅ SocketManager bây giờ sử dụng AuthManager để lấy token đúng cách
- ✅ Cập nhật CORS để chấp nhận kết nối từ cổng 5002

## Cách khởi động lại server:

### 1. Dừng server đang chạy (nếu có)
Nhấn `Ctrl + C` trong terminal đang chạy server

### 2. Khởi động lại server
```bash
cd /Users/mycomputer/Documents/StudioProjects/project_main/PRM392/BE
npm start
```

### 3. Kiểm tra server đã chạy đúng cổng
Bạn sẽ thấy thông báo:
```
Server running in development mode on port 5002
WebSocket server is ready
```

## Kiểm tra kết nối từ Android:

### 1. Build lại app Android
- Clean & Rebuild project trong Android Studio
- Hoặc chạy: `./gradlew clean assembleDebug`

### 2. Chạy app và kiểm tra log
Khi vào màn hình chat, bạn sẽ thấy log:
```
SocketManager: Connecting with token: eyJhbGciOiJIUzI1NiIs...
SocketManager: Socket connected successfully
```

### 3. Nếu vẫn lỗi "No token found"
Nghĩa là bạn chưa đăng nhập:
- Đăng xuất app (nếu đã đăng nhập)
- Đăng nhập lại để lấy token mới
- Token sẽ được lưu vào AuthManager

## Debug tips:

### Kiểm tra token trong app:
Thêm log trong UserChatActivity hoặc AdminChatActivity:
```java
AuthManager authManager = AuthManager.getInstance(this);
String token = authManager.getToken();
Log.d("ChatActivity", "Token: " + (token != null ? "Found" : "Not found"));
```

### Kiểm tra server nhận được token:
Log trong `BE/socket/chatSocket.js` sẽ hiển thị:
```
New client attempting to connect with token: eyJhbGciOi...
User authenticated: [username]
```

### Kiểm tra port đang được sử dụng:
```bash
# Trên Mac/Linux
lsof -i :5002

# Nếu cổng 5002 đang được dùng, kill process:
kill -9 [PID]
```

## Troubleshooting:

### Lỗi "Address already in use"
```bash
# Tìm process đang dùng cổng 5002
lsof -i :5002

# Kill process
kill -9 [PID]

# Hoặc đổi sang cổng khác trong .env
PORT=5003
```

### Lỗi "Connection refused"
- Kiểm tra server đang chạy trên đúng cổng 5002
- Kiểm tra firewall không block cổng 5002
- Với máy thật (không phải emulator), đổi IP từ `10.0.2.2` sang IP thật của máy tính

### Lỗi "Token expired" hoặc "Invalid token"
- Đăng xuất và đăng nhập lại để lấy token mới
- Token có thời hạn, cần refresh khi hết hạn
