package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.project.R;
import com.example.project.models.Bike;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BikeAdapter extends RecyclerView.Adapter<BikeAdapter.BikeViewHolder> {

    private List<Bike> bikeList;
    private OnBikeClickListener onBikeClickListener;

    public interface OnBikeClickListener {
        void onBikeClick(Bike bike);
    }

    public BikeAdapter(List<Bike> bikeList) {
        this.bikeList = bikeList;
    }

    public void setOnBikeClickListener(OnBikeClickListener listener) {
        this.onBikeClickListener = listener;
    }

    @NonNull
    @Override
    public BikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bike, parent, false);
        return new BikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BikeViewHolder holder, int position) {
        Bike bike = bikeList.get(position);
        holder.bind(bike);
    }

    @Override
    public int getItemCount() {
        return bikeList.size();
    }

    class BikeViewHolder extends RecyclerView.ViewHolder {
        private CardView cardViewBike;
        private ImageView ivBikeImage;
        private TextView tvBikeName;
        private TextView tvBikeBrand;
        private TextView tvBikePrice;
        private TextView tvBikeStock;
        private TextView tvBikeStatus;
        private ImageView ivDetailIcon;

        public BikeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewBike = itemView.findViewById(R.id.cardViewBike);
            ivBikeImage = itemView.findViewById(R.id.ivBikeImage);
            tvBikeName = itemView.findViewById(R.id.tvBikeName);
            tvBikeBrand = itemView.findViewById(R.id.tvBikeBrand);
            tvBikePrice = itemView.findViewById(R.id.tvBikePrice);
            tvBikeStock = itemView.findViewById(R.id.tvBikeStock);
            tvBikeStatus = itemView.findViewById(R.id.tvBikeStatus);
            ivDetailIcon = itemView.findViewById(R.id.ivDetailIcon);
        }

        public void bind(Bike bike) {
            // Load bike image
            if (bike.getImages() != null && !bike.getImages().isEmpty()) {
                String imageUrl = bike.getImages().get(0).getUrl();
                Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_bike_placeholder)
                    .error(R.drawable.ic_bike_placeholder)
                    .centerCrop()
                    .into(ivBikeImage);
            } else {
                // Set placeholder if no image
                ivBikeImage.setImageResource(R.drawable.ic_bike_placeholder);
            }

            // Set bike name
            tvBikeName.setText(bike.getName());

            // Set brand and model
            String brandModel = bike.getBrand();
            if (bike.getModel() != null && !bike.getModel().isEmpty()) {
                brandModel += " " + bike.getModel();
            }
            tvBikeBrand.setText(brandModel);

            // Format and set price
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = formatter.format(bike.getPrice()) + " ₫";
            tvBikePrice.setText(formattedPrice);

            // Set stock
            tvBikeStock.setText("Kho: " + bike.getStock());

            // Set status with color
            String status = bike.getStatus();
            tvBikeStatus.setText(status);
            switch (status.toLowerCase()) {
                case "available":
                    tvBikeStatus.setTextColor(itemView.getContext().getColor(R.color.green));
                    break;
                case "out_of_stock":
                    tvBikeStatus.setTextColor(itemView.getContext().getColor(R.color.red));
                    break;
                case "discontinued":
                    tvBikeStatus.setTextColor(itemView.getContext().getColor(R.color.orange));
                    break;
                default:
                    tvBikeStatus.setTextColor(itemView.getContext().getColor(R.color.gray));
                    break;
            }

            // Set featured badge
            if (bike.isFeatured()) {
                tvBikeStatus.setText("⭐ " + status);
            }

            // Set click listener
            cardViewBike.setOnClickListener(v -> {
                if (onBikeClickListener != null) {
                    onBikeClickListener.onBikeClick(bike);
                }
            });

            // Set detail icon click listener
            ivDetailIcon.setOnClickListener(v -> {
                if (onBikeClickListener != null) {
                    onBikeClickListener.onBikeClick(bike);
                }
            });
        }
    }
}
