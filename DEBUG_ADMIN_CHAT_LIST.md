# 🐛 Debug Admin Chat List - Hướng Dẫn

## ✅ Đã Hoàn Thành

### Backend
- ✅ Server đang chạy trên port 5001
- ✅ API `/api/chat/conversations` hoạt động tốt
- ✅ Database có 4 messages từ user "customer"
- ✅ Đã sửa lỗi bảo mật (loại bỏ passwordHash khỏi response)
- ✅ API trả về đúng format dữ liệu

### Frontend
- ✅ Build thành công
- ✅ Thêm chi tiết logging vào AdminChatListActivity
- ✅ ChatMessage.java đã có flexible user parsing
- ✅ ChatConversation.java model đúng

---

## 🔍 Các Bước Debug

### Bước 1: Kiểm Tra Backend Đang Chạy

```bash
curl http://localhost:5001/api/chat/conversations \
  -H "Authorization: Bearer <token>" \
  -v
```

**Expected:** Response 200 với danh sách conversations

---

### Bước 2: Test với Token Admin Thật

```bash
# Tạo token admin
cd /Users/mycomputer/Documents/StudioProjects/project_main/PRM392/BE
node -e "
require('dotenv').config();
const mongoose = require('mongoose');
const User = require('./models/User');
const jwt = require('jsonwebtoken');

mongoose.connect(process.env.MONGODB_URI)
  .then(async () => {
    const admin = await User.findOne({role: 'admin'});
    if (admin) {
      const token = jwt.sign({id: admin._id}, process.env.JWT_SECRET, {expiresIn: '1h'});
      console.log('Admin token:');
      console.log(token);
    }
    process.exit(0);
  });
"
```

**Copy token và test:**
```bash
curl http://localhost:5001/api/chat/conversations \
  -H "Authorization: Bearer <copy-token-here>" \
  -v
```

---

### Bước 3: Chạy App và Xem Log

#### 3.1. Đăng nhập bằng Admin
```
Email: admin@test.com
Password: (password của bạn)
```

#### 3.2. Vào AdminChatListActivity

#### 3.3. Mở Logcat và filter theo tag:
```
AdminChatListActivity
```

#### 3.4. Xem Log Quan Trọng:

**✅ Nếu thành công, bạn sẽ thấy:**
```
D/AdminChatListActivity: === Loading chat conversations ===
D/AdminChatListActivity: Token: Bearer eyJhbGciOiJ...
D/AdminChatListActivity: Token length: 180+
D/AdminChatListActivity: Is admin: true
D/AdminChatListActivity: Current user: admin
D/AdminChatListActivity: User role: admin
D/AdminChatListActivity: === API Response ===
D/AdminChatListActivity: Response code: 200
D/AdminChatListActivity: Response successful: true
D/AdminChatListActivity: Response body is not null
D/AdminChatListActivity: Response body success: true
D/AdminChatListActivity: Conversations count: 1
D/AdminChatListActivity: Processing conversations...
D/AdminChatListActivity: Conversation 0:
D/AdminChatListActivity:   - User: customer
D/AdminChatListActivity:   - User ID: 68ffe8bd227b4afb76b4de2b
D/AdminChatListActivity:   - LastMessage: Hello
D/AdminChatListActivity:   - Unread: 3
D/AdminChatListActivity: Loaded 1 chat users
```

**❌ Nếu có lỗi, bạn sẽ thấy:**

**Lỗi 401 - Unauthorized:**
```
D/AdminChatListActivity: Response code: 401
E/AdminChatListActivity: Error body: {"success":false,"message":"Token không hợp lệ hoặc đã hết hạn"}
```
→ **Giải pháp:** Token hết hạn hoặc không hợp lệ, đăng nhập lại

**Lỗi 403 - Forbidden:**
```
D/AdminChatListActivity: Response code: 403
E/AdminChatListActivity: Error body: {"success":false,"message":"Không có quyền truy cập"}
```
→ **Giải pháp:** User không phải admin, đăng nhập bằng tài khoản admin

**Lỗi kết nối:**
```
E/AdminChatListActivity: === API Call Failed ===
E/AdminChatListActivity: Error: Failed to connect to /10.0.2.2:5001
```
→ **Giải pháp:** Backend chưa chạy hoặc URL sai

**Response không có data:**
```
D/AdminChatListActivity: Conversations count: 0
```
→ **Giải pháp:** Chưa có messages trong database, cần tạo messages từ user

---

## 🔧 Các Vấn Đề Thường Gặp

### Vấn đề 1: "Vui lòng đăng nhập lại"
**Nguyên nhân:**
- Token null hoặc rỗng
- User chưa đăng nhập

**Giải pháp:**
1. Đăng nhập lại
2. Kiểm tra AuthManager có lưu token không:
   ```
   Log.d("AuthManager", "Token: " + authManager.getToken());
   ```

### Vấn đề 2: Response 401 - Unauthorized
**Nguyên nhân:**
- Token hết hạn
- Token không hợp lệ
- Token sai format

**Giải pháp:**
1. Đăng nhập lại để lấy token mới
2. Kiểm tra token có prefix "Bearer " không
3. Kiểm tra JWT_SECRET ở backend và token match

