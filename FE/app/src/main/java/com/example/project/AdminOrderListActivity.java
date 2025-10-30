package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderListActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private View filterAll, filterPending, filterConfirmed, filterShipped, filterDelivered, filterCancelled;
    private OrderAdapter orderAdapter;
    private List<Order> orders = new ArrayList<>();
    private String currentStatus = null;
    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_list);
        rvOrders = findViewById(R.id.rvOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        filterAll = findViewById(R.id.filterAll);
        filterPending = findViewById(R.id.filterPending);
        filterConfirmed = findViewById(R.id.filterConfirmed);
        filterShipped = findViewById(R.id.filterShipped);
        filterDelivered = findViewById(R.id.filterDelivered);
        filterCancelled = findViewById(R.id.filterCancelled);

        orderAdapter = new OrderAdapter(orders, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);

        setupFilters();
        loadOrdersFromApi(null, 1);
    }
    private void setupFilters() {
        View.OnClickListener onClick = v -> {
            if(v==filterAll) currentStatus = null;
            else if(v==filterPending) currentStatus = "pending";
            else if(v==filterConfirmed) currentStatus = "confirmed";
            else if(v==filterShipped) currentStatus = "shipped";
            else if(v==filterDelivered) currentStatus = "delivered";
            else if(v==filterCancelled) currentStatus = "cancelled";
            currentPage = 1; isLastPage = false;
            loadOrdersFromApi(currentStatus, currentPage);
        };
        filterAll.setOnClickListener(onClick);
        filterPending.setOnClickListener(onClick);
        filterConfirmed.setOnClickListener(onClick);
        filterShipped.setOnClickListener(onClick);
        filterDelivered.setOnClickListener(onClick);
        filterCancelled.setOnClickListener(onClick);
    }
    private void loadOrdersFromApi(String status, int page) {
        if(isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        AuthManager authManager = AuthManager.getInstance(this);
        ApiService api = RetrofitClient.getInstance().getApiService();
        api.getAllOrders(authManager.getAuthHeader(), status, page, pageSize)
                .enqueue(new Callback<com.example.project.models.ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<com.example.project.models.ApiResponse<Object>> call, Response<com.example.project.models.ApiResponse<Object>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful() && response.body()!=null && response.body().isSuccess()){
                    if(page==1) orders.clear();
                    List list = (List)response.body().getData();
                    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
                    if(list!=null) for(Object o: list){
                        if (!(o instanceof java.util.Map)) continue;
                        java.util.Map m = (java.util.Map) o;
                        String id = m.get("_id")+"";
                        String status = m.get("orderStatus")+"";
                        String date = m.get("orderDate")+"";
                        String summary = "";
                        String amount = m.get("finalAmount")+"";
                        long amountNum = 0L;
                        try {
                            Object amt = m.get("finalAmount");
                            if (amt instanceof Number) amountNum = ((Number) amt).longValue();
                            else if (amt instanceof String) amountNum = (long) Double.parseDouble((String)amt);
                        } catch(Exception ignored){}
                        String amountFormatted = fmt.format(amountNum) + " ₫";
                        orders.add(new Order(id, date, status, summary, amountFormatted, Order.mapStatusColor(status)));
                    }
                    orderAdapter.notifyDataSetChanged();
                    rvOrders.setVisibility(orders.isEmpty()?View.GONE:View.VISIBLE);
                    tvEmptyState.setVisibility(orders.isEmpty()?View.VISIBLE:View.GONE);
                }else{
                    tvEmptyState.setText("Không lấy được danh sách đơn hàng");
                    tvEmptyState.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setText("Lỗi kết nối: "+t.getMessage());
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
