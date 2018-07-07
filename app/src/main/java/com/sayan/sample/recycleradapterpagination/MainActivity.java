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
        initializeViews();
        setupRecyclerView();
        //initial load
        loadData(null, true, 0);
    }

    private void setupRecyclerView() {
        myRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        myRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initializeViews() {
        myRecyclerView = (RecyclerView) findViewById(R.id.rcview);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
    }

    private void createRetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Use this method for loading server data
     * @param myAdapter pass the adapter object to populate data (null for initial load)
     * @param isInitialLoad pass true for first time loading, pass false if you want to load more
     * @param loadingItemPosition item position of the adapter-item-list for adding new loaded data after the item position,
     *                            (pass last item position)
     */
    private void loadData(final MyAdapter myAdapter, final boolean isInitialLoad, final int loadingItemPosition) {
        retrofit.create(Service.class).fetchdata().enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getResult().equalsIgnoreCase("success")) {
                        if (isInitialLoad) {
                            //initial load
                            ArrayList<Model> models = (ArrayList<Model>) response.body().getData();
                            addRecyclerView((ArrayList<Model>) models.clone());
                        } else {
                            //load more
                            ArrayList<Model> models = (ArrayList<Model>) response.body().getData();
                            //send the clone of the items (Do not send new items directly)
                            reloadRecyclerWithNewData((ArrayList<Model>) models.clone(), myAdapter, loadingItemPosition);
                            //set adapter state to not loading (or loading complete)
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

    /**
     * use this method for
     * @param models
     * @param adapter
     * @param loadingItemPosition
     */
    private void reloadRecyclerWithNewData(ArrayList<Model> models, MyAdapter adapter, int loadingItemPosition) {
//        MyAdapter adapter = (MyAdapter) myRecyclerView.getAdapter();
        adapter.addNewDataToItems(models, loadingItemPosition);
    }

    /**
     * populate and set up the recycler view with items and set a scroll change listener
     * @param models models to add to the recycler adapter
     */
    private void addRecyclerView(final ArrayList<Model> models) {
        //adapter and recycler view set up
        final MyAdapter myAdapter = new MyAdapter(models, MainActivity.this);
        myAdapter.setHasStableIds(true);
        myRecyclerView.setAdapter(myAdapter);
        myRecyclerView.setItemViewCacheSize(20);
        myRecyclerView.setDrawingCacheEnabled(true);
        myRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        //This is important for detecting the scroll has reached to the bottom end of recycler
        //For this to work you need to use NestedScrollView as a parent of the recycler view
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //check if the adapter is already in loading state (when data is feching from the server)
                if (!myAdapter.isLoading()) {
                    //check if the scroll comes to an end
                    if ((scrollY + 50) >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                        // Load More Data, here use the adapter to populate new data and the items (the old data) already added to the adapter
                        onLoadMore(myAdapter, models);
                    }
                }
            }
        });
    }

    /**
     * While the scroll comes to the bottom of the recycler view, call this method (from the listener)
     * @param myAdapter the adapter to populate data
     * @param models the old data the adapter holds currently
     */
    public void onLoadMore(final MyAdapter myAdapter, final ArrayList<Model> models) {
        //show progress bar to user and make the adapter into the loading state
        myAdapter.setLoaded(true);
        //add an item to the itemlist of adapter (to identifying the loading)
        models.add(null);
        //notify the adapter about the new item inserted with the position
        myAdapter.notifyItemInserted(models.size() - 1);
        //Load more data for reyclerview
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //now load data from server (here use the adapter object and the last item position with initial load to false)
                loadData(myAdapter, false, models.size());
            }
        });
    }
}
