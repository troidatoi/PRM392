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
        
        // Handle original price and discount
        if (holder.tvOriginalPrice != null) {
            String originalPrice = product.getOriginalPrice();
            if (originalPrice != null && !originalPrice.isEmpty() && !originalPrice.equals(product.getPrice())) {
                holder.tvOriginalPrice.setText(originalPrice);
                holder.tvOriginalPrice.setVisibility(View.VISIBLE);
                holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                
                // Calculate discount percentage
                try {
                    long original = parsePrice(originalPrice);
                    long current = parsePrice(product.getPrice() != null ? product.getPrice() : "0");
                    if (original > current && original > 0) {
                        int discountPercent = (int) Math.round(((double)(original - current) / original) * 100);
                        if (discountPercent > 0) {
                            if (holder.tvDiscountPercent != null) {
                                holder.tvDiscountPercent.setText("-" + discountPercent + "%");
                                if (holder.discountBadge != null) {
                                    holder.discountBadge.setVisibility(View.VISIBLE);
                                }
                            }
                            if (holder.tvDiscountPercentInline != null) {
                                holder.tvDiscountPercentInline.setText("-" + discountPercent + "%");
                                holder.tvDiscountPercentInline.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }
            } else {
                holder.tvOriginalPrice.setVisibility(View.GONE);
                if (holder.discountBadge != null) {
                    holder.discountBadge.setVisibility(View.GONE);
                }
                if (holder.tvDiscountPercentInline != null) {
                    holder.tvDiscountPercentInline.setVisibility(View.GONE);
                }
            }
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
            
            // Pass bikeId if available for API call
            if (product.getBikeId() != null && !product.getBikeId().isEmpty()) {
                intent.putExtra("bikeId", product.getBikeId());
            }
            
            // Also pass basic info as fallback
            intent.putExtra("productName", product.getName() != null ? product.getName() : "Tên không xác định");
            intent.putExtra("productDescription", product.getDescription() != null ? product.getDescription() : "Mô tả không có");
            intent.putExtra("productPrice", product.getPrice() != null ? product.getPrice() : "0 ₫");
            intent.putExtra("productImage", product.getImageResId());
            if (product.getImageUrl() != null) {
                intent.putExtra("productImageUrl", product.getImageUrl());
            }
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private long parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0;
        try {
            // Remove "VNĐ", "₫", spaces, and dots
            String clean = priceStr.replace("VNĐ", "").replace("₫", "").replace(" ", "").replace(".", "").trim();
            return Long.parseLong(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductDescription;
        TextView tvProductPrice;
        TextView tvOriginalPrice;
        TextView tvDiscountPercent;
        TextView tvDiscountPercentInline;
        LinearLayout discountBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscountPercent = itemView.findViewById(R.id.tvDiscountPercent);
            tvDiscountPercentInline = itemView.findViewById(R.id.tvDiscountPercentInline);
            discountBadge = itemView.findViewById(R.id.discountBadge);
        }
    }
}
