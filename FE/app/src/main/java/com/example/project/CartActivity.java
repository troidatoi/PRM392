package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.project.adapters.StoreCartAdapter;

@SuppressWarnings("deprecation")
public class CartActivity extends AppCompatActivity implements StoreCartAdapter.OnCartItemListener {

    private RecyclerView rvCartItems;
    private StoreCartAdapter storeCartAdapter;
    private List<Object> displayItems = new ArrayList<>();
    private List<CartItem> cartItems;
    private List<StoreGroup> storeGroups;

    private TextView tvItemCount, tvBadge, tvSubtotal, tvTotal;
    private CardView emptyCartCard, btnCheckout;

    // Bottom Navigation
    private View navHome, navProducts, navCart, navAccount;
    private ImageView iconHome, iconProducts, iconCart, iconAccount;
    private TextView tvHome, tvProducts, tvCart, tvAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        loadCartFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload giỏ hàng sau khi quay lại từ Checkout (cart có thể đã bị xóa)
        loadCartFromApi();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvBadge = findViewById(R.id.tvBadge);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        emptyCartCard = findViewById(R.id.emptyCartCard);
        btnCheckout = findViewById(R.id.btnCheckout);

        // Bottom Navigation
        navHome = findViewById(R.id.navHome);
        navProducts = findViewById(R.id.navProducts);
        navCart = findViewById(R.id.navCart);
        navAccount = findViewById(R.id.navAccount);


        iconHome = findViewById(R.id.iconHome);
        iconProducts = findViewById(R.id.iconProducts);
        iconCart = findViewById(R.id.iconCart);
        iconAccount = findViewById(R.id.iconAccount);

        tvHome = findViewById(R.id.tvHome);
        tvProducts = findViewById(R.id.tvProducts);
        tvCart = findViewById(R.id.tvCart);
        tvAccount = findViewById(R.id.tvAccount);

