package com.example.watcher.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotificationService {
    @POST("notifier")
    Call <JsonObject> sendNotification(@Body  JsonObject body);
}
