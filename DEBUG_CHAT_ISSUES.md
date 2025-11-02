# 🐛 Debug Guide: Chat Issues

## ✅ Đã sửa các vấn đề chính:

### 1. **Lỗi token - Yêu cầu đăng nhập lại** ✅
**Nguyên nhân**: 
- App lấy token từ `SharedPreferences("UserPrefs")` 
- Nhưng AuthManager lưu token trong `SharedPreferences("auth_prefs")`

**Đã sửa**:
- `UserChatActivity.java` - Sử dụng `AuthManager` thay vì `SharedPreferences`
- `AdminChatActivity.java` - Sử dụng `AuthManager`  
- `AdminChatListActivity.java` - Sử dụng `AuthManager`

**Kết quả**: Không còn yêu cầu đăng nhập lại!

### 2. **Tin nhắn lưu vào DB nhưng không hiện trong chat** ✅
**Nguyên nhân**: 
- Token sai → API `getChatMessages` fail → không load được tin nhắn

**Đã sửa**:
- Dùng `authManager.getAuthHeader()` để lấy token đúng
- Token format: `"Bearer eyJhbGciOiJ..."`

### 3. **Không load tin nhắn cũ khi vào chat** ✅
**Nguyên nhân**: Token sai → API fail

**Đã sửa**: Giống mục 2

---

## 🧪 Testing Steps:

### Bước 1: Build lại app
```
Build > Clean Project
Build > Rebuild Project
```

### Bước 2: Test flow đầy đủ

#### A. Đăng nhập
1. Mở app
2. Đăng nhập với tài khoản
3. ✅ Kiểm tra log: `AuthManager` lưu token

#### B. Vào màn hình chat (User)
1. Từ `HomeActivity`, click vào chat
2. Vào `UserChatActivity`
3. ✅ Kiểm tra log:
   ```
   UserChatActivity: Loading messages for userId: 68ffe8bd...
   UserChatActivity: Loaded X messages
   SocketManager: Socket connected successfully
   SocketManager: Joined room: admin_68ffe8bd...
   ```

#### C. Gửi tin nhắn
1. Nhập tin nhắn và gửi
2. ✅ Kiểm tra log:
   ```
   SocketManager: Message sent to room: admin_68ffe8bd...
   SocketManager: Message received
   UserChatActivity: Tin nhắn mới được thêm
   ```
3. ✅ Tin nhắn xuất hiện trong chat

#### D. Load tin nhắn cũ
1. Thoát chat và vào lại
2. ✅ Kiểm tra: Tin nhắn cũ được load từ API
3. ✅ Kiểm tra database: Tin nhắn có trong MongoDB

---

## 🔍 Debug Checklist:

### Nếu vẫn bị yêu cầu đăng nhập lại:

**Kiểm tra 1**: Token có trong AuthManager không?
```java
// Thêm log trong UserChatActivity onCreate():
Log.d(TAG, "Token: " + (token != null ? "Found" : "Not found"));
Log.d(TAG, "UserId: " + userId);
```

**Kiểm tra 2**: AuthManager có lưu token không?
```java
// Sau khi đăng nhập, check:
AuthManager am = AuthManager.getInstance(this);
Log.d("Login", "Token saved: " + am.getToken());
```

### Nếu tin nhắn không hiện sau khi gửi:

**Kiểm tra 1**: Socket có kết nối không?
```
SocketManager: Socket connected successfully ✅
hoặc
SocketManager: Socket connection error ❌
```

**Kiểm tra 2**: Event `message:received` có được nhận không?
```
SocketManager: Message received ✅
UserChatActivity: handleMessageReceived called ✅
```

**Kiểm tra 3**: Backend có emit event không?
```bash
# Check server log:
Message sent to room admin_68ffe8bd...
```

### Nếu không load tin nhắn cũ:

**Kiểm tra 1**: API call có thành công không?
```
okhttp.OkHttpClient: --> GET http://10.0.2.2:5001/api/chat/messages/68ffe8bd...
okhttp.OkHttpClient: <-- 200 OK
```

**Kiểm tra 2**: Response có data không?
```
UserChatActivity: Loaded X messages
```
Nếu X = 0 → Không có tin nhắn trong DB
Nếu không có log này → API call failed

**Kiểm tra 3**: MongoDB có tin nhắn không?
```bash
# Connect to MongoDB và check:
db.chatmessages.find({ user: ObjectId("68ffe8bd...") })
```

---

## 🔧 Các file đã sửa:

### Frontend:
1. **UserChatActivity.java**
   - Dùng `AuthManager` để lấy token/userId
   - Check login status trước khi vào activity
   - Fix API call với token đúng

2. **AdminChatActivity.java**
   - Dùng `AuthManager` để lấy token
   - Check login status
   - Fix API call

3. **AdminChatListActivity.java**
   - Dùng `AuthManager` để lấy token
   - Check login status

### Backend:
- Không cần sửa (đã chạy đúng)

---

## 📊 Expected Flow:

### Khi gửi tin nhắn:
```
1. User nhập tin nhắn → Click send
2. App: socketManager.sendMessage(roomId, message)
3. Server: Nhận message → Lưu vào MongoDB
4. Server: io.to(roomId).emit('message:received', data)
5. App: Nhận event 'message:received'
6. App: handleMessageReceived() → Thêm vào messages list
7. App: messageAdapter.notifyItemInserted()
8. UI: Tin nhắn hiển thị ✅
```

### Khi load tin nhắn:
```
1. App: onCreate() → loadMessagesFromAPI()
2. App: apiService.getChatMessages(token, userId)
3. Server: GET /api/chat/messages/:userId
4. Server: MongoDB query → Trả về array messages
5. App: Nhận response → Parse messages
6. App: messages.add(msg) for each message
7. App: messageAdapter.notifyDataSetChanged()
8. UI: Tin nhắn hiển thị ✅
```

---

## 🎯 Common Issues:

| Vấn đề | Nguyên nhân | Giải pháp |
|--------|-------------|-----------|
| Yêu cầu đăng nhập lại | Token không tìm thấy | ✅ Đã sửa - Dùng AuthManager |
| Tin nhắn không hiện sau send | Socket không kết nối | Check log SocketManager |
| Không load tin nhắn cũ | API call failed (token sai) | ✅ Đã sửa - Dùng AuthManager |
| Tin nhắn trùng lặp | Event được nhận 2 lần | Check listener registration |
| Crash khi parse date | Date format sai | ✅ Đã sửa - Parse ISO & timestamp |

---

## ✅ What's Fixed:

1. ✅ **Token management** - Dùng AuthManager thống nhất
2. ✅ **API authentication** - Token đúng format `"Bearer ..."`
3. ✅ **Login check** - Check `authManager.isLoggedIn()` trước khi vào chat
4. ✅ **Date parsing** - Parse cả ISO string và timestamp

---

## 🚀 Next Steps:

1. **Build lại app Android**
2. **Test đăng nhập** - Không còn yêu cầu đăng nhập lại
3. **Test gửi tin nhắn** - Tin nhắn xuất hiện ngay lập tức
4. **Test load tin nhắn** - Tin nhắn cũ được load khi vào chat

Nếu vẫn có vấn đề, check log theo checklist phía trên! 📝
