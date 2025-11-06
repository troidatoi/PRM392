package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import com.example.project.network.ApiService;
import com.example.project.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFullActivity extends AppCompatActivity {

    private MapView mapView;
    private IMapController mapController;
    private CardView btnBack, btnMyLocation, btnZoomIn, btnZoomOut;
    private RecyclerView rvStores;
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    private StoreMapAdapter storeAdapter;
    private List<Store> storeList = new ArrayList<>();
    private GeoPoint userLocation;
    private Polyline currentRoadOverlay;
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        
        setContentView(R.layout.activity_map_full);
        
        initViews();
        setupMap();
        setupRecyclerView();
        setupListeners();
        checkLocationPermission();
        loadStores();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        btnBack = findViewById(R.id.btnBack);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        rvStores = findViewById(R.id.rvStores);
        bottomSheet = findViewById(R.id.bottomSheet);
        
        // Setup Bottom Sheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(300); // Height when collapsed
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupMap() {
        // Using OpenStreetMap MAPNIK
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        
        mapController = mapView.getController();
        mapController.setZoom(15.0);
        
        // Try to get current location and center map there
        getCurrentLocation();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvStores.setLayoutManager(layoutManager);
        
        storeAdapter = new StoreMapAdapter(storeList, new StoreMapAdapter.OnStoreMapActionListener() {
            @Override
            public void onDirectionClick(Store store) {
                drawRouteToStore(store);
            }

            @Override
            public void onStoreClick(Store store) {
                // Zoom to store location
                GeoPoint storeLocation = new GeoPoint(store.getLatitude(), store.getLongitude());
                mapController.animateTo(storeLocation);
                mapController.setZoom(16.0);
            }
        });
        
        rvStores.setAdapter(storeAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                GeoPoint myLocation = myLocationOverlay.getMyLocation();
                mapController.animateTo(myLocation);
                mapController.setZoom(16.0);
            } else {
                getCurrentLocation();
            }
        });

        btnZoomIn.setOnClickListener(v -> {
            mapController.zoomIn();
        });

        btnZoomOut.setOnClickListener(v -> {
            mapController.zoomOut();
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            setupMyLocationOverlay();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMyLocationOverlay();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Cần cấp quyền vị trí để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupMyLocationOverlay() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.animateTo(userLocation);
                mapController.setZoom(15.0);
                
                // Update adapter with user location
                if (storeAdapter != null) {
                    storeAdapter.setUserLocation(location.getLatitude(), location.getLongitude());
                }
            } else {
                // Default to Ho Chi Minh City
                GeoPoint defaultLocation = new GeoPoint(10.7769, 106.7009);
                mapController.setCenter(defaultLocation);
                Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStores() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiService.StoreResponse> call = apiService.getStores(null, null, null, null, 1, 1000, null);
        
        call.enqueue(new Callback<ApiService.StoreResponse>() {
            @Override
            public void onResponse(Call<ApiService.StoreResponse> call, Response<ApiService.StoreResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    storeList.clear();
                    storeList.addAll(response.body().getData());
                    storeAdapter.notifyDataSetChanged();
                    addStoreMarkers();
                } else {
                    Toast.makeText(MapFullActivity.this, "Không thể tải danh sách cửa hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.StoreResponse> call, Throwable t) {
                Toast.makeText(MapFullActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addStoreMarkers() {
        for (Store store : storeList) {
            double lat = store.getLatitude();
            double lon = store.getLongitude();
            
            if (lat != 0.0 && lon != 0.0) {
                Marker marker = new Marker(mapView);
                GeoPoint storeLocation = new GeoPoint(lat, lon);
                marker.setPosition(storeLocation);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(store.getName());
                marker.setSnippet(store.getAddress());
                
                marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                    mapController.animateTo(storeLocation);
                    return false;
                });
                
                mapView.getOverlays().add(marker);
            }
        }
        mapView.invalidate();
    }

    private void drawRouteToStore(Store store) {
        if (userLocation == null) {
            Toast.makeText(this, "Đang lấy vị trí của bạn...", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }

        // Remove previous route if exists
        if (currentRoadOverlay != null) {
            mapView.getOverlays().remove(currentRoadOverlay);
        }

        GeoPoint storeLocation = new GeoPoint(store.getLatitude(), store.getLongitude());
        
        // Run route calculation in background thread
        new Thread(() -> {
            RoadManager roadManager = new OSRMRoadManager(this, getPackageName());
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(userLocation);
            waypoints.add(storeLocation);
            
            Road road = roadManager.getRoad(waypoints);
            
            runOnUiThread(() -> {
                if (road != null && road.mStatus == Road.STATUS_OK) {
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    roadOverlay.getOutlinePaint().setColor(Color.parseColor("#2196F3"));
                    roadOverlay.getOutlinePaint().setStrokeWidth(10);
                    
                    currentRoadOverlay = roadOverlay;
                    mapView.getOverlays().add(roadOverlay);
                    mapView.invalidate();
                    
                    // Zoom to show entire route
                    mapController.animateTo(storeLocation);
                    mapController.setZoom(14.0);
                    
                    Toast.makeText(MapFullActivity.this, 
                        String.format("Khoảng cách: %.1f km", road.mLength), 
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapFullActivity.this, 
                        "Không thể tính đường đi", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
