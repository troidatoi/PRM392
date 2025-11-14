# Chat Push Notification - Implementation Complete ✅

## Tổng quan
Đã implement chức năng push notification realtime khi có tin nhắn chat mới. Notification hiển thị **tên người gửi** và **nội dung tin nhắn** khi app đang ở background.

## Tính năng đã implement

### 1. ✅ ChatNotificationHelper
**File:** `FE/app/src/main/java/com/example/project/utils/ChatNotificationHelper.java`

**Chức năng:**
- Tạo notification channel cho chat với priority cao
- Hiển thị notification với tên người gửi và nội dung tin nhắn
- Notification có âm thanh, rung, và LED
- Click notification mở đúng chat activity (User hoặc Admin)
- Cancel notification khi mở chat
- Hỗ trợ BigTextStyle để hiển thị toàn bộ tin nhắn

**Methods:**
```java
// Tạo notification channel
ChatNotificationHelper.createChatNotificationChannel(context);

// Hiển thị notification
ChatNotificationHelper.showChatNotification(
    context,
    senderId,      // ID người gửi
    senderName,    // Tên người gửi
    message,       // Nội dung tin nhắn
    isAdminChat    // true nếu admin chat, false nếu user chat
);

// Xóa notification của một conversation
ChatNotificationHelper.cancelChatNotification(context, senderId);

// Xóa tất cả notifications
ChatNotificationHelper.cancelAllChatNotifications(context);

// Kiểm tra notification có enabled không
boolean enabled = ChatNotificationHelper.areChatNotificationsEnabled(context);
```

### 2. ✅ SocketManager - Auto Notification
**File:** `FE/app/src/main/java/com/example/project/network/SocketManager.java`

**Chức năng:**
- Tự động kiểm tra khi nhận event `message:received`
- Chỉ hiển thị notification nếu:
  - App đang ở background
  - Tin nhắn không phải từ chính mình
  - Có đầy đủ thông tin người gửi và nội dung
- Parse thông tin từ Socket event và truyền vào notification

**Logic:**
```java
// Khi nhận tin nhắn mới
socket.on(EVENT_MESSAGE_RECEIVED, new Emitter.Listener() {
    @Override
    public void call(Object... args) {
        // Parse data
        JSONObject data = (JSONObject) args[0];
        
        // Kiểm tra app có ở foreground không
        if (isAppInForeground()) {
            // Skip notification nếu app đang mở
            return;
        }
        
        // Hiển thị notification với tên và nội dung
        showNotificationIfBackground(data);
    }
});
```

### 3. ✅ Chat Activities - Notification Management
**Files:**
- `FE/app/src/main/java/com/example/project/UserChatActivity.java`
- `FE/app/src/main/java/com/example/project/AdminChatActivity.java`

**Chức năng:**
- Tạo notification channel khi activity được mở
- Tự động xóa notification khi mở chat
- Đảm bảo user không thấy notification của conversation đang mở

**Code được thêm vào `onCreate()`:**
```java
// Create notification channel
ChatNotificationHelper.createChatNotificationChannel(this);

// Cancel notification for this chat when opened
ChatNotificationHelper.cancelChatNotification(this, userId);
```

### 4. ✅ MainActivity - Permission Request
**File:** `FE/app/src/main/java/com/example/project/MainActivity.java`

**Chức năng:**
- Request notification permission trên Android 13+ (API 33+)
- Tạo notification channels khi app khởi động

**Code:**
```java
// Request notification permissions (Android 13+)
NotificationHelper.requestNotificationPermission(this, 1001);
ChatNotificationHelper.createChatNotificationChannel(this);
```

## Flow hoạt động

### Scenario 1: User nhận tin nhắn từ Admin (App ở background)
```
1. Admin gửi tin nhắn
   ↓
2. Backend emit event `message:received` qua Socket
   ↓
3. SocketManager nhận event
   ↓
4. Kiểm tra app có ở background không → Yes
   ↓
5. Parse thông tin: senderName, message
   ↓
6. ChatNotificationHelper.showChatNotification()
   ↓
7. Notification hiển thị: "Admin" - "Xin chào, tôi có thể..."
   ↓
8. User click notification → Mở UserChatActivity
   ↓
9. Notification tự động xóa
```

### Scenario 2: Admin nhận tin nhắn từ User (App ở background)
```
1. User gửi tin nhắn
   ↓
2. Backend emit event `message:received` qua Socket
   ↓
3. SocketManager nhận event
   ↓
4. Kiểm tra app có ở background không → Yes
   ↓
5. Parse thông tin: userName, message
   ↓
6. ChatNotificationHelper.showChatNotification()
   ↓
7. Notification hiển thị: "Nguyễn Văn A" - "Cho tôi hỏi về sản phẩm..."
   ↓
8. Admin click notification → Mở AdminChatActivity với userId
   ↓
9. Notification tự động xóa
```

