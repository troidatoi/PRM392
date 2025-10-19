package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Bike;
import java.util.List;

public class BikeAdapter extends RecyclerView.Adapter<BikeAdapter.BikeViewHolder> {
    private List<Bike> bikes;
    private OnBikeClickListener listener;

    public interface OnBikeClickListener {
        void onBikeClick(Bike bike);
    }

    public BikeAdapter(List<Bike> bikes, OnBikeClickListener listener) {
        this.bikes = bikes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bike, parent, false);
        return new BikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BikeViewHolder holder, int position) {
        Bike bike = bikes.get(position);
        holder.bind(bike);
    }

    @Override
    public int getItemCount() {
        return bikes != null ? bikes.size() : 0;
    }

    public void updateBikes(List<Bike> newBikes) {
        this.bikes = newBikes;
        notifyDataSetChanged();
    }

    class BikeViewHolder extends RecyclerView.ViewHolder {
        private ImageView bikeImage;
        private TextView bikeName;
        private TextView bikeBrand;
        private TextView bikePrice;
        private TextView bikeOriginalPrice;
        private TextView bikeDiscount;
        private TextView bikeRating;
        private TextView bikeCategory;
        private TextView bikeStock;
        private View featuredBadge;
        private View newBadge;

        public BikeViewHolder(@NonNull View itemView) {
            super(itemView);
            bikeImage = itemView.findViewById(R.id.bike_image);
            bikeName = itemView.findViewById(R.id.bike_name);
            bikeBrand = itemView.findViewById(R.id.bike_brand);
            bikePrice = itemView.findViewById(R.id.bike_price);
            bikeOriginalPrice = itemView.findViewById(R.id.bike_original_price);
            bikeDiscount = itemView.findViewById(R.id.bike_discount);
            bikeRating = itemView.findViewById(R.id.bike_rating);
            bikeCategory = itemView.findViewById(R.id.bike_category);
            bikeStock = itemView.findViewById(R.id.bike_stock);
            featuredBadge = itemView.findViewById(R.id.featured_badge);
            newBadge = itemView.findViewById(R.id.new_badge);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onBikeClick(bikes.get(position));
                    }
                }
            });
        }

        public void bind(Bike bike) {
            bikeName.setText(bike.getName());
            bikeBrand.setText(bike.getBrand());
            bikePrice.setText(bike.getFormattedPrice());
            bikeCategory.setText(bike.getCategoryDisplayName());
            bikeStock.setText("Còn " + bike.getStock() + " xe");

            // Set rating
            if (bike.getRating() != null) {
                bikeRating.setText(String.format("⭐ %.1f (%d)", 
                    bike.getRating().getAverage(), bike.getRating().getCount()));
            } else {
                bikeRating.setText("⭐ Chưa có đánh giá");
            }

            // Handle original price and discount
            if (bike.getOriginalPrice() > 0 && bike.getOriginalPrice() > bike.getPrice()) {
                bikeOriginalPrice.setText(bike.getFormattedOriginalPrice());
                bikeOriginalPrice.setVisibility(View.VISIBLE);
                bikeDiscount.setText("-" + bike.getDiscountPercentage() + "%");
                bikeDiscount.setVisibility(View.VISIBLE);
            } else {
                bikeOriginalPrice.setVisibility(View.GONE);
                bikeDiscount.setVisibility(View.GONE);
            }

            // Handle badges
            featuredBadge.setVisibility(bike.isFeatured() ? View.VISIBLE : View.GONE);
            newBadge.setVisibility(bike.isNew() ? View.VISIBLE : View.GONE);

            // Load image
            String imageUrl = bike.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_bike_placeholder)
                        .error(R.drawable.ic_bike_placeholder)
                        .into(bikeImage);
            } else {
                bikeImage.setImageResource(R.drawable.ic_bike_placeholder);
            }
        }
    }
}


