package com.example.b3_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import com.example.b3_android.service.ColorService;
import com.example.b3_android.service.LocalisationParameterService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    int color;
    Toolbar toolbar;
    FloatingActionButton showMap;
    FloatingActionButton add;
    private SeekBar seekBar;
    FloatingActionButton refresh;

    LocalisationParameterService locationParameterService = new LocalisationParameterService();
    ColorService colorService = new ColorService();

    private SharedPreferences sharedPreferences;

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
                SharedPreferences sharedPreferences= getSharedPreferences("AppPreferences", MODE_PRIVATE);
                sharedPreferences.edit().putInt("radius", seekBar.getProgress()).apply();
            }
        });
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
}