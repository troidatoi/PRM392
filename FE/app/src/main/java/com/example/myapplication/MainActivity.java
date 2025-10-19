package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.myapplication.fragment.BikeListFragment;
import com.example.myapplication.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private BikeListFragment bikeListFragment;
    private HomeFragment homeFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupBottomNavigation();
        loadDefaultFragment();
    }
    
    private void initViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bikeListFragment = new BikeListFragment();
        homeFragment = new HomeFragment();
    }
    
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            String title = item.getTitle().toString();
            
            if ("Trang chủ".equals(title)) {
                loadFragment(homeFragment);
                return true;
            } else if ("Sản phẩm".equals(title)) {
                loadFragment(bikeListFragment);
                return true;
            } else if ("Giỏ hàng".equals(title)) {
                // Navigate to cart - TODO: implement
                return true;
            } else if ("Cá nhân".equals(title)) {
                // Navigate to profile - TODO: implement
                return true;
            }
            return false;
        });
    }
    
    private void loadDefaultFragment() {
        loadFragment(bikeListFragment);
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
