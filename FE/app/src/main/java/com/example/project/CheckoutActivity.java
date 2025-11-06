package com.example.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private CardView btnBack, btnConfirmOrder;
    private EditText etReceiverName, etReceiverPhone, etShippingAddress, etCity;
    private RadioGroup rgPaymentMethod;
    private RecyclerView rvOrderItems;
    private TextView tvSubtotal, tvShippingFee, tvTotal;

    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> orderItems;
    private double totalAmount = 0;
    private String storeId;
    private String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        prefillFromProfile();
        getStoreInfo();
        loadOrderItems();
        setupRecyclerView();
        calculateTotal();
        setupClickListeners();
    }

    private void prefillFromProfile() {
        try {
            com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
            com.example.project.models.User user = auth.getCurrentUser();
            if (user == null) return;

            // Name
            String name = null;
            if (user.getProfile() != null) {
                String fn = user.getProfile().getFirstName();
                String ln = user.getProfile().getLastName();
                if (fn != null || ln != null) {
                    name = ((fn==null?"":fn) + " " + (ln==null?"":ln)).trim();
                }
            }
            if ((name == null || name.isEmpty()) && user.getUsername() != null) {
                name = user.getUsername();
            }
            if (name != null && name.length() > 0 && etReceiverName.getText().toString().trim().isEmpty()) {
                etReceiverName.setText(name);
            }

            // Phone
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() && etReceiverPhone.getText().toString().trim().isEmpty()) {
                etReceiverPhone.setText(user.getPhoneNumber());
            }

            // Address and City (best-effort split by last comma)
            if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                String addr = user.getAddress();
                if (etShippingAddress.getText().toString().trim().isEmpty()) {
                    String main = addr;
                    String city = null;
                    int lastComma = addr.lastIndexOf(',');
                    if (lastComma > 0) {
                        main = addr.substring(0, lastComma).trim();
                        city = addr.substring(lastComma + 1).trim();
                    }
                    etShippingAddress.setText(main);
                    if (city != null && etCity.getText().toString().trim().isEmpty()) {
                        etCity.setText(city);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void getStoreInfo() {
        storeId = getIntent().getStringExtra("store_id");
        storeName = getIntent().getStringExtra("store_name");
        
        if (storeName != null) {
            setTitle("Thanh toán - " + storeName);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        etReceiverName = findViewById(R.id.etReceiverName);
        etReceiverPhone = findViewById(R.id.etReceiverPhone);
        etShippingAddress = findViewById(R.id.etShippingAddress);
        etCity = findViewById(R.id.etCity);

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rvOrderItems = findViewById(R.id.rvOrderItems);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
    }

    private void loadOrderItems() {
        // Load cart items from API for the selected store
        orderItems = new ArrayList<>();
        
        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        com.example.project.models.User user = auth.getCurrentUser();
        
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        api.getCartByUser(auth.getAuthHeader(), user.getId()).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    try {
                        java.util.Map dataMap = (java.util.Map) data;
                        java.util.List stores = (java.util.List) dataMap.get("itemsByStore");
                        if (stores != null) {
                            for (Object s : stores) {
                                java.util.Map store = (java.util.Map) s;
                                java.util.List storeItems = (java.util.List) store.get("items");
                                if (storeItems != null) {
                                    for (Object it : storeItems) {
                                        java.util.Map item = (java.util.Map) it;
                                        java.util.Map product = (java.util.Map) item.get("product");
                                        String name = String.valueOf(product.get("name"));
                                        double price = product.get("price") instanceof Number ? ((Number) product.get("price")).doubleValue() : 0;
                                        String priceText = formatPrice((long) price) + " VNĐ";
                                        int quantity = item.get("quantity") instanceof Number ? ((Number) item.get("quantity")).intValue() : 1;
                                        CartItem ci = new CartItem(name, "", priceText, R.drawable.splash_bike_background, quantity);
                                        orderItems.add(ci);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    checkoutAdapter.notifyDataSetChanged();
                    calculateTotal();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    private void setupRecyclerView() {
        checkoutAdapter = new CheckoutAdapter(orderItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOrderItems.setLayoutManager(layoutManager);
        rvOrderItems.setAdapter(checkoutAdapter);
    }

    private void calculateTotal() {
        totalAmount = 0;

        for (CartItem item : orderItems) {
            // Extract price from string (remove "VNĐ" and dots)
            String priceStr = item.getPrice().replace(" VNĐ", "").replace(".", "");
            try {
                double price = Double.parseDouble(priceStr);
                totalAmount += price * item.getQuantity();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Format currency
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedTotal = formatter.format(totalAmount) + " VNĐ";

        tvSubtotal.setText(formattedTotal);
        tvTotal.setText(formattedTotal);
        tvShippingFee.setText("Miễn phí");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void confirmOrder() {
        // Validate inputs
        String receiverName = etReceiverName.getText().toString().trim();
        String receiverPhone = etReceiverPhone.getText().toString().trim();
        String shippingAddress = etShippingAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();

        if (receiverName.isEmpty()) {
            etReceiverName.setError("Vui lòng nhập tên người nhận");
            etReceiverName.requestFocus();
            return;
        }

        if (receiverPhone.isEmpty()) {
            etReceiverPhone.setError("Vui lòng nhập số điện thoại");
            etReceiverPhone.requestFocus();
            return;
        }

        if (receiverPhone.length() < 10) {
            etReceiverPhone.setError("Số điện thoại không hợp lệ");
            etReceiverPhone.requestFocus();
            return;
        }

        if (shippingAddress.isEmpty()) {
            etShippingAddress.setError("Vui lòng nhập địa chỉ giao hàng");
            etShippingAddress.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("Vui lòng nhập thành phố");
            etCity.requestFocus();
            return;
        }

        // Get selected payment method
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        String paymentMethod = "cash"; // Default
        
        if (selectedPaymentId == R.id.rbCOD) {
            paymentMethod = "cash";
        } else if (selectedPaymentId == R.id.rbBankTransfer) {
            paymentMethod = "bank_transfer";
        } else if (selectedPaymentId == R.id.rbPayOS) {
            paymentMethod = "payos";
        }

        // Show confirmation dialog
        showConfirmationDialog(receiverName, receiverPhone, shippingAddress + "\n" + city, paymentMethod);
    }

    private void showConfirmationDialog(String name, String phone, String address, String paymentMethod) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedTotal = formatter.format(totalAmount) + " VNĐ";

        String message = "Người nhận: " + name + "\n" +
                "Số điện thoại: " + phone + "\n" +
                "Địa chỉ: " + address + "\n" +
                "Phương thức thanh toán: " + paymentMethod + "\n" +
                "Tổng tiền: " + formattedTotal + "\n\n" +
                "Xác nhận đặt hàng?";

        new AlertDialog.Builder(this)
                .setTitle("Xác Nhận Đơn Hàng")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Process order
                    processOrder(name, phone, address, paymentMethod);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void processOrder(String name, String phone, String address, String paymentMethod) {
        // TODO: Save order to database
        // TODO: Clear cart
        // TODO: Send notification/email

        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        com.example.project.models.User user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("userId", user.getId());
        java.util.Map<String, Object> shipping = new java.util.HashMap<>();
        shipping.put("fullName", name);
        shipping.put("phone", phone);
        shipping.put("address", address);
        shipping.put("city", etCity.getText().toString().trim());
        body.put("shippingAddress", shipping);
        body.put("paymentMethod", paymentMethod);
        body.put("notes", "");

        api.createOrders(auth.getAuthHeader(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        Object data = response.body().getData();
                        if (data instanceof java.util.Map) {
                            java.util.Map d = (java.util.Map) data;
                            Object ordersObj = d.get("orders");
                            
                            if (ordersObj instanceof java.util.List) {
                                java.util.List orders = (java.util.List) ordersObj;
                                
                                // Nếu paymentMethod là payos, tạo payment link và redirect
                                if ("payos".equals(paymentMethod) && orders.size() > 0) {
                                    // Lấy order đầu tiên (hoặc có thể xử lý nhiều orders)
                                    Object firstOrder = orders.get(0);
                                    if (firstOrder instanceof java.util.Map) {
                                        java.util.Map orderMap = (java.util.Map) firstOrder;
                                        Object orderObj = orderMap.get("order");
                                        if (orderObj instanceof java.util.Map) {
                                            Object orderId = ((java.util.Map) orderObj).get("_id");
                                            if (orderId != null) {
                                                createPaymentLinkAndRedirect(orderId.toString(), name, phone, address);
                                                return;
                                            }
                                        }
                                    }
                                }
                                
                                // Nếu không phải payos hoặc lỗi, hiển thị thông báo thông thường
                                Object summaryObj = d.get("summary");
                                String message = "Đơn hàng đã được tạo thành công.";
                                if (summaryObj instanceof java.util.Map) {
                                    java.util.Map summary = (java.util.Map) summaryObj;
                                    Object totalOrders = summary.get("totalOrders");
                                    Object totalAmount = summary.get("totalAmount");
                                    message = "Tạo " + String.valueOf(totalOrders) + " đơn hàng. Tổng: " + formatCurrency(totalAmount) + " VNĐ";
                                }
                                
                                new AlertDialog.Builder(CheckoutActivity.this)
                                    .setTitle("Thành công")
                                    .setMessage(message)
                                    .setPositiveButton("OK", (dialog, which) -> finish())
                                    .setCancelable(false)
                                    .show();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    // Fallback message
                    new AlertDialog.Builder(CheckoutActivity.this)
                        .setTitle("Thành công")
                        .setMessage("Đơn hàng đã được tạo thành công.")
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                } else {
                    String err = "Tạo đơn hàng thất bại. Vui lòng thử lại!";
                    try {
                        if (response.errorBody() != null) {
                            String eb = response.errorBody().string();
                            if (eb.contains("Thiếu tồn kho")) {
                                err = "Cửa hàng không còn đủ xe cho một số sản phẩm.";
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CheckoutActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Không thể tạo đơn hàng. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String formatCurrency(Object v) {
        long val = 0;
        if (v instanceof Number) val = ((Number) v).longValue();
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        return fmt.format(val);
    }
    
    /**
     * Tạo payment link và redirect user đến PayOS
     */
    private void createPaymentLinkAndRedirect(String orderId, String name, String phone, String address) {
        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        
        // Tạo returnUrl và cancelUrl
        // Sử dụng BASE_URL từ backend (từ .env) 
        // Lưu ý: PayOS cần URL công khai (HTTP/HTTPS), không thể dùng localhost
        // Trong production, cần sử dụng URL thật từ backend (.env FRONTEND_URL hoặc BASE_URL)
        // PayOS sẽ redirect về returnUrl với query param orderCode
        String returnUrl = "https://folding-html-jeans-probability.trycloudflare.com/payment/success";
        String cancelUrl = "https://folding-html-jeans-probability.trycloudflare.com/payment/cancel";
        
        // Hoặc sử dụng deep link (cần cấu hình trong AndroidManifest.xml)
        // String packageName = getPackageName();
        // returnUrl = packageName + "://payment/success?orderCode={orderCode}";
        // cancelUrl = packageName + "://payment/cancel?orderCode={orderCode}";
        
        java.util.Map<String, Object> paymentBody = new java.util.HashMap<>();
        paymentBody.put("returnUrl", returnUrl);
        paymentBody.put("cancelUrl", cancelUrl);
        
        // Show loading
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang tạo link thanh toán...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        api.createPaymentLink(auth.getAuthHeader(), orderId, paymentBody).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                progressDialog.dismiss();
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        Object data = response.body().getData();
                        if (data instanceof java.util.Map) {
                            java.util.Map paymentData = (java.util.Map) data;
                            Object checkoutUrl = paymentData.get("checkoutUrl");
                            
                            if (checkoutUrl != null) {
                                // Mở WebView activity trong app để thanh toán PayOS
                                Intent paymentIntent = new Intent(CheckoutActivity.this, PayOSPaymentActivity.class);
                                paymentIntent.putExtra("checkoutUrl", checkoutUrl.toString());
                                paymentIntent.putExtra("orderId", orderId);
                                
                                // Start activity and wait for result
                                startActivityForResult(paymentIntent, 1001);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(CheckoutActivity.this, "Không thể tạo link thanh toán", Toast.LENGTH_SHORT).show();
                } else {
                    String err = "Không thể tạo link thanh toán. Vui lòng thử lại!";
                    try {
                        if (response.errorBody() != null) {
                            String eb = response.errorBody().string();
                            // Parse error message if needed
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CheckoutActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK && data != null) {
                String paymentStatus = data.getStringExtra("paymentStatus");
                String orderId = data.getStringExtra("orderId");
                
                if ("success".equals(paymentStatus)) {
                    // Payment successful - PaymentResultActivity đã được mở từ PayOSPaymentActivity
                    // Có thể finish activity này hoặc show message
                    Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if ("cancelled".equals(paymentStatus)) {
                    // Payment cancelled
                    Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled payment
                Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

