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
        private TextView tvBikePrice;
        private ImageView ivDetailIcon;
        private ImageView ivAddToCart;

        public BikeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewBike = itemView.findViewById(R.id.cardViewBike);
            ivBikeImage = itemView.findViewById(R.id.ivBikeImage);
            tvBikeName = itemView.findViewById(R.id.tvBikeName);
            tvBikePrice = itemView.findViewById(R.id.tvBikePrice);
            ivDetailIcon = itemView.findViewById(R.id.ivDetailIcon);
            ivAddToCart = itemView.findViewById(R.id.ivAddToCart);
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

            // Add to cart: call API flow create cart (if needed) -> add item
            ivAddToCart.setOnClickListener(v -> {
                com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(itemView.getContext());
                com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
                com.example.project.models.User user = auth.getCurrentUser();
                if (user == null) {
                    android.widget.Toast.makeText(itemView.getContext(), "Vui lòng đăng nhập", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lấy 1 cửa hàng hoạt động để kiểm tra tồn kho và thêm vào giỏ
                api.getStores(null, null, null, true, 1, 1, null).enqueue(new retrofit2.Callback<com.example.project.network.ApiService.StoreResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.project.network.ApiService.StoreResponse> call, retrofit2.Response<com.example.project.network.ApiService.StoreResponse> response) {
                        final String[] storeIdHolder = new String[1];
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                            storeIdHolder[0] = response.body().getData().get(0).getId();
                        }

                        if (storeIdHolder[0] == null) {
                            android.widget.Toast.makeText(itemView.getContext(), "Không tìm thấy cửa hàng khả dụng", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // B5: kiểm tra tồn kho
                        java.util.Map<String, Object> checkBody = new java.util.HashMap<>();
                        checkBody.put("productId", bike.getId());
                        checkBody.put("storeId", storeIdHolder[0]);
                        checkBody.put("quantity", 1);
                        api.checkAvailability(auth.getAuthHeader(), checkBody).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call1, retrofit2.Response<com.example.project.models.ApiResponse<Object>> res1) {
                                // B4: đảm bảo có cart rồi B6: add item
                                java.util.Map<String, String> createBody = new java.util.HashMap<>();
                                createBody.put("userId", user.getId());
                                api.createCart(auth.getAuthHeader(), createBody).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call2, retrofit2.Response<com.example.project.models.ApiResponse<Object>> r2) {
                                        java.util.Map<String, Object> addBody = new java.util.HashMap<>();
                                        addBody.put("userId", user.getId());
                                        addBody.put("productId", bike.getId());
                                        addBody.put("storeId", storeIdHolder[0]);
                                        addBody.put("quantity", 1);
                                        api.addItemToCart(auth.getAuthHeader(), addBody).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                                            @Override
                                            public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call3, retrofit2.Response<com.example.project.models.ApiResponse<Object>> r3) {
                                                android.widget.Toast.makeText(itemView.getContext(), r3.isSuccessful() ? "Đã thêm vào giỏ: " + bike.getName() : "Thêm vào giỏ thất bại", android.widget.Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call3, Throwable t) {
                                                android.widget.Toast.makeText(itemView.getContext(), "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call2, Throwable t) {
                                        android.widget.Toast.makeText(itemView.getContext(), "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call1, Throwable t) {
                                android.widget.Toast.makeText(itemView.getContext(), "Lỗi kiểm tra tồn kho: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.project.network.ApiService.StoreResponse> call, Throwable t) {
                        android.widget.Toast.makeText(itemView.getContext(), "Lỗi lấy cửa hàng: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }
}
