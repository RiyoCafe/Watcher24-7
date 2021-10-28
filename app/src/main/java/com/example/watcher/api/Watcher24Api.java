package com.example.watcher.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Watcher24Api {
    public static final Watcher24Api instance = new Watcher24Api();
    public NotificationService notificationService;

    private Watcher24Api(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .followRedirects(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://us-central1-watcher24-7.cloudfunctions.net/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        notificationService =  retrofit.create(NotificationService.class);
    }
}