### Vấn đề 3: Response 403 - Forbidden
**Nguyên nhân:**
- User không phải admin
- Token của customer/user thường

**Giải pháp:**
1. Đăng nhập bằng tài khoản admin:
   - Email: admin@test.com
   - Password: (password bạn set)
2. Kiểm tra role trong log: `Is admin: true`

### Vấn đề 4: Không có conversations (count: 0)
**Nguyên nhân:**
- Database chưa có messages
- Messages đã bị xóa (isDeleted: true)

**Giải pháp:**
1. Tạo messages từ user chat activity
2. Hoặc insert test data:
```javascript
// Trong BE
const ChatMessage = require('./models/ChatMessage');
await ChatMessage.create({
  user: 'customer_user_id',
  message: 'Test message',
  messageType: 'text',
  sentAt: new Date()
});
```

### Vấn đề 5: App crash khi parse response
**Nguyên nhân:**
- lastMessage.user là String nhưng code expect Object
- Model không match với API response

**Giải pháp:**
- ✅ Đã fix với flexible parsing trong ChatMessage.java
- parseUserFromRaw() handles both String and Object

### Vấn đề 6: Empty State hiện "Chưa có tin nhắn nào"
**Nguyên nhân:**
- Conversations array rỗng
- Tất cả conversations có user = null

**Giải pháp:**
1. Kiểm tra log: "Conversations count: 0"
2. Tạo messages từ customer
3. Refresh lại màn hình

---

## 📱 Testing Flow

### Test Case 1: Happy Path (Có messages)
1. ✅ Backend đang chạy
2. ✅ Database có messages từ customer
3. ✅ Đăng nhập bằng admin
4. ✅ Vào AdminChatListActivity
5. ✅ Thấy danh sách users đã chat
6. ✅ Click vào user → mở chat

### Test Case 2: No Messages
1. ✅ Backend đang chạy
2. ❌ Database không có messages
3. ✅ Đăng nhập bằng admin
4. ✅ Vào AdminChatListActivity
5. ✅ Thấy "Chưa có tin nhắn nào"

### Test Case 3: Not Admin
1. ✅ Backend đang chạy
2. ✅ Database có messages
3. ❌ Đăng nhập bằng customer
4. ✅ Vào AdminChatListActivity
5. ❌ Response 403 - "Không có quyền truy cập"

### Test Case 4: Token Expired
1. ✅ Backend đang chạy
2. ✅ Database có messages
3. ✅ Đăng nhập bằng admin (token cũ)
4. ✅ Vào AdminChatListActivity
5. ❌ Response 401 - "Token không hợp lệ hoặc đã hết hạn"
6. ✅ Đăng nhập lại

---

## 🚀 Quick Fix Commands

### Restart Backend Server
```bash
cd /Users/mycomputer/Documents/StudioProjects/project_main/PRM392/BE
lsof -i :5001 | grep LISTEN | awk '{print $2}' | xargs kill -9 2>/dev/null
sleep 1
node server.js
```

### Check Database
```bash
cd /Users/mycomputer/Documents/StudioProjects/project_main/PRM392/BE
node -e "
require('dotenv').config();
const mongoose = require('mongoose');
const User = require('./models/User');
const ChatMessage = require('./models/ChatMessage');

mongoose.connect(process.env.MONGODB_URI).then(async () => {
  console.log('Users:', await User.countDocuments());
  console.log('Messages:', await ChatMessage.countDocuments());
  console.log('Admins:', await User.countDocuments({role: 'admin'}));
  process.exit(0);
});
"
```

### Rebuild App
```bash
cd /Users/mycomputer/Documents/StudioProjects/project_main/PRM392/FE
./gradlew clean assembleDebug
```

### Clear App Data (trong Android)
```
Settings → Apps → Your App → Storage → Clear Data
```

---

## 📝 Checklist Trước Khi Test

- [ ] Backend server đang chạy (port 5001)
- [ ] Database có messages
- [ ] Database có user với role "admin"
- [ ] App đã build mới nhất
- [ ] Đã đăng nhập bằng admin
- [ ] Logcat đang mở và filter "AdminChatListActivity"

---

## 🎯 Expected Results

**UI:**
- ProgressBar hiện khi loading
- RecyclerView hiện danh sách users
- Mỗi item hiện: avatar, username, last message, timestamp, unread badge
- Click vào item → mở AdminChatActivity với user đó

**Log:**
```
=== Loading chat conversations ===
Token: Bearer eyJ...
Is admin: true
Current user: admin
User role: admin
=== API Response ===
Response code: 200
Response successful: true
Conversations count: 1
Processing conversations...
Conversation 0:
  - User: customer
  - LastMessage: Hello
  - Unread: 3
Loaded 1 chat users
```

---

## 📞 Nếu Vẫn Còn Lỗi

Hãy gửi cho tôi:
1. **Full log** từ Logcat (filter: AdminChatListActivity)
2. **Screenshot** của màn hình lỗi
3. **Response body** từ curl test (nếu có)

Tôi sẽ giúp bạn debug tiếp! 🚀
