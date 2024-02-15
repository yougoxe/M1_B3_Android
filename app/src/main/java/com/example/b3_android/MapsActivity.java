package com.example.b3_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.b3_android.service.ColorService;
import com.example.b3_android.service.LocalisationParameterService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
    FloatingActionButton showMain;
    FrameLayout map;
    GoogleMap gMap;
    Location currentLocation;
    Marker marker;
    FusedLocationProviderClient fusedClient;
    private static final int REQUEST_CODE = 101;
    private int color;
    private String colorString;

    private ColorService colorService = new ColorService();
    LocalisationParameterService locationParameterService = new LocalisationParameterService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        map = findViewById(R.id.map);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        locationParameterService.setCurrentActivity(this);
        if (!locationParameterService.isLocationPermissionGranted()) {
            // Demander la permission de localisation
            locationParameterService.requestLocationPermission();
        } else {
            // Vérifier si la localisation est activée
            locationParameterService.checkLocationSettings();
        }

        getLocation();

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        this.color = this.colorService.getColors(sharedPreferences);
        this.colorString = this.colorService.getColorString(sharedPreferences);

        Window window = getWindow();
        this.colorService.setNotificationBarColor(this.color,window);

        showMain = findViewById(R.id.showMain);
        showMain.setBackgroundTintList(ColorStateList.valueOf(color));
        showMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    //Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;
        this.gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // disable default comportement
        this.gMap.getUiSettings().setRotateGesturesEnabled(false);
        this.gMap.getUiSettings().setZoomGesturesEnabled(false);
        this.gMap.getUiSettings().setScrollGesturesEnabled(false);

        float colorBitmap = BitmapDescriptorFactory.HUE_BLUE;
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        switch (this.colorString) {
            case "#000000":
                colorBitmap = BitmapDescriptorFactory.HUE_RED;
                break;
            case "#FF0000":
                colorBitmap = BitmapDescriptorFactory.HUE_RED;
                break;
            case "#00FF00":
                colorBitmap = BitmapDescriptorFactory.HUE_GREEN;
                break;
            case "#0000FF":
                colorBitmap = BitmapDescriptorFactory.HUE_BLUE;
                break;
            default:
                System.out.println("Couleur non reconnue");
        }
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Current Location").icon(BitmapDescriptorFactory.defaultMarker(colorBitmap));;

        // radius calcul
        SharedPreferences sharedPreferences= getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int radius = sharedPreferences.getInt("radius", 100);
        double zoomLevel = 15 - Math.log(radius * 1000 / 500) / Math.log(2);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom((float) zoomLevel)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        this.gMap.addMarker(markerOptions);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
}