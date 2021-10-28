package com.example.watcher.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesService {
    @GET("nearbysearch/json")
    Call<NearbyPlacesApiResponse> fetchNearByPlaces(@Query("location") String location,
                                         @Query("radius") int radius,
                                         @Query("type")String type,
                                         @Query("key")String key);
}
