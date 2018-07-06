package com.sayan.sample.recycleradapterpagination;

import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String API_BASE_URL = "https://api.myjson.com/";
    private RecyclerView myRecyclerView;
    private Retrofit retrofit;
    private NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createRetrofitClient();

        myRecyclerView = (RecyclerView) findViewById(R.id.rcview);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        myRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        myRecyclerView.setLayoutManager(linearLayoutManager);
        loadData(null, true, 0);
    }

    private void createRetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void loadData(final MyAdapter myAdapter, final boolean isInitialLoad, final int loadingItemPosition) {
        retrofit.create(Service.class).fetchdata().enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getResult().equalsIgnoreCase("success")) {
                        if (isInitialLoad) {
                            ArrayList<Model> models = (ArrayList<Model>) response.body().getData();
                            addRecyclerView((ArrayList<Model>) models.clone());

                        } else {
                            ArrayList<Model> models = (ArrayList<Model>) response.body().getData();
                            reloadRecyclerWithNewData((ArrayList<Model>) models.clone(), myAdapter, loadingItemPosition);
                            if (myAdapter != null) {
                                myAdapter.setLoaded(false);
                            }
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Could not load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {

            }
        });
    }

    private void reloadRecyclerWithNewData(ArrayList<Model> models, MyAdapter myAdapter, int loadingItemPosition) {
        MyAdapter adapter = (MyAdapter) myRecyclerView.getAdapter();
        adapter.addNewDataToItems(models, loadingItemPosition);
    }

    private void addRecyclerView(final ArrayList<Model> models) {
        final MyAdapter myAdapter = new MyAdapter(models, MainActivity.this);
        myAdapter.setHasStableIds(true);
        myRecyclerView.setAdapter(myAdapter);
        myRecyclerView.setItemViewCacheSize(20);
        myRecyclerView.setDrawingCacheEnabled(true);
        myRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!myAdapter.isLoading()) {
                    if ((scrollY + 50) >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                        // Load More Data
                        onLoadMore(myAdapter, models);
                    }
                }
            }
        });
    }

    public void onLoadMore(final MyAdapter myAdapter, final ArrayList<Model> models) {
        myAdapter.setLoaded(true);
        models.add(null);
        myAdapter.notifyItemInserted(models.size() - 1);
        //Load more data for reyclerview
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                loadData(myAdapter, false, models.size());
            }
        });

    }
}
