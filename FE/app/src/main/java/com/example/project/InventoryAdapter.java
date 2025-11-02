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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adapter hiển thị danh sách tất cả xe trong kho 1 cửa hàng. */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface OnInventoryActionListener {
        void onAdd(String productId);
        void onReduce(String productId);
    }

    public static class Row {
        public String productId;
        public String name;
        public String imageUrl;
        public double price;
        public int stock;
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnInventoryActionListener listener;

    public InventoryAdapter(OnInventoryActionListener listener) {
        this.listener = listener;
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        if (newRows != null) rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_row, parent, false);
        return new InventoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        Row r = rows.get(position);
        holder.tvName.setText(r.name);
        NumberFormat f = NumberFormat.getNumberInstance(Locale.getDefault());
        holder.tvPrice.setText(f.format(r.price) + " ₫");
        holder.tvStock.setText(String.valueOf(r.stock));

        if (r.imageUrl != null && !r.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(r.imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_bike_placeholder)
                    .error(R.drawable.ic_bike_placeholder)
                    .into(holder.ivBike);
        } else {
            holder.ivBike.setImageResource(R.drawable.ic_bike_placeholder);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAdd(r.productId);
        });
        holder.btnReduce.setOnClickListener(v -> {
            if (listener != null) listener.onReduce(r.productId);
        });
    }

    @Override
    public int getItemCount() { return rows.size(); }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBike; TextView tvName; TextView tvPrice; TextView tvStock; CardView btnAdd; CardView btnReduce;
        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBike = itemView.findViewById(R.id.ivBike);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnReduce = itemView.findViewById(R.id.btnReduce);
        }
    }
}


