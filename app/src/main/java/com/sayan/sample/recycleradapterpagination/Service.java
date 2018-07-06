package com.sayan.sample.recycleradapterpagination;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by AvikRayan on 7/2/2017.
 */

public interface Service {
    @GET("/bins/169ney")
    Call<Response> fetchdata();
}
