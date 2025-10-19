package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.BikeAdapter;
import com.example.myapplication.model.Bike;
import com.example.myapplication.repository.BikeRepository;
import java.util.ArrayList;
import java.util.List;

public class BikeListFragment extends Fragment implements BikeAdapter.OnBikeClickListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private BikeAdapter bikeAdapter;
    private BikeRepository bikeRepository;
    private List<Bike> bikes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bike_list, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadBikes();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_bikes);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        bikeRepository = new BikeRepository();
    }

    private void setupRecyclerView() {
        bikeAdapter = new BikeAdapter(bikes, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(bikeAdapter);
    }

    private void loadBikes() {
        showLoading(true);
        
        bikeRepository.getBikes(new BikeRepository.BikeCallback<List<Bike>>() {
            @Override
            public void onSuccess(List<Bike> data) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        bikes.clear();
                        bikes.addAll(data);
                        bikeAdapter.updateBikes(bikes);
                        
                        if (bikes.isEmpty()) {
                            showEmpty(true);
                        } else {
                            showEmpty(false);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showEmpty(true);
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            emptyView.setText("Không có xe đạp điện nào");
        }
    }

    @Override
    public void onBikeClick(Bike bike) {
        // TODO: Navigate to bike detail
        Toast.makeText(getContext(), "Clicked: " + bike.getName(), Toast.LENGTH_SHORT).show();
    }

    // Method to refresh data
    public void refresh() {
        loadBikes();
    }

    // Method to filter bikes
    public void filterBikes(String category, String search) {
        showLoading(true);
        
        bikeRepository.getBikes(null, null, category, null, null, null, null, search, null,
                new BikeRepository.BikeCallback<List<Bike>>() {
            @Override
            public void onSuccess(List<Bike> data) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        bikes.clear();
                        bikes.addAll(data);
                        bikeAdapter.updateBikes(bikes);
                        
                        if (bikes.isEmpty()) {
                            showEmpty(true);
                            emptyView.setText("Không tìm thấy xe đạp điện nào");
                        } else {
                            showEmpty(false);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showEmpty(true);
                        emptyView.setText("Lỗi tải dữ liệu: " + error);
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}