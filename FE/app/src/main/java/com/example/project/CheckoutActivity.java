package com.example.project;

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
    private EditText etReceiverName, etReceiverPhone, etShippingAddress;
    private RadioGroup rgPaymentMethod;
    private RecyclerView rvOrderItems;
    private TextView tvSubtotal, tvShippingFee, tvTotal;

    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> orderItems;
    private double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        loadOrderItems();
        setupRecyclerView();
        calculateTotal();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        etReceiverName = findViewById(R.id.etReceiverName);
        etReceiverPhone = findViewById(R.id.etReceiverPhone);
        etShippingAddress = findViewById(R.id.etShippingAddress);

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rvOrderItems = findViewById(R.id.rvOrderItems);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
    }

    private void loadOrderItems() {
        // Load cart items from intent or database
        // For now, using sample data
        orderItems = new ArrayList<>();

        orderItems.add(new CartItem(
            "Xe đạp điện VinFast Klara S",
            "Xe đạp điện thông minh",
            "29.990.000 VNĐ",
            R.drawable.splash_bike_background,
            1
        ));

        orderItems.add(new CartItem(
            "Xe đạp điện Yadea E3",
            "Thiết kế thời trang",
            "19.990.000 VNĐ",
            R.drawable.splash_bike_background,
            1
        ));

        orderItems.add(new CartItem(
            "Xe đạp điện Pega Cap A",
            "Tiết kiệm năng lượng",
            "15.000.000 VNĐ",
            R.drawable.splash_bike_background,
            1
        ));
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

        // Get selected payment method
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        String paymentMethod = "COD";

        if (selectedPaymentId == R.id.rbBankTransfer) {
            paymentMethod = "Chuyển khoản ngân hàng";
        } else if (selectedPaymentId == R.id.rbEWallet) {
            paymentMethod = "Ví điện tử";
        }

        // Show confirmation dialog
        showConfirmationDialog(receiverName, receiverPhone, shippingAddress, paymentMethod);
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

        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

        // Show success dialog
        new AlertDialog.Builder(this)
                .setTitle("Đặt Hàng Thành Công")
                .setMessage("Cảm ơn bạn đã đặt hàng!\n\nĐơn hàng của bạn đang được xử lý và sẽ được giao trong thời gian sớm nhất.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Return to home or order history
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}

