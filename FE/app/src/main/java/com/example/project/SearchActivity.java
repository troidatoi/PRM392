package com.example.project;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnClearSearch;
    private CardView btnBack, suggestionsCard, resultsCard;
    private RecyclerView rvSearchResults;
    private TextView tvNoResults;

    // Suggestion cards
    private CardView suggestion1, suggestion2, suggestion3, suggestion4, suggestion5;

    private ProductAdapter productAdapter;
    private List<Product> allProducts;
    private List<Product> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupSearchBox();
        setupSuggestions();
        loadProducts();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        suggestionsCard = findViewById(R.id.suggestionsCard);
        resultsCard = findViewById(R.id.resultsCard);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        tvNoResults = findViewById(R.id.tvNoResults);

        // Suggestions
        suggestion1 = findViewById(R.id.suggestion1);
        suggestion2 = findViewById(R.id.suggestion2);
        suggestion3 = findViewById(R.id.suggestion3);
        suggestion4 = findViewById(R.id.suggestion4);
        suggestion5 = findViewById(R.id.suggestion5);

        // Setup RecyclerView
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        searchResults = new ArrayList<>();
    }

    private void setupSearchBox() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Auto focus search box
        etSearch.requestFocus();

        // Search text change listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Show/hide clear button
                if (query.isEmpty()) {
                    btnClearSearch.setVisibility(View.GONE);
                    showSuggestions();
                } else {
                    btnClearSearch.setVisibility(View.VISIBLE);
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
        });
    }

    private void setupSuggestions() {
        // Suggestion 1: Xe đạp điện
        suggestion1.setOnClickListener(v -> {
            etSearch.setText("Xe đạp điện");
            etSearch.setSelection(etSearch.getText().length());
        });

        // Suggestion 2: Xe thể thao
        suggestion2.setOnClickListener(v -> {
            etSearch.setText("Xe thể thao");
            etSearch.setSelection(etSearch.getText().length());
        });

        // Suggestion 3: Xe đạp địa hình
        suggestion3.setOnClickListener(v -> {
            etSearch.setText("Xe đạp địa hình");
            etSearch.setSelection(etSearch.getText().length());
        });

        // Suggestion 4: Xe đạp trẻ em
        suggestion4.setOnClickListener(v -> {
            etSearch.setText("Xe đạp trẻ em");
            etSearch.setSelection(etSearch.getText().length());
        });

        // Suggestion 5: Xe đạp gấp
        suggestion5.setOnClickListener(v -> {
            etSearch.setText("Xe đạp gấp");
            etSearch.setSelection(etSearch.getText().length());
        });
    }

    private void loadProducts() {
        // Load all products - sử dụng cấu trúc Product hiện có
        allProducts = new ArrayList<>();

        // Xe đạp điện
        allProducts.add(new Product("Xe Đạp Điện VinFast Klara S",
            "Xe đạp điện thông minh, thiết kế hiện đại, pin Lithium 60V",
            "29.990.000 ₫", R.drawable.splash_bike_background));
        allProducts.add(new Product("Xe Đạp Điện Yadea E3",
            "Công nghệ Nhật Bản, động cơ 1200W, quãng đường 80km",
            "25.500.000 ₫", R.drawable.splash_bike_background));
        allProducts.add(new Product("Xe Đạp Điện Dibao Type R",
            "Thiết kế thể thao, phanh ABS, màn hình LCD",
            "32.000.000 ₫", R.drawable.splash_bike_background));

        // Xe thể thao
        allProducts.add(new Product("Xe Đạp Thể Thao Road Bike Pro",
            "Xe đạp đua chuyên nghiệp, khung carbon",
            "15.500.000 ₫", R.drawable.splash_bike_background));
        allProducts.add(new Product("Xe Đạp Thể Thao Giant TCR",
            "Thiết kế khí động học, Shimano 105",
            "18.900.000 ₫", R.drawable.splash_bike_background));

        // Xe địa hình
        allProducts.add(new Product("Xe Đạp Địa Hình MTB Pro",
            "Xe đạp leo núi chuyên dụng, phuộc 120mm",
            "8.500.000 ₫", R.drawable.splash_bike_background));
        allProducts.add(new Product("Xe Đạp Địa Hình Trek Marlin",
            "Khung nhôm cao cấp, phanh đĩa thủy lực",
            "12.900.000 ₫", R.drawable.splash_bike_background));

        // Xe đạp trẻ em
        allProducts.add(new Product("Xe Đạp Trẻ Em Disney Princess",
            "Cho bé gái 5-8 tuổi, có bánh phụ",
            "1.800.000 ₫", R.drawable.splash_bike_background));
        allProducts.add(new Product("Xe Đạp Trẻ Em Giant XTC Jr",
            "Xe đạp thể thao cho trẻ em, bánh 20 inch",
            "3.500.000 ₫", R.drawable.splash_bike_background));

        // Xe đạp gấp
        allProducts.add(new Product("Xe Đạp Gấp Dahon K3",
            "Siêu nhỏ gọn, dễ mang theo, 7kg",
            "5.800.000 ₫", R.drawable.splash_bike_background));
        allProducts.add(new Product("Xe Đạp Gấp Java Fit 16",
            "Xe gấp cao cấp, gấp trong 10 giây",
            "6.500.000 ₫", R.drawable.splash_bike_background));
    }

    private void performSearch(String query) {
        // Hide suggestions, show results
        suggestionsCard.setVisibility(View.GONE);
        resultsCard.setVisibility(View.VISIBLE);

        // Filter products
        searchResults.clear();
        String lowerQuery = query.toLowerCase();

        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(lowerQuery) ||
                product.getDescription().toLowerCase().contains(lowerQuery)) {
                searchResults.add(product);
            }
        }

        // Show results
        if (searchResults.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);

            if (productAdapter == null) {
                productAdapter = new ProductAdapter(searchResults, false);
                rvSearchResults.setAdapter(productAdapter);
            } else {
                productAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showSuggestions() {
        // Show suggestions, hide results
        suggestionsCard.setVisibility(View.VISIBLE);
        resultsCard.setVisibility(View.GONE);
    }
}
