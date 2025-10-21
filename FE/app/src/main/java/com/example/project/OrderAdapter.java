package com.example.project;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private Context context;

    public OrderAdapter(List<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.tvOrderId.setText("Đơn hàng #" + order.getOrderId());
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvOrderStatus.setText(order.getStatus());
        holder.tvOrderItems.setText(order.getItems());
        holder.tvOrderTotal.setText(order.getTotalAmount());

        // Set status badge color
        try {
            holder.statusBadge.setCardBackgroundColor(Color.parseColor(order.getStatusColor()));
        } catch (IllegalArgumentException e) {
            holder.statusBadge.setCardBackgroundColor(Color.parseColor("#2196F3"));
        }

        // View detail button click
        holder.btnViewDetail.setOnClickListener(v -> {
            Toast.makeText(context, "Chi tiết đơn hàng #" + order.getOrderId(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to order detail activity
        });

        // Order item click
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Xem đơn hàng #" + order.getOrderId(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to order detail activity
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderItems, tvOrderTotal;
        CardView statusBadge, btnViewDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
    }
}

