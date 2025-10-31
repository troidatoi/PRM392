package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.project.R;
import com.example.project.models.Bike;

import java.util.List;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ProductCardViewHolder> {

    private List<Bike> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Bike product);
        void onProductEdit(Bike product);
        void onProductDelete(Bike product);
    }

    public ProductCardAdapter(List<Bike> productList) {
        this.productList = productList;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new ProductCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductCardViewHolder holder, int position) {
        Bike product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductCardViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductId;
        private TextView tvProductValue;
        private View statusIndicator;
        private TextView btnAction;

        public ProductCardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductId = itemView.findViewById(R.id.tvProductId);
            tvProductValue = itemView.findViewById(R.id.tvProductValue);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            btnAction = itemView.findViewById(R.id.btnAction);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(productList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Bike product) {
            // Name
            tvProductName.setText(product.getName() != null ? product.getName() : "");

            // Secondary line: brand • model (fallback to id)
            String brand = safe(product.getBrand());
            String model = safe(product.getModel());
            String secondary = (brand.isEmpty() && model.isEmpty()) ? ("#" + safe(product.getId())) :
                    (brand.isEmpty() ? model : (model.isEmpty() ? brand : brand + " • " + model));
            tvProductId.setText(secondary);

            // Price formatted and stock suffix
            String priceText = formatCurrency(product.getPrice());
            tvProductValue.setText(priceText);

            // Image: first image url if available
            if (product.getImages() != null && !product.getImages().isEmpty() && product.getImages().get(0) != null) {
                String url = product.getImages().get(0).getUrl();
                if (url != null && !url.isEmpty()) {
                    RequestOptions opts = new RequestOptions()
                            .transform(new RoundedCorners(24))
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .placeholder(R.drawable.ic_bike_placeholder)
                            .error(R.drawable.ic_bike_placeholder);
                    Glide.with(itemView.getContext()).load(url).apply(opts).into(ivProductImage);
                } else {
                    ivProductImage.setImageResource(R.drawable.ic_bike_placeholder);
                }
            } else {
                ivProductImage.setImageResource(R.drawable.ic_bike_placeholder);
            }

            // Status indicator
            setStatusIndicator(product);

            // Action button state
            setActionButton(product);
        }

        private String safe(String s) { return s == null ? "" : s; }

        private String formatCurrency(double price) {
            try {
                java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
                return nf.format(price);
            } catch (Exception e) {
                return String.valueOf(price);
            }
        }

        private void setStatusIndicator(Bike product) {
            // Prefer real status if available; fallback to deterministic color
            String status = null;
            try { status = product.getStatus(); } catch (Exception ignored) {}

            if (status != null) {
                switch (status) {
                    case "available":
                        statusIndicator.setBackgroundResource(R.drawable.status_indicator_green);
                        return;
                    case "out_of_stock":
                        statusIndicator.setBackgroundResource(R.drawable.status_indicator_orange);
                        return;
                    case "draft":
                        statusIndicator.setBackgroundResource(R.drawable.status_indicator_grey);
                        return;
                    case "featured":
                        statusIndicator.setBackgroundResource(R.drawable.status_indicator_beige);
                        return;
                    default:
                        // fallthrough to hash-based
                        break;
                }
            }

            int hashBucket = Math.abs(product.getId().hashCode()) % 4;
            switch (hashBucket) {
                case 0:
                    statusIndicator.setBackgroundResource(R.drawable.status_indicator_beige);
                    break;
                case 1:
                    statusIndicator.setBackgroundResource(R.drawable.status_indicator_green);
                    break;
                case 2:
                    statusIndicator.setBackgroundResource(R.drawable.status_indicator_orange);
                    break;
                default:
                    statusIndicator.setBackgroundResource(R.drawable.status_indicator_grey);
                    break;
            }
        }

        private void setActionButton(Bike product) {
            // Always show Edit button as requested
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setText("Edit");
            statusIndicator.setVisibility(View.GONE);
            btnAction.setOnClickListener(v -> { if (listener != null) listener.onProductEdit(product); });
        }
    }
}
