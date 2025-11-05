package com.example.project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.api.IMapController;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.project.models.ApiResponse;
import com.example.project.models.Bike;
import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private RecyclerView rvStores;
    private ProductAdapter productAdapter;
    private StoreAdapter storeAdapter;
    private List<Product> productList;
    private List<Store> storeList;
    private ApiService apiService;

    // OpenStreetMap
    private MapView mapView;
    private IMapController mapController;
    private List<ApiService.MapStoresResponse.MapStore> mapStoresList;
    
    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double userLatitude;
    private double userLongitude;
    private boolean locationObtained = false;

    // Bottom Navigation
    private View navHome, navProducts, navCart, navAccount;
    private ImageView iconHome, iconProducts, iconCart, iconAccount;
    private TextView tvHome, tvProducts, tvCart, tvAccount;

    // Floating Chat Button
    private CardView btnFloatingChat;

    // Search Box
    private EditText etSearch;
    private CardView searchCard;
    
    // Stores Section Header
    private TextView tvStoresTitle;
    private TextView tvStoresSubtitle;
    private CardView btnLocationInput;
    
    // Greeting TextView
    private TextView tvGreeting;

    private static final String COLOR_BROWN = "#7B6047";
    private static final String COLOR_BE = "#CEB797";
    
    // Default location (Ho Chi Minh City)
    private static final double DEFAULT_LAT = 10.7769;
    private static final double DEFAULT_LNG = 106.7009;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize OSMDroid configuration first
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        
        // Initialize Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        initViews();
        setupSearchBox();
        setupRecyclerView();
        setupMap();
        setupBottomNavigation();
        setupChatButton();
        
        // Initialize API service
        apiService = RetrofitClient.getInstance().getApiService();
        
        // Load user info for greeting
        loadUserInfo();
        
        // Load bikes from API
        loadBikes();
        
        // Load stores without location filter initially (optional)
        loadStores(null, null);
    }
    
    // Show dialog for manual address input with dropdowns
    private void showLocationInputDialog() {
        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_location_input, null);
        
        Spinner spinnerProvince = dialogView.findViewById(R.id.spinnerProvince);
        Spinner spinnerDistrict = dialogView.findViewById(R.id.spinnerDistrict);
        EditText etStreet = dialogView.findViewById(R.id.etStreet);
        
        // Province data (major cities in Vietnam)
        String[] provinces = {
            "Chọn tỉnh/thành phố",
            "TP. Hồ Chí Minh",
            "Hà Nội",
            "Đà Nẵng",
            "Hải Phòng",
            "Cần Thơ",
            "An Giang",
            "Bà Rịa - Vũng Tàu",
            "Bắc Giang",
            "Bắc Kạn",
            "Bạc Liêu",
            "Bắc Ninh",
            "Bến Tre",
            "Bình Định",
            "Bình Dương",
            "Bình Phước",
            "Bình Thuận",
            "Cà Mau",
            "Cao Bằng",
            "Đắk Lắk",
            "Đắk Nông",
            "Điện Biên",
            "Đồng Nai",
            "Đồng Tháp",
            "Gia Lai",
            "Hà Giang",
            "Hà Nam",
            "Hà Tĩnh",
            "Hải Dương",
            "Hậu Giang",
            "Hòa Bình",
            "Hưng Yên",
            "Khánh Hòa",
            "Kiên Giang",
            "Kon Tum",
            "Lai Châu",
            "Lâm Đồng",
            "Lạng Sơn",
            "Lào Cai",
            "Long An",
            "Nam Định",
            "Nghệ An",
            "Ninh Bình",
            "Ninh Thuận",
            "Phú Thọ",
            "Phú Yên",
            "Quảng Bình",
            "Quảng Nam",
            "Quảng Ngãi",
            "Quảng Ninh",
            "Quảng Trị",
            "Sóc Trăng",
            "Sơn La",
            "Tây Ninh",
            "Thái Bình",
            "Thái Nguyên",
            "Thanh Hóa",
            "Thừa Thiên Huế",
            "Tiền Giang",
            "Trà Vinh",
            "Tuyên Quang",
            "Vĩnh Long",
            "Vĩnh Phúc",
            "Yên Bái"
        };
        
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);
        
        // District data - will be updated based on selected province
        String[] defaultDistricts = {"Chọn quận/huyện trước"};
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, defaultDistricts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);
        spinnerDistrict.setEnabled(false);
        
        // Map of provinces to districts
        java.util.Map<String, String[]> provinceDistricts = new java.util.HashMap<>();
        
        // TP.HCM districts
        provinceDistricts.put("TP. Hồ Chí Minh", new String[]{
            "Quận 1", "Quận 2", "Quận 3", "Quận 4", "Quận 5", "Quận 6", "Quận 7", 
            "Quận 8", "Quận 9", "Quận 10", "Quận 11", "Quận 12", "Quận Bình Tân",
            "Quận Bình Thạnh", "Quận Gò Vấp", "Quận Phú Nhuận", "Quận Tân Bình",
            "Quận Tân Phú", "Quận Thủ Đức", "Huyện Bình Chánh", "Huyện Cần Giờ",
            "Huyện Củ Chi", "Huyện Hóc Môn", "Huyện Nhà Bè"
        });
        
        // Hà Nội districts
        provinceDistricts.put("Hà Nội", new String[]{
            "Quận Ba Đình", "Quận Hoàn Kiếm", "Quận Tây Hồ", "Quận Long Biên",
            "Quận Cầu Giấy", "Quận Đống Đa", "Quận Hai Bà Trưng", "Quận Hoàng Mai",
            "Quận Thanh Xuân", "Huyện Sóc Sơn", "Huyện Đông Anh", "Huyện Gia Lâm",
            "Huyện Nam Từ Liêm", "Huyện Bắc Từ Liêm", "Huyện Mê Linh", "Huyện Hà Đông",
            "Huyện Sơn Tây", "Huyện Ba Vì", "Huyện Phúc Thọ", "Huyện Đan Phượng",
            "Huyện Hoài Đức", "Huyện Quốc Oai", "Huyện Thạch Thất", "Huyện Chương Mỹ",
            "Huyện Thanh Oai", "Huyện Thường Tín", "Huyện Phú Xuyên", "Huyện Ứng Hòa",
            "Huyện Mỹ Đức"
        });
        
        // Đà Nẵng districts
        provinceDistricts.put("Đà Nẵng", new String[]{
            "Quận Hải Châu", "Quận Thanh Khê", "Quận Sơn Trà", "Quận Ngũ Hành Sơn",
            "Quận Liên Chiểu", "Quận Cẩm Lệ", "Huyện Hòa Vang", "Huyện Hoàng Sa"
        });
        
        // Update district spinner when province changes
        spinnerProvince.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedProvince = (String) spinnerProvince.getSelectedItem();
                if (position > 0 && provinceDistricts.containsKey(selectedProvince)) {
                    String[] districts = provinceDistricts.get(selectedProvince);
                    // Add placeholder at the beginning
                    String[] districtsWithPlaceholder = new String[districts.length + 1];
                    districtsWithPlaceholder[0] = "Chọn quận/huyện";
                    System.arraycopy(districts, 0, districtsWithPlaceholder, 1, districts.length);
                    
                    ArrayAdapter<String> newDistrictAdapter = new ArrayAdapter<>(HomeActivity.this,
                        android.R.layout.simple_spinner_item, districtsWithPlaceholder);
                    newDistrictAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(newDistrictAdapter);
                    spinnerDistrict.setEnabled(true);
                } else if (position > 0) {
                    // If province not in map, allow manual entry - user can type in street field
                    String[] genericDistricts = {"Chọn quận/huyện", "Nhập quận/huyện vào ô đường"};
                    ArrayAdapter<String> genericAdapter = new ArrayAdapter<>(HomeActivity.this,
                        android.R.layout.simple_spinner_item, genericDistricts);
                    genericAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(genericAdapter);
                    spinnerDistrict.setEnabled(true);
                } else {
                    spinnerDistrict.setAdapter(districtAdapter);
                    spinnerDistrict.setEnabled(false);
                }
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn địa chỉ của bạn");
        builder.setView(dialogView);
        builder.setCancelable(true); // Allow user to dismiss
        
        // Add buttons
        builder.setPositiveButton("Tìm kiếm", null); // We'll handle click manually
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        
        // Handle search button click with validation (prevent auto-close on validation error)
        dialog.setOnShowListener(d -> {
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                int provincePos = spinnerProvince.getSelectedItemPosition();
                int districtPos = spinnerDistrict.getSelectedItemPosition();
                String street = etStreet.getText().toString().trim();
                
                if (provincePos == 0) {
                    Toast.makeText(this, "Vui lòng chọn tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                    return; // Keep dialog open
                }
                
                if (!spinnerDistrict.isEnabled()) {
                    Toast.makeText(this, "Vui lòng chọn quận/huyện", Toast.LENGTH_SHORT).show();
                    return; // Keep dialog open
                }
                
                // Build address string
                String province = (String) spinnerProvince.getSelectedItem();
                String district = (String) spinnerDistrict.getSelectedItem();
                
                // Check if district is placeholder (not actually selected)
                if (district == null || district.equals("Chọn quận/huyện") || 
                    district.equals("Chọn quận/huyện trước")) {
                    Toast.makeText(this, "Vui lòng chọn quận/huyện", Toast.LENGTH_SHORT).show();
                    return; // Keep dialog open
                }
                
                // If district is "Nhập quận/huyện vào ô đường", use street as district
                if (district.equals("Nhập quận/huyện vào ô đường")) {
                    if (street.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập quận/huyện vào ô đường", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Extract district from street (assume format: "Quận X, Đường Y" or just "Quận X")
                    String[] parts = street.split(",");
                    if (parts.length > 1) {
                        district = parts[0].trim();
                        street = parts[1].trim();
                    } else {
                        district = street;
                        street = "";
                    }
                }
                
                StringBuilder addressBuilder = new StringBuilder();
                if (!street.isEmpty()) {
                    addressBuilder.append(street).append(", ");
                }
                addressBuilder.append(district).append(", ").append(province);
                
                String fullAddress = addressBuilder.toString();
                dialog.dismiss(); // Close dialog before searching
                geocodeAndLoadStores(fullAddress);
            });
        });
        
        dialog.show();
    }
    
    // Geocode address and load stores
    private void geocodeAndLoadStores(String address) {
        // Show loading
        Toast.makeText(this, "Đang tìm địa chỉ...", Toast.LENGTH_SHORT).show();
        
        // Prepare request body
        java.util.Map<String, String> requestBody = new java.util.HashMap<>();
        requestBody.put("address", address);
        
        // Call geocode API
        apiService.geocodeAddress(requestBody).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Parse response - geocode API returns {latitude, longitude, formattedAddress}
                    try {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> data = (java.util.Map<String, Object>) response.body().getData();
                        if (data != null) {
                            Double lat = ((Number) data.get("latitude")).doubleValue();
                            Double lng = ((Number) data.get("longitude")).doubleValue();
                            String formattedAddress = (String) data.get("formattedAddress");
                            
                            userLatitude = lat;
                            userLongitude = lng;
                            locationObtained = true;
                            
                            // Update map
                            if (mapView != null && mapController != null) {
                                GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                                mapController.setCenter(userLocation);
                                mapController.setZoom(13.0);
                                addUserLocationMarker();
                                mapView.invalidate();
                            }
                            
                            // Load stores near this location
                            loadStores(userLatitude, userLongitude);
                            
                            Toast.makeText(HomeActivity.this, "Đã tìm thấy: " + formattedAddress, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this, "Không thể xác định địa chỉ", Toast.LENGTH_SHORT).show();
                        loadStores(null, null);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Không tìm thấy địa chỉ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    loadStores(null, null);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                loadStores(null, null);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        rvStores = findViewById(R.id.rvStores);

        // Greeting
        tvGreeting = findViewById(R.id.tvGreeting);

        // Search Box
        etSearch = findViewById(R.id.etSearch);
        searchCard = findViewById(R.id.searchCard);

        // Bottom Navigation
        navHome = findViewById(R.id.navHome);
        navProducts = findViewById(R.id.navProducts);
        navCart = findViewById(R.id.navCart);
        navAccount = findViewById(R.id.navAccount);

        iconHome = findViewById(R.id.iconHome);
        iconProducts = findViewById(R.id.iconProducts);
        iconCart = findViewById(R.id.iconCart);
        iconAccount = findViewById(R.id.iconAccount);

        tvHome = findViewById(R.id.tvHome);
        tvProducts = findViewById(R.id.tvProducts);
        tvCart = findViewById(R.id.tvCart);
        tvAccount = findViewById(R.id.tvAccount);

        // Chat Button
        btnFloatingChat = findViewById(R.id.btnFloatingChat);
        
        // Stores Section Header
        tvStoresTitle = findViewById(R.id.tvStoresTitle);
        tvStoresSubtitle = findViewById(R.id.tvStoresSubtitle);
        btnLocationInput = findViewById(R.id.btnLocationInput);
        
        // Setup location input button
        btnLocationInput.setOnClickListener(v -> showLocationInputDialog());
    }

    private void setupSearchBox() {
        // Khi bấm vào ô tìm kiếm, mở SearchActivity
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Khi bấm vào searchCard cũng mở SearchActivity
        searchCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Disable input trực tiếp ở HomeActivity
        etSearch.setFocusable(false);
        etSearch.setClickable(true);
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);

        // Set LinearLayoutManager with vertical orientation
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setAdapter(productAdapter);
        
        // Setup Stores RecyclerView
        storeList = new ArrayList<>();
        // Pass null for OnStoreActionListener (no edit/delete in user view)
        storeAdapter = new StoreAdapter(storeList, null);
        storeAdapter.setOnStoreClickListener(store -> {
            // Zoom to store location on map
            if (mapView != null && mapController != null) {
                GeoPoint storeLocation = new GeoPoint(store.getLatitude(), store.getLongitude());
                mapController.animateTo(storeLocation);
                mapController.setZoom(15.0);
                mapView.invalidate();
            }
        });
        
        LinearLayoutManager storesLayoutManager = new LinearLayoutManager(this);
        storesLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvStores.setLayoutManager(storesLayoutManager);
        rvStores.setAdapter(storeAdapter);
    }
    
    private void setupMap() {
        // Initialize OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        
        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapView.setBuiltInZoomControls(true);
            mapView.setClickable(true);
            
            mapController = mapView.getController();
            mapController.setZoom(12.0);
            
            // Set default location (Ho Chi Minh City)
            GeoPoint defaultLocation = new GeoPoint(DEFAULT_LAT, DEFAULT_LNG);
            mapController.setCenter(defaultLocation);
            
            // Add compass overlay
            CompassOverlay compassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
            compassOverlay.enableCompass();
            mapView.getOverlays().add(compassOverlay);
        }
    }

    private void setupBottomNavigation() {
        // Đặt mặc định: TẤT CẢ tab be, chỉ tab hiện tại là nâu đậm
        iconHome.setColorFilter(Color.parseColor(COLOR_BROWN));
        tvHome.setTextColor(Color.parseColor(COLOR_BROWN));
        iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
        tvProducts.setTextColor(Color.parseColor(COLOR_BE));
        iconCart.setColorFilter(Color.parseColor(COLOR_BE));
        tvCart.setTextColor(Color.parseColor(COLOR_BE));
        iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
        tvAccount.setTextColor(Color.parseColor(COLOR_BE));

        navHome.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvHome.setTextColor(Color.parseColor(COLOR_BROWN));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
            tvProducts.setTextColor(Color.parseColor(COLOR_BE));
            iconCart.setColorFilter(Color.parseColor(COLOR_BE));
            tvCart.setTextColor(Color.parseColor(COLOR_BE));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
            tvAccount.setTextColor(Color.parseColor(COLOR_BE));
            // Không cần mở lại Home nếu đã ở Home
        });
        navProducts.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BE));
            tvHome.setTextColor(Color.parseColor(COLOR_BE));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvProducts.setTextColor(Color.parseColor(COLOR_BROWN));
            iconCart.setColorFilter(Color.parseColor(COLOR_BE));
            tvCart.setTextColor(Color.parseColor(COLOR_BE));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
            tvAccount.setTextColor(Color.parseColor(COLOR_BE));
            Intent intent = new Intent(HomeActivity.this, ShopActivity.class);
            startActivity(intent);
        });
        navCart.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BE));
            tvHome.setTextColor(Color.parseColor(COLOR_BE));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
            tvProducts.setTextColor(Color.parseColor(COLOR_BE));
            iconCart.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvCart.setTextColor(Color.parseColor(COLOR_BROWN));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BE));
            tvAccount.setTextColor(Color.parseColor(COLOR_BE));
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });
        navAccount.setOnClickListener(v -> {
            iconHome.setColorFilter(Color.parseColor(COLOR_BE));
            tvHome.setTextColor(Color.parseColor(COLOR_BE));
            iconProducts.setColorFilter(Color.parseColor(COLOR_BE));
            tvProducts.setTextColor(Color.parseColor(COLOR_BE));
            iconCart.setColorFilter(Color.parseColor(COLOR_BE));
            tvCart.setTextColor(Color.parseColor(COLOR_BE));
            iconAccount.setColorFilter(Color.parseColor(COLOR_BROWN));
            tvAccount.setTextColor(Color.parseColor(COLOR_BROWN));
            Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
            startActivity(intent);
        });
    }

    private void selectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#2196F3"));
        text.setTextColor(Color.parseColor("#2196F3"));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void deselectNavItem(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#666666"));
        text.setTextColor(Color.parseColor("#666666"));
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void loadUserInfo() {
        // Check if user is logged in
        com.example.project.utils.AuthManager authManager = com.example.project.utils.AuthManager.getInstance(this);
        
        if (!authManager.isLoggedIn()) {
            // If not logged in, show default greeting
            tvGreeting.setText("Hello, User");
            return;
        }
        
        // Get current user from storage first
        com.example.project.models.User currentUser = authManager.getCurrentUser();
        if (currentUser != null && currentUser.getUsername() != null) {
            tvGreeting.setText("Hello, " + currentUser.getUsername());
        }
        
        // Then fetch fresh data from API
        String authHeader = authManager.getAuthHeader();
        if (authHeader != null) {
            apiService.getMe(authHeader).enqueue(new Callback<ApiResponse<com.example.project.models.User>>() {
                @Override
                public void onResponse(Call<ApiResponse<com.example.project.models.User>> call, Response<ApiResponse<com.example.project.models.User>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        com.example.project.models.User user = response.body().getData();
                        if (user != null && user.getUsername() != null) {
                            // Update greeting with username
                            tvGreeting.setText("Hello, " + user.getUsername());
                            // Update stored user data
                            authManager.updateUser(user);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<com.example.project.models.User>> call, Throwable t) {
                    // Keep the cached username if API fails
                }
            });
        }
    }

    private void loadBikes() {
        // Call API to get all bikes
        apiService.getBikes(1, 100, null, null, null, null, null, null, null).enqueue(new Callback<ApiResponse<Bike[]>>() {
            @Override
            public void onResponse(Call<ApiResponse<Bike[]>> call, Response<ApiResponse<Bike[]>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Bike[] bikes = response.body().getData();
                    if (bikes != null && bikes.length > 0) {
                        productList.clear();
                        
                        // Convert bikes to products
                        for (Bike bike : bikes) {
                            String priceText = String.format("%.0f ₫", bike.getPrice());
                            String imageUrl = (bike.getImages() != null && !bike.getImages().isEmpty()) 
                                ? bike.getImages().get(0).getUrl() 
                                : null;
                            
                            Product product = new Product(
                                bike.getName(),
                                bike.getDescription(),
                                priceText,
                                R.drawable.splash_bike_background,
                                imageUrl
                            );
                            product.setBikeId(bike.getId());
                            productList.add(product);
                        }
                        
                        productAdapter.notifyDataSetChanged();
                    } else {
                        // If no bikes, use sample data
                        loadSampleProducts();
                    }
                } else {
                    // Fallback to sample data if API fails
                    loadSampleProducts();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Bike[]>> call, Throwable t) {
                // Fallback to sample data if API fails
                loadSampleProducts();
            }
        });
    }
    
    private void loadSampleProducts() {
        // Xe đạp điện cao cấp
        productList.add(new Product(
            "Xe đạp điện VinFast Klara S",
            "Xe đạp điện thông minh, thiết kế hiện đại, pin Lithium 60V",
            "29.990.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Yadea E3",
            "Công nghệ Nhật Bản, động cơ 1200W, quãng đường 80km",
            "25.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Dibao Type R",
            "Thiết kế thể thao, phanh ABS, màn hình LCD",
            "32.000.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Pega Aska",
            "Phong cách trẻ trung, vận hành êm ái, bảo hành 3 năm",
            "27.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Giant M133S",
            "Thương hiệu uy tín, chất lượng cao, pin Lithium 72V",
            "35.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện HKbike Cap A+",
            "Pin lithium 60V, tốc độ cao, phanh đĩa trước sau",
            "28.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Bluera 133E",
            "Xe đạp điện cao cấp, sang trọng, động cơ mạnh mẽ",
            "31.200.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp điện Anbico Smart",
            "Công nghệ thông minh, tiết kiệm, GPS tích hợp",
            "26.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp thể thao
        productList.add(new Product(
            "Xe đạp thể thao Road Bike Pro",
            "Xe đạp đua chuyên nghiệp, khung carbon",
            "15.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp thể thao Giant TCR",
            "Thiết kế khí động học, Shimano 105",
            "18.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp địa hình
        productList.add(new Product(
            "Xe đạp địa hình MTB Pro",
            "Xe đạp leo núi chuyên dụng, phuộc 120mm",
            "8.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp địa hình Trek Marlin",
            "Khung nhôm cao cấp, phanh đĩa thủy lực",
            "12.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp địa hình Specialized",
            "Thiết kế bền bỉ, địa hình phức tạp",
            "11.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp gấp
        productList.add(new Product(
            "Xe đạp gấp Dahon K3",
            "Siêu nhỏ gọn, dễ mang theo, 7kg",
            "5.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp gấp Java Fit 16",
            "Xe gấp cao cấp, gấp trong 10 giây",
            "6.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp gấp Fornix BF-300",
            "Gọn nhẹ, phù hợp đi làm, đi học",
            "4.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp trẻ em
        productList.add(new Product(
            "Xe đạp trẻ em Disney Princess",
            "Cho bé gái 5-8 tuổi, có bánh phụ",
            "1.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp trẻ em Giant XTC Jr",
            "Xe đạp thể thao cho trẻ em, bánh 20 inch",
            "3.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp trẻ em BMX Freestyle",
            "Xe đạp biểu diễn cho trẻ từ 10 tuổi",
            "2.900.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Xe đạp touring
        productList.add(new Product(
            "Xe đạp Touring Giant Escape",
            "Xe đạp đi phố và đường dài, 21 tốc độ",
            "9.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp Touring Trek FX 2",
            "Thiết kế đa năng, di chuyển hàng ngày",
            "10.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        // Fixed Gear
        productList.add(new Product(
            "Xe đạp Fixed Gear Pista",
            "Xe cào cào thời trang, phong cách tối giản",
            "4.500.000 ₫",
            R.drawable.splash_bike_background
        ));

        productList.add(new Product(
            "Xe đạp Single Speed Classic",
            "1 tốc độ, thiết kế cổ điển",
            "3.800.000 ₫",
            R.drawable.splash_bike_background
        ));

        productAdapter.notifyDataSetChanged();
    }

    private void setupChatButton() {
        btnFloatingChat.setOnClickListener(v -> {
            // Open User Chat Activity
            Intent intent = new Intent(HomeActivity.this, UserChatActivity.class);
            startActivity(intent);
        });
    }
    
    // Request location permission
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            getCurrentLocation();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getCurrentLocation();
            } else {
                // Permission denied, load stores without location
                loadStores(null, null);
            }
        }
    }
    
    // Get current location with improved accuracy
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, load all stores
            loadStores(null, null);
            return;
        }
        
        try {
            // Use Network location (WiFi/Mobile) - faster but less accurate than GPS
            // This uses WiFi access points and cell towers for location
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Network location is less accurate, so accept wider range
                                // Accept if accuracy <= 500 meters (network location typical range)
                                float accuracy = location.getAccuracy();
                                if (accuracy <= 0 || accuracy <= 500) {
                                    userLatitude = location.getLatitude();
                                    userLongitude = location.getLongitude();
                                    locationObtained = true;
                                    
                                    // Update map to user location and add user marker
                                    if (mapView != null && mapController != null) {
                                        GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                                        
                                        // Center map on user location first
                                        mapController.setCenter(userLocation);
                                        mapController.setZoom(13.0);
                                        
                                        mapView.invalidate();
                                        
                                        // Add user location marker after a short delay
                                        mapView.postDelayed(() -> {
                                            if (locationObtained && mapView != null) {
                                                addUserLocationMarker();
                                            }
                                        }, 200);
                                    }
                                    
                                    // Load stores near user location
                                    loadStores(userLatitude, userLongitude);
                                } else {
                                    // Location accuracy too low, try getting last known location or load all stores
                                    getLastKnownLocation();
                                }
                            } else {
                                // Location not available, try last known location
                                getLastKnownLocation();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Location failed, try last known location
                        getLastKnownLocation();
                    });
        } catch (Exception e) {
            // Error getting location, try last known location
            getLastKnownLocation();
        }
    }
    
    // Get last known location as fallback
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            loadStores(null, null);
            return;
        }
        
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            userLatitude = location.getLatitude();
                            userLongitude = location.getLongitude();
                            locationObtained = true;
                            
                            // Update map
                            if (mapView != null && mapController != null) {
                                GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                                mapController.setCenter(userLocation);
                                mapController.setZoom(13.0);
                                addUserLocationMarker();
                                mapView.invalidate();
                            }
                            
                            // Load stores near user location
                            // Log location for debug
                            android.util.Log.d("UserLocation", "Got location: Lat=" + userLatitude + ", Lng=" + userLongitude);
                            loadStores(userLatitude, userLongitude);
                        } else {
                            // No location available, load all stores
                            loadStores(null, null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Last location also failed, load all stores
                        loadStores(null, null);
                    });
        } catch (Exception e) {
            loadStores(null, null);
        }
    }
    
    // Load stores from API (with optional user location)
    private void loadStores(Double lat, Double lng) {
        // Use 10km radius if location is provided
        Double radius = (lat != null && lng != null) ? 10.0 : null;
        
        // Log API call for debug
        android.util.Log.d("LoadStores", "Loading stores with lat=" + lat + ", lng=" + lng + ", radius=" + radius);
        
        apiService.getStoresForMap(lat, lng, null, radius)
            .enqueue(new Callback<ApiService.MapStoresResponse>() {
                @Override
                public void onResponse(Call<ApiService.MapStoresResponse> call, 
                                      Response<ApiService.MapStoresResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        ApiService.MapStoresResponse mapResponse = response.body();
                        if (mapResponse.getData() != null && !mapResponse.getData().isEmpty()) {
                            mapStoresList = mapResponse.getData();
                            // Log response for debug
                            android.util.Log.d("LoadStores", "Got " + mapStoresList.size() + " stores");
                            if (!mapStoresList.isEmpty() && mapStoresList.get(0).getDistance() != null) {
                                android.util.Log.d("LoadStores", "First store distance: " + mapStoresList.get(0).getDistance());
                            }
                            updateStoresList();
                            updateStoresHeader();
                            // Update markers if map is ready
                            if (mapView != null && mapController != null) {
                                updateMapMarkers();
                                // If user location is available, ensure map shows user location
                                if (locationObtained) {
                                    // Delay slightly to ensure markers are added
                                    mapView.postDelayed(() -> {
                                        if (mapController != null && locationObtained) {
                                            GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                                            mapController.setCenter(userLocation);
                                            mapController.setZoom(13.0);
                                            // Re-add user marker if needed
                                            addUserLocationMarker();
                                            mapView.invalidate();
                                        }
                                    }, 300);
                                }
                            }
                        } else {
                            // No stores found, try loading all stores
                            loadStoresFallback();
                        }
                    } else {
                        // API error, try loading all stores
                        loadStoresFallback();
                    }
                }

                @Override
                public void onFailure(Call<ApiService.MapStoresResponse> call, Throwable t) {
                    // Handle error - load all stores as fallback
                    loadStoresFallback();
                }
            });
    }
    
    // Fallback: Load all stores without location filter
    private void loadStoresFallback() {
        apiService.getStoresForMap(null, null, null, null)
            .enqueue(new Callback<ApiService.MapStoresResponse>() {
                @Override
                public void onResponse(Call<ApiService.MapStoresResponse> call, 
                                      Response<ApiService.MapStoresResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        ApiService.MapStoresResponse mapResponse = response.body();
                        if (mapResponse.getData() != null) {
                            mapStoresList = mapResponse.getData();
                            updateStoresList();
                            updateStoresHeader();
                            if (mapView != null && mapController != null) {
                                updateMapMarkers();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiService.MapStoresResponse> call, Throwable t) {
                    // Final fallback - show empty state
                }
            });
    }
    
    // Update stores section header with count and radius info
    private void updateStoresHeader() {
        if (tvStoresSubtitle == null) return;
        
        if (mapStoresList != null && !mapStoresList.isEmpty()) {
            int count = mapStoresList.size();
            
            if (locationObtained) {
                // Show count and radius info
                tvStoresSubtitle.setText(String.format("%d cửa hàng trong vòng 10km từ vị trí của bạn", count));
                tvStoresSubtitle.setVisibility(View.VISIBLE);
            } else {
                // Show total count
                tvStoresSubtitle.setText(String.format("Tổng cộng %d cửa hàng", count));
                tvStoresSubtitle.setVisibility(View.VISIBLE);
            }
        } else {
            tvStoresSubtitle.setVisibility(View.GONE);
        }
    }
    
    private void updateStoresList() {
        if (mapStoresList == null || mapStoresList.isEmpty()) {
            storeList.clear();
            storeAdapter.notifyDataSetChanged();
            return;
        }
        
        storeList.clear();
        
        // Sort by distance if available (closest first)
        List<ApiService.MapStoresResponse.MapStore> sortedStores = new ArrayList<>(mapStoresList);
        if (locationObtained) {
            // Sort by distance (closest first)
            sortedStores.sort((store1, store2) -> {
                Double dist1 = store1.getDistance();
                Double dist2 = store2.getDistance();
                if (dist1 == null && dist2 == null) return 0;
                if (dist1 == null) return 1;
                if (dist2 == null) return -1;
                return Double.compare(dist1, dist2);
            });
        }
        
        for (ApiService.MapStoresResponse.MapStore mapStore : sortedStores) {
            Store store = new Store();
            store.setId(mapStore.getId());
            store.setName(mapStore.getName());
            store.setAddress(mapStore.getAddress());
            store.setCity(mapStore.getCity());
            store.setPhone(mapStore.getPhone());
            store.setLatitude(mapStore.getLatitude());
            store.setLongitude(mapStore.getLongitude());
            store.setIsActive(mapStore.isOpenNow());
            
            // Set distance if available (this is the key to show km)
            if (mapStore.getDistance() != null) {
                store.setDistance(mapStore.getDistance());
                // Debug: Log distance to verify
                android.util.Log.d("StoreDistance", "Store: " + store.getName() + 
                    ", Distance: " + mapStore.getDistance() + " km");
            } else {
                // If no distance, log it
                android.util.Log.d("StoreDistance", "Store: " + store.getName() + ", No distance");
            }
            
            // Set status (always show Hoạt động/Đóng cửa)
            store.setStatus(mapStore.isOpenNow() ? "Hoạt động" : "Đóng cửa");
            
            storeList.add(store);
        }
        
        // Enable "Gần nhất" badge if location is available
        storeAdapter.setShowNearestBadge(locationObtained && !storeList.isEmpty());
        storeAdapter.notifyDataSetChanged();
        
        // Ensure RecyclerView is visible
        if (rvStores != null) {
            rvStores.setVisibility(View.VISIBLE);
        }
    }
    
    private Marker userLocationMarker = null;
    
    private void addUserLocationMarker() {
        if (mapView == null || !locationObtained) return;
        
        // Remove old marker if exists
        if (userLocationMarker != null) {
            mapView.getOverlays().remove(userLocationMarker);
        }
        
        GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
        userLocationMarker = new Marker(mapView);
        userLocationMarker.setPosition(userLocation);
        userLocationMarker.setTitle("Vị trí của bạn");
        userLocationMarker.setSnippet(String.format("Lat: %.6f, Lng: %.6f", userLatitude, userLongitude));
        userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        
        // Try to set a blue icon for user location (different from store markers)
        try {
            // Use default marker but we can distinguish by color/size later
            android.graphics.drawable.Drawable userIcon = getResources().getDrawable(R.drawable.ic_location, null);
            if (userIcon != null) {
                userIcon.setTint(android.graphics.Color.BLUE);
                userLocationMarker.setIcon(userIcon);
            }
        } catch (Exception e) {
            // Use default marker
        }
        
        // Add marker to map
        mapView.getOverlays().add(userLocationMarker);
        mapView.invalidate();
        
        // Force center map on user location
        if (mapController != null) {
            mapController.setCenter(userLocation);
            mapController.setZoom(13.0);
        }
    }
    
    private void updateMapMarkers() {
        if (mapView == null || mapStoresList == null || mapStoresList.isEmpty()) return;
        
        // Save user location marker before clearing
        Marker savedUserMarker = userLocationMarker;
        
        // Clear existing markers
        mapView.getOverlays().clear();
        
        // Re-add compass overlay
        CompassOverlay compassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);
        
        // Re-add user location marker if available
        if (locationObtained) {
            GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
            userLocationMarker = new Marker(mapView);
            userLocationMarker.setPosition(userLocation);
            userLocationMarker.setTitle("Vị trí của bạn");
            userLocationMarker.setSnippet(String.format("Lat: %.6f, Lng: %.6f", userLatitude, userLongitude));
            userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            
            // Try to set blue icon for user location
            try {
                android.graphics.drawable.Drawable userIcon = getResources().getDrawable(R.drawable.ic_location, null);
                if (userIcon != null) {
                    userIcon.setTint(android.graphics.Color.BLUE);
                    userLocationMarker.setIcon(userIcon);
                }
            } catch (Exception e) {
                // Use default
            }
            
            mapView.getOverlays().add(userLocationMarker);
        }
        
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        
        // Add markers for each store
        for (ApiService.MapStoresResponse.MapStore store : mapStoresList) {
            GeoPoint location = new GeoPoint(store.getLatitude(), store.getLongitude());
            String fullAddress = store.getAddress() + (store.getCity() != null && !store.getCity().isEmpty() 
                ? ", " + store.getCity() : "");
            
            Marker marker = new Marker(mapView);
            marker.setPosition(location);
            marker.setTitle(store.getName());
            marker.setSnippet(fullAddress);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener((marker1, mapView1) -> {
                // Info window sẽ tự động hiển thị
                return false;
            });
            
            mapView.getOverlays().add(marker);
            
            // Update bounds
            double lat = store.getLatitude();
            double lon = store.getLongitude();
            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
            if (lon < minLon) minLon = lon;
            if (lon > maxLon) maxLon = lon;
        }
        
        // Zoom to fit all markers (including user location if available)
        if (mapStoresList.size() > 0) {
            try {
                // Include user location in bounds if available
                if (locationObtained) {
                    double userLat = userLatitude;
                    double userLon = userLongitude;
                    if (userLat < minLat) minLat = userLat;
                    if (userLat > maxLat) maxLat = userLat;
                    if (userLon < minLon) minLon = userLon;
                    if (userLon > maxLon) maxLon = userLon;
                }
                
                GeoPoint center = new GeoPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2);
                double latSpan = maxLat - minLat;
                double lonSpan = maxLon - minLon;
                double maxSpan = Math.max(latSpan, lonSpan);
                
                // Calculate zoom level based on span
                double zoomLevel = 12.0;
                if (maxSpan > 0.1) zoomLevel = 10.0;
                else if (maxSpan > 0.05) zoomLevel = 11.0;
                else if (maxSpan > 0.02) zoomLevel = 12.0;
                else if (maxSpan > 0.01) zoomLevel = 13.0;
                else zoomLevel = 14.0;
                
                // If user location exists, prefer centering on user with stores visible
                if (locationObtained) {
                    GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                    // Center on user but adjust zoom to show all stores
                    mapController.setCenter(userLocation);
                    // Use a zoom that shows both user and stores (slightly zoomed out)
                    mapController.setZoom(Math.min(zoomLevel, 13.0));
                } else {
                    mapController.setCenter(center);
                    mapController.setZoom(zoomLevel);
                }
            } catch (Exception e) {
                // If error, zoom based on context
                if (locationObtained) {
                    // Zoom to user location
                    GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                    mapController.setCenter(userLocation);
                    mapController.setZoom(13.0);
                } else if (mapStoresList.size() > 0) {
                    // Zoom to first store
                    GeoPoint firstLocation = new GeoPoint(
                        mapStoresList.get(0).getLatitude(),
                        mapStoresList.get(0).getLongitude()
                    );
                    mapController.setCenter(firstLocation);
                    mapController.setZoom(12.0);
                }
            }
        } else {
            // Default to Ho Chi Minh City or user location
            if (locationObtained) {
                GeoPoint userLocation = new GeoPoint(userLatitude, userLongitude);
                mapController.setCenter(userLocation);
                mapController.setZoom(13.0);
            } else {
                GeoPoint defaultLocation = new GeoPoint(DEFAULT_LAT, DEFAULT_LNG);
                mapController.setCenter(defaultLocation);
                mapController.setZoom(12.0);
            }
        }
        
        // Refresh map
        mapView.invalidate();
    }
}
