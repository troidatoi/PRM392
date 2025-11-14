package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project.utils.ChatNotificationHelper;
import com.example.project.utils.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Request notification permissions and create channels
        NotificationHelper.requestNotificationPermission(this, 1001);
        ChatNotificationHelper.createChatNotificationChannel(this);

        // Get Start Button - Navigate to LoginActivity or Home
        RelativeLayout startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            // Navigate to LoginActivity or your main activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close splash screen
        });

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.backgroundImage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });
    }
}