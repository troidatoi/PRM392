package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreListActivity extends AppCompatActivity {
    
    private RecyclerView rvStores;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private CardView btnBack;
    private TextView tvTitle;
    
    private StoreAdapter storeAdapter;
    private List<Store> storeList;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_list);
        
        apiService = RetrofitClient.getInstance().getApiService();
        
        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadStores();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvStores = findViewById(R.id.rvStores);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTitle = findViewById(R.id.tvTitle);
        
        if (tvTitle != null) {
            tvTitle.setText("Danh sách Cửa hàng");
        }
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        storeList = new ArrayList<>();
        storeAdapter = new StoreAdapter(storeList, null);
        // Remove edit/delete buttons for user view - only show store info
        storeAdapter.setOnStoreClickListener(store -> {
            // Optional: Open store detail
            Toast.makeText(this, store.getName() + "\n" + store.getFullAddress(), Toast.LENGTH_LONG).show();
        });
        
        rvStores.setLayoutManager(new LinearLayoutManager(this));
        rvStores.setAdapter(storeAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadStores);
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
    }
    
    private void loadStores() {
        showLoading(true);
        
        // Call API to get all stores for map (no location filter)
        Call<ApiService.MapStoresResponse> call = apiService.getStoresForMap(null, null, null, null);
        
        call.enqueue(new Callback<ApiService.MapStoresResponse>() {
            @Override
            public void onResponse(Call<ApiService.MapStoresResponse> call, Response<ApiService.MapStoresResponse> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.MapStoresResponse mapResponse = response.body();
                    
                    if (mapResponse.isSuccess() && mapResponse.getData() != null) {
                        handleStoreResponse(mapResponse.getData());
                    } else {
                        String errorMsg = mapResponse.getMessage() != null ? mapResponse.getMessage() : "Không thể tải danh sách cửa hàng";
                        Toast.makeText(StoreListActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    }
                } else {
                    Toast.makeText(StoreListActivity.this, "Lỗi tải dữ liệu: " + response.message(), Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }
            
            @Override
            public void onFailure(Call<ApiService.MapStoresResponse> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(StoreListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }
    
    private void handleStoreResponse(List<ApiService.MapStoresResponse.MapStore> mapStores) {
        storeList.clear();
        
        // Convert MapStore to Store objects
        for (ApiService.MapStoresResponse.MapStore mapStore : mapStores) {
            Store store = new Store();
            store.setId(mapStore.getId());
            store.setName(mapStore.getName());
            store.setAddress(mapStore.getAddress());
            store.setCity(mapStore.getCity());
            store.setPhone(mapStore.getPhone());
            store.setLatitude(mapStore.getLatitude());
            store.setLongitude(mapStore.getLongitude());
            store.setIsActive(mapStore.isOpenNow());
            // Convert distance to string for display if available
            if (mapStore.getDistance() != null) {
                store.setStatus(String.format("%.2f km", mapStore.getDistance()));
            } else {
                store.setStatus(mapStore.isOpenNow() ? "Hoạt động" : "Đóng cửa");
            }
            
            storeList.add(store);
        }
        
        storeAdapter.notifyDataSetChanged();
        showEmptyState(storeList.isEmpty());
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvStores != null) {
            rvStores.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    private void showEmptyState(boolean show) {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvStores != null) {
            rvStores.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

