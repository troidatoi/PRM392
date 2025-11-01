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
    private OnStoreClickListener clickListener;
    private OnInventoryClickListener inventoryClickListener;

    public interface OnStoreActionListener {
        void onEditStore(Store store);
        void onDeleteStore(Store store);
    }

    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }

    public interface OnInventoryClickListener {
        void onInventoryClick(Store store);
    }

    public StoreAdapter(List<Store> storeList, OnStoreActionListener listener) {
        this.storeList = storeList;
        this.listener = listener;
    }

    public void setOnStoreClickListener(OnStoreClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnInventoryClickListener(OnInventoryClickListener listener) {
        this.inventoryClickListener = listener;
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
        holder.tvStoreAddress.setText(store.getFullAddress());
        
        // Show distance if available
        if (holder.tvStoreDistance != null) {
            String distanceText = store.getDistanceText();
            if (distanceText != null && !distanceText.isEmpty()) {
                holder.tvStoreDistance.setText("📍 " + distanceText + " từ vị trí của bạn");
                holder.tvStoreDistance.setVisibility(View.VISIBLE);
                // Debug log
                android.util.Log.d("StoreAdapter", "Showing distance for " + store.getName() + ": " + distanceText);
            } else {
                holder.tvStoreDistance.setVisibility(View.GONE);
                // Debug log
                android.util.Log.d("StoreAdapter", "No distance for " + store.getName() + ", distance=" + store.getDistance());
            }
        }
        
        // Show status (Hoạt động/Đóng cửa) - only if no distance
        String statusText = store.getDisplayStatus();
        if (statusText != null && statusText.contains("km")) {
            // If status contains distance, show only "Hoạt động" or "Đóng cửa"
            holder.tvStoreStatus.setText(store.isActive() ? "Hoạt động" : "Đóng cửa");
        } else {
            holder.tvStoreStatus.setText(statusText);
        }

        // Show "Gần nhất" badge for first item if enabled
        if (holder.nearestBadge != null) {
            holder.nearestBadge.setVisibility(
                (showNearestBadge && position == 0) ? View.VISIBLE : View.GONE
            );
        }

        // Set status badge color
        if (store.isActive()) {
            holder.statusBadge.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.statusBadge.setCardBackgroundColor(Color.parseColor("#FF5252")); // Red
        }

        // Edit button - only show if listener is available
        if (holder.btnEditStore != null) {
            holder.btnEditStore.setVisibility(listener != null ? View.VISIBLE : View.GONE);
            holder.btnEditStore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditStore(store);
                }
            });
        }

        // Delete button - only show if listener is available
        if (holder.btnDeleteStore != null) {
            holder.btnDeleteStore.setVisibility(listener != null ? View.VISIBLE : View.GONE);
            holder.btnDeleteStore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteStore(store);
                }
            });
        }

        // Inventory button - only show if listener is available
        if (holder.btnInventory != null) {
            holder.btnInventory.setVisibility(inventoryClickListener != null ? View.VISIBLE : View.GONE);
            holder.btnInventory.setOnClickListener(v -> {
                if (inventoryClickListener != null) {
                    inventoryClickListener.onInventoryClick(store);
                }
            });
        }

        // Store item click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onStoreClick(store);
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

    private boolean showNearestBadge = false;

    public void setShowNearestBadge(boolean show) {
        this.showNearestBadge = show;
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvStoreAddress, tvStoreStatus, tvStoreDistance;
        CardView statusBadge, nearestBadge, btnEditStore, btnDeleteStore, btnInventory;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvStoreStatus = itemView.findViewById(R.id.tvStoreStatus);
            tvStoreDistance = itemView.findViewById(R.id.tvStoreDistance);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            nearestBadge = itemView.findViewById(R.id.nearestBadge);
            btnEditStore = itemView.findViewById(R.id.btnEditStore);
            btnDeleteStore = itemView.findViewById(R.id.btnDeleteStore);
            btnInventory = itemView.findViewById(R.id.btnInventory);
        }
    }
}

