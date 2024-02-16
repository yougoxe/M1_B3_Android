package com.example.b3_android;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


import com.example.b3_android.DTO.WeatherData;
import com.example.b3_android.service.ColorService;
import com.example.b3_android.service.LocalisationParameterService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.IOException;
public class MainActivity extends AppCompatActivity {

    int color;
    Toolbar toolbar;
    FloatingActionButton swap;
    FloatingActionButton showMap;
    FloatingActionButton add;
    private SeekBar seekBar;
    LocalisationParameterService locationParameterService = new LocalisationParameterService();
    ColorService colorService = new ColorService();
    FusedLocationProviderClient fusedClient;
    Location currentLocation;
    final OkHttpClient client = new OkHttpClient();

    private TableLayout tableList;

    private static String responder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationParameterService.setCurrentActivity(this);
        if (!locationParameterService.isLocationPermissionGranted()) {
            // Check if location permission is enabled
            locationParameterService.requestLocationPermission();
        } else {
            // Check if location is enabled and prompt to enable it
            locationParameterService.checkLocationSettings();
        }

        // get principal color
        SharedPreferences sharedPreferences= getSharedPreferences("AppPreferences", MODE_PRIVATE);
        this.color = this.colorService.getColors(sharedPreferences);

        //configure the toolbar
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        Window window = getWindow();
        this.colorService.setToolbarColor(toolbar, color, window);
        setSupportActionBar(toolbar);

        swap = findViewById(R.id.swap);
        swap.setBackgroundTintList(ColorStateList.valueOf(color));
        swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = tableList.getChildCount();
                for (int i = count - 1; i >= 0; i--) {
                    View child = tableList.getChildAt(i);
                    tableList.removeViewAt(i);
                    tableList.addView(child);
                }
            }
        });

        showMap = findViewById(R.id.showMap);
        showMap.setBackgroundTintList(ColorStateList.valueOf(color));
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationParameterService.IsLocationActivated()){
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    locationParameterService.checkLocationSettings();
                }
            }
        });
        add = findViewById(R.id.add);
        add.setBackgroundTintList(ColorStateList.valueOf(color));
        Drawable AddDrawable = getResources().getDrawable(R.drawable.baseline_add_24);
        AddDrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        add.setImageDrawable(AddDrawable);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationParameterService.IsLocationActivated()){
                    Intent intent = new Intent(MainActivity.this, AddActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    locationParameterService.checkLocationSettings();
                }
            }
        });

        this.seekBar = findViewById(R.id.slider);
        this.seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        this.seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        int radius = sharedPreferences.getInt("radius",20);
        this.seekBar.setProgress(radius);
        this.seekBar.setDrawingCacheBackgroundColor(color);
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Not Used
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not Used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this, String.valueOf(seekBar.getProgress()), Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences= getSharedPreferences("AppPreferences", MODE_PRIVATE);
                sharedPreferences.edit().putInt("radius", seekBar.getProgress()).apply();
                callApi();
            }
        });

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        tableList = findViewById(R.id.list);
        tableList.removeAllViews();

        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setWeightSum(8);

        TableRow.LayoutParams param = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                4f
        );
        TextView labelNom = new TextView(this);
        labelNom.setLayoutParams(param);
        String message = getResources().getString(R.string.no_data_activity_main);
        labelNom.setText(message);
        row.addView(labelNom);

        tableList.addView(row);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_activity_main_params:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        Task<Location> task = fusedClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    callApi();
                }
            }
        });
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
                            tableList.removeAllViews();
                            Gson gson = new Gson();
                            WeatherData[] weatherDataArray = gson.fromJson(MainActivity.responder, WeatherData[].class);
                            for (WeatherData weatherData : weatherDataArray) {
                                    TableRow row = new TableRow(MainActivity.this);
                                    row.setGravity(Gravity.CENTER_VERTICAL);
                                    row.setWeightSum(8);

                                    TableRow.LayoutParams param = new TableRow.LayoutParams(
                                            TableRow.LayoutParams.MATCH_PARENT,
                                            TableRow.LayoutParams.WRAP_CONTENT,
                                            4f
                                    );
                                    param.setMargins(0, 20, 0, 20);
                                    TextView labelNom = new TextView(MainActivity.this);
                                    String text = weatherData.getLocationName() + " " + weatherData.getTemperature() + "°";
                                    labelNom.setText(text);
                                    row.addView(labelNom,param);

                                    TableRow.LayoutParams paramButton = new TableRow.LayoutParams(
                                            TableRow.LayoutParams.MATCH_PARENT,
                                            TableRow.LayoutParams.WRAP_CONTENT,
                                            1f
                                    );

                                    tableList.addView(row);
                                }

                        }
                    });
                } else {
                    Log.e("POST_ERROR", "Code d'erreur : " + response.code() +" "+ response.message());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String message = getResources().getString(R.string.enter_echec_activity_add);
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}