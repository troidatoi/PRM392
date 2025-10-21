package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StoreManagementActivity extends AppCompatActivity implements StoreAdapter.OnStoreActionListener {

    private CardView btnBack, btnAddStore, btnSearch;
    private RecyclerView rvStores;
    private TextView tvTotalStores, tvActiveStores;
    private LinearLayout emptyState;

    private StoreAdapter storeAdapter;
    private List<Store> storeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_management);

        initViews();
        setupRecyclerView();
        loadStores();
        updateStatistics();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddStore = findViewById(R.id.btnAddStore);
        btnSearch = findViewById(R.id.btnSearch);
        rvStores = findViewById(R.id.rvStores);
        tvTotalStores = findViewById(R.id.tvTotalStores);
        tvActiveStores = findViewById(R.id.tvActiveStores);
        emptyState = findViewById(R.id.emptyState);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Add store button
        btnAddStore.setOnClickListener(v -> {
            // TODO: Open Add Store Dialog or Activity
            showAddStoreDialog();
        });

        // Search button
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng tìm kiếm - Coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        storeList = new ArrayList<>();
        storeAdapter = new StoreAdapter(storeList, this);
        rvStores.setLayoutManager(new LinearLayoutManager(this));
        rvStores.setAdapter(storeAdapter);
    }

    private void loadStores() {
        // Load sample stores - replace with actual data from database
        storeList.clear();

        storeList.add(new Store("1", "Cửa Hàng Xe Đạp Sài Gòn",
            "123 Nguyễn Huệ, Quận 1, TP.HCM", "0901234567", "Hoạt động", 45));

        storeList.add(new Store("2", "Xe Đạp Thể Thao ABC",
            "456 Lê Lợi, Quận 3, TP.HCM", "0912345678", "Hoạt động", 32));

        storeList.add(new Store("3", "Cửa Hàng Xe Điện XYZ",
            "789 Trần Hưng Đạo, Quận 5, TP.HCM", "0923456789", "Đóng cửa", 15));

        storeList.add(new Store("4", "Xe Đạp Địa Hình Pro",
            "321 Võ Văn Tần, Quận 3, TP.HCM", "0934567890", "Hoạt động", 28));

        storeList.add(new Store("5", "Giant Bike Store",
            "654 Điện Biên Phủ, Quận Bình Thạnh, TP.HCM", "0945678901", "Hoạt động", 52));

        storeList.add(new Store("6", "Cửa Hàng Xe Gấp Mini",
            "987 Cách Mạng Tháng 8, Quận 10, TP.HCM", "0956789012", "Hoạt động", 18));

        storeList.add(new Store("7", "Xe Đạp Trẻ Em Happy",
            "147 Lý Thường Kiệt, Quận 11, TP.HCM", "0967890123", "Đóng cửa", 22));

        storeList.add(new Store("8", "VinFast E-Bike Center",
            "258 Nguyễn Văn Cừ, Quận 5, TP.HCM", "0978901234", "Hoạt động", 38));

        storeAdapter.notifyDataSetChanged();

        // Show/hide empty state
        if (storeList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvStores.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvStores.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatistics() {
        int totalStores = storeList.size();
        int activeStores = 0;

        for (Store store : storeList) {
            if (store.isActive()) {
                activeStores++;
            }
        }

        tvTotalStores.setText(String.valueOf(totalStores));
        tvActiveStores.setText(String.valueOf(activeStores));
    }

    private void showAddStoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Cửa Hàng Mới");
        builder.setMessage("Chức năng thêm cửa hàng sẽ được cập nhật trong phiên bản tiếp theo.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onEditStore(Store store) {
        // Handle edit store
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh Sửa Cửa Hàng");
        builder.setMessage("Bạn muốn chỉnh sửa: " + store.getName() + "?");
        builder.setPositiveButton("Đồng ý", (dialog, which) -> {
            Toast.makeText(this, "Chức năng chỉnh sửa - Coming soon", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDeleteStore(Store store) {
        // Handle delete store
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa Cửa Hàng");
        builder.setMessage("Bạn có chắc chắn muốn xóa cửa hàng: " + store.getName() + "?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            storeList.remove(store);
            storeAdapter.notifyDataSetChanged();
            updateStatistics();

            // Show/hide empty state
            if (storeList.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rvStores.setVisibility(View.GONE);
            }

            Toast.makeText(this, "Đã xóa cửa hàng: " + store.getName(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
