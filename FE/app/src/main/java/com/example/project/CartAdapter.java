package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(int position, int newQuantity);
        void onItemRemoved(int position);
        void onItemSelected(int position, boolean isSelected);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvProductName.setText(item.getName());
        holder.tvProductPrice.setText(item.getPrice());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.ivProductImage.setImageResource(item.getImageResId());
        holder.cbSelectItem.setChecked(item.isSelected());

        // Checkbox click
        holder.cbSelectItem.setOnCheckedChangeListener(null); // Clear previous listener
        holder.checkboxCard.setOnClickListener(v -> {
            boolean newState = !holder.cbSelectItem.isChecked();
            holder.cbSelectItem.setChecked(newState);
            item.setSelected(newState);
            if (listener != null) {
                listener.onItemSelected(holder.getAdapterPosition(), newState);
            }
        });

        holder.cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (listener != null) {
                listener.onItemSelected(holder.getAdapterPosition(), isChecked);
            }
        });

        // Decrease quantity
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                item.setQuantity(currentQuantity - 1);
                holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
                if (listener != null) {
                    listener.onQuantityChanged(holder.getAdapterPosition(), item.getQuantity());
                }
            }
        });

        // Increase quantity
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            item.setQuantity(currentQuantity + 1);
            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
            if (listener != null) {
                listener.onQuantityChanged(holder.getAdapterPosition(), item.getQuantity());
            }
        });

        // Remove item
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvQuantity;
        CardView btnDecrease, btnIncrease, btnRemove, checkboxCard;
        CheckBox cbSelectItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            cbSelectItem = itemView.findViewById(R.id.cbSelectItem);
            checkboxCard = itemView.findViewById(R.id.checkboxCard);
        }
    }
}
