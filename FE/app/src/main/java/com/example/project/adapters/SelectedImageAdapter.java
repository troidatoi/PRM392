package com.example.project.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;

import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris;
    private OnImageClickListener onImageClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position);
        void onRemoveImage(int position);
    }

    public SelectedImageAdapter(List<Uri> imageUris, OnImageClickListener listener) {
        this.imageUris = imageUris;
        this.onImageClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.imageView.setImageURI(imageUri);
        holder.tvImageNumber.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
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
