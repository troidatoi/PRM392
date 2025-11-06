package com.example.project.network;

import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.models.LoginRequest;
import com.example.project.models.RegisterRequest;
import com.example.project.models.User;
import com.example.project.Store;
import com.example.project.ChatMessage;
import com.example.project.ChatConversation;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {
    
    // Auth endpoints
    @POST("auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest request);
    
    @POST("auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest request);
    
    @POST("auth/logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String token);
    
    @GET("auth/me")
    Call<ApiResponse<User>> getMe(@Header("Authorization") String token);
    
    @PUT("auth/me")
    Call<ApiResponse<User>> updateProfile(@Header("Authorization") String token, @Body User user);
    
    @PUT("auth/me/change-password")
    Call<ApiResponse<Void>> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest request);
    
    // User management endpoints (Admin/Staff only)
    @GET("users")
    Call<ApiResponse<User[]>> getUsers(@Header("Authorization") String token);
    
    @GET("users/{id}")
    Call<ApiResponse<User>> getUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId);
    
    @PUT("users/{id}")
    Call<ApiResponse<User>> updateUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId, @Body java.util.Map<String, Object> body);
    
    @retrofit2.http.DELETE("users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Header("Authorization") String token, @retrofit2.http.Path("id") String userId);
    
    // Bike endpoints
    @GET("bikes")
    Call<ApiResponse<Bike[]>> getBikes(@retrofit2.http.Query("page") int page,
                                       @retrofit2.http.Query("limit") int limit,
                                       @retrofit2.http.Query("category") String category,
                                       @retrofit2.http.Query("status") String status,
                                       @retrofit2.http.Query("brand") String brand,
                                       @retrofit2.http.Query("minPrice") Double minPrice,
                                       @retrofit2.http.Query("maxPrice") Double maxPrice,
                                       @retrofit2.http.Query("search") String search,
                                       @retrofit2.http.Query("sortBy") String sortBy);
    
    @GET("bikes/{id}")
    Call<ApiResponse<Bike>> getBikeById(@retrofit2.http.Path("id") String bikeId);
    
    @Multipart
    @POST("bikes")
    Call<ApiResponse<Bike>> createBike(
        @Header("Authorization") String token,
        @Part("name") RequestBody name,
        @Part("brand") RequestBody brand,
        @Part("model") RequestBody model,
        @Part("price") RequestBody price,
        @Part("description") RequestBody description,
        @Part("color") RequestBody color,
        @Part("category") RequestBody category,
        @Part("status") RequestBody status,
        @Part("battery") RequestBody battery,
        @Part("motor") RequestBody motor,
        @Part("range") RequestBody range,
        @Part("maxSpeed") RequestBody maxSpeed,
        @Part("weight") RequestBody weight,
        @Part("chargingTime") RequestBody chargingTime,
        @Part("features") RequestBody features,
        @Part("warranty") RequestBody warranty,
        @Part("originalPrice") RequestBody originalPrice,
        @Part("tags") RequestBody tags,
        @Part List<MultipartBody.Part> images
    );
    
    @Multipart
    @PUT("bikes/{id}")
    Call<ApiResponse<Bike>> updateBike(
        @Header("Authorization") String token,
        @retrofit2.http.Path("id") String bikeId,
        @Part("name") RequestBody name,
        @Part("brand") RequestBody brand,
        @Part("model") RequestBody model,
        @Part("price") RequestBody price,
        @Part("description") RequestBody description,
        @Part("color") RequestBody color,
        @Part("category") RequestBody category,
        @Part("status") RequestBody status,
        @Part("battery") RequestBody battery,
        @Part("motor") RequestBody motor,
        @Part("range") RequestBody range,
        @Part("maxSpeed") RequestBody maxSpeed,
        @Part("weight") RequestBody weight,
        @Part("chargingTime") RequestBody chargingTime,
        @Part("features") RequestBody features,
        @Part("warranty") RequestBody warranty,
        @Part("originalPrice") RequestBody originalPrice,
        @Part("tags") RequestBody tags,
        @Part List<MultipartBody.Part> images
    );
    
    @retrofit2.http.DELETE("bikes/{id}")
    Call<ApiResponse<Void>> deleteBike(@Header("Authorization") String token, @retrofit2.http.Path("id") String bikeId);
    
    @GET("bikes/featured/list")
    Call<ApiResponse<Bike[]>> getFeaturedBikes(@retrofit2.http.Query("limit") int limit);
    
    @GET("bikes/categories/list")
    Call<ApiResponse<Object[]>> getCategories();
    
    // Cart endpoints
    @POST("cart/create")
    Call<ApiResponse<Object>> createCart(@Header("Authorization") String token,
                                         @Body java.util.Map<String, String> body);

    @POST("inventory/check-availability")
    Call<ApiResponse<Object>> checkAvailability(@Header("Authorization") String token,
                                                @Body java.util.Map<String, Object> body);

    @POST("cart/add-item")
    Call<ApiResponse<Object>> addItemToCart(@Header("Authorization") String token,
                                            @Body java.util.Map<String, Object> body);

    @GET("cart/user/{userId}")
    Call<ApiResponse<Object>> getCartByUser(@Header("Authorization") String token,
                                            @retrofit2.http.Path("userId") String userId);

    @PUT("cart/update-quantity/{itemId}")
    Call<ApiResponse<Object>> updateCartItemQuantity(@Header("Authorization") String token,
                                                    @retrofit2.http.Path("itemId") String itemId,
                                                    @Body java.util.Map<String, Object> body);

    @retrofit2.http.DELETE("cart/remove-item/{itemId}")
    Call<ApiResponse<Object>> removeCartItem(@Header("Authorization") String token,
                                            @retrofit2.http.Path("itemId") String itemId);

    // Inventory endpoints
    @GET("inventory/product/{productId}")
    Call<ApiResponse<java.util.List<Object>>> getProductInventory(
        @retrofit2.http.Path("productId") String productId
    );
    
    @GET("inventory/store/{storeId}")
    Call<ApiResponse<Object>> getInventoryByStore(
        @Header("Authorization") String token,
        @retrofit2.http.Path("storeId") String storeId,
        @retrofit2.http.Query("page") Integer page,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("status") String status
    );

    @POST("inventory/add")
    Call<ApiResponse<Object>> addStock(
        @Header("Authorization") String token,
        @Body java.util.Map<String, Object> body
    );

    @POST("inventory/reduce")
    Call<ApiResponse<Object>> reduceStock(
        @Header("Authorization") String token,
        @Body java.util.Map<String, Object> body
    );

    // Store endpoints
    @GET("stores")
    Call<StoreResponse> getStores(
        @retrofit2.http.Query("city") String city,
        @retrofit2.http.Query("district") String district,
        @retrofit2.http.Query("storeType") String storeType,
        @retrofit2.http.Query("isActive") Boolean isActive,
        @retrofit2.http.Query("page") int page,
        @retrofit2.http.Query("limit") int limit,
        @retrofit2.http.Query("sort") String sort
    );
    
    @GET("stores/{id}")
    Call<ApiResponse<Store>> getStoreById(@retrofit2.http.Path("id") String storeId);
    
    @POST("stores")
    Call<ApiResponse<Store>> createStore(@Header("Authorization") String token, @Body Store store);
    
    @PUT("stores/{id}")
    Call<ApiResponse<Store>> updateStore(@Header("Authorization") String token, 
                                       @retrofit2.http.Path("id") String storeId, 
                                       @Body Store store);
    
    @retrofit2.http.DELETE("stores/{id}")
    Call<ApiResponse<Void>> deleteStore(@Header("Authorization") String token, 
                                      @retrofit2.http.Path("id") String storeId);
    
    @POST("stores/nearby")
    Call<ApiResponse<Store[]>> getNearbyStores(@Body NearbyStoreRequest request);
    
    // Store locations/map endpoints (for displaying all stores)
    @GET("locations/map")
    Call<MapStoresResponse> getStoresForMap(
        @retrofit2.http.Query("lat") Double lat,
        @retrofit2.http.Query("lng") Double lng,
        @retrofit2.http.Query("address") String address,
        @retrofit2.http.Query("radius") Double radius
    );
    
    // Geocode endpoint (convert address to coordinates)
    @POST("locations/geocode")
    Call<ApiResponse<Object>> geocodeAddress(@Body java.util.Map<String, String> body);

    // Order endpoints
    @POST("orders/create")
    Call<ApiResponse<Object>> createOrders(
        @Header("Authorization") String token,
        @Body java.util.Map<String, Object> body
    );
    
    @POST("orders/estimate")
    Call<ApiResponse<Object>> estimateOrders(
        @Header("Authorization") String token,
        @Body java.util.Map<String, Object> body
    );
    
    // PayOS endpoints
    @POST("payos/orders/{orderId}/create-link")
    Call<ApiResponse<Object>> createPayOSPaymentLink(
        @Header("Authorization") String token,
        @retrofit2.http.Path("orderId") String orderId,
        @Body java.util.Map<String, Object> body
    );
    
    @POST("payos/orders/{orderId}/confirm-payment")
    Call<ApiResponse<Object>> confirmPayOSPayment(
        @Header("Authorization") String token,
        @retrofit2.http.Path("orderId") String orderId,
        @Body java.util.Map<String, Object> body
    );
    
    @GET("orders/user/{userId}")
    Call<ApiResponse<Object>> getUserOrders(
        @Header("Authorization") String token,
        @retrofit2.http.Path("userId") String userId,
        @retrofit2.http.Query("status") String status,
        @retrofit2.http.Query("page") Integer page,
        @retrofit2.http.Query("limit") Integer limit
    );
    
    @GET("orders/{orderId}")
    Call<ApiResponse<Object>> getOrderDetails(
        @Header("Authorization") String token,
        @retrofit2.http.Path("orderId") String orderId
    );
    
    @GET("orders")
    Call<ApiResponse<Object>> getAllOrders(
        @Header("Authorization") String token,
        @retrofit2.http.Query("status") String status,
        @retrofit2.http.Query("page") Integer page,
        @retrofit2.http.Query("limit") Integer limit
    );
    
    @PUT("orders/{orderId}/status")
    Call<ApiResponse<Object>> updateOrderStatus(
        @Header("Authorization") String token,
        @retrofit2.http.Path("orderId") String orderId,
        @Body java.util.Map<String, Object> body
    );
    
    // Change password request model
    class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        
        public ChangePasswordRequest() {}
        
        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
        
        public String getCurrentPassword() {
            return currentPassword;
        }
        
        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
    
    // Store response model for pagination
    class StoreResponse {
        private boolean success;
        private String message;
        private int count;
        private int total;
        private int page;
        private int pages;
        private List<Store> data;
        private Store singleData; // For single store response
        
        public StoreResponse() {}
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public int getPage() {
            return page;
        }
        
        public void setPage(int page) {
            this.page = page;
        }
        
        public int getPages() {
            return pages;
        }
        
        public void setPages(int pages) {
            this.pages = pages;
        }
        
        public List<Store> getData() {
            return data;
        }
        
        public void setData(List<Store> data) {
            this.data = data;
        }
        
        public Store getSingleData() {
            return singleData;
        }
        
        public void setSingleData(Store singleData) {
            this.singleData = singleData;
        }
    }
    
    // Nearby store request model
    class NearbyStoreRequest {
        private double lat;
        private double lng;
        private double radius;
        private int limit;
        
        public NearbyStoreRequest() {}
        
        public NearbyStoreRequest(double lat, double lng, double radius, int limit) {
            this.lat = lat;
            this.lng = lng;
            this.radius = radius;
            this.limit = limit;
        }
        
        public double getLat() {
            return lat;
        }
        
        public void setLat(double lat) {
            this.lat = lat;
        }
        
        public double getLng() {
            return lng;
        }
        
        public void setLng(double lng) {
            this.lng = lng;
        }
        
        public double getRadius() {
            return radius;
        }
        
        public void setRadius(double radius) {
            this.radius = radius;
        }
        
        public int getLimit() {
            return limit;
        }
        
        public void setLimit(int limit) {
            this.limit = limit;
        }
    }
    
    // Map stores response model
    class MapStoresResponse {
        private boolean success;
        private String message;
        private int count;
        private UserLocation userLocation;
        private Double searchRadius;
        private List<MapStore> data;
        
        public MapStoresResponse() {}
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public UserLocation getUserLocation() {
            return userLocation;
        }
        
        public void setUserLocation(UserLocation userLocation) {
            this.userLocation = userLocation;
        }
        
        public Double getSearchRadius() {
            return searchRadius;
        }
        
        public void setSearchRadius(Double searchRadius) {
            this.searchRadius = searchRadius;
        }
        
        public List<MapStore> getData() {
            return data;
        }
        
        public void setData(List<MapStore> data) {
            this.data = data;
        }
        
        public static class UserLocation {
            private Double latitude;
            private Double longitude;
            private String address;
            
            public Double getLatitude() {
                return latitude;
            }
            
            public void setLatitude(Double latitude) {
                this.latitude = latitude;
            }
            
            public Double getLongitude() {
                return longitude;
            }
            
            public void setLongitude(Double longitude) {
                this.longitude = longitude;
            }
            
            public String getAddress() {
                return address;
            }
            
            public void setAddress(String address) {
                this.address = address;
            }
        }
        
        public static class MapStore {
            @com.google.gson.annotations.SerializedName("id")
            private String id;
            private String name;
            private double latitude;
            private double longitude;
            private String address;
            private String city;
            private String phone;
            @com.google.gson.annotations.SerializedName("isOpenNow")
            private boolean isOpenNow;
            private Double distance;
            
            public String getId() {
                return id;
            }
            
            public void setId(String id) {
                this.id = id;
            }
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public double getLatitude() {
                return latitude;
            }
            
            public void setLatitude(double latitude) {
                this.latitude = latitude;
            }
            
            public double getLongitude() {
                return longitude;
            }
            
            public void setLongitude(double longitude) {
                this.longitude = longitude;
            }
            
            public String getAddress() {
                return address;
            }
            
            public void setAddress(String address) {
                this.address = address;
            }
            
            public String getCity() {
                return city;
            }
            
            public void setCity(String city) {
                this.city = city;
            }
            
            public String getPhone() {
                return phone;
            }
            
            public void setPhone(String phone) {
                this.phone = phone;
            }
            
            public boolean isOpenNow() {
                return isOpenNow;
            }
            
            public void setOpenNow(boolean openNow) {
                isOpenNow = openNow;
            }
            
            public Double getDistance() {
                return distance;
            }
            
            public void setDistance(Double distance) {
                this.distance = distance;
            }
            
            public String getFullAddress() {
                if (city != null && !city.isEmpty()) {
                    return address + ", " + city;
                }
                return address != null ? address : "";
            }
        }
    }
    
    // Chat endpoints
    @GET("chat/conversations")
    Call<ApiResponse<ChatConversation[]>> getChatConversations(@Header("Authorization") String token);
    
    @GET("chat/messages/{userId}")
    Call<ApiResponse<ChatMessage[]>> getChatMessages(
        @Header("Authorization") String token,
        @retrofit2.http.Path("userId") String userId,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("before") String before
    );
    
    @GET("chat/online-users")
    Call<ApiResponse<User[]>> getOnlineUsers(@Header("Authorization") String token);
    
    @PUT("chat/read-all")
    Call<ApiResponse<Void>> markAllAsRead(
        @Header("Authorization") String token,
        @Body MarkReadRequest request
    );
    
    // Shipping Rate endpoints
    @GET("shipping-rates")
    Call<ApiResponse<ShippingRate[]>> getShippingRates(@retrofit2.http.Query("activeOnly") String activeOnly);
    
    @GET("shipping-rates/{id}")
    Call<ApiResponse<ShippingRate>> getShippingRateById(@retrofit2.http.Path("id") String id);
    
    @POST("shipping-rates")
    Call<ApiResponse<ShippingRate>> createShippingRate(@Header("Authorization") String token, @Body ShippingRate rate);
    
    @PUT("shipping-rates/{id}")
    Call<ApiResponse<ShippingRate>> updateShippingRate(@Header("Authorization") String token, @retrofit2.http.Path("id") String id, @Body ShippingRate rate);
    
    @retrofit2.http.DELETE("shipping-rates/{id}")
    Call<ApiResponse<Void>> deleteShippingRate(@Header("Authorization") String token, @retrofit2.http.Path("id") String id);
    
    @POST("shipping-rates/calculate")
    Call<ApiResponse<CalculateShippingResponse>> calculateShippingFee(@Body java.util.Map<String, Object> body);
    
    // ShippingRate model
    class ShippingRate {
        @com.google.gson.annotations.SerializedName("_id")
        private String id;
        private double minDistance;
        private Double maxDistance;
        private double pricePerKm;
        private String note;
        private boolean isActive;
        private int order;
        
        // Constructors
        public ShippingRate() {}
        
        public ShippingRate(double minDistance, Double maxDistance, double pricePerKm, String note, int order) {
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.pricePerKm = pricePerKm;
            this.note = note;
            this.order = order;
            this.isActive = true;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public double getMinDistance() { return minDistance; }
        public void setMinDistance(double minDistance) { this.minDistance = minDistance; }
        
        public Double getMaxDistance() { return maxDistance; }
        public void setMaxDistance(Double maxDistance) { this.maxDistance = maxDistance; }
        
        public double getPricePerKm() { return pricePerKm; }
        public void setPricePerKm(double pricePerKm) { this.pricePerKm = pricePerKm; }
        
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        
        public String getDistanceRange() {
            if (maxDistance == null) {
                return minDistance + "+ km";
            }
            return minDistance + " - " + maxDistance + " km";
        }
    }
    
    // Calculate shipping response
    class CalculateShippingResponse {
        private double distanceKm;
        private double roundedDistanceKm;
        private double shippingFee;
        
        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
        
        public double getRoundedDistanceKm() { return roundedDistanceKm; }
        public void setRoundedDistanceKm(double roundedDistanceKm) { this.roundedDistanceKm = roundedDistanceKm; }
        
        public double getShippingFee() { return shippingFee; }
        public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    }
    
    // Request classes for Chat
    class MarkReadRequest {
        private String senderId;
        
        public MarkReadRequest(String senderId) {
            this.senderId = senderId;
        }
        
        public String getSenderId() {
            return senderId;
        }
        
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
    }
}
