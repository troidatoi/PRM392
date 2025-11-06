package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.network.ApiService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ShippingRateAdapter extends RecyclerView.Adapter<ShippingRateAdapter.ShippingRateViewHolder> {

    private List<ApiService.ShippingRate> rateList;
    private OnRateActionListener listener;

    public interface OnRateActionListener {
        void onEditRate(ApiService.ShippingRate rate);
        void onDeleteRate(ApiService.ShippingRate rate);
    }

    public ShippingRateAdapter(List<ApiService.ShippingRate> rateList, OnRateActionListener listener) {
        this.rateList = rateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShippingRateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipping_rate, parent, false);
        return new ShippingRateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShippingRateViewHolder holder, int position) {
        ApiService.ShippingRate rate = rateList.get(position);

        // Set distance range
        holder.tvDistanceRange.setText(rate.getDistanceRange());

        // Set price per km
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        String priceText = formatter.format((long)rate.getPricePerKm()) + " ₫/km";
        holder.tvPricePerKm.setText(priceText);

        // Set note
        if (rate.getNote() != null && !rate.getNote().isEmpty()) {
            holder.tvNote.setText(rate.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        // Set status
        if (rate.isActive()) {
            holder.statusBadge.setBackgroundColor(0xFFE8F5E9);
            holder.tvStatus.setText("Đang hoạt động");
            holder.tvStatus.setTextColor(0xFF4CAF50);
        } else {
            holder.statusBadge.setBackgroundColor(0xFFFFEBEE);
            holder.tvStatus.setText("Đã tắt");
            holder.tvStatus.setTextColor(0xFFF44336);
        }

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditRate(rate);
            }
        });

        // Hide delete button - rates are fixed
        if (holder.btnDelete != null) {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return rateList != null ? rateList.size() : 0;
    }

    public void updateList(List<ApiService.ShippingRate> newList) {
        this.rateList = newList;
        notifyDataSetChanged();
    }

    static class ShippingRateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistanceRange;
        TextView tvPricePerKm;
        TextView tvNote;
        TextView tvStatus;
        View statusBadge;
        CardView btnEdit;
        CardView btnDelete;

        public ShippingRateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDistanceRange = itemView.findViewById(R.id.tvDistanceRange);
            tvPricePerKm = itemView.findViewById(R.id.tvPricePerKm);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

