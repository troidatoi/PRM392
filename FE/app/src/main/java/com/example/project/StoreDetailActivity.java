package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StoreDetailActivity extends AppCompatActivity {

    private TextView tvStoreName, tvStoreAddress, tvStoreStatus, tvStorePhone, tvStoreEmail, tvOperatingHours;
    private LinearLayout llPhone, llEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        // Initialize Views
        tvStoreName = findViewById(R.id.tvStoreNameDetail);
        tvStoreAddress = findViewById(R.id.tvStoreAddressDetail);
        tvStoreStatus = findViewById(R.id.tvStoreStatusDetail);
        tvStorePhone = findViewById(R.id.tvStorePhone);
        tvStoreEmail = findViewById(R.id.tvStoreEmail);
        tvOperatingHours = findViewById(R.id.tvOperatingHoursDetail);
        llPhone = findViewById(R.id.llPhone);
        llEmail = findViewById(R.id.llEmail);

        // Get Store data from Intent
        Store store = (Store) getIntent().getSerializableExtra("STORE_DETAIL");

        if (store != null) {
            populateStoreDetails(store);
        }
    }

    private void populateStoreDetails(Store store) {
        // Set basic info
        tvStoreName.setText(store.getName());
        tvStoreAddress.setText(store.getFullAddress());

        // Set status
        if (store.isOpenNow()) {
            tvStoreStatus.setText("Đang mở");
            tvStoreStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            tvStoreStatus.setText("Đóng cửa");
            tvStoreStatus.setTextColor(Color.parseColor("#FF5252")); // Red
        }

        // Set contact info
        if (store.getPhone() != null && !store.getPhone().isEmpty()) {
            tvStorePhone.setText(store.getPhone());
            llPhone.setVisibility(View.VISIBLE);
            llPhone.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + store.getPhone()));
                startActivity(intent);
            });
        } else {
            llPhone.setVisibility(View.GONE);
        }

        if (store.getEmail() != null && !store.getEmail().isEmpty()) {
            tvStoreEmail.setText(store.getEmail());
            llEmail.setVisibility(View.VISIBLE);
            llEmail.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + store.getEmail()));
                startActivity(intent);
            });
        } else {
            llEmail.setVisibility(View.GONE);
        }

        // Set operating hours
        tvOperatingHours.setText(store.getDetailedOperatingHoursText());
    }
}