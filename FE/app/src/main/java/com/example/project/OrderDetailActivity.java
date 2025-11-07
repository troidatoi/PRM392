package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import com.example.project.utils.AuthManager;
import com.example.project.models.ApiResponse;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.bumptech.glide.Glide;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import android.view.ViewGroup;
import static com.example.project.Order.mapStatusColor;
import static com.example.project.Order.mapStatusText;
import android.widget.Button;

public class OrderDetailActivity extends AppCompatActivity {

    private CardView btnBack, btnContactSupport;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvPaymentMethod;
    private TextView tvReceiverName, tvReceiverPhone, tvShippingAddress;
    private TextView tvSubtotal, tvShippingFee, tvTotal;
    private RecyclerView rvOrderItems;
    private View loadingView;
    private Button btnConfirmOrder, btnShipOrder, btnDelivered, btnCancelOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_new);

        initViews();
        setupListeners();
        loadOrderData();
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack);
        btnContactSupport = findViewById(R.id.btnContactSupport);

        // Order Info
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);

        // Shipping Address
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);

        // Order Summary
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);

        // RecyclerView
        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        loadingView = findViewById(R.id.progressBarLoading);
        if (loadingView == null) {
            loadingView = new android.widget.ProgressBar(this);
        }
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnShipOrder = findViewById(R.id.btnShipOrder);
        btnDelivered = findViewById(R.id.btnDelivered);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement contact support functionality
                // For example: open chat, call support, or send email
            }
        });

        // Admin: Xác nhận đơn hàng (Pending -> Confirmed)
        btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus("confirmed");
            }
        });

        // Admin: Giao hàng (Confirmed -> Shipped)
        btnShipOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus("shipped");
            }
        });

        // User: Xác nhận đã nhận hàng (Shipped -> Delivered)
        btnDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus("delivered");
            }
        });

        // User: Hủy đơn hàng
        btnCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelOrderDialog();
            }
        });
    }

    private void loadOrderData() {
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            finish();
            return;
        }
        showLoading(true);
        AuthManager auth = AuthManager.getInstance(this);
        ApiService api = RetrofitClient.getInstance().getApiService();
        api.getOrderDetails(auth.getAuthHeader(), orderId).enqueue(new retrofit2.Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Object>> call, retrofit2.Response<ApiResponse<Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        Object d = response.body().getData();
                        if (!(d instanceof java.util.Map)) return;
                        java.util.Map m = (java.util.Map) d;
                        String id = val(m,"_id");
                        String orderNumber = val(m, "orderNumber");
                        String date = formatDate(val(m, "orderDate"));
                        String status = val(m,"orderStatus");
                        String payment = val(m, "paymentMethod");
                        String note = val(m, "notes");
                        String displayNumber = orderNumber;
                        if (displayNumber == null || displayNumber.isEmpty()) {
                            displayNumber = id != null && id.length() > 6 ? id.substring(id.length()-6) : id;
                        }
                        tvOrderId.setText("#" + displayNumber);
                        tvOrderDate.setText(date);
                        tvOrderStatus.setText(mapStatusText(status));
                        tvOrderStatus.setTextColor(android.graphics.Color.parseColor(mapStatusColor(status)));
                        tvPaymentMethod.setText(mapPaymentText(payment));
                        // Địa chỉ giao hàng
                        Object ship = m.get("shippingAddress");
                        if (ship instanceof java.util.Map) {
                            java.util.Map s = (java.util.Map)ship;
                            tvReceiverName.setText(val(s, "fullName"));
                            tvReceiverPhone.setText(val(s, "phone"));
                            tvShippingAddress.setText(val(s, "address")+", "+val(s,"city"));
                        }
                        // Tổng tiền - totalAmount = tổng sản phẩm, shippingFee = phí ship, finalAmount = tổng cộng
                        long shippingFee = valN(m, "shippingFee");
                        long totalAmountValue = valN(m, "totalAmount");
                        long finalAmountValue = valN(m, "finalAmount");
                        
                        // Nếu finalAmount chưa có, tính từ totalAmount + shippingFee
                        if (finalAmountValue == 0 && totalAmountValue > 0) {
                            finalAmountValue = totalAmountValue + shippingFee;
                        }
                        
                        // Hiển thị: Tạm tính = tổng sản phẩm (totalAmount - shippingFee nếu đã tính)
                        // Hoặc nếu totalAmount đã là tổng sản phẩm, thì dùng trực tiếp
                        long subtotalValue = totalAmountValue;
                        if (finalAmountValue > 0 && shippingFee > 0) {
                            // Nếu có finalAmount và shippingFee, tính lại subtotal
                            subtotalValue = finalAmountValue - shippingFee;
                        }
                        
                        tvSubtotal.setText(formatCurrency(subtotalValue));
                        tvShippingFee.setText(shippingFee > 0 ? formatCurrency(shippingFee) : "Miễn phí");
                        tvTotal.setText(formatCurrency(finalAmountValue > 0 ? finalAmountValue : (subtotalValue + shippingFee)));
                        // Products
                        Object details = m.get("orderDetails");
                        if (details instanceof java.util.List) {
                            List<OrderItemModel> list = new ArrayList<>();
                            for (Object o : (java.util.List)details) {
                                if (!(o instanceof java.util.Map)) continue;
                                java.util.Map md = (java.util.Map) o;
                                Object prod = md.get("product");
                                String prodName = ""; String img = ""; long price = 0;
                                if (prod instanceof java.util.Map) {
                                    java.util.Map pm = (java.util.Map)prod;
                                    prodName = val(pm, "name");
                                    if (pm.get("images") instanceof java.util.List && !((java.util.List)pm.get("images")).isEmpty()) {
                                        img = String.valueOf(((java.util.List)pm.get("images")).get(0));
                                    }
                                    try { price = (long) Double.parseDouble(val(pm,"price")); } catch(Exception ignored){}
                                }
                                int qty = 1;
                                try{ qty = (int) Double.parseDouble(val(md,"quantity")); }catch(Exception ignored){}
                                long total = valN(md,"totalPrice");
                                list.add(new OrderItemModel(prodName, img, price, qty, total));
                            }
                            OrderProductAdapter adapter = new OrderProductAdapter(list, OrderDetailActivity.this);
                            rvOrderItems.setAdapter(adapter);
                        }
                        // Timeline update:
                        updateTimeline(status, val(m, "orderDate"), val(m, "updatedAt"));
                        AuthManager auth = AuthManager.getInstance(OrderDetailActivity.this);
                        boolean isAdmin = auth.getCurrentUser() != null && "admin".equalsIgnoreCase(auth.getCurrentUser().getRole());
                        if (isAdmin) {
                            // Admin: Hiển thị các nút thay đổi trạng thái, ẩn nút liên hệ hỗ trợ
                            btnContactSupport.setVisibility(View.GONE);
                            if ((status.equalsIgnoreCase("pending") || mapStatusText(status).equals("Chờ xác nhận"))) {
                                btnConfirmOrder.setVisibility(View.VISIBLE);
                            } else { btnConfirmOrder.setVisibility(View.GONE); }
                            if ((status.equalsIgnoreCase("confirmed") || mapStatusText(status).equals("Đã xác nhận"))) {
                                btnShipOrder.setVisibility(View.VISIBLE);
                            } else { btnShipOrder.setVisibility(View.GONE); }
                            // Admin có thể xác nhận đã giao hàng khi đơn đang giao
                            if ((status.equalsIgnoreCase("shipped") || mapStatusText(status).equals("Đang giao hàng"))) {
                                btnDelivered.setVisibility(View.VISIBLE);
                            } else { btnDelivered.setVisibility(View.GONE); }
                            // Admin không có nút hủy
                            btnCancelOrder.setVisibility(View.GONE);
                        } else {
                            // User: Ẩn nút liên hệ hỗ trợ
                            btnContactSupport.setVisibility(View.GONE);
                            if ((status.equalsIgnoreCase("shipped") || mapStatusText(status).equals("Đang giao hàng"))) {
                                btnDelivered.setVisibility(View.VISIBLE);
                            } else { btnDelivered.setVisibility(View.GONE); }
                            btnConfirmOrder.setVisibility(View.GONE);
                            btnShipOrder.setVisibility(View.GONE);
                            // User có thể hủy đơn khi đơn đang ở trạng thái Pending hoặc Confirmed
                            if ((status.equalsIgnoreCase("pending") || mapStatusText(status).equals("Chờ xác nhận") ||
                                status.equalsIgnoreCase("confirmed") || mapStatusText(status).equals("Đã xác nhận"))) {
                                btnCancelOrder.setVisibility(View.VISIBLE);
                            } else {
                                btnCancelOrder.setVisibility(View.GONE);
                            }
                        }
                    } catch(Exception e) {
                        showErr("Lỗi dữ liệu đơn hàng. Thử lại sau.");
                    }
                } else {
                    showErr("Không tìm thấy đơn hàng.");
                }
            }
            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Object>> call, Throwable t) {
                showLoading(false);
                showErr("Lỗi mạng. Vui lòng thử lại!");
            }
        });
    }
    private void showLoading(boolean show) {
        if (loadingView == null) return;
        loadingView.setVisibility(show?View.VISIBLE:View.GONE);
    }
    private void showErr(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show();
        new Handler().postDelayed(this::finish, 1200);
    }

    // Thay đổi trạng thái đơn hàng
    private void updateOrderStatus(String newStatus) {
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            android.widget.Toast.makeText(this, "Không tìm thấy ID đơn hàng", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog xác nhận
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn thay đổi trạng thái đơn hàng?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    performUpdateOrderStatus(orderId, newStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performUpdateOrderStatus(String orderId, String newStatus) {
        showLoading(true);
        AuthManager auth = AuthManager.getInstance(this);
        ApiService api = RetrofitClient.getInstance().getApiService();

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("status", newStatus);

        api.updateOrderStatus(auth.getAuthHeader(), orderId, body).enqueue(new retrofit2.Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Object>> call, retrofit2.Response<ApiResponse<Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    android.widget.Toast.makeText(OrderDetailActivity.this, "Cập nhật trạng thái thành công!", android.widget.Toast.LENGTH_SHORT).show();
                    // Reload order data để hiển thị trạng thái mới
                    loadOrderData();
                } else {
                    android.widget.Toast.makeText(OrderDetailActivity.this, "Cập nhật thất bại. Vui lòng thử lại!", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Object>> call, Throwable t) {
                showLoading(false);
                android.widget.Toast.makeText(OrderDetailActivity.this, "Lỗi mạng: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị dialog nhập lý do hủy đơn hàng
    private void showCancelOrderDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập lý do hủy đơn hàng");
        input.setMinLines(3);
        input.setMaxLines(5);
        input.setPadding(50, 30, 50, 30);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc muốn hủy đơn hàng này?")
                .setView(input)
                .setPositiveButton("Hủy đơn hàng", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        android.widget.Toast.makeText(OrderDetailActivity.this, "Vui lòng nhập lý do hủy đơn", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    performCancelOrder(reason);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    // Thực hiện hủy đơn hàng
    private void performCancelOrder(String reason) {
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            android.widget.Toast.makeText(this, "Không tìm thấy ID đơn hàng", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        AuthManager auth = AuthManager.getInstance(this);
        ApiService api = RetrofitClient.getInstance().getApiService();

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("reason", reason);

        api.cancelOrder(auth.getAuthHeader(), orderId, body).enqueue(new retrofit2.Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Object>> call, retrofit2.Response<ApiResponse<Object>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    android.widget.Toast.makeText(OrderDetailActivity.this, "Hủy đơn hàng thành công!", android.widget.Toast.LENGTH_SHORT).show();
                    // Reload order data để hiển thị trạng thái mới
                    loadOrderData();
                } else {
                    String errorMsg = "Hủy đơn hàng thất bại. Vui lòng thử lại!";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    android.widget.Toast.makeText(OrderDetailActivity.this, errorMsg, android.widget.Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Object>> call, Throwable t) {
                showLoading(false);
                android.widget.Toast.makeText(OrderDetailActivity.this, "Lỗi mạng: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String val(java.util.Map m, String key) { Object v = m.get(key); return v == null ? "" : String.valueOf(v); }
    private long valN(java.util.Map m, String... keys) {
        for (String key : keys) {
            Object v = m.get(key);
            if (v instanceof Number) return ((Number)v).longValue();
            if (v instanceof String) try{return Long.parseLong((String)v);}catch(Exception ignored){}
        }
        return 0;
    }
    private String formatDate(String iso) {
        try { OffsetDateTime odt = OffsetDateTime.parse(iso); return odt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));} catch (Exception e) {return iso;}
    }
    private String formatCurrency(long v) { java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new Locale("vi", "VN")); return fmt.format(v) + " ₫"; }
    private String statusToColor(String status) { if (status==null) return "#2196F3"; switch(status.toLowerCase()) {case "pending":return "#FFC107";case "confirmed":return "#64B5F6";case "shipped":return "#2196F3";case "delivered":return "#4CAF50";case "cancelled":return "#F44336";default:return "#2196F3";} }
    private String mapStatusText(String st) { if (st==null) return ""; switch(st.toLowerCase()) {case "pending":return "Chờ xác nhận";case "confirmed":return "Đã xác nhận";case "shipped":return "Đang giao";case "delivered":return "✓ Đã giao";case "cancelled":return "Đã hủy";default:return st;} }
    private String mapPaymentText(String p) { if (p==null) return ""; if (p.toLowerCase().contains("vnpay")) return "VNPay"; if (p.toLowerCase().contains("cod")) return "💵 COD"; return p;}

    private void updateTimeline(String status, String created, String updated) {
        String[] keys = {"pending", "confirmed", "shipped", "delivered", "cancelled"};
        int idxActive = 0;
        for(int i=0;i<keys.length;i++) if(keys[i].equalsIgnoreCase(status)) idxActive = i;
        int[] dotIds = {R.id.dotPending, R.id.dotConfirmed, R.id.dotShipped, R.id.dotDelivered, R.id.dotCancelled};
        int[] tvIds = {R.id.tvStatusPending, R.id.tvStatusConfirmed, R.id.tvStatusShipped, R.id.tvStatusDelivered, R.id.tvStatusCancelled};
        for (int i = 0; i < 5; i++) {
            android.widget.TextView tv = findViewById(tvIds[i]);
            View dot = findViewById(dotIds[i]);
            if (tv == null || dot == null) continue;
            if (i == idxActive) {
                tv.setTextColor(android.graphics.Color.parseColor(Order.mapStatusColor(keys[i])));
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
                dot.setBackgroundTintList(getColorState(Order.mapStatusColor(keys[i])));
            } else {
                tv.setTextColor(android.graphics.Color.parseColor("#BDBDBD"));
                tv.setTypeface(null, android.graphics.Typeface.NORMAL);
                dot.setBackgroundTintList(getColorState("#BDBDBD"));
            }
        }
    }
    private android.content.res.ColorStateList getColorState(String s) {
        return android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(s));
    }
    // Tìm text hiển thị trạng thái trong timeline: truyền text hashcode cho từng dòng
    private android.widget.TextView findTextTimeline(int h) {
        java.util.ArrayList<android.widget.TextView> all = new java.util.ArrayList<>();
        android.view.ViewGroup root = findViewById(android.R.id.content);
        findAllTextViews(all, root);
        for (android.widget.TextView t : all) {
            if (t.getText() == null) continue;
            String txt = t.getText().toString().trim();
            if (txt.equals("Chờ xác nhận") || txt.equals("Đã xác nhận") || txt.equals("Đang giao hàng") || txt.equals("Đã giao") || txt.equals("Đã hủy")) return t;
            if (t.getText().hashCode() == h) return t;
        }
        return null;
    }
    private void findAllTextViews(java.util.List<android.widget.TextView> acc, android.view.View v) {
        if (v instanceof android.widget.TextView) acc.add((android.widget.TextView)v);
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup)v;
            for (int i=0; i<vg.getChildCount(); ++i) findAllTextViews(acc, vg.getChildAt(i));
        }
    }
    private void setTimelineDate(int hash, String iso) {
        android.widget.TextView t = findTextTimeline(hash);
        if (t == null) return;
        if (iso == null || iso.isEmpty()) { t.setVisibility(android.view.View.GONE); return; }
        t.setVisibility(android.view.View.VISIBLE);
        t.setText(formatDate(iso));
    }
    private android.widget.TextView findTimelineLabel(String label) {
        java.util.ArrayList<android.widget.TextView> all = new java.util.ArrayList<>();
        android.view.ViewGroup root = findViewById(android.R.id.content);
        findAllTextViews(all, root);
        for(android.widget.TextView t: all) {
            if(t.getText()!=null && t.getText().toString().trim().contains(label)) return t;
        }
        return null;
    }
}


// Định nghĩa dữ liệu sản phẩm cho adapter:
class OrderItemModel {
    public String name, image;
    public long price, total;
    public int qty;
    public OrderItemModel(String name, String image, long price, int qty, long total) {
        this.name = name; this.image = image; this.price = price; this.qty = qty; this.total = total;
    }
}
// Adapter sản phẩm cho đơn hàng
class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.Holder> {
    private final java.util.List<OrderItemModel> items;
    private final android.content.Context ctx;
    public OrderProductAdapter(java.util.List<OrderItemModel> items, android.content.Context ctx) {this.items=items; this.ctx=ctx;}
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_checkout_product, parent, false);
        return new Holder(v); }
    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        OrderItemModel it = items.get(i);
        ((TextView)h.itemView.findViewById(R.id.tvProductName)).setText(it.name);
        ((TextView)h.itemView.findViewById(R.id.tvProductQuantity)).setText("x"+it.qty);
        ((TextView)h.itemView.findViewById(R.id.tvProductPrice)).setText(formatCurrency(it.price));
        ((TextView)h.itemView.findViewById(R.id.tvProductTotal)).setText(formatCurrency(it.total));
        android.widget.ImageView iv = h.itemView.findViewById(R.id.ivProductImage);
        if(it.image!=null&&it.image.length()>10) Glide.with(ctx).load(it.image).into(iv); else iv.setImageResource(R.drawable.splash_bike_background);
    }
    private String formatCurrency(long v) { java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new Locale("vi", "VN")); return fmt.format(v) + " ₫"; }
    @Override public int getItemCount() {return items.size();}
    static class Holder extends RecyclerView.ViewHolder {public Holder(@NonNull View v){super(v);}}
}

