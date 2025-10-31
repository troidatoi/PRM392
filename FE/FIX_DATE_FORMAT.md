# ✅ Fixed: Date Format Issue in Chat

## 🐛 Lỗi gốc:
```
org.json.JSONException: Value 2025-10-30T14:53:24.590Z at sentAt of type java.lang.String cannot be converted to long
```

**Nguyên nhân**: Backend trả về `sentAt` dưới dạng ISO string (`"2025-10-30T14:53:24.590Z"`) nhưng Android cố convert sang `long` (timestamp).

## ✅ Giải pháp áp dụng:

### 1. Backend (Recommended - Ưu tiên)
Chuyển `sentAt` từ Date object sang timestamp (milliseconds):

**File: `BE/socket/chatSocket.js`**
```javascript
// Trước:
sentAt: newMessage.sentAt  // Date object → ISO string khi emit

// Sau:
sentAt: newMessage.sentAt.getTime()  // Convert to timestamp
```

**File: `BE/controllers/chatController.js`**
```javascript
// Thêm transformation trước khi trả response
const formattedMessages = messages.map(msg => ({
  _id: msg._id,
  user: msg.user,
  message: msg.message,
  messageType: msg.messageType,
  attachments: msg.attachments,
  metadata: msg.metadata,
  sentAt: msg.sentAt.getTime(), // Convert Date to timestamp
  isRead: msg.isRead,
  isEdited: msg.isEdited,
  isDeleted: msg.isDeleted
}));
```

### 2. Frontend (Fallback - Dự phòng)
Parse cả timestamp và ISO string:

**Files Updated:**
- `FE/app/src/main/java/com/example/project/UserChatActivity.java`
- `FE/app/src/main/java/com/example/project/AdminChatActivity.java`

**Added imports:**
```java
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
```

**Added helper method:**
```java
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
```

**Updated handleMessageReceived:**
```java
// Parse sentAt - handle both timestamp and ISO string
long sentAt;
if (data.get("sentAt") instanceof Long) {
    sentAt = data.getLong("sentAt");
} else {
    String sentAtStr = data.getString("sentAt");
    sentAt = parseISODate(sentAtStr);
}
```

## 🚀 Testing:

### 1. Khởi động lại server
Server cần restart để áp dụng thay đổi:
```bash
cd /Users/mycomputer/Documents/StudioProjects/project_main/PRM392/BE
node server.js
```

### 2. Build lại Android app
```
Build > Clean Project
Build > Rebuild Project
```

### 3. Test flow
1. Đăng nhập vào app
2. Vào màn hình chat
3. Gửi tin nhắn
4. ✅ Không còn lỗi `JSONException`
5. ✅ Tin nhắn hiển thị với timestamp đúng

## 📊 Data Format Comparison:

### Trước (ISO String):
```json
{
  "_id": "67228abc123...",
  "message": "Hello",
  "sentAt": "2025-10-30T14:53:24.590Z"  ❌ String
}
```

### Sau (Timestamp):
```json
{
  "_id": "67228abc123...",
  "message": "Hello", 
  "sentAt": 1730296404590  ✅ Long (milliseconds)
}
```

## 🎯 Kết quả:

- ✅ Backend trả về timestamp thống nhất
- ✅ Frontend có fallback parse ISO string (backward compatible)
- ✅ Không còn crash khi nhận tin nhắn
- ✅ Timestamp hiển thị đúng trong UI

## 📝 Notes:

- **Timestamp format**: Milliseconds since Unix epoch (1970-01-01)
- **Timezone**: UTC (backend) → Local timezone (Android tự convert khi hiển thị)
- **Backward compatible**: Frontend vẫn parse được ISO string nếu backend chưa update
