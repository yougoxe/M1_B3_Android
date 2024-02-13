package com.example.b3_android.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class LocalisationParameterService extends Service {
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private Activity currentActivity;

    public LocalisationParameterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    public boolean IsLocationActivated()
    {
        LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void checkLocationSettings() {
        if (currentActivity == null) {
            return;
        }
        LocationManager locationManager = (LocationManager) currentActivity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
            builder.setMessage("La géolocalisation est désactivée. Voulez-vous l'activer?")
                    .setCancelable(false)
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            currentActivity.startActivity(intent);
                        }
                    })
                    .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            closeApp();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void closeApp() {
        if (currentActivity != null) {
            currentActivity.finish();
        }
    }

    public void requestLocationPermission() {
        if (currentActivity == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(currentActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    public boolean isLocationPermissionGranted() {
        if (currentActivity == null) {
            return false;
        }
        return ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
