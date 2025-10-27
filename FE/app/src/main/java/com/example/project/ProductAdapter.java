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

        holder.tvProductName.setText(product.getName());
        holder.tvProductDescription.setText(product.getDescription());
        holder.tvProductPrice.setText(product.getPrice());
        
        // Handle original price if exists
        if (product.getOriginalPrice() != null && !product.getOriginalPrice().isEmpty()) {
            holder.tvOriginalPrice.setText(product.getOriginalPrice());
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
        }
        
        // Handle status tags
        if (product.isBestSeller()) {
            holder.bestSellerTag.setVisibility(View.VISIBLE);
            holder.statusTagContainer.setVisibility(View.VISIBLE);
        } else {
            holder.bestSellerTag.setVisibility(View.GONE);
        }
        
        if (product.isSoldOut()) {
            holder.soldOutTag.setVisibility(View.VISIBLE);
            holder.statusTagContainer.setVisibility(View.VISIBLE);
        } else {
            holder.soldOutTag.setVisibility(View.GONE);
        }
        
        // Hide status container if no tags are visible
        if (holder.bestSellerTag.getVisibility() == View.GONE && 
            holder.soldOutTag.getVisibility() == View.GONE) {
            holder.statusTagContainer.setVisibility(View.GONE);
        }
        
        // Load image from URL if available, otherwise use default resource
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

        // Click on product card to view details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productDescription", product.getDescription());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productImage", product.getImageResId());
            if (product.getImageUrl() != null) {
                intent.putExtra("productImageUrl", product.getImageUrl());
            }
            v.getContext().startActivity(intent);
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });
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
        TextView tvOriginalPrice;
        CardView btnAddToCart;
        
        // Status tags
        LinearLayout statusTagContainer;
        LinearLayout bestSellerTag;
        LinearLayout soldOutTag;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            
            // Status tags
            statusTagContainer = itemView.findViewById(R.id.statusTagContainer);
            bestSellerTag = itemView.findViewById(R.id.bestSellerTag);
            soldOutTag = itemView.findViewById(R.id.soldOutTag);
        }
    }
}
