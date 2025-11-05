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
        private ImageView ivDetailIcon;
        private ImageView ivAddToCart;

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
            ivDetailIcon = itemView.findViewById(R.id.ivDetailIcon);
            ivAddToCart = itemView.findViewById(R.id.ivAddToCart);
            
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

            // Set detail icon click listener
            ivDetailIcon.setOnClickListener(v -> {
                if (onBikeClickListener != null) {
                    onBikeClickListener.onBikeClick(bike);
                }
            });

            // Add to cart: mở dialog chọn cửa hàng và số lượng
            ivAddToCart.setOnClickListener(v -> openSelectStoreQtyDialog(bike));
        }

        private void openSelectStoreQtyDialog(Bike bike) {
            com.example.project.utils.AuthManager auth = com.example.project.utils.AuthManager.getInstance(itemView.getContext());
            com.example.project.network.ApiService api = com.example.project.network.RetrofitClient.getInstance().getApiService();
            com.example.project.models.User user = auth.getCurrentUser();
            if (user == null) { android.widget.Toast.makeText(itemView.getContext(), "Vui lòng đăng nhập", android.widget.Toast.LENGTH_SHORT).show(); return; }

            android.view.View dialogView = android.view.LayoutInflater.from(itemView.getContext()).inflate(com.example.project.R.layout.dialog_select_store_qty, null);
            android.widget.Spinner sp = dialogView.findViewById(com.example.project.R.id.spStores);
            android.widget.TextView tvQty = dialogView.findViewById(com.example.project.R.id.tvQty);
            android.widget.TextView tvStockInfo = dialogView.findViewById(com.example.project.R.id.tvStockInfo);
            androidx.cardview.widget.CardView btnDec = dialogView.findViewById(com.example.project.R.id.btnDec);
            androidx.cardview.widget.CardView btnInc = dialogView.findViewById(com.example.project.R.id.btnInc);

            final java.util.List<com.example.project.Store> stores = new java.util.ArrayList<>();
            final java.util.List<String> display = new java.util.ArrayList<>();
            final java.util.Map<String, Integer> storeIdToStock = new java.util.HashMap<>();
            final int[] maxStock = {0};
            final String[] selectedStoreId = {null};
            final int[] qty = {1};
            tvQty.setText("1");

            api.getStores(null, null, null, true, 1, 100, null).enqueue(new retrofit2.Callback<com.example.project.network.ApiService.StoreResponse>() {
                @Override public void onResponse(retrofit2.Call<com.example.project.network.ApiService.StoreResponse> call, retrofit2.Response<com.example.project.network.ApiService.StoreResponse> response) {
                    if (response.isSuccessful() && response.body()!=null && response.body().getData()!=null) {
                        stores.addAll(response.body().getData());
                        for (com.example.project.Store s : stores) {
                            java.util.Map<String,Object> body = new java.util.HashMap<>();
                            body.put("productId", bike.getId());
                            body.put("storeId", s.getId());
                            body.put("quantity", 1);
                            api.checkAvailability(auth.getAuthHeader(), body).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                                @Override public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call1, retrofit2.Response<com.example.project.models.ApiResponse<Object>> res1) {
                                    int stock = 0;
                                    if (res1.isSuccessful() && res1.body()!=null && res1.body().isSuccess() && res1.body().getData() instanceof java.util.Map) {
                                        Object cur = ((java.util.Map)res1.body().getData()).get("currentStock");
                                        if (cur instanceof Number) stock = ((Number)cur).intValue();
                                    }
                                    storeIdToStock.put(s.getId(), stock);
                                    display.add(s.getName() + " — còn " + stock);
                                    if (display.size()==stores.size()) {
                                        android.widget.ArrayAdapter<String> ad = new android.widget.ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item, display);
                                        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        sp.setAdapter(ad);
                                        if (!stores.isEmpty()) {
                                            selectedStoreId[0] = stores.get(0).getId();
                                            maxStock[0] = storeIdToStock.getOrDefault(selectedStoreId[0], 0);
                                            tvStockInfo.setText("Kho: " + maxStock[0]);
                                        }
                                    }
                                }
                                @Override public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call1, Throwable t) { }
                            });
                        }
                    }
                }
                @Override public void onFailure(retrofit2.Call<com.example.project.network.ApiService.StoreResponse> call, Throwable t) { }
            });

            sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                    if (position>=0 && position<stores.size()) {
                        selectedStoreId[0] = stores.get(position).getId();
                        maxStock[0] = storeIdToStock.getOrDefault(selectedStoreId[0], 0);
                        tvStockInfo.setText("Kho: " + maxStock[0]);
                        if (qty[0] > maxStock[0] && maxStock[0] > 0) { qty[0] = maxStock[0]; tvQty.setText(String.valueOf(qty[0])); }
                    }
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
            });

            btnDec.setOnClickListener(v -> { if (qty[0] > 1) { qty[0]--; tvQty.setText(String.valueOf(qty[0])); } });
            btnInc.setOnClickListener(v -> { int limit = maxStock[0] == 0 ? 99 : maxStock[0]; if (qty[0] < limit) { qty[0]++; tvQty.setText(String.valueOf(qty[0])); } });

            new androidx.appcompat.app.AlertDialog.Builder(itemView.getContext())
                .setView(dialogView)
                .setPositiveButton("Thêm vào giỏ", (d, w) -> {
                    if (selectedStoreId[0] == null) { android.widget.Toast.makeText(itemView.getContext(), "Vui lòng chọn cửa hàng", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    // Kiểm tra tồn kho với số lượng đã chọn trước khi thêm
                    java.util.Map<String,Object> check = new java.util.HashMap<>();
                    check.put("productId", bike.getId());
                    check.put("storeId", selectedStoreId[0]);
                    check.put("quantity", qty[0]);
                    api.checkAvailability(auth.getAuthHeader(), check).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                        @Override public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> callCk, retrofit2.Response<com.example.project.models.ApiResponse<Object>> resCk) {
                            if (resCk.isSuccessful() && resCk.body()!=null && resCk.body().isSuccess()) {
                                java.util.Map<String,String> c = new java.util.HashMap<>(); c.put("userId", user.getId());
                                api.createCart(auth.getAuthHeader(), c).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                                    @Override public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, retrofit2.Response<com.example.project.models.ApiResponse<Object>> resp) {
                                        java.util.Map<String,Object> add = new java.util.HashMap<>();
                                        add.put("userId", user.getId()); add.put("productId", bike.getId()); add.put("storeId", selectedStoreId[0]); add.put("quantity", qty[0]);
                                        api.addItemToCart(auth.getAuthHeader(), add).enqueue(new retrofit2.Callback<com.example.project.models.ApiResponse<Object>>() {
                                            @Override public void onResponse(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call2, retrofit2.Response<com.example.project.models.ApiResponse<Object>> r2) {
                                                android.widget.Toast.makeText(itemView.getContext(), r2.isSuccessful()? "Đã thêm vào giỏ" : "Thêm thất bại", android.widget.Toast.LENGTH_SHORT).show();
                                            }
                                            @Override public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call2, Throwable t2) { android.widget.Toast.makeText(itemView.getContext(), "Lỗi: "+t2.getMessage(), android.widget.Toast.LENGTH_SHORT).show(); }
                                        });
                                    }
                                    @Override public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> call, Throwable t) { android.widget.Toast.makeText(itemView.getContext(), "Lỗi: "+t.getMessage(), android.widget.Toast.LENGTH_SHORT).show(); }
                                });
                            } else {
                                android.widget.Toast.makeText(itemView.getContext(), "Số lượng vượt tồn kho", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onFailure(retrofit2.Call<com.example.project.models.ApiResponse<Object>> callCk, Throwable t) {
                            android.widget.Toast.makeText(itemView.getContext(), "Lỗi kiểm tra tồn kho: "+t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
        }
    }
}
