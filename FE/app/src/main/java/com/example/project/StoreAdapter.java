package com.example.project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private List<Store> storeList;
    private OnStoreActionListener listener;

    public interface OnStoreActionListener {
        void onEditStore(Store store);
        void onDeleteStore(Store store);
    }

    public StoreAdapter(List<Store> storeList, OnStoreActionListener listener) {
        this.storeList = storeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = storeList.get(position);

        holder.tvStoreName.setText(store.getName());
        holder.tvStoreAddress.setText(store.getAddress());
        holder.tvProductCount.setText("• " + store.getProductCount() + " sản phẩm");
        holder.tvStoreStatus.setText(store.getStatus());

        // Set status badge color
        if (store.isActive()) {
            holder.statusBadge.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.statusBadge.setCardBackgroundColor(Color.parseColor("#FF5252")); // Red
        }

        // Edit button
        holder.btnEditStore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditStore(store);
            }
        });

        // Delete button
        holder.btnDeleteStore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteStore(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public void updateStores(List<Store> newStores) {
        this.storeList = newStores;
        notifyDataSetChanged();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvStoreAddress, tvStoreStatus, tvProductCount;
        CardView statusBadge, btnEditStore, btnDeleteStore;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvStoreStatus = itemView.findViewById(R.id.tvStoreStatus);
            tvProductCount = itemView.findViewById(R.id.tvProductCount);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            btnEditStore = itemView.findViewById(R.id.btnEditStore);
            btnDeleteStore = itemView.findViewById(R.id.btnDeleteStore);
        }
    }
}

