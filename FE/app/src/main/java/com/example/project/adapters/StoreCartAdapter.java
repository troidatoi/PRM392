package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.CartItem;
import com.example.project.R;

import java.util.ArrayList;
import java.util.List;

public class StoreCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_STORE_HEADER = 0;
    private static final int TYPE_CART_ITEM = 1;
    
    private List<Object> items = new ArrayList<>();
    private OnCartItemListener listener;
    
    public interface OnCartItemListener {
        void onQuantityChanged(int position, int delta);
        void onItemRemoved(int position);
        void onStoreCheckoutClicked(String storeId, String storeName);
    }
    
    public StoreCartAdapter(List<Object> items, OnCartItemListener listener) {
        this.items = items;
        this.listener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof StoreHeader) {
            return TYPE_STORE_HEADER;
        } else {
            return TYPE_CART_ITEM;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_STORE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_header, parent, false);
            return new StoreHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            return new CartItemViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof StoreHeaderViewHolder) {
            StoreHeader header = (StoreHeader) items.get(position);
            ((StoreHeaderViewHolder) holder).bind(header, listener);
        } else if (holder instanceof CartItemViewHolder) {
            CartItem item = (CartItem) items.get(position);
            ((CartItemViewHolder) holder).bind(item, position, listener);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public static class StoreHeader {
        public String storeId;
        public String storeName;
        public long storeTotal;
        
        public StoreHeader(String storeId, String storeName, long storeTotal) {
            this.storeId = storeId;
            this.storeName = storeName;
            this.storeTotal = storeTotal;
        }
    }
    
    public static class StoreHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStoreName, tvStoreTotal;
        private View btnCheckoutStore;
        
        public StoreHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreTotal = itemView.findViewById(R.id.tvStoreTotal);
            btnCheckoutStore = itemView.findViewById(R.id.btnCheckoutStore);
        }
        
        void bind(StoreHeader header, OnCartItemListener listener) {
            tvStoreName.setText(header.storeName != null ? header.storeName : "Cửa hàng");
            tvStoreTotal.setText(formatPrice(header.storeTotal) + " VNĐ");
            
            btnCheckoutStore.setOnClickListener(v -> {
                if (listener != null && header.storeId != null) {
                    listener.onStoreCheckoutClicked(header.storeId, header.storeName);
                }
            });
        }
        
        private String formatPrice(long price) {
            return String.format("%,d", price).replace(",", ".");
        }
    }
    
    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvProductPrice, tvQuantity;
        private View btnDecrease, btnIncrease, btnRemove;
        
        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
        }
        
        void bind(CartItem item, int position, OnCartItemListener listener) {
            tvProductName.setText(item.getName());
            tvProductPrice.setText(item.getPrice());
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            
            btnDecrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(position, -1);
                }
            });
            
            btnIncrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(position, 1);
                }
            });
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(position);
                }
            });
        }
    }
}
