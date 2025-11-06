package com.example.prm392.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.project.R;
import com.example.project.AccountActivity;
import com.example.project.CartActivity;
import com.example.project.HomeActivity;
import com.example.project.ShopActivity;

public class BottomNavigationHelper {

    // Color constants
    private static final int COLOR_ACTIVE = 0xFF2196F3; // Blue
    private static final int COLOR_INACTIVE = 0xFF9E9E9E; // Gray

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

        // Reset all to inactive state
        setNavItemState(iconHome, tvHome, false);
        setNavItemState(iconProducts, tvProducts, false);
        setNavItemState(iconCart, tvCart, false);
        setNavItemState(iconAccount, tvAccount, false);

        // Set active state based on current tab
        switch (activeTab) {
            case 0: // Home
                setNavItemState(iconHome, tvHome, true);
                break;
            case 1: // Products
                setNavItemState(iconProducts, tvProducts, true);
                break;
            case 2: // Cart
                setNavItemState(iconCart, tvCart, true);
                break;
            case 3: // Account
                setNavItemState(iconAccount, tvAccount, true);
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
     * Set the active/inactive state of a navigation item
     */
    private static void setNavItemState(ImageView icon, TextView text, boolean isActive) {
        if (icon != null && text != null) {
            int color = isActive ? COLOR_ACTIVE : COLOR_INACTIVE;
            icon.setColorFilter(color);
            text.setTextColor(color);

            if (isActive) {
                text.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                text.setTypeface(null, android.graphics.Typeface.NORMAL);
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

