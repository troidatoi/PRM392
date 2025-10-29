package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
            // Set product name
            tvProductName.setText(product.getName());

            // Set product ID (using a mock ID for now)
            tvProductId.setText("#" + product.getId());

            // Set product value/price
            tvProductValue.setText("$" + product.getPrice());

            // Set status indicator based on product status
            setStatusIndicator(product);

            // Set action button if needed
            setActionButton(product);
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
            // Show action button for some products
            int action = Math.abs(product.getId().hashCode()) % 3;
            
            if (action == 0) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("Edit");
                statusIndicator.setVisibility(View.GONE);
                
                btnAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProductEdit(product);
                    }
                });
            } else {
                btnAction.setVisibility(View.GONE);
                statusIndicator.setVisibility(View.VISIBLE);
            }
        }
    }
}
