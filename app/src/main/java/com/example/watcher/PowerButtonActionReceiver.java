package com.example.watcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.watcher.api.NotificationService;
import com.example.watcher.api.Watcher24Api;
import com.example.watcher.helper.DeviceLocationFinder;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PowerButtonActionReceiver extends BroadcastReceiver {
    private static final String TAG = "PowerButtonActionReceiv";
    private long lastClickedTimestamp = 0;
    private int clickCnt = 0;


    public PowerButtonActionReceiver() {
        lastClickedTimestamp = System.currentTimeMillis();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long currentTimestamp = System.currentTimeMillis();
        Log.d(TAG, "onReceive: " + (currentTimestamp - lastClickedTimestamp));
        int interval = 700;
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            if (currentTimestamp - lastClickedTimestamp <= interval)
                clickCnt++;
            else
                clickCnt = 0;
            lastClickedTimestamp = System.currentTimeMillis();
            Log.d(TAG, "onReceive: screen off. cnt: " + clickCnt);
            Toast.makeText(context, "screen off", Toast.LENGTH_SHORT).show();
        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            if (currentTimestamp - lastClickedTimestamp <= interval)
                clickCnt++;
            else clickCnt = 0;
            lastClickedTimestamp = System.currentTimeMillis();
            Log.d(TAG, "onReceive: screen on: " + clickCnt);
            Toast.makeText(context, "screen on", Toast.LENGTH_SHORT).show();
        }
        if (clickCnt == 3) {
            Log.d(TAG, "onReceive: power button clicked 3 times");
            sendNotifcation(context);
        }
    }

    private void sendNotifcation(Context context) {
        Log.d(TAG, "sendNotifcation: inside");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String messageSenderID = mAuth.getCurrentUser().getUid();
        DeviceLocationFinder.getCurrentDeviceLocation(context, new DeviceLocationFinder.OnDeviceLocationFoundListener() {
            @Override
            public void onDeviceLocationFound(LatLng latLng) {
                Log.d(TAG, "onDeviceLocationFound: lat: "+latLng.latitude+ " lon: "+latLng.longitude);
                DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();

                RootRef.child("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Log.d("real name",snapshot.child("username").getValue().toString());
                           String uname=snapshot.child("username").getValue().toString();
                            Log.d(TAG, "onDataChange: found userName: "+uname);
                            JsonObject json = new JsonObject();

                            json.addProperty("topic", messageSenderID);
                            json.addProperty("title", "Help " + uname + " immediately !!!");
                            StringBuilder messageBuilder = new StringBuilder("Hey. I am in danger.");
                            messageBuilder.append("\n").append("Find me here...").append("\n")
                                    .append("Latitude: ").append(latLng.latitude).append(", ")
                                    .append("Longtitude: ").append(latLng.longitude);
                            json.addProperty("message", messageBuilder.toString());

                            NotificationService service = Watcher24Api.instance.notificationService;
                            Call<JsonObject> call = service.sendNotification(json);
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    if(response.isSuccessful() && response.body()!=null)
                                    {
                                        Log.d(TAG, "onResponse: " +response.body().toString());
                                    }else
                                    {
                                        Log.w(TAG, "onResponse: background loaction sent was not sent");
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Log.e(TAG, "onFailure: error sending location in background");

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
    }
}
