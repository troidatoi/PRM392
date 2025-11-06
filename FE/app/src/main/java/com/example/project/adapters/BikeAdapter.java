package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Paint;

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
        private TextView tvBikePrice;
        private TextView tvBikeOriginalPrice;
        private TextView tvDiscountPercent;
        private TextView tvDiscountPercentInline;
        private View discountBadge;

        public BikeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewBike = itemView.findViewById(R.id.cardViewBike);
            ivBikeImage = itemView.findViewById(R.id.ivBikeImage);
            tvBikeName = itemView.findViewById(R.id.tvBikeName);
            tvBikePrice = itemView.findViewById(R.id.tvBikePrice);
            tvBikeOriginalPrice = itemView.findViewById(R.id.tvBikeOriginalPrice);
            tvDiscountPercent = itemView.findViewById(R.id.tvDiscountPercent);
            tvDiscountPercentInline = itemView.findViewById(R.id.tvDiscountPercentInline);
            discountBadge = itemView.findViewById(R.id.discountBadge);
            
            // Add strikethrough effect to original price
            tvBikeOriginalPrice.setPaintFlags(tvBikeOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
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

            // Format and set price
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedPrice = formatter.format(bike.getPrice()) + " ₫";
            tvBikePrice.setText(formattedPrice);

            // Handle original price and discount
            if (bike.getOriginalPrice() > 0 && bike.getOriginalPrice() > bike.getPrice()) {
                String formattedOriginalPrice = formatter.format(bike.getOriginalPrice()) + " ₫";
                tvBikeOriginalPrice.setText(formattedOriginalPrice);
                tvBikeOriginalPrice.setVisibility(View.VISIBLE);
                tvBikeOriginalPrice.setPaintFlags(tvBikeOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

                // Calculate discount percentage
                int discountPercent = (int) Math.round(((bike.getOriginalPrice() - bike.getPrice()) / bike.getOriginalPrice()) * 100);
                if (discountPercent > 0) {
                    if (tvDiscountPercent != null && discountBadge != null) {
                        tvDiscountPercent.setText("-" + discountPercent + "%");
                        discountBadge.setVisibility(View.VISIBLE);
                    }
                    if (tvDiscountPercentInline != null) {
                        tvDiscountPercentInline.setText("-" + discountPercent + "%");
                        tvDiscountPercentInline.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                tvBikeOriginalPrice.setVisibility(View.GONE);
                if (discountBadge != null) {
                    discountBadge.setVisibility(View.GONE);
                }
                if (tvDiscountPercentInline != null) {
                    tvDiscountPercentInline.setVisibility(View.GONE);
                }
            }


            // Set click listener
            cardViewBike.setOnClickListener(v -> {
                if (onBikeClickListener != null) {
                    onBikeClickListener.onBikeClick(bike);
                }
            });
        }
    }
}
