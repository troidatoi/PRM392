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

public class FeaturedBikeAdapter extends RecyclerView.Adapter<FeaturedBikeAdapter.ViewHolder> {
    public interface OnFeaturedBikeClickListener {
        void onFeaturedBikeClick(Bike bike);
    }

    private List<Bike> items;
    private final OnFeaturedBikeClickListener listener;

    public FeaturedBikeAdapter(List<Bike> items, OnFeaturedBikeClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_bike, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView price;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.featured_image);
            name = itemView.findViewById(R.id.featured_name);
            price = itemView.findViewById(R.id.featured_price);
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onFeaturedBikeClick(items.get(getAdapterPosition()));
                }
            });
        }

        void bind(Bike bike) {
            name.setText(bike.getName());
            price.setText(bike.getFormattedPrice());
            String imageUrl = bike.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_bike_placeholder)
                        .error(R.drawable.ic_bike_placeholder)
                        .into(image);
            } else {
                image.setImageResource(R.drawable.ic_bike_placeholder);
            }
        }
    }
}



