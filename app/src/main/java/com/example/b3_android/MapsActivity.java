package com.example.b3_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.b3_android.DTO.WeatherData;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    final OkHttpClient client = new OkHttpClient();

    private ColorService colorService = new ColorService();
    LocalisationParameterService locationParameterService = new LocalisationParameterService();

    private static String responder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        map = findViewById(R.id.map);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        locationParameterService.setCurrentActivity(this);
        if (!locationParameterService.isLocationPermissionGranted()) {
            // Check if location permission is enabled
            locationParameterService.requestLocationPermission();
        } else {
            // Check if location is enabled and prompt to enable it
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

        this.gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Prevent the map from centering on the marker.
                showDialogWithMarkerInfo(marker.getTitle(), marker.getSnippet());
                return true;
            }
        });

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
                break;
        }
        String title = getResources().getString(R.string.current_location_activity_map);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title).icon(BitmapDescriptorFactory.defaultMarker(colorBitmap));;

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

        callApi();
    }


    private void showDialogWithMarkerInfo(String title, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nothing to do
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
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

    public void addMarkers(WeatherData weatherData){
        LatLng markerPosition = new LatLng(weatherData.getLatitude(), weatherData.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(markerPosition);
        markerOptions.title(weatherData.getLocationName());

        // format Date
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().locale);
        String formattedDate = outputDateFormat.format(weatherData.getDate());

        String weatherName = "";
        switch (weatherData.getWeatherType().getId()) {
            case 1:
                weatherName = getResources().getString(R.string.sunny_activity_map);
                break;
            case 2:
                weatherName = getResources().getString(R.string.cloudy_activity_map);
                break;
            case 3:
                weatherName = getResources().getString(R.string.rain_activity_map);
                break;
            case 4:
                weatherName = getResources().getString(R.string.fog_activity_map);
                break;
            case 5:
                weatherName = getResources().getString(R.string.partly_cloud_activity_map);
                break;
            case 6:
                weatherName = getResources().getString(R.string.showers_activity_map);
                break;
            case 7:
                weatherName = getResources().getString(R.string.snow_activity_map);
                break;
            case 8:
                weatherName = getResources().getString(R.string.thunder_activity_map);
                break;
        }


        markerOptions.snippet(weatherName+ " " + weatherData.getTemperature() + "° \n" + formattedDate);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.layout.activity_maps,weatherData.getWeatherType().getId())));
        this.gMap.addMarker(markerOptions);
    }

    private Bitmap getMarkerBitmapFromView(int resId, int id) {
        View customMarkerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(resId, null);
        TextView markerText = new TextView(getApplicationContext());
        switch (id) {
            case 1:
                markerText.setText("☀️ ️");
                break;
            case 2:
                markerText.setText("☁️ ️");
                break;
            case 3:
                markerText.setText("🌧️");
                break;
            case 4:
                markerText.setText("🌫️");
                break;
            case 5:
                markerText.setText("⛅  ");
                break;
            case 6:
                markerText.setText("🌦️  ");
                break;
            case 7:
                markerText.setText("❄️ ");
                break;
            case 8:
                markerText.setText("🌩️");
                break;
        }
        markerText.setTextColor(Color.WHITE);
        markerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
        markerText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerText.layout(0, 0, markerText.getMeasuredWidth(), markerText.getMeasuredHeight());
        markerText.setDrawingCacheEnabled(true);
        markerText.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(markerText.getMeasuredWidth(), markerText.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        markerText.draw(canvas);
        return returnedBitmap;
    }

    public void callApi()
    {
        SharedPreferences sharedPreferences= getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String apilink = sharedPreferences.getString("apiLink", "");
        int radius = sharedPreferences.getInt("radius",20);
        String url = apilink+"/api/reports/closests?"+"longitude=" + currentLocation.getLongitude() + "&latitude=" + currentLocation.getLatitude() + "&kmRadius=" + radius;;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                e.getMessage();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    responder = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            WeatherData[] weatherDataArray = gson.fromJson(MapsActivity.responder, WeatherData[].class);

                            for (WeatherData weatherData : weatherDataArray) {

                                addMarkers(weatherData);
                            }
                        }
                    });
                } else {
                    Log.e("POST_ERROR", "Code d'erreur : " + response.code() +" "+ response.message());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String message = getResources().getString(R.string.enter_echec_activity_add);
                            Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}