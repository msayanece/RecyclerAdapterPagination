package com.sayan.sample.recycleradapterpagination;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String API_BASE_URL = "https://api.myjson.com/";
    private RecyclerView myRecyclerView;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createRetrofitClient();

        myRecyclerView = (RecyclerView) findViewById(R.id.rcview);
        myRecyclerView.setHasFixedSize(true);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void createRetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void loadData() {
        retrofit.create(Service.class).fetchdata().enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.isSuccessful() && response.body() != null){
                    if (response.body().getResult().equalsIgnoreCase("success")){
                        myRecyclerView.setAdapter(new MyAdapter(response.body().getData(), MainActivity.this));
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Could not load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {

            }
        });
    }
}
