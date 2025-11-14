# 🐛 Debug Guide: Tại sao không có thông báo đẩy?

## ✅ Đã thêm Debug Mode

### Thay đổi mới:
1. **FORCE_NOTIFICATION_FOR_TESTING = true** - Hiện notification ngay cả khi app foreground (để test dễ hơn)
2. **Thêm extensive logging** - Xem chi tiết từng bước

## 📝 Cách kiểm tra

### Bước 1: Install app mới
```bash
# APK location:
PRM392/FE/app/build/outputs/apk/debug/app-debug.apk

# Install qua adb:
adb install -r app-debug.apk
```

### Bước 2: Xem Logcat
```bash
# Filter SocketManager logs:
adb logcat -s SocketManager:D

# Filter ChatNotificationHelper logs:
adb logcat -s ChatNotificationHelper:D

# Hoặc xem tất cả:
adb logcat | grep -E "SocketManager|ChatNotificationHelper"
```

### Bước 3: Test notification
1. Đăng nhập vào app
2. Mở chat
3. GỬI TIN NHẮN từ device/account khác
4. XEM LOGCAT ngay lập tức

## 🔍 Log Messages cần chú ý

### ✅ Logs thành công:
```
SocketManager: === NOTIFICATION DEBUG START ===
SocketManager: Received message data: {...}
SocketManager: App in foreground: true
SocketManager: Force notification mode: true
SocketManager: ⚠️ FORCE MODE ENABLED - Showing notification even in foreground
SocketManager: Parsed - senderId: xxx, senderName: xxx
SocketManager: Current user ID: yyy
SocketManager: Sender ID: xxx
SocketManager: User role: admin, isFromAdmin: true
SocketManager: 🔔 Showing notification - From: Admin, Message: Hello
SocketManager: ✅ Notification shown successfully
SocketManager: === NOTIFICATION DEBUG END ===

ChatNotificationHelper: === showChatNotification START ===
ChatNotificationHelper: Sender ID: xxx
ChatNotificationHelper: Sender Name: Admin
ChatNotificationHelper: Message: Hello
ChatNotificationHelper: Is Admin Chat: false
ChatNotificationHelper: ✅ Notifications are ENABLED
ChatNotificationHelper: Notification ID: 2xxx
ChatNotificationHelper: 📤 Calling notificationManager.notify()...
ChatNotificationHelper: ✅ Notification displayed successfully!
ChatNotificationHelper: === showChatNotification END ===
```

### ❌ Các vấn đề có thể gặp:

#### 1. Không nhận được message từ Socket
```
# Không thấy log này:
SocketManager: === NOTIFICATION DEBUG START ===
```
**Giải pháp:**
- Kiểm tra Socket connection: `adb logcat -s SocketManager:D | grep "connected"`
- Kiểm tra Backend server có đang chạy không
- Kiểm tra network connection

#### 2. Message từ chính mình
```
SocketManager: ❌ Skipping notification - Message from self or no current user
```
**Giải pháp:**
- Gửi message từ account KHÁC (admin gửi cho user, hoặc ngược lại)
- Không test với message từ chính mình

#### 3. Notification permission bị tắt
```
ChatNotificationHelper: ❌ Notifications are DISABLED in system settings
```
**Giải pháp:**
```bash
# Mở Settings > Apps > Your App > Notifications
# Hoặc force enable qua adb:
adb shell pm grant com.example.project android.permission.POST_NOTIFICATIONS
```

#### 4. NotificationManager null
```
ChatNotificationHelper: ❌ NotificationManager is null
```
**Giải pháp:**
- Restart app
- Reinstall app

## 🔧 Cấu hình hiện tại

### Force Notification Mode
```java
// File: SocketManager.java line ~28
private static boolean FORCE_NOTIFICATION_FOR_TESTING = true;
```

**✅ Đang BẬT** - Notification sẽ hiện ngay cả khi app foreground

