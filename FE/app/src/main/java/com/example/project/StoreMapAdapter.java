package com.example.project;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoreMapAdapter extends RecyclerView.Adapter<StoreMapAdapter.StoreViewHolder> {

    private List<Store> storeList;
    private OnStoreMapActionListener listener;
    private double userLatitude;
    private double userLongitude;

    public interface OnStoreMapActionListener {
        void onDirectionClick(Store store);
        void onStoreClick(Store store);
    }

    public StoreMapAdapter(List<Store> storeList, OnStoreMapActionListener listener) {
        this.storeList = storeList;
        this.listener = listener;
    }

    public void setUserLocation(double latitude, double longitude) {
        this.userLatitude = latitude;
        this.userLongitude = longitude;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_map, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.bind(store);
    }

    @Override
    public int getItemCount() {
        return storeList != null ? storeList.size() : 0;
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName, tvStoreAddress, tvStoreDistance;
        CardView btnDirection;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            tvStoreDistance = itemView.findViewById(R.id.tvStoreDistance);
            btnDirection = itemView.findViewById(R.id.btnDirection);
        }

        public void bind(Store store) {
            tvStoreName.setText(store.getName());
            tvStoreAddress.setText(store.getAddress());

            // Calculate distance if user location is available
            if (userLatitude != 0.0 && userLongitude != 0.0) {
                float[] results = new float[1];
                Location.distanceBetween(userLatitude, userLongitude,
                        store.getLatitude(), store.getLongitude(), results);
                float distanceKm = results[0] / 1000;
                tvStoreDistance.setText(String.format("%.1f km", distanceKm));
            } else {
                tvStoreDistance.setText("-- km");
            }

            // Direction button click
            btnDirection.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDirectionClick(store);
                }
            });

            // Store item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStoreClick(store);
                }
            });
        }
    }
}
