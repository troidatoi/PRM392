package com.example.project;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private boolean isGridMode;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
        this.isGridMode = false;
    }

    public ProductAdapter(List<Product> productList, boolean isGridMode) {
        this.productList = productList;
        this.isGridMode = isGridMode;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isGridMode ? R.layout.item_product_grid : R.layout.item_product;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Safe text setting with null checks
        if (holder.tvProductName != null) {
            holder.tvProductName.setText(product.getName() != null ? product.getName() : "Tên không xác định");
        }
        
        if (holder.tvProductDescription != null) {
            holder.tvProductDescription.setText(product.getDescription() != null ? product.getDescription() : "Mô tả không có");
        }
        
        if (holder.tvProductPrice != null) {
            holder.tvProductPrice.setText(product.getPrice() != null ? product.getPrice() : "0 ₫");
        }
        
        // Load image from URL if available, otherwise use default resource
        if (holder.ivProductImage != null) {
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .apply(new RequestOptions()
                        .placeholder(product.getImageResId()) // Default image while loading
                        .error(product.getImageResId()) // Default image if error
                        .transform(new RoundedCorners(16)))
                    .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setImageResource(product.getImageResId());
            }
        }

        // Click on product card to view details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("productName", product.getName() != null ? product.getName() : "Tên không xác định");
            intent.putExtra("productDescription", product.getDescription() != null ? product.getDescription() : "Mô tả không có");
            intent.putExtra("productPrice", product.getPrice() != null ? product.getPrice() : "0 ₫");
            intent.putExtra("productImage", product.getImageResId());
            if (product.getImageUrl() != null) {
                intent.putExtra("productImageUrl", product.getImageUrl());
            }
            v.getContext().startActivity(intent);
        });

        if (holder.btnAddToCart != null) {
            holder.btnAddToCart.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Đã thêm " + (product.getName() != null ? product.getName() : "sản phẩm") + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductDescription;
        TextView tvProductPrice;
        CardView btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
