package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

public class StoreSelectionAdapter extends RecyclerView.Adapter<StoreSelectionAdapter.StoreViewHolder> {

    public static class StoreItem {
        private String id;
        private String name;
        private String address;
        private int stock;
        private boolean isSelected;

        public StoreItem(String id, String name, String address, int stock) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.stock = stock;
            this.isSelected = false;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public int getStock() { return stock; }
        public boolean isSelected() { return isSelected; }
        public void setSelected(boolean selected) { isSelected = selected; }
    }

    private List<StoreItem> storeList;
    private OnStoreSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnStoreSelectedListener {
        void onStoreSelected(StoreItem store, int position);
    }

    public StoreSelectionAdapter(List<StoreItem> storeList, OnStoreSelectedListener listener) {
        this.storeList = storeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_selection, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreItem store = storeList.get(position);
        
        holder.tvStoreName.setText(store.getName());
        holder.tvStoreAddress.setText(store.getAddress());
        holder.tvStoreStock.setText("Còn " + store.getStock());

        // Update selection state
        boolean isSelected = position == selectedPosition;
        store.setSelected(isSelected);

        if (isSelected) {
            holder.ivRadioIcon.setImageResource(R.drawable.ic_radio_checked);
            holder.cardStore.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.light_gray));
            // Add visual indicator by changing background
        } else {
            holder.ivRadioIcon.setImageResource(R.drawable.ic_radio_unchecked);
            holder.cardStore.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.light_gray));
        }

        // Stock badge color
        if (store.getStock() == 0) {
            holder.cardStockBadge.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.light_red));
            holder.tvStoreStock.setTextColor(holder.itemView.getContext().getColor(R.color.red));
            holder.tvStoreStock.setText("Hết hàng");
        } else if (store.getStock() < 5) {
            holder.cardStockBadge.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.light_orange));
            holder.tvStoreStock.setTextColor(holder.itemView.getContext().getColor(R.color.orange));
        } else {
            holder.cardStockBadge.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.light_green));
            holder.tvStoreStock.setTextColor(holder.itemView.getContext().getColor(R.color.green));
        }

        // Click listener
        holder.cardStore.setOnClickListener(v -> {
            if (store.getStock() > 0) {
                int previousPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                
                if (listener != null) {
                    listener.onStoreSelected(store, selectedPosition);
                }
            }
        });

        // Disable click if out of stock
        holder.cardStore.setEnabled(store.getStock() > 0);
        holder.cardStore.setAlpha(store.getStock() > 0 ? 1.0f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    public StoreItem getSelectedStore() {
        if (selectedPosition >= 0 && selectedPosition < storeList.size()) {
            return storeList.get(selectedPosition);
        }
        return null;
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        CardView cardStore;
        ImageView ivRadioIcon;
        TextView tvStoreName;
        TextView tvStoreAddress;
        TextView tvStoreStock;
        CardView cardStockBadge;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            cardStore = itemView.findViewById(R.id.cardStore);
            ivRadioIcon = itemView.findViewById(R.id.ivRadioIcon);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvStoreStock = itemView.findViewById(R.id.tvStoreStock);
            cardStockBadge = itemView.findViewById(R.id.cardStockBadge);
        }
    }
}
