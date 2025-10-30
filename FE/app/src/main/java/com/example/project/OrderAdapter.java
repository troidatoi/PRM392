package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;
import com.example.project.utils.AuthManager;

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

        // Show nút Xác nhận nếu trạng thái là 'Chờ xác nhận' (pending, hoặc tên tiếng Việt)
        if (order.getStatus().equalsIgnoreCase("Chờ xác nhận") || order.getStatus().equalsIgnoreCase("pending")) {
            holder.btnConfirmOrder.setVisibility(View.VISIBLE);
        } else {
            holder.btnConfirmOrder.setVisibility(View.GONE);
        }
        holder.btnConfirmOrder.setOnClickListener(v -> {
            v.setEnabled(false);
            // Gọi API xác nhận đơn
            AuthManager auth = AuthManager.getInstance(context);
            ApiService api = RetrofitClient.getInstance().getApiService();
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("status", "confirmed");
            api.updateOrderStatus(auth.getAuthHeader(), order.getOrderId(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                @Override public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body()!=null && response.body().isSuccess()) {
                        Toast.makeText(context, "Đã xác nhận đơn hàng!", Toast.LENGTH_SHORT).show();
                        order.setStatus("Đã xác nhận");
                        notifyItemChanged(position);
                    } else {
                        Toast.makeText(context, "Xác nhận thất bại", Toast.LENGTH_SHORT).show();
                        v.setEnabled(true);
                    }
                }
                @Override public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                }
            });
        });

        // Hiện nút "Giao hàng" nếu trạng thái là "Đã xác nhận"
        if (order.getStatus().equalsIgnoreCase("Đã xác nhận") || order.getStatus().equalsIgnoreCase("confirmed")) {
            holder.btnShipOrder.setVisibility(View.VISIBLE);
        } else {
            holder.btnShipOrder.setVisibility(View.GONE);
        }
        holder.btnShipOrder.setOnClickListener(v -> {
            v.setEnabled(false);
            AuthManager auth = AuthManager.getInstance(context);
            ApiService api = RetrofitClient.getInstance().getApiService();
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("status", "shipped");
            api.updateOrderStatus(auth.getAuthHeader(), order.getOrderId(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body()!=null && response.body().isSuccess()) {
                        Toast.makeText(context, "Đã chuyển sang trạng thái Đang giao hàng!", Toast.LENGTH_SHORT).show();
                        order.setStatus("Đang giao hàng");
                        notifyItemChanged(position);
                    } else {
                        Toast.makeText(context, "Chuyển trạng thái thất bại", Toast.LENGTH_SHORT).show();
                        v.setEnabled(true);
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) {
                    Toast.makeText(context, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                }
            });
        });

        // View detail button click
        holder.btnViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            context.startActivity(intent);
        });

        // Order item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderItems, tvOrderTotal;
        CardView statusBadge, btnViewDetail, btnConfirmOrder, btnShipOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnConfirmOrder = itemView.findViewById(R.id.btnConfirmOrder);
            btnShipOrder = itemView.findViewById(R.id.btnShipOrder);
        }
    }
}

