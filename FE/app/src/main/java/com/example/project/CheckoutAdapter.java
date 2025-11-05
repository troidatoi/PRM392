package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<CheckoutRow> rows;

    public CheckoutAdapter(List<CheckoutRow> rows) {
        this.rows = rows != null ? rows : new java.util.ArrayList<>();
    }
    
    public void updateData(List<CheckoutRow> newRows) {
        this.rows = newRows != null ? newRows : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getType() == CheckoutRow.Type.HEADER ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_store_header, parent, false);
            return new HeaderHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_product, parent, false);
            return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CheckoutRow row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            HeaderHolder h = (HeaderHolder) holder;
            h.tvStoreName.setText(row.getStoreName());
            if (row.getShippingFee() != null) {
                java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                String feeText = fmt.format(row.getShippingFee()) + " VNĐ";
                String dist = row.getDistanceKm() != null ? String.valueOf(row.getDistanceKm()) + " km" : null;
                h.tvStoreShipping.setText((dist != null ? dist + " - " : "") + feeText);
            } else {
                h.tvStoreShipping.setText("Phí ship: đang tính...");
            }
        } else if (holder instanceof ItemHolder) {
            CartItem item = row.getItem();
            ItemHolder it = (ItemHolder) holder;
            it.tvProductName.setText(item.getName());
            it.tvProductPrice.setText(item.getPrice());
            it.tvProductQuantity.setText("Số lượng: " + item.getQuantity());
            it.ivProductImage.setImageResource(item.getImageResId());
            try {
                long unit = item.getUnitPrice() > 0 ? item.getUnitPrice() : Long.parseLong(item.getPrice().replace(" VNĐ", "").replace(".", ""));
                long total = unit * item.getQuantity();
                java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                it.tvProductTotal.setText("Thành tiền: " + fmt.format(total) + " VNĐ");
            } catch (Exception ignored) {
                it.tvProductTotal.setText("Thành tiền: " + item.getPrice());
            }
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public void updateHeaderShipping(String storeId, Double distanceKm, Long shippingFee) {
        for (CheckoutRow r : rows) {
            if (r.getType() == CheckoutRow.Type.HEADER && storeId != null && storeId.equals(r.getStoreId())) {
                r.setShipping(distanceKm, shippingFee);
            }
        }
        notifyDataSetChanged();
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductTotal;

        ItemHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductTotal = itemView.findViewById(R.id.tvProductTotal);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvStoreShipping;
        HeaderHolder(View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreShipping = itemView.findViewById(R.id.tvStoreShipping);
        }
    }
}

