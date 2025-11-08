package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.project.network.ApiService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class InventoryTurnoverAdapter extends RecyclerView.Adapter<InventoryTurnoverAdapter.TurnoverViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(ApiService.ProductTurnover product);
    }

    private List<ApiService.ProductTurnover> originalList = new ArrayList<>();
    private List<ApiService.ProductTurnover> filteredList = new ArrayList<>();
    private OnProductClickListener listener;
    
    // Sort state
    private String currentSortBy = "turnover"; // "turnover", "stock", "sales", "name", "revenue"
    private boolean sortAscending = false;

    public InventoryTurnoverAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<ApiService.ProductTurnover> products) {
        originalList.clear();
        if (products != null) {
            originalList.addAll(products);
        }
        applyFiltersAndSort();
    }

    public void setSort(String sortBy, boolean ascending) {
        this.currentSortBy = sortBy;
        this.sortAscending = ascending;
        applyFiltersAndSort();
    }

    public void filter(String query, String category, String status) {
        applyFiltersAndSort(query, category, status);
    }

    private void applyFiltersAndSort() {
        applyFiltersAndSort(null, null, null);
    }

    private void applyFiltersAndSort(String query, String category, String status) {
        filteredList.clear();
        
        // Apply filters
        for (ApiService.ProductTurnover product : originalList) {
            boolean matches = true;
            
            // Text search filter
            if (query != null && !query.trim().isEmpty()) {
                String searchQuery = query.toLowerCase();
                String productName = product.getProductName() != null ? product.getProductName().toLowerCase() : "";
                String productBrand = product.getProductBrand() != null ? product.getProductBrand().toLowerCase() : "";
                if (!productName.contains(searchQuery) && !productBrand.contains(searchQuery)) {
                    matches = false;
                }
            }
            
            // Category filter
            if (category != null && !category.isEmpty() && !category.equals("all")) {
                if (product.getProductCategory() == null || !product.getProductCategory().equals(category)) {
                    matches = false;
                }
            }
            
            // Status filter (fast/slow/out of stock)
            if (status != null && !status.isEmpty() && !status.equals("all")) {
                double ratio = product.getTurnoverRatio();
                int stock = product.getCurrentStock();
                
                switch (status) {
                    case "fast":
                        if (ratio < 1.5) matches = false;
                        break;
                    case "slow":
                        if (ratio >= 0.5) matches = false;
                        break;
                    case "out_of_stock":
                        if (stock > 0) matches = false;
                        break;
                    case "low_stock":
                        if (stock >= 10) matches = false; // Adjust threshold as needed
                        break;
                }
            }
            
            if (matches) {
                filteredList.add(product);
            }
        }
        
        // Apply sort
        sortList();
        
        notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(filteredList, new Comparator<ApiService.ProductTurnover>() {
            @Override
            public int compare(ApiService.ProductTurnover p1, ApiService.ProductTurnover p2) {
                int result = 0;
                
                switch (currentSortBy) {
                    case "turnover":
                        result = Double.compare(p1.getTurnoverRatio(), p2.getTurnoverRatio());
                        break;
                    case "stock":
                        result = Integer.compare(p1.getCurrentStock(), p2.getCurrentStock());
                        break;
                    case "sales":
                        result = Integer.compare(p1.getTotalQuantitySold(), p2.getTotalQuantitySold());
                        break;
                    case "name":
                        String name1 = p1.getProductName() != null ? p1.getProductName() : "";
                        String name2 = p2.getProductName() != null ? p2.getProductName() : "";
                        result = name1.compareToIgnoreCase(name2);
                        break;
                    case "revenue":
                        result = Double.compare(p1.getTotalRevenue(), p2.getTotalRevenue());
                        break;
                    default:
                        result = Double.compare(p1.getTurnoverRatio(), p2.getTurnoverRatio());
                }
                
                return sortAscending ? result : -result;
            }
        });
    }

    @NonNull
    @Override
    public TurnoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_turnover_row, parent, false);
        return new TurnoverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TurnoverViewHolder holder, int position) {
        ApiService.ProductTurnover product = filteredList.get(position);
        
        // Product name
        holder.tvProductName.setText(product.getProductName() != null ? product.getProductName() : "N/A");
        
        // Brand
        if (product.getProductBrand() != null) {
            holder.tvBrand.setText(product.getProductBrand());
            holder.tvBrand.setVisibility(View.VISIBLE);
        } else {
            holder.tvBrand.setVisibility(View.GONE);
        }
        
        // Stock
        int stock = product.getCurrentStock();
        holder.tvStock.setText(String.valueOf(stock));
        
        // Quantity sold
        holder.tvQuantitySold.setText(String.valueOf(product.getTotalQuantitySold()));
        
        // Turnover ratio
        double ratio = product.getTurnoverRatio();
        if (Double.isInfinite(ratio) || Double.isNaN(ratio)) {
            holder.tvTurnoverRatio.setText("∞");
        } else {
            holder.tvTurnoverRatio.setText(String.format(Locale.getDefault(), "%.2f", ratio));
        }
        
        // Status badge
        String statusText = getStatusText(product);
        int statusColor = getStatusColor(product);
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(statusColor);
        holder.tvStatus.setBackgroundResource(getStatusBackground(product));
        
        // Image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_bike_placeholder)
                    .error(R.drawable.ic_bike_placeholder)
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.ic_bike_placeholder);
        }
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    private String getStatusText(ApiService.ProductTurnover product) {
        int stock = product.getCurrentStock();
        double ratio = product.getTurnoverRatio();
        
        if (stock == 0) {
            return "Hết hàng";
        } else if (ratio >= 1.5) {
            return "⚡ Nhanh";
        } else if (ratio >= 0.5) {
            return "✓ Bình thường";
        } else {
            return "🐌 Chậm";
        }
    }

    private int getStatusColor(ApiService.ProductTurnover product) {
        int stock = product.getCurrentStock();
        double ratio = product.getTurnoverRatio();
        
        if (stock == 0) {
            return 0xFFDC2626; // Red
        } else if (ratio >= 1.5) {
            return 0xFF10B981; // Green
        } else if (ratio >= 0.5) {
            return 0xFF3B82F6; // Blue
        } else {
            return 0xFFF59E0B; // Orange
        }
    }

    private int getStatusBackground(ApiService.ProductTurnover product) {
        int stock = product.getCurrentStock();
        double ratio = product.getTurnoverRatio();
        
        if (stock == 0) {
            return R.drawable.bg_status_out_of_stock;
        } else if (ratio >= 1.5) {
            return R.drawable.bg_status_fast;
        } else if (ratio >= 0.5) {
            return R.drawable.bg_status_normal;
        } else {
            return R.drawable.bg_status_slow;
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public int getFilteredCount() {
        return filteredList.size();
    }

    static class TurnoverViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName;
        TextView tvBrand;
        TextView tvStock;
        TextView tvQuantitySold;
        TextView tvTurnoverRatio;
        TextView tvStatus;
        CardView cardView;

        TurnoverViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvQuantitySold = itemView.findViewById(R.id.tvQuantitySold);
            tvTurnoverRatio = itemView.findViewById(R.id.tvTurnoverRatio);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}

