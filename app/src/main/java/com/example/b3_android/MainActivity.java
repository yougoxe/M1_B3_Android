package com.example.b3_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.b3_android.service.LocalisationParameterService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton showMap;
    FloatingActionButton add;
    FloatingActionButton refresh;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_ENABLE_LOCATION = 1002;

    LocalisationParameterService locationParameterService = new LocalisationParameterService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationParameterService.setCurrentActivity(this);
        if (!locationParameterService.isLocationPermissionGranted()) {
            // Demander la permission de localisation
            locationParameterService.requestLocationPermission();
        } else {
            // Vérifier si la localisation est activée
                locationParameterService.checkLocationSettings();
        }


        //configurer la toolbar
        this.configureToolbar();

        // récupéré la couleur de la toolbar
        TypedArray attributes = getTheme().obtainStyledAttributes(new int[] {androidx.appcompat.R.attr.colorPrimary});
        int colorPrimary = attributes.getColor(0, 0);
        attributes.recycle();


        int color = Color.parseColor("#000000");
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
        refresh = findViewById(R.id.refresh);
        refresh.setBackgroundTintList(ColorStateList.valueOf(color));
        Drawable refreshDrawable = getResources().getDrawable(R.drawable.baseline_refresh_24);
        refreshDrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        refresh.setImageDrawable(refreshDrawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //2 - Inflate the menu and add it to the Toolbar
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //3 - Handle actions on menu items
        switch (item.getItemId()) {
            case R.id.menu_activity_main_params:
                Toast.makeText(this, "Il n'y a rien à paramétrer ici, passez votre chemin...", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureToolbar(){
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        // Sets the Toolbar
        setSupportActionBar(toolbar);
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("La géolocalisation est désactivée. Voulez-vous l'activer?")
                    .setCancelable(false)
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, REQUEST_ENABLE_LOCATION);
                        }
                    })
                    .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Toast.makeText(MainActivity.this, "La géolocalisation est nécessaire pour utiliser cette application.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            checkLocationEnabled();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationEnabled();
            } else {
                Toast.makeText(this, "Permission de géolocalisation refusée.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}