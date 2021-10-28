package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.droidsonroids.gif.GifImageView;

public class MessageActivity extends AppCompatActivity {

    private String messageReceiverID, messageSenderID,messageReceiverImage;

    private GifImageView b;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 19;
    private final int STORAGE_PERMISSION_CODE = 1;private int i;
    private CircleImageView userImage;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef,locationRef,ChatsRef;
    private ImageButton SendMessageButton,sendLocation_button,showLocationButton;
    private EditText MessageInputText;
    private String saveCurrentTime, saveCurrentDate;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private double latitude,longitude;
    private double sendLatitude,sendLongitude;
    private String username,check_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        locationRef = FirebaseDatabase.getInstance().getReference();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        Log.d("receiveridin message",messageReceiverID);
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
        check_url="https://us-central1-watcher24-7.cloudfunctions.net/notifier";
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        IntializeControllers();
        //username=SignUp.notifier_name;
        //Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_user).into(userImage);
        //username=Prevalent.UserName;
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendMessage();
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickSOSbuttonFOrDanger();


            }
        });

        sendLocation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enableGps();
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);

                } else {
                    // getCurrentDeviceLocation();
                    getCurrentLocationduplicate();


                }
            }
        });

        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationRef.child("Location").child(messageReceiverID).child(messageSenderID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            sendLatitude= (double) snapshot.child("latitude").getValue();
                            sendLongitude= (double) snapshot.child("longitude").getValue();
                            Log.d("location for sending",sendLatitude+","+sendLongitude);
                            LatLng mapsLatLng = new LatLng(sendLatitude,sendLongitude);

                            Intent intent = new Intent(MessageActivity.this, LocationMapActivity.class);
                            intent.putExtra("latlang", mapsLatLng);
                            startActivity(intent);
                        }else{
                            Toast.makeText(MessageActivity.this,"location can not be found",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

    }

    private void clickSOSbuttonFOrDanger() {

        JSONObject json_request1=new JSONObject();
        try {

            json_request1.put("topic",messageSenderID);
            json_request1.put("title","Help "+FinalHomeActivity2.uname+" immediately !!!");

            json_request1.put("message","we are sending her current location as soon as possible");
            Log.d("username found ?: ",FinalHomeActivity2.uname);
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

                        ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE_LOCATION_PERMISSION);

                    } else {
                        //  Log.d("in sos button","is it clicked");
                        getCurrentLocation();
                    }
                    Log.d("helping mehrab",jsonObject.getString("status"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MessageActivity.this,"is it catch whrer",Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("helping mehrab","yes or no?");
            }
        });
        MySingelton.getInstance(MessageActivity.this).addToRequestQueue(jsonObjectRequest1);


    }


    private void IntializeControllers()
    {


        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        SendMessageButton =  findViewById(R.id.send_messages);
        showLocationButton=findViewById(R.id.show_location_btn);
        sendLocation_button=findViewById(R.id.location_msg_button);
        MessageInputText = (EditText) findViewById(R.id.text_messages);
        b=findViewById(R.id.mssage_sos_button);


        userMessagesList = (RecyclerView) findViewById(R.id.message_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
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
            json_request1.put("title","Location of "+FinalHomeActivity2.uname);

            json_request1.put("message",latitude+","+longitude);
            Log.d("username found ?: ",FinalHomeActivity2.uname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest1=new JsonObjectRequest(Request.Method.POST, check_url, json_request1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject jsonObject) {
                try {
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
        MySingelton.getInstance(MessageActivity.this).addToRequestQueue(jsonObjectRequest1);
    }

    private void getCurrentDeviceLocation(){
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        /**
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            // if (isPermissionGranted) {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Log.d("Message Activity", "getDeviceLocation onComplete: location found");
                        Location location = task.getResult();
                        if (location == null) {
                            Toast.makeText(getApplicationContext(), "location is null", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(getApplicationContext(), "location found\"       "+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        //deviceLatLng = currentLocation;
                        // if(onDeviceLatLngFoundListener!=null)
                        // onDeviceLatLngFoundListener.onDeviceLatLngFound(currentLocation);
                    }
                    else {
                        Log.d("Message Activity", "getDeviceLocation onComplete: location not found");
                    }
                }
            });
            //  }else {
            //this.getLocationPermission();
            // }
        } catch (SecurityException e) {
            e.printStackTrace();
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
                LocationServices.getFusedLocationProviderClient(MessageActivity.this)
                        .removeLocationUpdates(this);

                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();


                    sendCareeLocation();
                    Map map=new HashMap();
                    map.put("latitude",latitude);
                    map.put("longitude",longitude);
                   /* RootRef.child("Location").child(messageSenderID).child(messageReceiverID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) {
                                Log.d("location sent value", latitude + "," + longitude);
                                Toast.makeText(MessageActivity.this, "You sent your current location "+latitude + "," + longitude, Toast.LENGTH_LONG).show();
                            }
                            else{
                                Log.d("location eror",task.getException().toString());
                                Toast.makeText(MessageActivity.this, "Your location can not be sent", Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/

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
                                                Toast.makeText(MessageActivity.this, "You sent your current location "+latitude + "," + longitude, Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                Log.d("location eror",task.getException().toString());
                                                Toast.makeText(MessageActivity.this, "Your location can not be sent", Toast.LENGTH_LONG).show();
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

        LocationServices.getFusedLocationProviderClient(MessageActivity.this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    @SuppressLint("MissingPermission")
    private void getCurrentLocationduplicate()
    {


        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(MessageActivity.this)
                        .removeLocationUpdates(this);

                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                    sendSenderLocation();
                    Map map=new HashMap();
                    map.put("latitude",latitude);
                    map.put("longitude",longitude);
                    RootRef.child("Location").child(messageSenderID).child(messageReceiverID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) {
                                Log.d("location sent value", latitude + "," + longitude);
                                Toast.makeText(MessageActivity.this, "You sent your current location "+latitude + "," + longitude, Toast.LENGTH_LONG).show();
                            }
                            else{
                                Log.d("location eror",task.getException().toString());
                                Toast.makeText(MessageActivity.this, "Your location can not be sent", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }

        };

        LocationServices.getFusedLocationProviderClient(MessageActivity.this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void sendSenderLocation() {

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
            Log.d("all location :",address+","+city+","+state+","+country+","+postalCode+","+knownName);
        } catch (IOException e) {
            e.printStackTrace();
        }


        JSONObject json_request1=new JSONObject();
        try {

            json_request1.put("topic",messageReceiverID+"_"+messageSenderID);
            json_request1.put("title",FinalHomeActivity2.uname+"  sent her current location");

            json_request1.put("message",latitude+","+longitude);
            Log.d("username found ?: ",FinalHomeActivity2.uname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest1=new JsonObjectRequest(Request.Method.POST, check_url, json_request1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject jsonObject) {
                try {
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
        MySingelton.getInstance(MessageActivity.this).addToRequestQueue(jsonObjectRequest1);
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        RootRef.child("Messages").child(mAuth.getCurrentUser().getUid()).child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            messagesList.clear();
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                messagesList.add(dataSnapshot.getValue(Messages.class));
                            }
                            messageAdapter.notifyDataSetChanged();
                            userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }



    private void SendMessage()
    {
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        //Toast.makeText(MessageActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(MessageActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText("");
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}