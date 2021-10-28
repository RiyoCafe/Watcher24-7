package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar ;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class FinalHomeActivity2 extends AppCompatActivity {
    TabLayout tabLayout;
    TabItem tabItem1,tabItem2,tabItem3;
    ViewPager viewPager;
    PageAdapter pageAdapter;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 19;
    private final int STORAGE_PERMISSION_CODE = 1;
    private DatabaseReference RootRef,ChatsRef;
    private double latitude,longitude;
    private GifImageView b;
    private String username,check_url, messageSenderID,firebase_name;
    Toolbar toolbar;
    private FirebaseAuth mAuth;
    public static String uname;
    private boolean Flag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_home2);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.bar_color));
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabItem1 = (TabItem)findViewById(R.id.tab1);
        tabItem2 = (TabItem)findViewById(R.id.tab2);
        tabItem3 = (TabItem)findViewById(R.id.tab3);
        viewPager = (ViewPager)findViewById(R.id.vPager);
        // akahne comeento ut korci
        //b=findViewById(R.id.sos_button_home);
        toolbar=findViewById(R.id.main_page_toolbar);
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");


        check_url="https://us-central1-watcher24-7.cloudfunctions.net/notifier";
        setSupportActionBar(toolbar);
        pageAdapter = new PageAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.child("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Log.d("real name",snapshot.child("username").getValue().toString());
                    uname=snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Log.d("after fire",firebase_name);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition()==0 || tab.getPosition()==1 || tab.getPosition()==2){
                    pageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        // akhane comment out korci


       /* b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickSOSbuttonFOrDanger();
            }
        });*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0,     spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.find_friends_menu)
        {
            Intent intent=new Intent(FinalHomeActivity2.this, FindFriendsActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.setting_menu)
        {
            Intent intent=new Intent(FinalHomeActivity2.this, SettingActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clickSOSbuttonFOrDanger() {

        JSONObject json_request1=new JSONObject();
        try {


            json_request1.put("topic",messageSenderID);
            json_request1.put("title","Help "+uname+" immediately !!!");

            json_request1.put("message","we are sending her current location as soon as possible");
            Log.d("username found ?: ",uname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest1=new JsonObjectRequest(Request.Method.POST, check_url, json_request1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject jsonObject) {
                try {
                    enableGps();
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(FinalHomeActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE_LOCATION_PERMISSION);

                    } else {
                        //  Log.d("in sos button","is it clicked");
                        getCurrentLocation();
                    }
                    Log.d("helping mehrab",jsonObject.getString("status"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("helping mehrab","yes or no?");
            }
        });
        MySingelton.getInstance(FinalHomeActivity2.this).addToRequestQueue(jsonObjectRequest1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode ==STORAGE_PERMISSION_CODE  && grantResults.length > 0) {
            getCurrentLocation();
        }
        else if(requestCode == 175 && grantResults.length > 0)
        {

        }
        else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableGps() {

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS for address verification");

            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });

            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation()
    {


        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(FinalHomeActivity2.this)
                        .removeLocationUpdates(this);

                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                    Log.d("final test","sending caree location");
                    sendCareeLocation();
                    Map map=new HashMap();
                    map.put("latitude",latitude);
                    map.put("longitude",longitude);

                    Flag=true;

                    ChatsRef.child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                                    String messageReceiverID=dataSnapshot.getKey();
                                    Log.d("final test","database loop");
                                    RootRef.child("Location").child(messageSenderID).child(messageReceiverID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful()) {
                                                Log.d("location sent value", latitude + "," + longitude);
                                                Toast.makeText(FinalHomeActivity2.this, "You sent your current location "+latitude + "," + longitude, Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                Log.d("location eror",task.getException().toString());
                                                Toast.makeText(FinalHomeActivity2.this, "Your location can not be sent", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }

            }

        };

        LocationServices.getFusedLocationProviderClient(FinalHomeActivity2.this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void  sendCareeLocation(){

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            Log.d("all locationfrom caree:",address+","+city+","+state+","+country+","+postalCode+","+knownName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject json_request1=new JSONObject();
        try {

            json_request1.put("topic",messageSenderID);
            json_request1.put("title","Location of "+uname);

            json_request1.put("message",latitude+","+longitude);
            Log.d("final test",uname+" request build "+latitude+","+longitude+","+messageSenderID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest1=new JsonObjectRequest(Request.Method.POST, check_url, json_request1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject jsonObject) {
                try {
                    Log.d("final test",jsonObject.getString("status"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("final test","yes or no?");
            }
        });
        MySingelton.getInstance(FinalHomeActivity2.this).addToRequestQueue(jsonObjectRequest1);
    }

}