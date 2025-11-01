package com.example.project.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.R;

import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder> {

    private List<Object> imageSources; // Can be Uri or String (URL)
    private OnImageClickListener onImageClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position);
        void onRemoveImage(int position);
    }

    public SelectedImageAdapter(List<Uri> imageUris, OnImageClickListener listener) {
        this.imageSources = new java.util.ArrayList<>();
        if (imageUris != null) {
            for (Uri uri : imageUris) {
                this.imageSources.add(uri);
            }
        }
        this.onImageClickListener = listener;
    }

    // Add Uri (for new selected images)
    public void addImageUri(Uri imageUri) {
        if (imageSources == null) {
            imageSources = new java.util.ArrayList<>();
        }
        imageSources.add(imageUri);
        notifyItemInserted(imageSources.size() - 1);
    }

    // Add String URL (for existing images from server)
    public void addImageUrl(String imageUrl) {
        if (imageSources == null) {
            imageSources = new java.util.ArrayList<>();
        }
        imageSources.add(imageUrl);
        notifyItemInserted(imageSources.size() - 1);
    }

    // Add all existing URLs
    public void addImageUrls(List<String> imageUrls) {
        if (imageSources == null) {
            imageSources = new java.util.ArrayList<>();
        }
        int startPosition = imageSources.size();
        imageSources.addAll(imageUrls);
        notifyItemRangeInserted(startPosition, imageUrls.size());
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object imageSource = imageSources.get(position);
        if (imageSource instanceof Uri) {
            // Local Uri from gallery
            holder.imageView.setImageURI((Uri) imageSource);
        } else if (imageSource instanceof String) {
            // URL string from server - use Glide to load
            Glide.with(holder.imageView.getContext())
                    .load((String) imageSource)
                    .placeholder(R.drawable.ic_bike_placeholder)
                    .error(R.drawable.ic_bike_placeholder)
                    .centerCrop()
                    .into(holder.imageView);
        }
        holder.tvImageNumber.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return imageSources != null ? imageSources.size() : 0;
    }

    public void removeItem(int position) {
        if (imageSources != null && position >= 0 && position < imageSources.size()) {
            imageSources.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imageSources.size());
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvImageNumber;
        ImageView btnRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSelectedImage);
            tvImageNumber = itemView.findViewById(R.id.tvImageNumber);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);

            itemView.setOnClickListener(v -> {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(getAdapterPosition());
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (onImageClickListener != null) {
                    onImageClickListener.onRemoveImage(getAdapterPosition());
                }
            });
        }
    }
}
