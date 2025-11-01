package com.example.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AddProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Redirect to CreateBikeActivity which has full functionality including image selection
        Intent intent = new Intent(this, CreateBikeActivity.class);
        startActivity(intent);
        finish();
    }
}
