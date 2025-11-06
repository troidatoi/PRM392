package com.example.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PayOSPaymentActivity extends AppCompatActivity {

    private WebView webView;
    private String checkoutUrl;
    private String orderId;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payos_payment);

        // Get data from intent
        checkoutUrl = getIntent().getStringExtra("checkoutUrl");
        orderId = getIntent().getStringExtra("orderId");

        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có link thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup WebView trước
        webView = findViewById(R.id.webView);
        if (webView == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy WebView", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        // Setup toolbar sau khi webView đã được khởi tạo
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thanh toán PayOS");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toolbar.setNavigationOnClickListener(v -> {
                    if (webView != null && webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        onBackPressed();
                    }
                });
            }
        }

        // Set WebViewClient để xử lý redirect
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                android.util.Log.d("PayOSPayment", "Loading URL: " + url);

                // Parse URL để kiểm tra query parameters
                Uri uri = Uri.parse(url);
                String code = uri.getQueryParameter("code");
                String orderCodeParam = uri.getQueryParameter("orderCode");
                String status = uri.getQueryParameter("status");

                // Kiểm tra nếu là return URL (thanh toán thành công)
                // PayOS sẽ redirect về returnUrl với code=00 nếu thành công
                if (url.contains("payos-payment-success") || 
                    url.contains("/payment/success") || 
                    (code != null && "00".equals(code)) ||
                    "success".equals(status)) {
                    handlePaymentSuccess(url);
                    return true;
                }

                // Kiểm tra nếu là cancel URL (hủy thanh toán)
                if (url.contains("payos-payment-cancel") || 
                    url.contains("/payment/cancel") || 
                    "cancel".equals(status)) {
                    handlePaymentCancel();
                    return true;
                }

                // Cho phép WebView load URL bình thường
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                android.util.Log.d("PayOSPayment", "Page finished: " + url);
                
                // Kiểm tra URL sau khi page load xong
                Uri uri = Uri.parse(url);
                String code = uri.getQueryParameter("code");
                String status = uri.getQueryParameter("status");
                
                if (url.contains("payos-payment-success") || 
                    url.contains("/payment/success") || 
                    (code != null && "00".equals(code)) ||
                    "success".equals(status)) {
                    handlePaymentSuccess(url);
                } else if (url.contains("payos-payment-cancel") || 
                          url.contains("/payment/cancel") ||
                          "cancel".equals(status)) {
                    handlePaymentCancel();
                }
            }
        });

        // Load PayOS checkout URL
        android.util.Log.d("PayOSPayment", "Loading checkout URL: " + checkoutUrl);
        webView.loadUrl(checkoutUrl);
    }

    private void handlePaymentSuccess(String url) {
        android.util.Log.d("PayOSPayment", "Payment success detected: " + url);
        
        // Parse URL để lấy thông tin
        Uri uri = Uri.parse(url);
        String code = uri.getQueryParameter("code");
        String orderCode = uri.getQueryParameter("orderCode");
        
        // Tạo intent để trả về kết quả
        Intent resultIntent = new Intent();
        resultIntent.putExtra("paymentStatus", "success");
        resultIntent.putExtra("code", code);
        resultIntent.putExtra("orderCode", orderCode);
        resultIntent.putExtra("orderId", orderId);
        setResult(RESULT_OK, resultIntent);
        
        // Hiển thị thông báo và đóng activity
        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handlePaymentCancel() {
        android.util.Log.d("PayOSPayment", "Payment cancelled");
        
        // Tạo intent để trả về kết quả
        Intent resultIntent = new Intent();
        resultIntent.putExtra("paymentStatus", "cancelled");
        resultIntent.putExtra("orderId", orderId);
        setResult(RESULT_CANCELED, resultIntent);
        
        // Hiển thị thông báo và đóng activity
        Toast.makeText(this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            // Hỏi xác nhận nếu đang thanh toán
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hủy thanh toán?")
                .setMessage("Bạn có chắc muốn hủy thanh toán?")
                .setPositiveButton("Hủy thanh toán", (dialog, which) -> {
                    handlePaymentCancel();
                })
                .setNegativeButton("Tiếp tục", null)
                .show();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}