**Để TẮT sau khi test xong:**
```java
private static boolean FORCE_NOTIFICATION_FOR_TESTING = false;
```

## 📱 Test Steps chi tiết

### Test 1: Với FORCE_NOTIFICATION_FOR_TESTING = true (hiện tại)
1. Install app
2. Đăng nhập USER account
3. Mở UserChatActivity
4. Để app ở screen (không nhấn Home)
5. Từ admin panel/device khác, gửi tin nhắn tới user này
6. **Expected:** 
   - ✅ Tin nhắn hiện trong chat UI
   - ✅ Notification CŨNG hiện (do force mode)
   - ✅ Có âm thanh/rung

### Test 2: Notification khi app background (behavior thật)
1. Mở app và chat
2. **Nhấn Home button** (app vào background)
3. Gửi tin nhắn từ người khác
4. **Expected:**
   - ✅ Notification hiện
   - ✅ Click notification mở app và chat

## 🎯 Checklist Debug

Hãy kiểm tra theo thứ tự:

- [ ] **Backend server đang chạy?**
  ```bash
  # Terminal 1: Start backend
  cd PRM392/BE
  npm start
  # Should see: Server running on port 5001
  ```

- [ ] **Socket connected?**
  ```bash
  adb logcat -s SocketManager:D | grep "connected"
  # Should see: Socket connected
  ```

- [ ] **Message received?**
  ```bash
  adb logcat -s SocketManager:D | grep "Message received"
  # Should see: Message received
  ```

- [ ] **Notification permission granted?**
  ```bash
  # Check trong app Settings hoặc:
  adb shell dumpsys notification_policy
  ```

- [ ] **Notification channel created?**
  ```bash
  adb logcat -s ChatNotificationHelper:D | grep "channel created"
  # Should see: Chat notification channel created
  ```

- [ ] **Gửi từ account KHÁC?**
  - ✅ Admin → User (OK)
  - ✅ User → Admin (OK)
  - ❌ User → User (same account - sẽ bị skip)

## 💡 Quick Fix

Nếu vẫn không thấy notification, thử các bước sau:

### 1. Force grant permission
```bash
adb shell pm grant com.example.project android.permission.POST_NOTIFICATIONS
```

### 2. Clear app data và reinstall
```bash
adb uninstall com.example.project
adb install app-debug.apk
```

### 3. Kiểm tra Do Not Disturb mode
```bash
# Tắt DND mode:
Settings > Sound > Do Not Disturb > Turn OFF
```

### 4. Test với notification test
Thêm code này vào MainActivity.onCreate() để test notification ngay:
```java
// Test notification
ChatNotificationHelper.showChatNotification(
    this,
    "test123",
    "Test Sender",
    "This is a test notification",
    false
);
```

## 📊 Expected Results

Với **FORCE_NOTIFICATION_FOR_TESTING = true**, bạn sẽ thấy:

1. **Logs đầy đủ** trong Logcat
2. **Notification hiện ngay** khi có message (kể cả foreground)
3. **Click notification** mở đúng chat
4. **Âm thanh + rung**

## ⚠️ Lưu ý Production

**Trước khi deploy:**
```java
// Set lại thành false trong SocketManager.java:
private static boolean FORCE_NOTIFICATION_FOR_TESTING = false;
```

Để notification chỉ hiện khi app ở background (behavior đúng).

## 🆘 Vẫn không work?

Gửi logs cho tôi:
```bash
adb logcat -d > logcat.txt
# Hoặc
adb logcat -s SocketManager:D ChatNotificationHelper:D > debug.txt
```

Và cho biết:
1. Có thấy "=== NOTIFICATION DEBUG START ===" không?
2. Current user ID vs Sender ID là gì?
3. Notification permission status?
4. Android version?

---

## ✅ Build mới đã sẵn sàng

APK với debug mode đã được build:
```
PRM392/FE/app/build/outputs/apk/debug/app-debug.apk
```

Install và xem logcat để debug! 🔍
