package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.CategoryAdapter;
import com.example.myapplication.adapter.FeaturedBikeAdapter;
import com.example.myapplication.model.Bike;
import com.example.myapplication.model.Category;
import com.example.myapplication.repository.BikeRepository;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements FeaturedBikeAdapter.OnFeaturedBikeClickListener, CategoryAdapter.OnCategoryClickListener {
    private RecyclerView featuredRecycler;
    private RecyclerView categoriesRecycler;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FeaturedBikeAdapter featuredAdapter;
    private CategoryAdapter categoryAdapter;
    private final List<Bike> featuredBikes = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private BikeRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupLists();
        loadData();
        return view;
    }

    private void initViews(View view) {
        featuredRecycler = view.findViewById(R.id.recycler_featured);
        categoriesRecycler = view.findViewById(R.id.recycler_categories);
        progressBar = view.findViewById(R.id.home_progress);
        emptyView = view.findViewById(R.id.home_empty);
        repository = new BikeRepository();
    }

    private void setupLists() {
        featuredAdapter = new FeaturedBikeAdapter(featuredBikes, this);
        featuredRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredRecycler.setAdapter(featuredAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(featuredRecycler);

        categoryAdapter = new CategoryAdapter(categories, this);
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriesRecycler.setAdapter(categoryAdapter);
    }

    private void loadData() {
        showLoading(true);

        repository.getFeaturedBikes(8, new BikeRepository.BikeCallback<List<Bike>>() {
            @Override
            public void onSuccess(List<Bike> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    featuredBikes.clear();
                    if (data != null) featuredBikes.addAll(data);
                    featuredAdapter.notifyDataSetChanged();
                    maybeShowEmpty();
                    showLoading(false);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    maybeShowEmpty();
                });
            }
        });

        repository.getCategories(new BikeRepository.BikeCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    categories.clear();
                    if (data != null) categories.addAll(data);
                    categoryAdapter.notifyDataSetChanged();
                    maybeShowEmpty();
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(HomeFragment.this::maybeShowEmpty);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void maybeShowEmpty() {
        boolean empty = featuredBikes.isEmpty() && categories.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onFeaturedBikeClick(Bike bike) {
        // For now just no-op or could show a toast via BikeListFragment later
    }

    @Override
    public void onCategoryClick(Category category) {
        // In a full app we could navigate or filter list; left as a hook
    }
}


