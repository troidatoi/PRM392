package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class ImageCarouselUrlAdapter extends RecyclerView.Adapter<ImageCarouselUrlAdapter.ImageViewHolder> {

    private List<String> imageUrlList;

    public ImageCarouselUrlAdapter(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_carousel, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrlList.get(position);
        
        // Load image using Glide
        Glide.with(holder.itemView.getContext())
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.splash_bike_background)
            .error(R.drawable.splash_bike_background)
            .centerCrop()
            .into(holder.ivCarouselImage);
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarouselImage;

        ImageViewHolder(View itemView) {
            super(itemView);
            ivCarouselImage = itemView.findViewById(R.id.ivCarouselImage);
        }
    }
}
