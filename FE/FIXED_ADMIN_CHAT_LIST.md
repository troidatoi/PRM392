# 🐛 Fixed: Admin không load được danh sách chat

## ❌ Vấn đề:
Admin vào màn hình chat list nhưng không load được danh sách người đã gửi tin nhắn.

## 🔍 Nguyên nhân:
**Token bị duplicate prefix "Bearer"**

Khi gọi API, code thêm `"Bearer "` vào token:
```java
apiService.getChatConversations("Bearer " + token)
```

Nhưng `token` từ `AuthManager.getAuthHeader()` đã có sẵn prefix `"Bearer "`:
```java
// AuthManager.java
public String getAuthHeader() {
    String token = getToken();
    return token != null ? "Bearer " + token : null;  // ← Đã có "Bearer "
}
```

Kết quả: Token thành `"Bearer Bearer eyJhbGci..."` → Backend từ chối → API fail!

## ✅ Giải pháp:

### File đã sửa:

1. **AdminChatListActivity.java**
   ```java
   // Trước:
   apiService.getChatConversations("Bearer " + token)
   
   // Sau:
   apiService.getChatConversations(token)  // token đã có "Bearer " prefix
   ```

2. **AdminChatActivity.java**
   ```java
   // Trước:
   apiService.getChatMessages("Bearer " + token, userId, 100, null)
   
   // Sau:
   apiService.getChatMessages(token, userId, 100, null)  // token đã có "Bearer " prefix
   ```

### Thêm log để debug:
```java
Log.d(TAG, "Loading chat conversations with token");
Log.d(TAG, "Loading messages for userId: " + userId);
```

## 🧪 Testing:

### Bước 1: Build lại app
```
Build > Clean Project
Build > Rebuild Project
```

### Bước 2: Test admin chat list
1. Đăng nhập với tài khoản admin
2. Vào màn hình Chat Management
3. ✅ Danh sách người dùng đã chat sẽ hiển thị
4. Click vào user → Vào chat với user đó
5. ✅ Tin nhắn cũ được load

### Kiểm tra log:
```
AdminChatListActivity: Loading chat conversations with token
okhttp.OkHttpClient: --> GET http://10.0.2.2:5001/api/chat/conversations
okhttp.OkHttpClient: <-- 200 OK
AdminChatListActivity: Loaded X conversations
```

## 📊 API Flow:

### Đúng:
```
1. AuthManager.getAuthHeader() → "Bearer eyJhbGci..."
2. apiService.getChatConversations(token)
3. Backend nhận: "Bearer eyJhbGci..." ✅
4. JWT verify thành công ✅
5. Trả về danh sách conversations ✅
```

### Sai (trước khi sửa):
```
1. AuthManager.getAuthHeader() → "Bearer eyJhbGci..."
2. apiService.getChatConversations("Bearer " + token)
3. Backend nhận: "Bearer Bearer eyJhbGci..." ❌
4. JWT verify fail ❌
5. Return 401 Unauthorized ❌
```

## 🎯 Tóm tắt:

**Root cause**: Duplicate "Bearer " prefix trong Authorization header

**Files sửa**:
- `AdminChatListActivity.java` - Bỏ "Bearer " prefix
- `AdminChatActivity.java` - Bỏ "Bearer " prefix

**Result**: 
- ✅ Admin load được danh sách chat
- ✅ Admin load được tin nhắn với user
- ✅ API authentication hoạt động đúng

---

**Note**: `UserChatActivity.java` đã sử dụng token đúng từ đầu (không bị lỗi này).
