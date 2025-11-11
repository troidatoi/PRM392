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
    private OnViewDetailsClickListener viewDetailsClickListener;

    public interface OnViewDetailsClickListener {
        void onViewDetailsClick(Store store);
    }

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

    public void setOnViewDetailsClickListener(OnViewDetailsClickListener listener) {
        this.viewDetailsClickListener = listener;
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
        android.util.Log.d("StoreAdapter", "onBindViewHolder position=" + position + " of " + storeList.size());
        Store store = storeList.get(position);
        android.util.Log.d("StoreAdapter", "Binding store: " + store.getName());

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
        
        // Show operating hours and status (Đang mở/Đóng cửa) based on current time
        boolean isOpenNow = store.isOpenNow();
        String operatingHoursText = store.getOperatingHoursText();
        String detailedOperatingHoursText = store.getDetailedOperatingHoursText();
        
        // Display operating hours for today
        if (holder.llOperatingHoursToday != null) {
            if (operatingHoursText != null && !operatingHoursText.isEmpty()) {
                holder.tvOperatingHours.setText(operatingHoursText);
                holder.llOperatingHoursToday.setVisibility(View.VISIBLE);
            } else {
                holder.llOperatingHoursToday.setVisibility(View.GONE);
            }
        } else if (holder.tvOperatingHours != null) {
            // Fallback for old layout
            if (operatingHoursText != null && !operatingHoursText.isEmpty()) {
                holder.tvOperatingHours.setText(operatingHoursText);
                holder.tvOperatingHours.setVisibility(View.VISIBLE);
            } else {
                holder.tvOperatingHours.setVisibility(View.GONE);
            }
        }
        
        // Display detailed operating hours for all days
        if (holder.tvDetailedOperatingHours != null) {
            if (detailedOperatingHoursText != null && !detailedOperatingHoursText.isEmpty()) {
                holder.tvDetailedOperatingHours.setText(detailedOperatingHoursText);
                holder.tvDetailedOperatingHours.setVisibility(View.VISIBLE);
            } else {
                holder.tvDetailedOperatingHours.setVisibility(View.GONE);
            }
        }
        
        // Show status (Đang mở/Đóng cửa) based on operating hours and current time
        if (store.getOperatingHours() != null) {
            // Use operating hours to determine status
            holder.tvStoreStatus.setText(isOpenNow ? "Đang mở" : "Đóng cửa");
            // Set status badge color based on isOpenNow
            if (isOpenNow) {
                holder.statusBadge.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
            } else {
                holder.statusBadge.setCardBackgroundColor(Color.parseColor("#FF5252")); // Red
            }
        } else {
            // Fallback to isActive if no operating hours
            String statusText = store.getDisplayStatus();
            if (statusText != null && statusText.contains("km")) {
                holder.tvStoreStatus.setText(store.isActive() ? "Hoạt động" : "Đóng cửa");
            } else {
                holder.tvStoreStatus.setText(statusText);
            }
            
            // Set status badge color
            if (store.isActive()) {
                holder.statusBadge.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
            } else {
                holder.statusBadge.setCardBackgroundColor(Color.parseColor("#FF5252")); // Red
            }
        }

        // Show "Gần nhất" badge for first item if enabled
        if (holder.nearestBadge != null) {
            holder.nearestBadge.setVisibility(
                (showNearestBadge && position == 0) ? View.VISIBLE : View.GONE
            );
        }

        // View Details button
        if (holder.btnViewDetails != null) {
            holder.btnViewDetails.setOnClickListener(v -> {
                if (viewDetailsClickListener != null) {
                    viewDetailsClickListener.onViewDetailsClick(store);
                }
            });
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
        int count = storeList.size();
        android.util.Log.d("StoreAdapter", "getItemCount() returning: " + count);
        return count;
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
        TextView tvStoreName, tvStoreAddress, tvStoreStatus, tvStoreDistance, tvOperatingHours, tvDetailedOperatingHours;
        android.view.ViewGroup llOperatingHoursToday;
        CardView statusBadge, nearestBadge, btnViewDetails, btnEditStore, btnDeleteStore, btnInventory;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvStoreStatus = itemView.findViewById(R.id.tvStoreStatus);
            tvStoreDistance = itemView.findViewById(R.id.tvStoreDistance);
            tvOperatingHours = itemView.findViewById(R.id.tvOperatingHours);
            tvDetailedOperatingHours = itemView.findViewById(R.id.tvDetailedOperatingHours);
            llOperatingHoursToday = itemView.findViewById(R.id.llOperatingHoursToday);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            nearestBadge = itemView.findViewById(R.id.nearestBadge);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnEditStore = itemView.findViewById(R.id.btnEditStore);
            btnDeleteStore = itemView.findViewById(R.id.btnDeleteStore);
            btnInventory = itemView.findViewById(R.id.btnInventory);
        }
    }
}