        btnCheckout.setVisibility(View.VISIBLE);
        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            // Không truyền store_id để checkout tổng
            startActivity(intent);
        });
    }

    private static class StoreGroup {
        String storeId;
        String storeName;
        long storeTotal;
        List<CartItem> items = new ArrayList<>();
    }


    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        storeGroups = new ArrayList<>();
        displayItems = new ArrayList<>();
        storeCartAdapter = new StoreCartAdapter(displayItems, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCartItems.setLayoutManager(layoutManager);
        rvCartItems.setAdapter(storeCartAdapter);
    }

    private void loadCartFromApi() {
        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        com.example.project.models.User user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getCartByUser(auth.getAuthHeader(), user.getId()).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    // Parse tối thiểu: lấy cart.itemCount và items của từng store
                    int itemCount = 0;
                    java.util.List<CartItem> items = new java.util.ArrayList<>();
                    java.util.List<StoreGroup> groups = new java.util.ArrayList<>();
                    try {
                        java.util.Map dataMap = (java.util.Map) data;
                        java.util.Map cartMap = (java.util.Map) dataMap.get("cart");
                        if (cartMap != null && cartMap.get("itemCount") instanceof Number) {
                            itemCount = ((Number) cartMap.get("itemCount")).intValue();
                        }
                        java.util.List stores = (java.util.List) dataMap.get("itemsByStore");
                        if (stores != null) {
                            for (Object s : stores) {
                                java.util.Map store = (java.util.Map) s;
                                StoreGroup group = new StoreGroup();
                                
                                // Lấy storeId và storeName
                                String outerStoreId = null;
                                try {
                                    Object sidOuter = store.get("storeId");
                                    if (sidOuter == null) sidOuter = store.get("_id");
                                    if (sidOuter == null) sidOuter = store.get("id");
                                    if (sidOuter == null) {
                                        Object storeObj = store.get("store");
                                        if (storeObj instanceof java.util.Map) {
                                            Object sId = ((java.util.Map) storeObj).get("_id");
                                            if (sId == null) sId = ((java.util.Map) storeObj).get("id");
                                            if (sId != null) sidOuter = sId;
                                            Object sName = ((java.util.Map) storeObj).get("name");
                                            if (sName != null) group.storeName = String.valueOf(sName);
                                        }
                                    }
                                    if (sidOuter != null) outerStoreId = String.valueOf(sidOuter);
                                } catch (Exception ignored) {}
                                group.storeId = outerStoreId;
                                
                                // Lấy storeTotal
                                try {
                                    Object st = store.get("storeTotal");
                                    if (st instanceof Number) group.storeTotal = ((Number) st).longValue();
                                } catch (Exception ignored) {}
                                
                                java.util.List storeItems = (java.util.List) store.get("items");
                                if (storeItems == null) {
                                    Object altItems = store.get("cartItems");
                                    if (altItems instanceof java.util.List) {
                                        storeItems = (java.util.List) altItems;
                                    }
                                }
                                if (storeItems != null) {
                                    for (Object it : storeItems) {
                                        try {
                                            java.util.Map item = (java.util.Map) it;
                                            Object productObj = item.get("product");
                                            if (productObj == null) productObj = item.get("bike");
                                            String name = "Sản phẩm";
                                            double price = 0;
                                            String productIdStr = null;

                                            if (productObj instanceof java.util.Map) {
                                                java.util.Map product = (java.util.Map) productObj;
                                                Object n = product.get("name");
                                                if (n != null) name = String.valueOf(n);
                                                Object p = product.get("price");
                                                if (p instanceof Number) price = ((Number) p).doubleValue();
                                                Object pid = product.get("_id");
                                                if (pid == null) pid = product.get("id");
                                                if (pid != null) productIdStr = String.valueOf(pid);
                                            } else {
                                                // Fallback: lấy từ trường trên item
                                                Object n2 = item.get("productName");
                                                if (n2 != null) name = String.valueOf(n2);
                                                Object p2 = item.get("unitPrice");
                                                if (p2 instanceof Number) price = ((Number) p2).doubleValue();
                                                Object pid2 = item.get("productId");
                                                if (pid2 != null) productIdStr = String.valueOf(pid2);
                                            }

                                            String priceText = formatPrice((long) price) + " VNĐ";
                                            int quantity = item.get("quantity") instanceof Number ? ((Number) item.get("quantity")).intValue() : 1;
                                            CartItem ci = new CartItem(name, "", priceText, R.drawable.splash_bike_background, quantity);

                                            // Lưu các ID cần thiết cho API calls
                                            Object itemId = item.get("_id");
                                            if (itemId != null) ci.setItemId(String.valueOf(itemId));
                                            if (productIdStr != null) ci.setProductId(productIdStr);
                                            if (outerStoreId != null) ci.setStoreId(outerStoreId);
                                            ci.setUnitPrice((long) price);

                                            items.add(ci);
                                            group.items.add(ci);
                                        } catch (Exception ignored) {}
                                    }
                                }
                                groups.add(group);
                            }
                        }
                    } catch (Exception ignore) {}

                    cartItems.clear();
                    cartItems.addAll(items);
                    storeGroups.clear();
                    storeGroups.addAll(groups);
                    
                    // Tạo danh sách hiển thị theo từng cửa hàng
                    displayItems.clear();
                    for (StoreGroup group : groups) {
                        // Thêm header cửa hàng
                        displayItems.add(new StoreCartAdapter.StoreHeader(
                            group.storeId,
                            group.storeName != null ? group.storeName : "Cửa hàng", 
                            group.storeTotal
                        ));
                        // Thêm các item của cửa hàng
                        displayItems.addAll(group.items);
                    }
                    
                    storeCartAdapter.notifyDataSetChanged();
                    updateCartSummary();
                    tvItemCount.setText(itemCount + " sản phẩm");
                    tvBadge.setText(String.valueOf(itemCount));
                } else {
                    updateCartSummary();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    

    private void setupBottomNavigation() {
        // Set Cart as selected by default
        selectNavItem(iconCart, tvCart);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navProducts.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ShopActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navCart.setOnClickListener(v -> {
            selectNavItem(iconCart, tvCart);
            deselectNavItem(iconHome, tvHome);
            deselectNavItem(iconProducts, tvProducts);
            deselectNavItem(iconAccount, tvAccount);
        });

        navAccount.setOnClickListener(v -> {
            // Open Account Activity
            Intent intent = new Intent(CartActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }

    private void selectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#2196F3"));
        text.setTextColor(Color.parseColor("#2196F3"));
    }

    private void deselectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#666666"));
        text.setTextColor(Color.parseColor("#666666"));
    }

    private void loadCartItems() {
        // Sample cart items
        cartItems.add(new CartItem(
            "Xe đạp điện VinFast Klara S",
            "Xe đạp điện thông minh",
            "29.990.000 VNĐ",
            R.drawable.splash_bike_background,
            1
        ));

        cartItems.add(new CartItem(
            "Xe đạp điện Yadea E3",
            "Công nghệ Nhật Bản",
            "25.500.000 VNĐ",
            R.drawable.splash_bike_background,
            2
        ));

        cartItems.add(new CartItem(
            "Xe đạp điện Dibao Type R",
            "Thiết kế thể thao",
            "32.000.000 VNĐ",
            R.drawable.splash_bike_background,
            1
        ));

        storeCartAdapter.notifyDataSetChanged();
        checkEmptyCart();
    }

    private void updateCartSummary() {
        int totalItems = 0;
        long subtotal = 0;

        // Tính toán từ displayItems (chỉ lấy CartItem, bỏ qua StoreHeader)
        for (Object item : displayItems) {
            if (item instanceof CartItem) {
                CartItem cartItem = (CartItem) item;
                totalItems += cartItem.getQuantity();
                // Only count selected items in subtotal
                if (cartItem.isSelected()) {
                    subtotal += cartItem.getTotalPrice();
                }
            }
        }

        // Update UI
        tvItemCount.setText(totalItems + " sản phẩm");
        tvBadge.setText(String.valueOf(totalItems));

        String formattedSubtotal = formatPrice(subtotal);
        tvSubtotal.setText(formattedSubtotal);
        tvTotal.setText(formattedSubtotal);
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " VNĐ";
    }

    private void checkEmptyCart() {
        if (cartItems.isEmpty()) {
            emptyCartCard.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            tvItemCount.setText("0 sản phẩm");
            tvBadge.setText("0");
            btnCheckout.setEnabled(false);
            btnCheckout.setAlpha(0.5f);
        } else {
            emptyCartCard.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(true);
            btnCheckout.setAlpha(1f);
        }
    }

    @Override
    public void onQuantityChanged(int position, int delta) {
        if (position < 0 || position >= displayItems.size()) return;
        
        Object item = displayItems.get(position);
        if (!(item instanceof CartItem)) return;
        
        CartItem cartItem = (CartItem) item;
        int newQuantity = cartItem.getQuantity() + delta;
        
        if (newQuantity <= 0) {
            onItemRemoved(position);
            return;
        }
        
        // Nếu tăng số lượng (delta > 0), kiểm tra stock trước
        if (delta > 0) {
            checkStockAndUpdateQuantity(cartItem, newQuantity, position);
        } else {
            // Giảm số lượng thì cập nhật trực tiếp
            updateQuantityDirectly(cartItem, newQuantity, position);
        }
    }
    
    private void checkStockAndUpdateQuantity(CartItem cartItem, int newQuantity, int position) {
        if (cartItem.getProductId() == null || cartItem.getStoreId() == null) {
            Toast.makeText(this, "Thiếu thông tin sản phẩm/cửa hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("productId", cartItem.getProductId());
        body.put("storeId", cartItem.getStoreId());
        body.put("quantity", newQuantity);
        
        api.checkAvailability(auth.getAuthHeader(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Stock đủ, cập nhật số lượng
                    updateQuantityDirectly(cartItem, newQuantity, position);
                } else {
                    // Hiển thị thông báo thân thiện cho khách hàng
                    String errorMsg = "Cửa hàng không còn đủ xe này trong kho!";
                    Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Không thể kiểm tra tồn kho. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateQuantityDirectly(CartItem cartItem, int newQuantity, int position) {
        // Cập nhật số lượng trong cartItem (optimistic update)
        cartItem.setQuantity(newQuantity);
        
        // Cập nhật UI ngay lập tức (optimistic UI)
        storeCartAdapter.notifyItemChanged(position);
        updateCartSummary();
        
        // Gọi API để cập nhật
        updateItemQuantity(cartItem, newQuantity);
    }

    @Override
    public void onItemRemoved(int position) {
        if (position < 0 || position >= displayItems.size()) return;
        
        Object item = displayItems.get(position);
        if (!(item instanceof CartItem)) return;
        
        CartItem cartItem = (CartItem) item;
        
        // Gọi API để xóa
        removeItemFromCart(cartItem);
        
        // Cập nhật UI
        displayItems.remove(position);
        cartItems.remove(cartItem);
        storeCartAdapter.notifyItemRemoved(position);
        
        updateCartSummary();
        checkEmptyCart();
        Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    // Đã bỏ thanh toán theo từng cửa hàng; dùng nút thanh toán chung ở cuối màn hình
    
    private void updateItemQuantity(CartItem cartItem, int newQuantity) {
        if (cartItem.getItemId() == null || cartItem.getItemId().isEmpty()) {
            Toast.makeText(this, "Thiếu itemId cho cập nhật số lượng, tải lại giỏ hàng", Toast.LENGTH_SHORT).show();
            loadCartFromApi();
            return;
        }
        
        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("quantity", newQuantity);
        
        api.updateCartItemQuantity(auth.getAuthHeader(), cartItem.getItemId(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Thành công - UI đã được cập nhật optimistic, không cần làm gì thêm
                    Toast.makeText(CartActivity.this, "Cập nhật số lượng thành công", Toast.LENGTH_SHORT).show();
                } else {
                    // Thất bại - hiển thị thông báo hết hàng và reload
                    String errorMsg = "Cửa hàng không còn đủ xe này trong kho!";
                    Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    loadCartFromApi(); // Reload để revert về trạng thái cũ
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Cửa hàng không còn đủ xe này trong kho!", Toast.LENGTH_LONG).show();
                loadCartFromApi(); // Reload để revert về trạng thái cũ
            }
        });
    }
    
    private void removeItemFromCart(CartItem cartItem) {
        if (cartItem.getItemId() == null || cartItem.getItemId().isEmpty()) {
            Toast.makeText(this, "Thiếu itemId cho xóa sản phẩm, tải lại giỏ hàng", Toast.LENGTH_SHORT).show();
            loadCartFromApi();
            return;
        }
        
        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        
        api.removeCartItem(auth.getAuthHeader(), cartItem.getItemId()).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Thành công, reload giỏ hàng để đồng bộ
                    loadCartFromApi();
                } else {
                    String errorMsg = "Xóa sản phẩm thất bại";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += " (HTTP " + response.code() + ")";
                        }
                    }
                    Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    loadCartFromApi(); // Reload để đồng bộ
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi xóa sản phẩm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadCartFromApi(); // Reload để đồng bộ
            }
        });
    }
}
