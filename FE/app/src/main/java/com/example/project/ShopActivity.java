package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.adapters.BikeAdapter;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private BikeAdapter bikeAdapter;
    private List<Bike> bikeList;
    private ApiService apiService;

    // Top Bar
    private EditText etSearch;
    private ImageView iconFilter;

    // Categories
    private LinearLayout categoryAll, categoryMountain, categoryFolding, categoryEGravel, btnFilter;
    private LinearLayout lastSelectedCategory;

    // View Toggle
    private LinearLayout viewGrid, viewList;
    private boolean isGridMode = true;

    // Bottom Navigation
    private View navHome, navProducts, navCart, navAccount;
    private View blurHome, blurProducts, blurCart, blurAccount;
    private ImageView iconHome, iconProducts, iconCart, iconAccount;
    private TextView tvHome, tvProducts, tvCart, tvAccount, tvResultsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Initialize API service FIRST
        apiService = RetrofitClient.getInstance().getApiService();
        
        initViews();
        setupTopBar();
        setupCategories();
        setupViewToggle();
        setupRecyclerView();
        setupBottomNavigation();
        
        // Load bikes from API
        loadBikes();
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        tvResultsCount = findViewById(R.id.tvResultsCount);

        // Top Bar
        etSearch = findViewById(R.id.etSearch);
        iconFilter = findViewById(R.id.iconFilter);

        // Categories
        categoryAll = findViewById(R.id.categoryAll);
        categoryMountain = findViewById(R.id.categoryMountain);
        categoryFolding = findViewById(R.id.categoryFolding);
        categoryEGravel = findViewById(R.id.categoryEGravel);
        btnFilter = findViewById(R.id.btnFilter);

        // View Toggle
        viewGrid = findViewById(R.id.viewGrid);
        viewList = findViewById(R.id.viewList);

        // Bottom Navigation
        navHome = findViewById(R.id.navHome);
        navProducts = findViewById(R.id.navProducts);
        navCart = findViewById(R.id.navCart);
        navAccount = findViewById(R.id.navAccount);

        blurHome = findViewById(R.id.blurHome);
        blurProducts = findViewById(R.id.blurProducts);
        blurCart = findViewById(R.id.blurCart);
        blurAccount = findViewById(R.id.blurAccount);

        iconHome = findViewById(R.id.iconHome);
        iconProducts = findViewById(R.id.iconProducts);
        iconCart = findViewById(R.id.iconCart);
        iconAccount = findViewById(R.id.iconAccount);

        tvHome = findViewById(R.id.tvHome);
        tvProducts = findViewById(R.id.tvProducts);
        tvCart = findViewById(R.id.tvCart);
        tvAccount = findViewById(R.id.tvAccount);
    }

    private void setupTopBar() {
        // Setup search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Search with delay to avoid too many API calls
                String searchQuery = s.toString().trim();
                if (searchQuery.length() >= 2 || searchQuery.isEmpty()) {
                    loadBikes(null, searchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        iconFilter.setOnClickListener(v -> {
            // Open advanced filter dialog
            // TODO: Implement advanced filter dialog
        });
    }

    private void setupCategories() {
        // Load categories from API
        loadCategories();
        
        // Set "All" as selected by default
        lastSelectedCategory = categoryAll;

        categoryAll.setOnClickListener(v -> {
            selectCategory(categoryAll);
            filterByCategory("All");
        });

        categoryMountain.setOnClickListener(v -> {
            selectCategory(categoryMountain);
            filterByCategory("Mountain");
        });

        categoryFolding.setOnClickListener(v -> {
            selectCategory(categoryFolding);
            filterByCategory("Folding");
        });

        categoryEGravel.setOnClickListener(v -> {
            selectCategory(categoryEGravel);
            filterByCategory("E-gravel");
        });

        btnFilter.setOnClickListener(v -> {
            // Open filter dialog
        });
    }

    private void selectCategory(LinearLayout category) {
        // Reset last selected
        if (lastSelectedCategory != null) {
            lastSelectedCategory.setBackgroundResource(R.drawable.category_button_inactive);
            TextView text = (TextView) lastSelectedCategory.getChildAt(0);
            if (text != null) {
                text.setTextColor(getResources().getColor(R.color.category_text_inactive, null));
            }
        }

        // Set new selected
        category.setBackgroundResource(R.drawable.category_button_active);
        TextView text = (TextView) category.getChildAt(0);
        if (text != null) {
            text.setTextColor(getResources().getColor(R.color.white, null));
        }
        lastSelectedCategory = category;
    }

    private void setupViewToggle() {
        viewGrid.setOnClickListener(v -> {
            if (!isGridMode) {
                isGridMode = true;
                updateViewToggle();
                switchToGridLayout();
            }
        });

        viewList.setOnClickListener(v -> {
            if (isGridMode) {
                isGridMode = false;
                updateViewToggle();
                switchToListLayout();
            }
        });
    }

    private void updateViewToggle() {
        if (isGridMode) {
            viewGrid.setBackgroundResource(R.drawable.view_toggle_active);
            viewList.setBackgroundResource(android.R.color.transparent);
            ((ImageView) viewGrid.getChildAt(0)).setColorFilter(getResources().getColor(R.color.white, null));
            ((ImageView) viewList.getChildAt(0)).setColorFilter(getResources().getColor(R.color.category_text_inactive, null));
        } else {
            viewList.setBackgroundResource(R.drawable.view_toggle_active);
            viewGrid.setBackgroundResource(android.R.color.transparent);
            ((ImageView) viewList.getChildAt(0)).setColorFilter(getResources().getColor(R.color.white, null));
            ((ImageView) viewGrid.getChildAt(0)).setColorFilter(getResources().getColor(R.color.category_text_inactive, null));
        }
    }

    private void switchToGridLayout() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(bikeAdapter);
    }

    private void switchToListLayout() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(bikeAdapter);
    }

    private void filterByCategory(String category) {
        // Call API with category filter
        String categoryFilter = null;
        if (!"All".equals(category)) {
            // Map display names to API category IDs
            switch (category) {
                case "Mountain":
                    categoryFilter = "mountain";
                    break;
                case "Folding":
                    categoryFilter = "folding";
                    break;
                case "Cargo":
                    categoryFilter = "cargo";
                    break;
                case "Sport":
                    categoryFilter = "sport";
                    break;
                case "City":
                    categoryFilter = "city";
                    break;
                case "Other":
                    categoryFilter = "other";
                    break;
            }
        }
        
        // Get current search query
        String searchQuery = etSearch.getText().toString().trim();
        loadBikes(categoryFilter, searchQuery);
    }

    private void setupSearchBox() {
        // Not needed in new layout
    }

    private void setupRecyclerView() {
        bikeList = new ArrayList<>();
        bikeAdapter = new BikeAdapter(bikeList);
        bikeAdapter.setOnBikeClickListener(bike -> {
            Intent intent = new Intent(ShopActivity.this, ProductDetailActivity.class);
            intent.putExtra("bikeId", bike.getId());
            // Pass basic info as fallback
            intent.putExtra("productName", bike.getName());
            intent.putExtra("productDescription", bike.getDescription());
            intent.putExtra("productPrice", String.format("%.0f ₫", bike.getPrice()));
            if (bike.getImages() != null && !bike.getImages().isEmpty()) {
                intent.putExtra("productImageUrl", bike.getImages().get(0).getUrl());
            }
            startActivity(intent);
        });

        // Set GridLayoutManager with 2 columns for shop view
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(bikeAdapter);
    }

    private void setupBottomNavigation() {
        // Set Products as selected by default
        selectNavItem(blurProducts, iconProducts, tvProducts);

        navHome.setOnClickListener(v -> {
            finish(); // Go back to home
        });

        navProducts.setOnClickListener(v -> {
            selectNavItem(blurProducts, iconProducts, tvProducts);
            deselectNavItem(blurHome, iconHome, tvHome);
            deselectNavItem(blurCart, iconCart, tvCart);
            deselectNavItem(blurAccount, iconAccount, tvAccount);
        });

        navCart.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, CartActivity.class);
            startActivity(intent);
        });

        navAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }

    private void selectNavItem(View blur, ImageView icon, TextView text) {
        blur.setVisibility(View.VISIBLE);
        icon.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"));
        text.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void deselectNavItem(View blur, ImageView icon, TextView text) {
        blur.setVisibility(View.GONE);
        icon.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"));
        text.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
    }

    private void loadBikes() {
        loadBikes(null, null);
    }
    
    private void loadBikes(String category) {
        loadBikes(category, null);
    }
    
    private void loadBikes(String category, String search) {
        // Call API to get bikes with optional category filter and search
        apiService.getBikes(1, 100, category, null, null, null, null, search, null)
            .enqueue(new Callback<ApiResponse<Bike[]>>() {
                @Override
                public void onResponse(Call<ApiResponse<Bike[]>> call, Response<ApiResponse<Bike[]>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Bike[] bikes = response.body().getData();
                        if (bikes != null) {
                            bikeList.clear();
                            java.util.Collections.addAll(bikeList, bikes);
                            updateResultsCount(bikeList.size());
                            bikeAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Bike[]>> call, Throwable t) {
                    // Handle error silently or show an empty state
                    updateResultsCount(0);
                }
            });
    }
    
    private void updateResultsCount(int count) {
        if (tvResultsCount != null) {
            tvResultsCount.setText(String.format("%,d Results", count));
        }
    }
    
    private String formatPrice(double price) {
        // Format price with dot as thousands separator
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        return formatter.format(price).replace(",", ".") + " ₫";
    }
    
    private void loadCategories() {
        apiService.getCategories()
            .enqueue(new Callback<ApiResponse<Object[]>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object[]>> call, Response<ApiResponse<Object[]>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Object[] categories = response.body().getData();
                        if (categories != null && categories.length > 0) {
                            updateCategoryButtons(categories);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Object[]>> call, Throwable t) {
                    // Handle error silently - keep default categories
                }
            });
    }
    
    private void updateCategoryButtons(Object[] categories) {
        if (categories == null) return;
        // Map category IDs to display names
        java.util.Map<String, String> categoryNames = new java.util.HashMap<>();
        categoryNames.put("city", "City");
        categoryNames.put("mountain", "Mountain");
        categoryNames.put("folding", "Folding");
        categoryNames.put("cargo", "Cargo");
        categoryNames.put("sport", "Sport");
        categoryNames.put("other", "Other");
        
        // Update category buttons with real data
        for (Object categoryObj : categories) {
            if (categoryObj instanceof java.util.Map<?, ?>) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> category = (java.util.Map<String, Object>) categoryObj;
                Object idObj = category.get("id");
                if (idObj instanceof String) {
                    String categoryId = (String) idObj;
                    String displayName = categoryNames.get(categoryId);
                    if (displayName != null) {
                        switch (categoryId) {
                            case "mountain":
                                updateCategoryButton(categoryMountain, displayName);
                                break;
                            case "folding":
                                updateCategoryButton(categoryFolding, displayName);
                                break;
                            case "cargo":
                                updateCategoryButton(categoryEGravel, displayName);
                                break;
                        }
                    }
                }
            }
        }
    }
    
    private void updateCategoryButton(LinearLayout categoryButton, String displayName) {
        TextView textView = (TextView) categoryButton.getChildAt(0);
        if (textView != null) {
            textView.setText(displayName);
        }
    }
}
