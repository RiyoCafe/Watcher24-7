package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watcher.api.GoogleMapsApi;
import com.example.watcher.api.NearbyPlacesApiResponse;
import com.example.watcher.helper.DeviceLocationFinder;
import com.example.watcher.helper.MapCustomizer;
import com.example.watcher.places.NearByPlace;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = LocationMapActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private MapCustomizer mapCustomizer;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private ImageView button1, button2;

    private LatLng latLng;

    private String friendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_map);
        button1 = findViewById(R.id.hospital);
        button2 = findViewById(R.id.policeStation);
        latLng = getIntent().getParcelableExtra("latlang");

        friendName = getIntent().getExtras().get(MessageActivity.FRIEND_NAME).toString();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        getLocationPermission();

        updateLocationUI(map);
        if(latLng!=null)
            Log.e("after map ready enteres", latLng.toString());
        mapCustomizer = new MapCustomizer(map);
        initListeners();
    }


    private void initListeners() {
        button1.setOnClickListener(v -> {
            Toast.makeText(this, "Showing nearby hospitals...", Toast.LENGTH_SHORT).show();
            showNearByHospitals();
        });


        button2.setOnClickListener(v -> {
            Toast.makeText(this, "Showing nearby police stations...", Toast.LENGTH_SHORT).show();
            showNearByPoliceStations();
        });
    }

    private void showNearByHospitals() {
        map.clear();
        if(latLng!=null)
            makePlacesCall("hospital", latLng);
        else{
            DeviceLocationFinder.getCurrentDeviceLocation(getApplicationContext(),
                    latLng -> {
                       makePlacesCall("hospital", latLng);
                    });
        }

    }

    private void showNearByPoliceStations() {
        map.clear();
        if(latLng!=null)
            makePlacesCall("police", latLng);
        else{
            DeviceLocationFinder.getCurrentDeviceLocation(getApplicationContext(),
                    latLng -> {
                        makePlacesCall("police", latLng);
                    });
        }
    }

    private void makePlacesCall(String type, LatLng latLng) {
        String location = latLng.latitude + "," + latLng.longitude;
        Call<NearbyPlacesApiResponse> call = GoogleMapsApi.instance.placesService
                .fetchNearByPlaces(location, 15000, type, BuildConfig.GOOGLE_MAP_WEB_API_KEY);

        call.enqueue(new Callback<NearbyPlacesApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<NearbyPlacesApiResponse> call, @NonNull Response<NearbyPlacesApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "onResponse: " + response.body());
                    showNearByPlaces(response.body().getResults());
                } else {
                    Log.e(TAG, "onResponse: " + type + " fetching is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NearbyPlacesApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: error fetching " + type, t);
            }
        });
    }

    public void showNearByPlaces(List<NearByPlace> nearByPlaceList) {
        if (nearByPlaceList.size() == 0)
            Toast.makeText(this, "No nearby locations", Toast.LENGTH_SHORT).show();
        List<LatLng> latLngList = new ArrayList<>();
        for (NearByPlace nearbyPlace : nearByPlaceList) {
            LatLng latLng = nearbyPlace.getGeometry().getLocation().getLatLng();
            latLngList.add(latLng);
            mapCustomizer.addMarker(latLng, nearbyPlace.getName());
        }
        mapCustomizer.animateCameraWithBounds(latLngList);
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }

        updateLocationUI(map);
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI(GoogleMap map) {
        DeviceLocationFinder.getCurrentDeviceLocation(getApplicationContext(),
                latLng -> {
                    map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("My location"));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    map.setMyLocationEnabled(true);
                    map.getUiSettings().setMyLocationButtonEnabled(true);
                });

        if (latLng != null) {
            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(friendName+" is here."));

            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Location is here"));

            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            map.animateCamera(CameraUpdateFactory.zoomIn());

            map.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM), 2000, null);
        }
    }
}