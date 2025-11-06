package com.example.project.utils;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class AdminNavHelper {

    /**
     * Set active state for a navigation tab
     */
    public static void setActiveTab(CardView activeCard, ImageView activeIcon, TextView activeText) {
        if (activeCard != null) {
            activeCard.setCardBackgroundColor(0xFF2196F3); // Blue
            activeCard.setCardElevation(8f);
        }
        if (activeIcon != null) {
            activeIcon.setColorFilter(0xFFFFFFFF); // White
        }
        if (activeText != null) {
            activeText.setTextColor(0xFF2196F3); // Blue
        }
    }

    /**
     * Reset tab to inactive state
     */
    public static void resetTab(CardView card, ImageView icon, TextView text) {
        if (card != null) {
            card.setCardBackgroundColor(0xFFF5F5F5); // Light gray
            card.setCardElevation(0f);
        }
        if (icon != null) {
            icon.setColorFilter(0xFF64748B); // Slate gray
        }
        if (text != null) {
            text.setTextColor(0xFF64748B); // Slate gray
        }
    }

    /**
     * Reset all navigation tabs
     */
    public static void resetAllTabs(
            CardView cardDashboard, ImageView iconDashboard, TextView tvDashboard,
            CardView cardUserManagement, ImageView iconUserManagement, TextView tvUserManagement,
            CardView cardProductManagement, ImageView iconProductManagement, TextView tvProductManagement,
            CardView cardStoreManagement, ImageView iconStoreManagement, TextView tvStoreManagement,
            CardView cardOrderManagement, ImageView iconOrderManagement, TextView tvOrderManagement,
            CardView cardChatManagement, ImageView iconChatManagement, TextView tvChatManagement
    ) {
        resetTab(cardDashboard, iconDashboard, tvDashboard);
        resetTab(cardUserManagement, iconUserManagement, tvUserManagement);
        resetTab(cardProductManagement, iconProductManagement, tvProductManagement);
        resetTab(cardStoreManagement, iconStoreManagement, tvStoreManagement);
        resetTab(cardOrderManagement, iconOrderManagement, tvOrderManagement);
        resetTab(cardChatManagement, iconChatManagement, tvChatManagement);
    }
}
