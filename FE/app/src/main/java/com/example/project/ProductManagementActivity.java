package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {

    private CardView btnBack, btnAddProduct;
    private RecyclerView rvProducts;
    private LinearLayout emptyState;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        initViews();
        setupRecyclerView();
        loadProducts();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        rvProducts = findViewById(R.id.rvProducts);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(productAdapter);
    }

    private void loadProducts() {
        // TODO: Load products from database
        // For now, show some sample products
        productList.add(new Product(
            "Xe đạp điện VinFast Klara S",
            "Pin Lithium 60V, tốc độ 50km/h",
            "29.990.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp thể thao Giant TCR",
            "Khung carbon, siêu nhẹ",
            "18.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        if (productList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }

        productAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductManagementActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload products when returning from add product screen
        loadProducts();
    }
}
