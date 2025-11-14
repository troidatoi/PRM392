package com.example.project.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.project.R;
import com.example.project.UserChatActivity;
import com.example.project.AdminChatActivity;

/**
 * Helper class để quản lý push notifications cho chat
 * Hiển thị thông báo realtime khi có tin nhắn mới
 */
public class ChatNotificationHelper {
    private static final String TAG = "ChatNotificationHelper";
    
    // Chat Notification Channel
    private static final String CHAT_CHANNEL_ID = "chat_notification_channel";
    private static final String CHAT_CHANNEL_NAME = "Tin nhắn Chat";
    private static final String CHAT_CHANNEL_DESCRIPTION = "Thông báo khi có tin nhắn chat mới";
    
    // Notification IDs - mỗi conversation có ID riêng
    private static final int CHAT_NOTIFICATION_BASE_ID = 2000;
    
    /**
     * Tạo notification channel cho chat (Android 8.0+)
     */
    public static void createChatNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHAT_CHANNEL_ID,
                CHAT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // High importance để có âm thanh và hiện đầy đủ
            );
            
            channel.setDescription(CHAT_CHANNEL_DESCRIPTION);
            channel.setShowBadge(true); // Hiển thị badge
            channel.enableLights(true); // LED notification
            channel.enableVibration(true); // Rung
            channel.setVibrationPattern(new long[]{0, 250, 250, 250}); // Pattern rung
            
            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Chat notification channel created");
            }
        }
    }
    
    /**
     * Hiển thị notification khi có tin nhắn chat mới
     * 
     * @param context Context của ứng dụng
     * @param senderId ID của người gửi (để tạo notification ID riêng)
     * @param senderName Tên người gửi
     * @param message Nội dung tin nhắn
     * @param isAdminChat true nếu đây là chat admin, false nếu user chat
     */
    public static void showChatNotification(
        Context context, 
        String senderId,
        String senderName, 
        String message,
        boolean isAdminChat
    ) {
        try {
            Log.d(TAG, "=== showChatNotification START ===");
            Log.d(TAG, "Sender ID: " + senderId);
            Log.d(TAG, "Sender Name: " + senderName);
            Log.d(TAG, "Message: " + message);
            Log.d(TAG, "Is Admin Chat: " + isAdminChat);
            
            // Tạo channel nếu chưa có
            createChatNotificationChannel(context);
            
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager == null) {
                Log.e(TAG, "❌ NotificationManager is null");
                return;
            }
            
            // Kiểm tra notifications có enabled không
            if (!notificationManager.areNotificationsEnabled()) {
                Log.e(TAG, "❌ Notifications are DISABLED in system settings");
                return;
            } else {
                Log.d(TAG, "✅ Notifications are ENABLED");
            }
            
            // Tạo intent để mở chat activity khi click notification
            Intent intent;
            if (isAdminChat) {
                intent = new Intent(context, AdminChatActivity.class);
                intent.putExtra("userId", senderId);
                intent.putExtra("userName", senderName);
            } else {
                intent = new Intent(context, UserChatActivity.class);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                senderId.hashCode(), // Unique request code cho mỗi sender
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Get app icon
            int iconResId = R.mipmap.ic_launcher;
            try {
                iconResId = context.getApplicationInfo().icon;
            } catch (Exception e) {
                Log.w(TAG, "Could not get app icon, using default");
            }
            
            // Tạo notification với nội dung đầy đủ
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle(senderName) // Tên người gửi
                .setContentText(message) // Nội dung tin nhắn
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(message) // Hiển thị toàn bộ tin nhắn khi expand
                    .setBigContentTitle(senderName))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority cao để hiện đầy đủ
                .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Category message
                .setAutoCancel(true) // Tự động xóa khi click
                .setContentIntent(pendingIntent)
                .setShowWhen(true) // Hiển thị thời gian
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Âm thanh, rung, đèn mặc định
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // Hiển thị trên lock screen
            
            // Tạo unique notification ID dựa trên sender ID
            int notificationId = CHAT_NOTIFICATION_BASE_ID + Math.abs(senderId.hashCode() % 1000);
            
            Log.d(TAG, "Notification ID: " + notificationId);
            Log.d(TAG, "📤 Calling notificationManager.notify()...");
            
            // Hiển thị notification
            notificationManager.notify(notificationId, builder.build());
            
            Log.d(TAG, "✅ Notification displayed successfully!");
            Log.d(TAG, String.format(
                "Chat notification displayed - Sender: %s, Message: %s, ID: %d", 
                senderName, 
                message.substring(0, Math.min(20, message.length())),
                notificationId
            ));
            Log.d(TAG, "=== showChatNotification END ===");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing chat notification", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Xóa notification của một conversation cụ thể
     * 
     * @param context Context của ứng dụng
     * @param senderId ID của người gửi
     */
    public static void cancelChatNotification(Context context, String senderId) {
        try {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                int notificationId = CHAT_NOTIFICATION_BASE_ID + Math.abs(senderId.hashCode() % 1000);
                notificationManager.cancel(notificationId);
                Log.d(TAG, "Chat notification cancelled for sender: " + senderId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling chat notification", e);
        }
    }
    
    /**
     * Xóa tất cả chat notifications
     */
    public static void cancelAllChatNotifications(Context context) {
        try {
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                // Cancel all notifications in the range
                for (int i = 0; i < 1000; i++) {
                    notificationManager.cancel(CHAT_NOTIFICATION_BASE_ID + i);
                }
                Log.d(TAG, "All chat notifications cancelled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling all chat notifications", e);
        }
    }
    
    /**
     * Kiểm tra xem chat notifications có được enable không
     * 
     * @param context Context của ứng dụng
     * @return true nếu notifications được bật
     */
    public static boolean areChatNotificationsEnabled(Context context) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager == null) {
            return false;
        }
        
        // Check if notifications are enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHAT_CHANNEL_ID);
            if (channel != null) {
                return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
        }
        
        return notificationManager.areNotificationsEnabled();
    }
}
