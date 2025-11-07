package com.example.project;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductStockAdapter extends RecyclerView.Adapter<ProductStockAdapter.ViewHolder> {

    private List<ProductStock> stockList;

    public ProductStockAdapter(List<ProductStock> stockList) {
        this.stockList = stockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductStock stock = stockList.get(position);

        holder.tvStoreName.setText(stock.getStoreName());
        holder.tvStoreAddress.setText(stock.getStoreAddress());
        holder.tvStockQuantity.setText(String.valueOf(stock.getQuantity()));
        holder.tvStockStatus.setText(stock.getStatus());

        // Set colors and background based on stock quantity
        int statusColor;
        int backgroundColor;
        
        if (stock.getQuantity() > 10) {
            // Green - Còn hàng
            statusColor = Color.parseColor("#4CAF50");
            backgroundColor = Color.parseColor("#E8F5E9");
        } else if (stock.getQuantity() > 0) {
            // Orange - Sắp hết
            statusColor = Color.parseColor("#FF9800");
            backgroundColor = Color.parseColor("#FFF3E0");
        } else {
            // Red - Hết hàng
            statusColor = Color.parseColor("#F44336");
            backgroundColor = Color.parseColor("#FFEBEE");
        }

        holder.tvStockQuantity.setTextColor(statusColor);
        holder.tvStockStatus.setTextColor(statusColor);
        
        // Create rounded background for status badge
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(32f); // 12dp in pixels approximately
        drawable.setColor(backgroundColor);
        holder.tvStockStatus.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvStoreAddress, tvStockQuantity, tvStockStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvStockQuantity = itemView.findViewById(R.id.tvStockQuantity);
            tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
        }
    }
}

