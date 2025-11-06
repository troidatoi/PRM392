package com.example.project;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
    private View iconFilter;

    // Categories
    private View categoryAll, categoryMountain, categoryFolding, categoryEGravel;
    private View lastSelectedCategory;

    // View Toggle
    private View viewGrid, viewList;
    private boolean isGridMode = true;
    
    // Filter variables
    private String selectedPriceRange = "all";
    private List<String> selectedCategories = new ArrayList<>();

    // Bottom Navigation
    private View navHome, navProducts, navCart, navAccount;
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
        iconFilter = findViewById(R.id.iconFilter);

        // Categories
        categoryAll = findViewById(R.id.categoryAll);
        categoryMountain = findViewById(R.id.categoryMountain);
        categoryFolding = findViewById(R.id.categoryFolding);
        categoryEGravel = findViewById(R.id.categoryEGravel);

        // View Toggle
        viewGrid = findViewById(R.id.viewGrid);
        viewList = findViewById(R.id.viewList);

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
    }

    private void setupTopBar() {
        iconFilter.setOnClickListener(v -> {
            showFilterDialog();
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
    }

    private void selectCategory(View category) {
        // Reset last selected - set to white background
        if (lastSelectedCategory != null) {
            if (lastSelectedCategory instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) lastSelectedCategory).setCardBackgroundColor(Color.WHITE);
                TextView text = (TextView) ((androidx.cardview.widget.CardView) lastSelectedCategory).getChildAt(0);
                if (text != null) {
                    text.setTextColor(Color.parseColor("#666666"));
                    text.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        }

        // Set new selected - set to blue background
        if (category instanceof androidx.cardview.widget.CardView) {
            ((androidx.cardview.widget.CardView) category).setCardBackgroundColor(Color.parseColor("#2196F3"));
            TextView text = (TextView) ((androidx.cardview.widget.CardView) category).getChildAt(0);
            if (text != null) {
                text.setTextColor(Color.WHITE);
                text.setTypeface(null, android.graphics.Typeface.BOLD);
            }
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
            // Grid is active
            if (viewGrid instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) viewGrid).setCardBackgroundColor(Color.parseColor("#2196F3"));
                ImageView gridIcon = (ImageView) ((androidx.cardview.widget.CardView) viewGrid).getChildAt(0).findViewById(R.id.iconHome);
                if (gridIcon == null) {
                    // Try to get ImageView directly from RelativeLayout
                    android.view.ViewGroup layout = (android.view.ViewGroup) ((androidx.cardview.widget.CardView) viewGrid).getChildAt(0);
                    if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof ImageView) {
                        gridIcon = (ImageView) layout.getChildAt(0);
                        gridIcon.setColorFilter(Color.WHITE);
                    }
                }
            }
            if (viewList instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) viewList).setCardBackgroundColor(Color.TRANSPARENT);
                android.view.ViewGroup layout = (android.view.ViewGroup) ((androidx.cardview.widget.CardView) viewList).getChildAt(0);
                if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof ImageView) {
                    ImageView listIcon = (ImageView) layout.getChildAt(0);
                    listIcon.setColorFilter(Color.parseColor("#9E9E9E"));
                }
            }
        } else {
            // List is active
            if (viewList instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) viewList).setCardBackgroundColor(Color.parseColor("#2196F3"));
                android.view.ViewGroup layout = (android.view.ViewGroup) ((androidx.cardview.widget.CardView) viewList).getChildAt(0);
                if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof ImageView) {
                    ImageView listIcon = (ImageView) layout.getChildAt(0);
                    listIcon.setColorFilter(Color.WHITE);
                }
            }
            if (viewGrid instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) viewGrid).setCardBackgroundColor(Color.TRANSPARENT);
                android.view.ViewGroup layout = (android.view.ViewGroup) ((androidx.cardview.widget.CardView) viewGrid).getChildAt(0);
                if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof ImageView) {
                    ImageView gridIcon = (ImageView) layout.getChildAt(0);
                    gridIcon.setColorFilter(Color.parseColor("#9E9E9E"));
                }
            }
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
        
        // Load bikes without search query
        loadBikes(categoryFilter, null);
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
        // Set Products as selected by default (blue color and bold)
        selectNavItem(iconProducts, tvProducts);
        
        // Set other tabs as deselected
        deselectNavItem(iconHome, tvHome);
        deselectNavItem(iconCart, tvCart);
        deselectNavItem(iconAccount, tvAccount);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Close shop activity
        });

        navProducts.setOnClickListener(v -> {
            // Already on products page
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

    private void selectNavItem(ImageView icon, TextView text) {
        // Set blue color for selected item
        icon.setColorFilter(Color.parseColor("#2196F3"));
        text.setTextColor(Color.parseColor("#2196F3"));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void deselectNavItem(ImageView icon, TextView text) {
        // Set gray color for unselected items
        icon.setColorFilter(Color.parseColor("#666666"));
        text.setTextColor(Color.parseColor("#666666"));
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
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
    
    private void updateCategoryButton(View categoryButton, String displayName) {
        if (categoryButton instanceof androidx.cardview.widget.CardView) {
            TextView textView = (TextView) ((androidx.cardview.widget.CardView) categoryButton).getChildAt(0);
            if (textView != null) {
                textView.setText(displayName);
            }
        }
    }
    
    private void showFilterDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Price range cards
        CardView priceAll = dialog.findViewById(R.id.priceAll);
        CardView priceUnder10M = dialog.findViewById(R.id.priceUnder10M);
        CardView price10to20M = dialog.findViewById(R.id.price10to20M);
        CardView price20to50M = dialog.findViewById(R.id.price20to50M);
        CardView priceAbove50M = dialog.findViewById(R.id.priceAbove50M);
        
        // Category checkboxes
        CheckBox cbMountain = dialog.findViewById(R.id.cbMountain);
        CheckBox cbFolding = dialog.findViewById(R.id.cbFolding);
        CheckBox cbEGravel = dialog.findViewById(R.id.cbEGravel);
        CheckBox cbRoad = dialog.findViewById(R.id.cbRoad);
        
        // Buttons
        CardView btnClose = dialog.findViewById(R.id.btnCloseFilter);
        CardView btnReset = dialog.findViewById(R.id.btnResetFilter);
        CardView btnApply = dialog.findViewById(R.id.btnApplyFilter);
        
        // Set current selections
        setSelectedPriceCard(priceAll, selectedPriceRange.equals("all"));
        setSelectedPriceCard(priceUnder10M, selectedPriceRange.equals("under10"));
        setSelectedPriceCard(price10to20M, selectedPriceRange.equals("10to20"));
        setSelectedPriceCard(price20to50M, selectedPriceRange.equals("20to50"));
        setSelectedPriceCard(priceAbove50M, selectedPriceRange.equals("above50"));
        
        cbMountain.setChecked(selectedCategories.contains("mountain"));
        cbFolding.setChecked(selectedCategories.contains("folding"));
        cbEGravel.setChecked(selectedCategories.contains("cargo"));
        cbRoad.setChecked(selectedCategories.contains("road"));
        
        // Price range click listeners
        priceAll.setOnClickListener(v -> {
            selectedPriceRange = "all";
            setSelectedPriceCard(priceAll, true);
            setSelectedPriceCard(priceUnder10M, false);
            setSelectedPriceCard(price10to20M, false);
            setSelectedPriceCard(price20to50M, false);
            setSelectedPriceCard(priceAbove50M, false);
        });
        
        priceUnder10M.setOnClickListener(v -> {
            selectedPriceRange = "under10";
            setSelectedPriceCard(priceAll, false);
            setSelectedPriceCard(priceUnder10M, true);
            setSelectedPriceCard(price10to20M, false);
            setSelectedPriceCard(price20to50M, false);
            setSelectedPriceCard(priceAbove50M, false);
        });
        
        price10to20M.setOnClickListener(v -> {
            selectedPriceRange = "10to20";
            setSelectedPriceCard(priceAll, false);
            setSelectedPriceCard(priceUnder10M, false);
            setSelectedPriceCard(price10to20M, true);
            setSelectedPriceCard(price20to50M, false);
            setSelectedPriceCard(priceAbove50M, false);
        });
        
        price20to50M.setOnClickListener(v -> {
            selectedPriceRange = "20to50";
            setSelectedPriceCard(priceAll, false);
            setSelectedPriceCard(priceUnder10M, false);
            setSelectedPriceCard(price10to20M, false);
            setSelectedPriceCard(price20to50M, true);
            setSelectedPriceCard(priceAbove50M, false);
        });
        
        priceAbove50M.setOnClickListener(v -> {
            selectedPriceRange = "above50";
            setSelectedPriceCard(priceAll, false);
            setSelectedPriceCard(priceUnder10M, false);
            setSelectedPriceCard(price10to20M, false);
            setSelectedPriceCard(price20to50M, false);
            setSelectedPriceCard(priceAbove50M, true);
        });
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        btnReset.setOnClickListener(v -> {
            selectedPriceRange = "all";
            selectedCategories.clear();
            setSelectedPriceCard(priceAll, true);
            setSelectedPriceCard(priceUnder10M, false);
            setSelectedPriceCard(price10to20M, false);
            setSelectedPriceCard(price20to50M, false);
            setSelectedPriceCard(priceAbove50M, false);
            cbMountain.setChecked(false);
            cbFolding.setChecked(false);
            cbEGravel.setChecked(false);
            cbRoad.setChecked(false);
        });
        
        btnApply.setOnClickListener(v -> {
            // Update selected categories
            selectedCategories.clear();
            if (cbMountain.isChecked()) selectedCategories.add("mountain");
            if (cbFolding.isChecked()) selectedCategories.add("folding");
            if (cbEGravel.isChecked()) selectedCategories.add("cargo");
            if (cbRoad.isChecked()) selectedCategories.add("road");
            
            // Apply filters
            applyFilters();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void setSelectedPriceCard(CardView card, boolean isSelected) {
        TextView textView = (TextView) card.getChildAt(0);
        if (isSelected) {
            card.setCardBackgroundColor(Color.parseColor("#2196F3"));
            textView.setTextColor(Color.WHITE);
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            card.setCardBackgroundColor(Color.WHITE);
            textView.setTextColor(Color.parseColor("#666666"));
            textView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
    
    private void applyFilters() {
        if (bikeList == null) return;
        
        List<Bike> filteredList = new ArrayList<>();
        
        for (Bike bike : bikeList) {
            boolean matchesPrice = false;
            boolean matchesCategory = false;
            
            // Check price range
            double price = bike.getPrice();
            switch (selectedPriceRange) {
                case "all":
                    matchesPrice = true;
                    break;
                case "under10":
                    matchesPrice = price < 10000000;
                    break;
                case "10to20":
                    matchesPrice = price >= 10000000 && price < 20000000;
                    break;
                case "20to50":
                    matchesPrice = price >= 20000000 && price < 50000000;
                    break;
                case "above50":
                    matchesPrice = price >= 50000000;
                    break;
            }
            
            // Check category
            if (selectedCategories.isEmpty()) {
                matchesCategory = true;
            } else {
                String bikeCategory = bike.getCategory();
                matchesCategory = selectedCategories.contains(bikeCategory);
            }
            
            // Add to filtered list if matches both criteria
            if (matchesPrice && matchesCategory) {
                filteredList.add(bike);
            }
        }
        
        // Update adapter with filtered list
        bikeAdapter = new BikeAdapter(filteredList);
        rvProducts.setAdapter(bikeAdapter);
        
        // Update results count
        tvResultsCount.setText(filteredList.size() + " Results");
    }
}

