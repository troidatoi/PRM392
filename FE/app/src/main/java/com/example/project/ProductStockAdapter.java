package com.example.project;

import android.graphics.Color;
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

        // Set color based on status
        int statusColor;
        if (stock.getQuantity() > 10) {
            statusColor = Color.parseColor("#4CAF50"); // Green - Còn hàng
        } else if (stock.getQuantity() > 0) {
            statusColor = Color.parseColor("#FF9800"); // Orange - Sắp hết
        } else {
            statusColor = Color.parseColor("#F44336"); // Red - Hết hàng
        }

        holder.tvStockQuantity.setTextColor(statusColor);
        holder.tvStockStatus.setTextColor(statusColor);
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

