package com.example.project.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.project.CartActivity;
import com.example.project.R;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Helper class để quản lý badge notifications trên app icon
 * Hiển thị số lượng sản phẩm trong giỏ hàng khi app đóng
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "cart_badge_channel";
    private static final String CHANNEL_NAME = "Cart Badge Notifications";
    private static final int NOTIFICATION_ID = 1001;

    /**
     * Tạo notification channel cho Android 8.0+ (API 26+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT // Default importance để badge hiển thị số
            );
            channel.setDescription("Hiển thị số lượng sản phẩm trong giỏ hàng");
            channel.setShowBadge(true); // Bật badge và hiển thị số
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Cập nhật badge trên app icon với số lượng sản phẩm trong giỏ hàng
     * @param context Context của ứng dụng
     * @param cartItemCount Số lượng sản phẩm trong giỏ hàng
     */
    public static void updateCartBadge(Context context, int cartItemCount) {
        try {
            createNotificationChannel(context);

            NotificationManager notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager is null");
                return;
            }

            // Nếu giỏ hàng trống, xóa badge và notification
            if (cartItemCount <= 0) {
                removeCartBadge(context);
                return;
            }

            // Tạo Intent để mở CartActivity khi click vào notification
            Intent intent = new Intent(context, CartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
            } else {
                pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
            }

            // Tạo notification với badge count
            // Sử dụng icon đơn giản, nếu không có thì dùng icon mặc định
            int iconResId = R.drawable.ic_launcher_foreground;
            try {
                // Kiểm tra xem icon có tồn tại không
                context.getResources().getDrawable(iconResId);
            } catch (Exception e) {
                // Nếu không có, dùng icon mặc định của hệ thống
                iconResId = android.R.drawable.ic_menu_report_image;
            }
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle("Giỏ hàng của bạn")
                .setContentText("Bạn có " + cartItemCount + " sản phẩm trong giỏ hàng")
                .setNumber(cartItemCount) // Hiển thị số trên notification và badge
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Default priority để badge hiển thị số
                .setOngoing(true) // Ongoing notification để không bị swipe away
                .setAutoCancel(false) // Không tự động cancel
                .setContentIntent(pendingIntent)
                .setShowWhen(false) // Ẩn thời gian
                .setOnlyAlertOnce(true) // Chỉ alert một lần
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // Badge icon nhỏ
                .setCategory(NotificationCompat.CATEGORY_STATUS); // Category status để hiển thị badge

            // Hiển thị notification
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            // Cập nhật badge trên app icon (hoạt động trên Samsung, Xiaomi, etc.)
            // ShortcutBadger sẽ hiển thị số trên app icon
            try {
                boolean success = ShortcutBadger.applyCount(context, cartItemCount);
                if (success) {
                    Log.d(TAG, "ShortcutBadger badge updated successfully: " + cartItemCount);
                } else {
                    Log.w(TAG, "ShortcutBadger applyCount returned false");
                }
            } catch (Exception e) {
                Log.w(TAG, "ShortcutBadger not supported on this device: " + e.getMessage());
                // Notification badge với setNumber() vẫn sẽ hiển thị số trên một số thiết bị
            }
            
            // Đảm bảo notification được hiển thị để badge có thể hiển thị số
            // Với setNumber(), badge sẽ tự động hiển thị số trên hầu hết thiết bị Android

        } catch (Exception e) {
            Log.e(TAG, "Error updating cart badge: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa badge và notification
     */
    public static void removeCartBadge(Context context) {
        try {
            NotificationManager notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.cancel(NOTIFICATION_ID);
            }

            // Xóa badge trên app icon
            try {
                ShortcutBadger.removeCount(context);
                Log.d(TAG, "Badge removed successfully");
            } catch (Exception e) {
                Log.w(TAG, "Error removing badge: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing cart badge: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra quyền notification (Android 13+)
     */
    public static boolean checkNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager != null && notificationManager.areNotificationsEnabled();
        }
        return true; // Android < 13 không cần permission
    }

    /**
     * Yêu cầu quyền notification (Android 13+)
     * Gọi từ Activity khi cần
     */
    public static void requestNotificationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, 
                    android.Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    requestCode);
            }
        }
    }

    // Payment Badge Notification
    private static final String PAYMENT_CHANNEL_ID = "payment_badge_channel";
    private static final String PAYMENT_CHANNEL_NAME = "Payment Badge Notifications";
    private static final int PAYMENT_NOTIFICATION_ID = 1002;

    /**
     * Tạo notification channel cho payment badge
     */
    public static void createPaymentNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                PAYMENT_CHANNEL_ID,
                PAYMENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo thanh toán chưa hoàn tất");
            channel.setShowBadge(true);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Cập nhật badge thanh toán chưa hoàn tất
     * @param context Context của ứng dụng
     * @param pendingCount Số lượng payment pending
     * @param orderId Order ID để mở khi click (optional)
     */
    public static void updatePaymentBadge(Context context, int pendingCount, String orderId) {
        try {
            Log.d(TAG, "updatePaymentBadge called with count: " + pendingCount + ", orderId: " + orderId);
            
            createPaymentNotificationChannel(context);

            NotificationManager notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager is null");
                return;
            }

            // Nếu không có payment pending, xóa badge
            if (pendingCount <= 0) {
                Log.d(TAG, "No pending payments, removing badge");
                removePaymentBadge(context);
                return;
            }
            
            Log.d(TAG, "Creating payment notification with count: " + pendingCount);

            // Tạo Intent để mở payment screen khi click vào notification
            Intent intent;
            if (orderId != null && !orderId.isEmpty()) {
                // Mở order detail với orderId cụ thể
                intent = new Intent(context, com.example.project.OrderDetailActivity.class);
                intent.putExtra("ORDER_ID", orderId);
            } else {
                // Mở order history
                intent = new Intent(context, com.example.project.OrderHistoryActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
            } else {
                pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
            }

            int iconResId = R.drawable.ic_launcher_foreground;
            try {
                context.getResources().getDrawable(iconResId);
            } catch (Exception e) {
                iconResId = android.R.drawable.ic_menu_report_image;
            }
            
            // Format giống hệt như cart notification
            String message = "Bạn có " + pendingCount + " thanh toán chưa hoàn tất";
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PAYMENT_CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle("Thanh toán chưa hoàn tất")
                .setContentText(message)
                .setNumber(pendingCount) // Hiển thị số trên notification và badge
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Default priority để badge hiển thị số
                .setOngoing(true) // Ongoing notification để không bị swipe away
                .setAutoCancel(false) // Không tự động cancel
                .setContentIntent(pendingIntent)
                .setShowWhen(false) // Ẩn thời gian
                .setOnlyAlertOnce(true) // Chỉ alert một lần
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // Badge icon nhỏ
                .setCategory(NotificationCompat.CATEGORY_STATUS); // Category status để hiển thị badge

            notificationManager.notify(PAYMENT_NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Payment notification displayed with ID: " + PAYMENT_NOTIFICATION_ID);

            // Cập nhật badge trên app icon
            try {
                boolean success = ShortcutBadger.applyCount(context, pendingCount);
                if (success) {
                    Log.d(TAG, "Payment badge updated successfully: " + pendingCount);
                } else {
                    Log.w(TAG, "ShortcutBadger applyCount returned false");
                }
            } catch (Exception e) {
                Log.w(TAG, "ShortcutBadger not supported: " + e.getMessage());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating payment badge: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa payment badge và notification
     */
    public static void removePaymentBadge(Context context) {
        try {
            NotificationManager notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.cancel(PAYMENT_NOTIFICATION_ID);
            }

            // Xóa badge trên app icon
            try {
                ShortcutBadger.removeCount(context);
                Log.d(TAG, "Payment badge removed successfully");
            } catch (Exception e) {
                Log.w(TAG, "Error removing payment badge: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing payment badge: " + e.getMessage(), e);
        }
    }
}