### Scenario 3: App đang ở foreground
```
1. Tin nhắn mới đến
   ↓
2. SocketManager kiểm tra: isAppInForeground() → Yes
   ↓
3. Skip notification (không hiện)
   ↓
4. Tin nhắn hiển thị trực tiếp trong chat UI
```

## Cấu hình Notification

### Notification Channel Settings
- **Channel ID:** `chat_notification_channel`
- **Channel Name:** "Tin nhắn Chat"
- **Importance:** HIGH (có âm thanh)
- **Badge:** Enabled
- **LED:** Enabled
- **Vibration:** Enabled (0, 250, 250, 250)
- **Sound:** Default system sound

### Notification Properties
- **Small Icon:** App icon
- **Title:** Tên người gửi
- **Content:** Nội dung tin nhắn
- **Style:** BigTextStyle (expand để xem toàn bộ)
- **Priority:** HIGH
- **Category:** MESSAGE
- **Auto Cancel:** true (click để dismiss)
- **Visibility:** PUBLIC (hiện trên lock screen)

## Permissions Required

### AndroidManifest.xml
```xml
<!-- Notification permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
```

✅ **Đã có sẵn trong AndroidManifest.xml**

## Testing Guide

### Test 1: Notification khi app ở background
1. Đăng nhập vào app
2. Mở chat và gửi tin nhắn
3. Nhấn Home button (app vào background)
4. Cho người khác (Admin/User) gửi tin nhắn
5. ✅ **Expected:** Notification hiển thị với tên người gửi và nội dung

### Test 2: Không notification khi app foreground
1. Mở chat activity
2. Nhận tin nhắn mới
3. ✅ **Expected:** Tin nhắn hiển thị trong chat, KHÔNG có notification

### Test 3: Click notification
1. Nhận notification
2. Click vào notification
3. ✅ **Expected:** Mở đúng chat activity với đúng người
4. ✅ **Expected:** Notification tự động xóa

### Test 4: Multiple conversations
1. Nhận tin nhắn từ nhiều người khác nhau
2. ✅ **Expected:** Mỗi người có notification riêng
3. Mở chat với một người
4. ✅ **Expected:** Chỉ notification của người đó bị xóa

### Test 5: Permission check (Android 13+)
1. Chạy app lần đầu trên Android 13+
2. ✅ **Expected:** Popup yêu cầu quyền notification
3. Allow permission
4. ✅ **Expected:** Notification hoạt động bình thường

## Troubleshooting

### Không nhận được notification
1. **Kiểm tra permission:**
   ```java
   boolean enabled = ChatNotificationHelper.areChatNotificationsEnabled(context);
   ```
   
2. **Kiểm tra log:**
   ```
   Tag: SocketManager
   - "Message received"
   - "App is in foreground, skipping notification" (nếu app mở)
   - "Showing notification - From: X, Message: Y"
   ```

3. **Kiểm tra Settings:**
   - Settings > Apps > Your App > Notifications
   - Đảm bảo "Tin nhắn Chat" channel enabled

### Notification không có âm thanh
- Kiểm tra channel importance = HIGH
- Kiểm tra volume settings trên device
- Kiểm tra Do Not Disturb mode

### App không mở đúng activity khi click
- Kiểm tra PendingIntent flags: `FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE`
- Kiểm tra intent extras có đầy đủ userId, userName không

## Summary

### Files Created
- ✅ `ChatNotificationHelper.java` - Core notification logic

### Files Modified
- ✅ `SocketManager.java` - Auto notification on message received
- ✅ `UserChatActivity.java` - Create channel & cancel notification
- ✅ `AdminChatActivity.java` - Create channel & cancel notification
- ✅ `MainActivity.java` - Request permissions

### Features Delivered
✅ Realtime push notification khi có tin nhắn mới  
✅ Hiển thị tên người gửi  
✅ Hiển thị nội dung tin nhắn đầy đủ  
✅ Chỉ hiện khi app ở background  
✅ Click notification mở đúng chat  
✅ Auto cancel khi mở chat  
✅ Hỗ trợ Android 13+ permissions  
✅ Multiple conversations support  
✅ Sound, vibration, LED effects  

## Hoàn thành 100% ✨

Chức năng push notification cho chat đã được implement đầy đủ và ready để test!
