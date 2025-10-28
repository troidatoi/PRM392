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

@SuppressWarnings("deprecation")
public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

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

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            } else {
                // Navigate to Checkout Activity
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCartItems.setLayoutManager(layoutManager);
        rvCartItems.setAdapter(cartAdapter);
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
                                        items.add(ci);
                                    }
                                }
                            }
                        }
                    } catch (Exception ignore) {}

                    cartItems.clear();
                    cartItems.addAll(items);
                    cartAdapter.notifyDataSetChanged();
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

        cartAdapter.notifyDataSetChanged();
        checkEmptyCart();
    }

    private void updateCartSummary() {
        int totalItems = 0;
        long subtotal = 0;

        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            // Only count selected items in subtotal
            if (item.isSelected()) {
                subtotal += item.getTotalPrice();
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
        } else {
            emptyCartCard.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity) {
        updateCartSummary();
    }

    @Override
    public void onItemRemoved(int position) {
        cartItems.remove(position);
        cartAdapter.notifyItemRemoved(position);
        updateCartSummary();
        checkEmptyCart();
        Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(int position, boolean isSelected) {
        updateCartSummary();
    }
}
