package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Login button click
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                performLogin();
            }
        });

        // Register link click
        findViewById(R.id.tvRegister).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot password click
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Check if logging in as admin
        if (isAdminAccount(email, password)) {
            Toast.makeText(this, "Đăng nhập với quyền Admin thành công!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, AdminManagementActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // TODO: Implement actual login logic here (e.g., Firebase, API call)
        // For now, just show a success message and navigate to HomeActivity

        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Check if the credentials belong to an admin account
     *
     * @param email    User email
     * @param password User password
     * @return true if admin account, false otherwise
     */
    private boolean isAdminAccount(String email, String password) {
        // Default admin credentials
        // TODO: In production, use proper authentication with database
        return email.equals("admin@example.com") && password.equals("admin123");
    }
}
