package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private CardView btnBack;
    private RecyclerView rvOrders;
    private LinearLayout emptyState;
    private View filterAll, filterPending, filterConfirmed, filterShipped, filterDelivered, filterCancelled;

    private OrderAdapter orderAdapter;
    private List<Order> orders;
    private List<Order> allOrders;
    
    private String currentStatus = null; // null = all
    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean paginationAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        setupRecyclerView();
        setupFilters();
        applyFilter("all");
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrders);
        emptyState = findViewById(R.id.emptyState);
        filterAll = findViewById(R.id.filterAll);
        filterPending = findViewById(R.id.filterPending);
        filterConfirmed = findViewById(R.id.filterConfirmed);
        filterShipped = findViewById(R.id.filterShipped);
        filterDelivered = findViewById(R.id.filterDelivered);
        filterCancelled = findViewById(R.id.filterCancelled);
    }

    private void setupRecyclerView() {
        orders = new ArrayList<>();
        allOrders = new ArrayList<>();
        orderAdapter = new OrderAdapter(orders, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOrders.setLayoutManager(layoutManager);
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrdersFromApi(String status, int page) {
        if (isLoading) return;
        isLoading = true;

        com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(this);
        com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
        com.example.project.models.User user = auth.getCurrentUser();
        if (user == null) {
            isLoading = false;
            android.widget.Toast.makeText(this, "Vui lòng đăng nhập", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Khi load trang đầu, clear danh sách
        if (page == 1) {
            orders.clear();
            orderAdapter.notifyDataSetChanged();
            emptyState.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }

        api.getUserOrders(auth.getAuthHeader(), user.getId(),
                (status == null || status.equals("all")) ? null : status,
                page, pageSize)
            .enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                    isLoading = false;
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        try {
                            Object data = response.body().getData();
                            java.util.List list = (java.util.List) data;
                            int added = 0;
                            if (list != null) {
                                for (Object o : list) {
                                    if (!(o instanceof java.util.Map)) continue;
                                    java.util.Map m = (java.util.Map) o;

                                    String id = safeString(m.get("_id"));
                                    String status = safeString(m.get("orderStatus"));
                                    String date = formatDate(safeString(m.get("orderDate")));

                                    // finalAmount or totalAmount
                                    String amountText = formatCurrency(safeNumber(m.get("finalAmount"), m.get("totalAmount")));

                                    // Build items summary from orderDetails
                                    String itemsSummary = buildItemsSummary(m.get("orderDetails"));

                                    String color = statusToColor(status);

                                    orders.add(new Order(id, date, status, itemsSummary, amountText, color));
                                    added++;
                                }
                            }

                            orderAdapter.notifyDataSetChanged();

                            // Pagination handling
                            Object pg = response.body().getPagination();
                            int total = 0, pages = 1, current = page;
                            if (pg instanceof java.util.Map) {
                                java.util.Map pm = (java.util.Map) pg;
                                try { current = toInt(pm.get("page"), page); } catch (Exception ignored) {}
                                try { pages = toInt(pm.get("pages"), 1); } catch (Exception ignored) {}
                                try { total = toInt(pm.get("total"), 0); } catch (Exception ignored) {}
                            }
                            isLastPage = (current >= pages) || (added == 0) || (orders.size() >= total && total > 0);

                            if (orders.isEmpty()) {
                                emptyState.setVisibility(View.VISIBLE);
                                rvOrders.setVisibility(View.GONE);
                            } else {
                                emptyState.setVisibility(View.GONE);
                                rvOrders.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            android.widget.Toast.makeText(OrderHistoryActivity.this, "Lỗi xử lý dữ liệu", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.widget.Toast.makeText(OrderHistoryActivity.this, "Tải đơn hàng thất bại", android.widget.Toast.LENGTH_SHORT).show();
                        if (orders.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            rvOrders.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                    isLoading = false;
                    android.widget.Toast.makeText(OrderHistoryActivity.this, "Lỗi mạng: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    if (orders.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        rvOrders.setVisibility(View.GONE);
                    }
                }
            });
    }

    private void setupFilters() {
        View.OnClickListener onClick = v -> {
            String status = null;
            if (v.getId() == R.id.filterAll) status = "all";
            else if (v.getId() == R.id.filterPending) status = "pending";
            else if (v.getId() == R.id.filterConfirmed) status = "confirmed";
            else if (v.getId() == R.id.filterShipped) status = "shipped";
            else if (v.getId() == R.id.filterDelivered) status = "delivered";
            else if (v.getId() == R.id.filterCancelled) status = "cancelled";

            applyFilter(status);
        };

        filterAll.setOnClickListener(onClick);
        filterPending.setOnClickListener(onClick);
        filterConfirmed.setOnClickListener(onClick);
        filterShipped.setOnClickListener(onClick);
        filterDelivered.setOnClickListener(onClick);
        filterCancelled.setOnClickListener(onClick);
    }

    private void applyFilter(String status) {
        currentStatus = status;
        currentPage = 1;
        isLastPage = false;
        loadOrdersFromApi(currentStatus, currentPage);
        addPaginationIfNeeded();
    }

    private void addPaginationIfNeeded() {
        if (paginationAdded) return;
        rvOrders.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;
                androidx.recyclerview.widget.LinearLayoutManager lm = (androidx.recyclerview.widget.LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) return;
                int visibleItemCount = lm.getChildCount();
                int totalItemCount = lm.getItemCount();
                int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2 && firstVisibleItemPosition >= 0) {
                        currentPage += 1;
                        loadOrdersFromApi(currentStatus, currentPage);
                    }
                }
            }
        });
        paginationAdded = true;
    }

    private String safeString(Object v) { return v == null ? "" : String.valueOf(v); }
    private int toInt(Object v, int def) { try { if (v instanceof Number) return ((Number) v).intValue(); return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; } }
    private long toLong(Object v, long def) { try { if (v instanceof Number) return ((Number) v).longValue(); return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return def; } }
    private long safeNumber(Object a, Object b) { if (a instanceof Number) return ((Number)a).longValue(); if (b instanceof Number) return ((Number)b).longValue(); return toLong(a, 0); }
    private String formatCurrency(long v) {
        java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return fmt.format(v) + " ₫";
    }
    private String formatDate(String iso) {
        try {
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(iso);
            java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return odt.toLocalDate().format(df);
        } catch (Exception e) {
            return iso != null && iso.length() >= 10 ? iso.substring(0, 10) : "";
        }
    }
    private String buildItemsSummary(Object detailsObj) {
        if (!(detailsObj instanceof java.util.List)) return "";
        java.util.List list = (java.util.List) detailsObj;
        if (list.isEmpty()) return "";
        int count = 0;
        String first = null;
        for (Object o : list) {
            if (!(o instanceof java.util.Map)) continue;
            java.util.Map d = (java.util.Map) o;
            Object qty = d.get("quantity");
            Object prodObj = d.get("product");
            String name = null;
            if (prodObj instanceof java.util.Map) {
                Object n = ((java.util.Map) prodObj).get("name");
                if (n != null) name = String.valueOf(n);
            }
            if (first == null && name != null) first = name + " x" + toInt(qty, 1);
            count++;
        }
        if (first == null) first = "Sản phẩm";
        if (count <= 1) return first;
        return first + " +" + (count - 1) + " mục";
    }
    private String statusToColor(String status) {
        if (status == null) return "#2196F3";
        switch (status.toLowerCase()) {
            case "pending": return "#FFC107"; // amber
            case "confirmed": return "#64B5F6"; // light blue
            case "shipped": return "#2196F3"; // blue
            case "delivered": return "#4CAF50"; // green
            case "cancelled": return "#F44336"; // red
            default: return "#2196F3";
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
