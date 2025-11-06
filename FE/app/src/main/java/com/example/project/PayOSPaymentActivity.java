package com.example.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PayOSPaymentActivity extends AppCompatActivity {
    private static final String TAG = "PayOSPaymentActivity";
    private WebView webView;
    private ProgressBar progressBar;
    private String orderId;
    private String checkoutUrl;
    
    // URLs để detect khi thanh toán thành công/hủy
    private static final String SUCCESS_URL_PATTERN = "/payment/success";
    private static final String CANCEL_URL_PATTERN = "/payment/cancel";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payos_payment);

        // Get data from intent
        Intent intent = getIntent();
        checkoutUrl = intent.getStringExtra("checkoutUrl");
        orderId = intent.getStringExtra("orderId");

        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Không có link thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán PayOS");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup WebView
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setDefaultTextEncodingName("utf-8");
        
        // Allow mixed content (HTTP/HTTPS)
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Set WebViewClient to handle URL loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, "Loading URL: " + url);
                
                // Check if this is a success or cancel redirect
                if (url.contains(SUCCESS_URL_PATTERN)) {
                    handlePaymentSuccess(url);
                    return true;
                } else if (url.contains(CANCEL_URL_PATTERN)) {
                    handlePaymentCancel(url);
                    return true;
                }
                
                // Let WebView handle other URLs
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "Page started: " + url);
                
                // Check for success/cancel in URL
                if (url.contains(SUCCESS_URL_PATTERN)) {
                    handlePaymentSuccess(url);
                } else if (url.contains(CANCEL_URL_PATTERN)) {
                    handlePaymentCancel(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Page finished: " + url);
            }
        });

        // Set WebChromeClient for progress updates
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Load PayOS payment page
        Log.d(TAG, "Loading checkout URL: " + checkoutUrl);
        webView.loadUrl(checkoutUrl);
    }

    private void handlePaymentSuccess(String url) {
        Log.d(TAG, "Payment success detected: " + url);
        
        // Extract orderCode from URL if available
        String orderCode = extractOrderCodeFromUrl(url);
        
        // Close WebView and return to app
        Intent resultIntent = new Intent();
        resultIntent.putExtra("paymentStatus", "success");
        resultIntent.putExtra("orderId", orderId);
        if (orderCode != null) {
            resultIntent.putExtra("orderCode", orderCode);
        }
        
        setResult(RESULT_OK, resultIntent);
        
        // Navigate to payment result screen
        Intent paymentResultIntent = new Intent(this, PaymentResultActivity.class);
        if (orderCode != null) {
            paymentResultIntent.putExtra("orderCode", orderCode);
        }
        paymentResultIntent.putExtra("orderId", orderId);
        startActivity(paymentResultIntent);
        
        finish();
    }

    private void handlePaymentCancel(String url) {
        Log.d(TAG, "Payment cancelled detected: " + url);
        
        Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("paymentStatus", "cancelled");
        resultIntent.putExtra("orderId", orderId);
        
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    private String extractOrderCodeFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String orderCode = uri.getQueryParameter("orderCode");
            if (orderCode == null) {
                // Try orderId parameter
                orderCode = uri.getQueryParameter("orderId");
            }
            return orderCode;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting orderCode from URL", e);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Show confirmation dialog before canceling payment
            new android.app.AlertDialog.Builder(this)
                .setTitle("Hủy thanh toán?")
                .setMessage("Bạn có chắc chắn muốn hủy thanh toán?")
                .setPositiveButton("Hủy thanh toán", (dialog, which) -> {
                    handlePaymentCancel("");
                    super.onBackPressed();
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

