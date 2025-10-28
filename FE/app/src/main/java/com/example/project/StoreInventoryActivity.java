package com.example.project;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreInventoryActivity extends AppCompatActivity implements InventoryAdapter.OnInventoryActionListener {

    private String storeId;
    private String storeName;

    private TextView tvTitle;
    private EditText etSearch;
    private RecyclerView rvInventory;
    private ProgressBar progressBar;

    private final List<InventoryAdapter.Row> allRows = new ArrayList<>();
    private final List<InventoryAdapter.Row> filteredRows = new ArrayList<>();
    private InventoryAdapter adapter;

    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_inventory);

        storeId = getIntent().getStringExtra("storeId");
        storeName = getIntent().getStringExtra("storeName");
        if (storeId == null) {
            Toast.makeText(this, "Không tìm thấy storeId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance().getApiService();
        authManager = AuthManager.getInstance(this);

        tvTitle = findViewById(R.id.tvTitle);
        etSearch = findViewById(R.id.etSearch);
        rvInventory = findViewById(R.id.rvInventory);
        progressBar = findViewById(R.id.progressBar);

        tvTitle.setText(storeName != null ? ("Kho - " + storeName) : "Kho cửa hàng");

        adapter = new InventoryAdapter(this);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        rvInventory.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadData();
    }

    private void showLoading(boolean show) { if (progressBar != null) progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE); }

    private void loadData() {
        showLoading(true);
        // 1) Load all bikes (public endpoint)
        apiService.getBikes(1, 100, null, null, null, null, null, null, null)
                .enqueue(new Callback<ApiResponse<Bike[]>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Bike[]>> call, Response<ApiResponse<Bike[]>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Bike[] bikes = response.body().getData();
                            Map<String, InventoryAdapter.Row> productMap = new HashMap<>();
                            if (bikes != null) {
                                for (Bike b : bikes) {
                                    InventoryAdapter.Row r = new InventoryAdapter.Row();
                                    r.productId = b.getId();
                                    r.name = b.getName();
                                    r.price = b.getPrice();
                                    if (b.getImages() != null && !b.getImages().isEmpty()) {
                                        r.imageUrl = b.getImages().get(0).getUrl();
                                    }
                                    r.stock = 0; // default until inventory merged
                                    productMap.put(r.productId, r);
                                }
                            }
                            // 2) Load inventory for store and merge
                            loadInventoryAndMerge(productMap);
                        } else {
                            showLoading(false);
                            String msg = response.body() != null ? response.body().getMessage() : ("HTTP " + response.code());
                            Toast.makeText(StoreInventoryActivity.this, "Lỗi tải danh sách xe: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Bike[]>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(StoreInventoryActivity.this, "Lỗi kết nối khi tải xe: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadInventoryAndMerge(Map<String, InventoryAdapter.Row> productMap) {
        String token = authManager.getAuthHeader();
        apiService.getInventoryByStore(token, storeId, 1, 1000, null)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            try {
                                Object data = response.body().getData();
                                // BE trả về dạng List hoặc { data: List, pagination: ... }
                                List list = null;
                                if (data instanceof List) {
                                    list = (List) data;
                                } else if (data instanceof Map) {
                                    Object inner = ((Map) data).get("data");
                                    if (inner instanceof List) list = (List) inner;
                                }
                                if (list != null) {
                                    for (Object it : list) {
                                        Map inv = (Map) it;
                                        Map product = (Map) inv.get("product");
                                        if (product != null) {
                                            String pid = String.valueOf(product.get("_id"));
                                            Number st = (Number) inv.get("stock");
                                            InventoryAdapter.Row r = productMap.get(pid);
                                            if (r != null && st != null) r.stock = st.intValue();
                                        }
                                    }
                                }
                            } catch (Exception ignore) {}

                            // Build list and show
                            allRows.clear();
                            allRows.addAll(productMap.values());
                            filter(etSearch.getText().toString());
                        } else {
                            Toast.makeText(StoreInventoryActivity.this, "Lỗi tải tồn kho", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(StoreInventoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filter(String q) {
        String query = q == null ? "" : q.trim().toLowerCase(Locale.getDefault());
        filteredRows.clear();
        if (query.isEmpty()) {
            filteredRows.addAll(allRows);
        } else {
            for (InventoryAdapter.Row r : allRows) {
                if (r.name != null && r.name.toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredRows.add(r);
                }
            }
        }
        adapter.setRows(filteredRows);
    }

    @Override
    public void onAdd(String productId) {
        String token = authManager.getAuthHeader();
        Map<String, Object> body = new HashMap<>();
        body.put("productId", productId);
        body.put("storeId", storeId);
        body.put("quantity", 1);
        // Optimistic update
        adjustLocalStock(productId, +1);
        apiService.addStock(token, body)
                .enqueue(simpleRefreshCallback("Nhập kho thành công", "Nhập kho thất bại"));
    }

    @Override
    public void onReduce(String productId) {
        String token = authManager.getAuthHeader();
        Map<String, Object> body = new HashMap<>();
        body.put("productId", productId);
        body.put("storeId", storeId);
        body.put("quantity", 1);
        // Optimistic update (không cho xuống dưới 0)
        adjustLocalStock(productId, -1);
        apiService.reduceStock(token, body)
                .enqueue(simpleRefreshCallback("Xuất kho thành công", "Xuất kho thất bại"));
    }

    private void adjustLocalStock(String productId, int delta) {
        boolean changed = false;
        for (InventoryAdapter.Row r : allRows) {
            if (productId.equals(r.productId)) {
                int newStock = r.stock + delta;
                if (newStock < 0) newStock = 0;
                r.stock = newStock;
                changed = true;
                break;
            }
        }
        if (changed) filter(etSearch.getText().toString());
    }

    private Callback<ApiResponse<Object>> simpleRefreshCallback(String ok, String fail) {
        return new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loadData();
                    Toast.makeText(StoreInventoryActivity.this, ok, Toast.LENGTH_SHORT).show();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : ("HTTP " + response.code());
                    Toast.makeText(StoreInventoryActivity.this, fail + ": " + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(StoreInventoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}


