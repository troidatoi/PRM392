package com.example.project.network;

import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.models.LoginRequest;
import com.example.project.models.RegisterRequest;
import com.example.project.models.User;
import com.example.project.Store;
import com.example.project.OperatingHours;
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
    
    @GET("auth/check-duplicate")
    Call<ApiResponse<CheckDuplicateResponse>> checkDuplicate(
        @retrofit2.http.Query("username") String username,
        @retrofit2.http.Query("email") String email,
        @retrofit2.http.Query("phoneNumber") String phoneNumber
    );
    
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
    
    @GET("users/count/total")
    Call<TotalUsersResponse> getTotalUsers(
        @Header("Authorization") String token,
        @retrofit2.http.Query("role") String role,
        @retrofit2.http.Query("isActive") String isActive
    );
    
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
    
    @GET("bikes/count/total")
    Call<TotalBikesResponse> getTotalBikes(
        @retrofit2.http.Query("status") String status,
        @retrofit2.http.Query("category") String category,
        @retrofit2.http.Query("brand") String brand
    );
    
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

    @GET("inventory/turnover-ratio")
    Call<InventoryTurnoverResponse> getInventoryTurnover(
        @Header("Authorization") String token,
        @retrofit2.http.Query("startDate") String startDate,
        @retrofit2.http.Query("endDate") String endDate,
        @retrofit2.http.Query("storeId") String storeId,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("minStock") Integer minStock,
        @retrofit2.http.Query("sortBy") String sortBy
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
    
    @GET("payos/users/{userId}/pending-payments")
    Call<ApiResponse<Object>> getPendingPayments(
        @Header("Authorization") String token,
        @retrofit2.http.Path("userId") String userId
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
    Call<OrderResponse> getAllOrders(
        @Header("Authorization") String token,
        @retrofit2.http.Query("status") String status,
        @retrofit2.http.Query("page") Integer page,
        @retrofit2.http.Query("limit") Integer limit
    );
    
    @GET("orders/revenue/total")
    Call<RevenueResponse> getTotalRevenue(
        @Header("Authorization") String token,
        @retrofit2.http.Query("startDate") String startDate,
        @retrofit2.http.Query("endDate") String endDate,
        @retrofit2.http.Query("storeId") String storeId
    );
    
    @GET("orders/revenue/by-store")
    Call<RevenueByStoreResponse> getRevenueByStore(
        @Header("Authorization") String token,
        @retrofit2.http.Query("startDate") String startDate,
        @retrofit2.http.Query("endDate") String endDate,
        @retrofit2.http.Query("status") String status
    );
    
    @GET("orders/by-day-of-week")
    Call<OrdersByDayOfWeekResponse> getOrdersByDayOfWeek(
        @Header("Authorization") String token,
        @retrofit2.http.Query("startDate") String startDate,
        @retrofit2.http.Query("endDate") String endDate,
        @retrofit2.http.Query("storeId") String storeId,
        @retrofit2.http.Query("status") String status
    );
    
    @GET("orders/top-bikes")
    Call<TopBikesResponse> getTopBikes(
        @Header("Authorization") String token,
        @retrofit2.http.Query("limit") Integer limit,
        @retrofit2.http.Query("startDate") String startDate,
        @retrofit2.http.Query("endDate") String endDate,
        @retrofit2.http.Query("storeId") String storeId,
        @retrofit2.http.Query("status") String status
    );
    
    @PUT("orders/{orderId}/status")
    Call<ApiResponse<Object>> updateOrderStatus(
        @Header("Authorization") String token,
        @retrofit2.http.Path("orderId") String orderId,
        @Body java.util.Map<String, Object> body
    );
    
    @POST("orders/{orderId}/cancel")
    Call<ApiResponse<Object>> cancelOrder(
        @Header("Authorization") String token,
        @retrofit2.http.Path("orderId") String orderId,
        @Body java.util.Map<String, Object> body
    );
    
    // Check duplicate response model
    class CheckDuplicateResponse {
        private boolean usernameExists;
        private boolean emailExists;
        private boolean phoneExists;
        
        public CheckDuplicateResponse() {}
        
        public boolean isUsernameExists() {
            return usernameExists;
        }
        
        public void setUsernameExists(boolean usernameExists) {
            this.usernameExists = usernameExists;
        }
        
        public boolean isEmailExists() {
            return emailExists;
        }
        
        public void setEmailExists(boolean emailExists) {
            this.emailExists = emailExists;
        }
        
        public boolean isPhoneExists() {
            return phoneExists;
        }
        
        public void setPhoneExists(boolean phoneExists) {
            this.phoneExists = phoneExists;
        }
    }
    
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
            private boolean isActive;
            private OperatingHours operatingHours;
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

            public boolean isActive() {
                return isActive;
            }

            public void setActive(boolean active) {
                isActive = active;
            }

            public OperatingHours getOperatingHours() {
                return operatingHours;
            }

            public void setOperatingHours(OperatingHours operatingHours) {
                this.operatingHours = operatingHours;
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
    
    // Order Response class
    class OrderResponse {
        private boolean success;
        private String message;
        private int count;
        private int total;
        private int page;
        private int pages;
        private List<OrderData> data;
        
        public OrderResponse() {}
        
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
        
        public List<OrderData> getData() {
            return data;
        }
        
        public void setData(List<OrderData> data) {
            this.data = data;
        }
    }
    
    // Order Data class
    class OrderData {
        private String _id;
        private String orderNumber;
        private String userId;
        private String status;
        private double totalAmount;
        private String createdAt;
        private String updatedAt;
        private List<OrderItem> items;
        private UserInfo user;
        
        public OrderData() {}
        
        public String getId() {
            return _id;
        }
        
        public void setId(String _id) {
            this._id = _id;
        }
        
        public String getOrderNumber() {
            return orderNumber;
        }
        
        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public double getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public String getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
        
        public String getUpdatedAt() {
            return updatedAt;
        }
        
        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
        
        public List<OrderItem> getItems() {
            return items;
        }
        
        public void setItems(List<OrderItem> items) {
            this.items = items;
        }
        
        public UserInfo getUser() {
            return user;
        }
        
        public void setUser(UserInfo user) {
            this.user = user;
        }
    }
    
    // Order Item class
    class OrderItem {
        private String bikeId;
        private String bikeName;
        private int quantity;
        private double price;
        private ProductInfo product; // Product object from backend
        
        public OrderItem() {}
        
        public String getBikeId() {
            return bikeId;
        }
        
        public void setBikeId(String bikeId) {
            this.bikeId = bikeId;
        }
        
        public String getBikeName() {
            // Return product name if available, otherwise bikeName
            if (product != null && product.getName() != null) {
                return product.getName();
            }
            return bikeName;
        }
        
        public void setBikeName(String bikeName) {
            this.bikeName = bikeName;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            this.price = price;
        }
        
        public ProductInfo getProduct() {
            return product;
        }
        
        public void setProduct(ProductInfo product) {
            this.product = product;
        }
    }
    
    // Product Info class for order items
    class ProductInfo {
        private String _id;
        private String name;
        private java.util.List<String> images;
        private double price;
        
        public ProductInfo() {}
        
        public String getId() {
            return _id;
        }
        
        public void setId(String _id) {
            this._id = _id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public java.util.List<String> getImages() {
            return images;
        }
        
        public void setImages(java.util.List<String> images) {
            this.images = images;
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            this.price = price;
        }
    }
    
    // User Info class for order
    class UserInfo {
        private String _id;
        private String username;
        private String email;
        
        public UserInfo() {}
        
        public String getId() {
            return _id;
        }
        
        public void setId(String _id) {
            this._id = _id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    // Revenue Response class
    class RevenueResponse {
        private boolean success;
        private String message;
        private RevenueData data;
        
        public RevenueResponse() {}
        
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
        
        public RevenueData getData() {
            return data;
        }
        
        public void setData(RevenueData data) {
            this.data = data;
        }
    }
    
    // Revenue Data class
    class RevenueData {
        private double totalRevenue;
        private int totalOrders;
        private double averageOrderValue;
        private List<RevenueByStore> revenueByStore;
        private List<RevenueByMonth> revenueByMonth;
        private Period period;
        
        public RevenueData() {}
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public int getTotalOrders() {
            return totalOrders;
        }
        
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }
        
        public double getAverageOrderValue() {
            return averageOrderValue;
        }
        
        public void setAverageOrderValue(double averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
        }
        
        public List<RevenueByStore> getRevenueByStore() {
            return revenueByStore;
        }
        
        public void setRevenueByStore(List<RevenueByStore> revenueByStore) {
            this.revenueByStore = revenueByStore;
        }
        
        public List<RevenueByMonth> getRevenueByMonth() {
            return revenueByMonth;
        }
        
        public void setRevenueByMonth(List<RevenueByMonth> revenueByMonth) {
            this.revenueByMonth = revenueByMonth;
        }
        
        public Period getPeriod() {
            return period;
        }
        
        public void setPeriod(Period period) {
            this.period = period;
        }
    }
    
    // Revenue By Store class
    class RevenueByStore {
        private String storeId;
        private String storeName;
        private double totalRevenue;
        private int totalOrders;
        
        public RevenueByStore() {}
        
        public String getStoreId() {
            return storeId;
        }
        
        public void setStoreId(String storeId) {
            this.storeId = storeId;
        }
        
        public String getStoreName() {
            return storeName;
        }
        
        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public int getTotalOrders() {
            return totalOrders;
        }
        
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }
    }
    
    // Revenue By Month class
    class RevenueByMonth {
        private int year;
        private int month;
        private double totalRevenue;
        private int totalOrders;
        
        public RevenueByMonth() {}
        
        public int getYear() {
            return year;
        }
        
        public void setYear(int year) {
            this.year = year;
        }
        
        public int getMonth() {
            return month;
        }
        
        public void setMonth(int month) {
            this.month = month;
        }
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public int getTotalOrders() {
            return totalOrders;
        }
        
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }
    }
    
    // Period class
    class Period {
        private String startDate;
        private String endDate;
        private String storeId;
        
        public Period() {}
        
        public String getStartDate() {
            return startDate;
        }
        
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        
        public String getEndDate() {
            return endDate;
        }
        
        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
        
        public String getStoreId() {
            return storeId;
        }
        
        public void setStoreId(String storeId) {
            this.storeId = storeId;
        }
    }
    
    // Total Users Response class
    class TotalUsersResponse {
        private boolean success;
        private String message;
        private TotalUsersData data;
        
        public TotalUsersResponse() {}
        
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
        
        public TotalUsersData getData() {
            return data;
        }
        
        public void setData(TotalUsersData data) {
            this.data = data;
        }
    }
    
    // Total Users Data class
    class TotalUsersData {
        private int totalUsers;
        private int activeUsers;
        private int inactiveUsers;
        private List<UsersByRole> usersByRole;
        private Filters filters;
        
        public TotalUsersData() {}
        
        public int getTotalUsers() {
            return totalUsers;
        }
        
        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }
        
        public int getActiveUsers() {
            return activeUsers;
        }
        
        public void setActiveUsers(int activeUsers) {
            this.activeUsers = activeUsers;
        }
        
        public int getInactiveUsers() {
            return inactiveUsers;
        }
        
        public void setInactiveUsers(int inactiveUsers) {
            this.inactiveUsers = inactiveUsers;
        }
        
        public List<UsersByRole> getUsersByRole() {
            return usersByRole;
        }
        
        public void setUsersByRole(List<UsersByRole> usersByRole) {
            this.usersByRole = usersByRole;
        }
        
        public Filters getFilters() {
            return filters;
        }
        
        public void setFilters(Filters filters) {
            this.filters = filters;
        }
    }
    
    // Users By Role class
    class UsersByRole {
        private String role;
        private int count;
        
        public UsersByRole() {}
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
    
    // Filters class
    class Filters {
        private String role;
        private Boolean isActive;
        
        public Filters() {}
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public Boolean getIsActive() {
            return isActive;
        }
        
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }
    
    // Total Bikes Response class
    class TotalBikesResponse {
        private boolean success;
        private String message;
        private TotalBikesData data;
        
        public TotalBikesResponse() {}
        
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
        
        public TotalBikesData getData() {
            return data;
        }
        
        public void setData(TotalBikesData data) {
            this.data = data;
        }
    }
    
    // Total Bikes Data class
    class TotalBikesData {
        private int totalBikes;
        private int availableBikes;
        private int unavailableBikes;
        private List<BikesByStatus> bikesByStatus;
        private List<BikesByCategory> bikesByCategory;
        private BikeFilters filters;
        
        public TotalBikesData() {}
        
        public int getTotalBikes() {
            return totalBikes;
        }
        
        public void setTotalBikes(int totalBikes) {
            this.totalBikes = totalBikes;
        }
        
        public int getAvailableBikes() {
            return availableBikes;
        }
        
        public void setAvailableBikes(int availableBikes) {
            this.availableBikes = availableBikes;
        }
        
        public int getUnavailableBikes() {
            return unavailableBikes;
        }
        
        public void setUnavailableBikes(int unavailableBikes) {
            this.unavailableBikes = unavailableBikes;
        }
        
        public List<BikesByStatus> getBikesByStatus() {
            return bikesByStatus;
        }
        
        public void setBikesByStatus(List<BikesByStatus> bikesByStatus) {
            this.bikesByStatus = bikesByStatus;
        }
        
        public List<BikesByCategory> getBikesByCategory() {
            return bikesByCategory;
        }
        
        public void setBikesByCategory(List<BikesByCategory> bikesByCategory) {
            this.bikesByCategory = bikesByCategory;
        }
        
        public BikeFilters getFilters() {
            return filters;
        }
        
        public void setFilters(BikeFilters filters) {
            this.filters = filters;
        }
    }
    
    // Bikes By Status class
    class BikesByStatus {
        private String status;
        private int count;
        
        public BikesByStatus() {}
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
    
    // Bikes By Category class
    class BikesByCategory {
        private String category;
        private int count;
        
        public BikesByCategory() {}
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
    
    // Bike Filters class
    class BikeFilters {
        private String status;
        private String category;
        private String brand;
        
        public BikeFilters() {}
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getBrand() {
            return brand;
        }
        
        public void setBrand(String brand) {
            this.brand = brand;
        }
    }
    
    // Orders By Day Of Week Response class
    class OrdersByDayOfWeekResponse {
        private boolean success;
        private String message;
        private OrdersByDayOfWeekData data;
        
        public OrdersByDayOfWeekResponse() {}
        
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
        
        public OrdersByDayOfWeekData getData() {
            return data;
        }
        
        public void setData(OrdersByDayOfWeekData data) {
            this.data = data;
        }
    }
    
    // Orders By Day Of Week Data class
    class OrdersByDayOfWeekData {
        private List<OrderByDay> ordersByDay;
        private Period period;
        
        public OrdersByDayOfWeekData() {}
        
        public List<OrderByDay> getOrdersByDay() {
            return ordersByDay;
        }
        
        public void setOrdersByDay(List<OrderByDay> ordersByDay) {
            this.ordersByDay = ordersByDay;
        }
        
        public Period getPeriod() {
            return period;
        }
        
        public void setPeriod(Period period) {
            this.period = period;
        }
    }
    
    // Order By Day class
    class OrderByDay {
        private int dayOfWeek;
        private String dayName;
        private int totalOrders;
        private double totalRevenue;
        private double averageOrderValue;
        
        public OrderByDay() {}
        
        public int getDayOfWeek() {
            return dayOfWeek;
        }
        
        public void setDayOfWeek(int dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }
        
        public String getDayName() {
            return dayName;
        }
        
        public void setDayName(String dayName) {
            this.dayName = dayName;
        }
        
        public int getTotalOrders() {
            return totalOrders;
        }
        
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public double getAverageOrderValue() {
            return averageOrderValue;
        }
        
        public void setAverageOrderValue(double averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
        }
    }
    
    // Top Bikes Response class
    class TopBikesResponse {
        private boolean success;
        private String message;
        private TopBikesData data;
        
        public TopBikesResponse() {}
        
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
        
        public TopBikesData getData() {
            return data;
        }
        
        public void setData(TopBikesData data) {
            this.data = data;
        }
    }
    
    // Top Bikes Data class
    class TopBikesData {
        private List<TopBike> topBikes;
        private int limit;
        private Period period;
        
        public TopBikesData() {}
        
        public List<TopBike> getTopBikes() {
            return topBikes;
        }
        
        public void setTopBikes(List<TopBike> topBikes) {
            this.topBikes = topBikes;
        }
        
        public int getLimit() {
            return limit;
        }
        
        public void setLimit(int limit) {
            this.limit = limit;
        }
        
        public Period getPeriod() {
            return period;
        }
        
        public void setPeriod(Period period) {
            this.period = period;
        }
    }
    
    // Top Bike class
    class TopBike {
        private String bikeId;
        private String bikeName;
        private String bikeBrand;
        private BikeImage bikeImage;
        private double bikePrice;
        private String bikeCategory;
        private int totalOrders;
        private int totalQuantity;
        private double totalRevenue;
        private double averageQuantity;
        
        public TopBike() {}
        
        public String getBikeId() {
            return bikeId;
        }
        
        public void setBikeId(String bikeId) {
            this.bikeId = bikeId;
        }
        
        public String getBikeName() {
            return bikeName;
        }
        
        public void setBikeName(String bikeName) {
            this.bikeName = bikeName;
        }
        
        public String getBikeBrand() {
            return bikeBrand;
        }
        
        public void setBikeBrand(String bikeBrand) {
            this.bikeBrand = bikeBrand;
        }
        
        public BikeImage getBikeImage() {
            return bikeImage;
        }
        
        public void setBikeImage(BikeImage bikeImage) {
            this.bikeImage = bikeImage;
        }
        
        // Helper method to get image URL
        public String getBikeImageUrl() {
            if (bikeImage != null && bikeImage.getUrl() != null) {
                return bikeImage.getUrl();
            }
            return null;
        }
        
        public double getBikePrice() {
            return bikePrice;
        }
        
        public void setBikePrice(double bikePrice) {
            this.bikePrice = bikePrice;
        }
        
        public String getBikeCategory() {
            return bikeCategory;
        }
        
        public void setBikeCategory(String bikeCategory) {
            this.bikeCategory = bikeCategory;
        }
        
        public int getTotalOrders() {
            return totalOrders;
        }
        
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }
        
        public int getTotalQuantity() {
            return totalQuantity;
        }
        
        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public double getAverageQuantity() {
            return averageQuantity;
        }
        
        public void setAverageQuantity(double averageQuantity) {
            this.averageQuantity = averageQuantity;
        }
    }
    
    // Bike Image class
    class BikeImage {
        private String url;
        private String alt;
        private String _id;
        
        public BikeImage() {}
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getAlt() {
            return alt;
        }
        
        public void setAlt(String alt) {
            this.alt = alt;
        }
        
        public String get_id() {
            return _id;
        }
        
        public void set_id(String _id) {
            this._id = _id;
        }
    }
    
    // Revenue By Store Response class
    class RevenueByStoreResponse {
        private boolean success;
        private String message;
        private RevenueByStoreData data;
        
        public RevenueByStoreResponse() {}
        
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
        
        public RevenueByStoreData getData() {
            return data;
        }
        
        public void setData(RevenueByStoreData data) {
            this.data = data;
        }
    }
    
    // Revenue By Store Data class
    class RevenueByStoreData {
        private List<StoreRevenue> revenueByStore;
        private RevenueSummary summary;
        private Period period;
        
        public RevenueByStoreData() {}
        
        public List<StoreRevenue> getRevenueByStore() {
            return revenueByStore;
        }
        
        public void setRevenueByStore(List<StoreRevenue> revenueByStore) {
            this.revenueByStore = revenueByStore;
        }
        
        public RevenueSummary getSummary() {
            return summary;
        }
        
        public void setSummary(RevenueSummary summary) {
            this.summary = summary;
        }
        
        public Period getPeriod() {
            return period;
        }
        
        public void setPeriod(Period period) {
            this.period = period;
        }
    }
    
    // Store Revenue class
    class StoreRevenue {
        private String storeId;
        private String storeName;
        private String storeAddress;
        private String storeCity;
        private double totalRevenue;
        private int totalOrders;
        private double averageOrderValue;
        
        public StoreRevenue() {}
        
        public String getStoreId() {
            return storeId;
        }
        
        public void setStoreId(String storeId) {
            this.storeId = storeId;
        }
        
        public String getStoreName() {
            return storeName;
        }
        
        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }
        
        public String getStoreAddress() {
            return storeAddress;
        }
        
        public void setStoreAddress(String storeAddress) {
            this.storeAddress = storeAddress;
        }
        
        public String getStoreCity() {
            return storeCity;
        }
        
        public void setStoreCity(String storeCity) {
            this.storeCity = storeCity;
        }
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public int getTotalOrders() {
            return totalOrders;
        }
        
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }
        
        public double getAverageOrderValue() {
            return averageOrderValue;
        }
        
        public void setAverageOrderValue(double averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
        }
    }
    
    // Revenue Summary class
    class RevenueSummary {
        private double totalRevenue;
        private int totalOrders;
        private int storeCount;
        
        public RevenueSummary() {}
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public int getTotalOrders() {
            return totalOrders;
        }
        
        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }
        
        public int getStoreCount() {
            return storeCount;
        }
        
        public void setStoreCount(int storeCount) {
            this.storeCount = storeCount;
        }
    }
    
    // Inventory Turnover Response
    class InventoryTurnoverResponse {
        private boolean success;
        private String message;
        private InventoryTurnoverData data;
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public InventoryTurnoverData getData() { return data; }
        public void setData(InventoryTurnoverData data) { this.data = data; }
    }
    
    class InventoryTurnoverData {
        private List<ProductTurnover> products;
        private Summary summary;
        private Period period;
        
        public List<ProductTurnover> getProducts() { return products; }
        public void setProducts(List<ProductTurnover> products) { this.products = products; }
        
        public Summary getSummary() { return summary; }
        public void setSummary(Summary summary) { this.summary = summary; }
        
        public Period getPeriod() { return period; }
        public void setPeriod(Period period) { this.period = period; }
    }
    
    class ProductTurnover {
        private String productId;
        private String productName;
        private String productBrand;
        private String productCategory;
        private String imageUrl;
        private int currentStock;
        private int totalStockAcrossStores;
        private int totalQuantitySold;
        private int totalOrders;
        private double turnoverRatio;
        private double daysToSellOut;
        private double totalRevenue;
        private double averagePrice;
        
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getProductBrand() { return productBrand; }
        public void setProductBrand(String productBrand) { this.productBrand = productBrand; }
        
        public String getProductCategory() { return productCategory; }
        public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public int getCurrentStock() { return currentStock; }
        public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
        
        public int getTotalStockAcrossStores() { return totalStockAcrossStores; }
        public void setTotalStockAcrossStores(int totalStockAcrossStores) { this.totalStockAcrossStores = totalStockAcrossStores; }
        
        public int getTotalQuantitySold() { return totalQuantitySold; }
        public void setTotalQuantitySold(int totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }
        
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        
        public double getTurnoverRatio() { return turnoverRatio; }
        public void setTurnoverRatio(double turnoverRatio) { this.turnoverRatio = turnoverRatio; }
        
        public double getDaysToSellOut() { return daysToSellOut; }
        public void setDaysToSellOut(double daysToSellOut) { this.daysToSellOut = daysToSellOut; }
        
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public double getAveragePrice() { return averagePrice; }
        public void setAveragePrice(double averagePrice) { this.averagePrice = averagePrice; }
    }
    
    class Summary {
        private double averageTurnoverRatio;
        private int totalProducts;
        private int fastMovingProducts;
        private int slowMovingProducts;
        private int outOfStockProducts;
        
        public double getAverageTurnoverRatio() { return averageTurnoverRatio; }
        public void setAverageTurnoverRatio(double averageTurnoverRatio) { this.averageTurnoverRatio = averageTurnoverRatio; }
        
        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
        
        public int getFastMovingProducts() { return fastMovingProducts; }
        public void setFastMovingProducts(int fastMovingProducts) { this.fastMovingProducts = fastMovingProducts; }
        
        public int getSlowMovingProducts() { return slowMovingProducts; }
        public void setSlowMovingProducts(int slowMovingProducts) { this.slowMovingProducts = slowMovingProducts; }
        
        public int getOutOfStockProducts() { return outOfStockProducts; }
        public void setOutOfStockProducts(int outOfStockProducts) { this.outOfStockProducts = outOfStockProducts; }
    }
}
