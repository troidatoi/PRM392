package com.example.project.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.project.R;
import com.example.project.AccountActivity;
import com.example.project.CartActivity;
import com.example.project.HomeActivity;
import com.example.project.ShopActivity;

public class BottomNavigationHelper {

    // Modern color scheme with vibrant blue
    private static final int COLOR_ACTIVE = 0xFF2196F3; // Material Blue
    private static final int COLOR_INACTIVE = 0xFF9E9E9E; // Gray
    private static final int COLOR_ACTIVE_BG = 0x1F2196F3; // Light blue 12% opacity
    private static final int COLOR_HOVER_BG = 0x0A2196F3; // Very light blue 4% opacity

    /**
     * Setup bottom navigation bar with proper active state
     * @param activity Current activity
     * @param activeTab Which tab is currently active (0=Home, 1=Products, 2=Cart, 3=Account)
     */
    public static void setupBottomNavigation(Activity activity, int activeTab) {
        View bottomNav = activity.findViewById(R.id.bottomNavBar);
        if (bottomNav == null) return;

        // Get all navigation items
        LinearLayout navHome = bottomNav.findViewById(R.id.navHome);
        LinearLayout navProducts = bottomNav.findViewById(R.id.navProducts);
        LinearLayout navCart = bottomNav.findViewById(R.id.navCart);
        LinearLayout navAccount = bottomNav.findViewById(R.id.navAccount);

        ImageView iconHome = bottomNav.findViewById(R.id.iconHome);
        ImageView iconProducts = bottomNav.findViewById(R.id.iconProducts);
        ImageView iconCart = bottomNav.findViewById(R.id.iconCart);
        ImageView iconAccount = bottomNav.findViewById(R.id.iconAccount);

        TextView tvHome = bottomNav.findViewById(R.id.tvHome);
        TextView tvProducts = bottomNav.findViewById(R.id.tvProducts);
        TextView tvCart = bottomNav.findViewById(R.id.tvCart);
        TextView tvAccount = bottomNav.findViewById(R.id.tvAccount);

        // Get icon containers for visual effects
        CardView homeContainer = bottomNav.findViewById(R.id.homeIconContainer);
        CardView productsContainer = bottomNav.findViewById(R.id.productsIconContainer);
        CardView cartContainer = bottomNav.findViewById(R.id.cartIconContainer);
        CardView accountContainer = bottomNav.findViewById(R.id.accountIconContainer);

        // Reset all to inactive state
        setNavItemState(iconHome, tvHome, homeContainer, false);
        setNavItemState(iconProducts, tvProducts, productsContainer, false);
        setNavItemState(iconCart, tvCart, cartContainer, false);
        setNavItemState(iconAccount, tvAccount, accountContainer, false);

        // Set active state based on current tab
        switch (activeTab) {
            case 0: // Home
                setNavItemState(iconHome, tvHome, homeContainer, true);
                break;
            case 1: // Products
                setNavItemState(iconProducts, tvProducts, productsContainer, true);
                break;
            case 2: // Cart
                setNavItemState(iconCart, tvCart, cartContainer, true);
                break;
            case 3: // Account
                setNavItemState(iconAccount, tvAccount, accountContainer, true);
                break;
        }

        // Setup click listeners
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (navProducts != null) {
            navProducts.setOnClickListener(v -> {
                if (!(activity instanceof ShopActivity)) {
                    Intent intent = new Intent(activity, ShopActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (navCart != null) {
            navCart.setOnClickListener(v -> {
                if (!(activity instanceof CartActivity)) {
                    Intent intent = new Intent(activity, CartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }

        if (navAccount != null) {
            navAccount.setOnClickListener(v -> {
                if (!(activity instanceof AccountActivity)) {
                    Intent intent = new Intent(activity, AccountActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }
            });
        }
    }

    /**
     * Set the active/inactive state of a navigation item with enhanced visual effects
     */
    private static void setNavItemState(ImageView icon, TextView text, CardView container, boolean isActive) {
        if (icon != null && text != null) {
            int color = isActive ? COLOR_ACTIVE : COLOR_INACTIVE;
            icon.setColorFilter(color);
            text.setTextColor(color);

            if (isActive) {
                // Active state - bold text with vibrant background
                text.setTypeface(null, android.graphics.Typeface.BOLD);
                if (container != null) {
                    container.setCardBackgroundColor(COLOR_ACTIVE_BG);
                    container.setCardElevation(3f); // Subtle elevation for depth
                    container.setRadius(20f); // Smooth rounded corners
                }
            } else {
                // Inactive state - normal weight with transparent background
                text.setTypeface(null, android.graphics.Typeface.NORMAL);
                if (container != null) {
                    container.setCardBackgroundColor(0x00000000); // Fully transparent
                    container.setCardElevation(0f);
                    container.setRadius(20f);
                }
            }
        }
    }

    /**
     * Update cart badge count
     */
    public static void updateCartBadge(Activity activity, int count) {
        View bottomNav = activity.findViewById(R.id.bottomNavBar);
        if (bottomNav == null) return;

        TextView badge = bottomNav.findViewById(R.id.tvCartBadge);
        if (badge != null) {
            if (count > 0) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(count > 99 ? "99+" : String.valueOf(count));
            } else {
                badge.setVisibility(View.GONE);
            }
        }
    }
}

