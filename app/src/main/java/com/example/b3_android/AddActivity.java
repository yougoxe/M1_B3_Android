package com.example.b3_android;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.b3_android.service.ColorService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddActivity extends AppCompatActivity {

    private EditText temperatureEditText;
    private Spinner unitSpinner;
    private Button submitButton;
    int color;
    private ColorService colorService = new ColorService();
    Location currentLocation;
    FusedLocationProviderClient fusedClient;
    private Toolbar toolbar;

    final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // get form
        temperatureEditText = findViewById(R.id.temperatureEditText);
        unitSpinner = findViewById(R.id.unitSpinner);
        submitButton = findViewById(R.id.submitButton);

        // get principal color
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        this.color = this.colorService.getColors(sharedPreferences);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weather_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        // configure toolbar
        this.toolbar = (Toolbar) findViewById(R.id.activity_add_toolbar);
        this.configureToolbar();
        Window window = getWindow();
        this.colorService.setToolbarColor(this.toolbar, color,window);

        // get location
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        // form button
        submitButton.setBackgroundColor(color);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void configureToolbar(){
        // back to home button in the navbar
        this.toolbar.setBackgroundColor(color);
        setSupportActionBar(this.toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        Drawable drawable = getResources().getDrawable(R.drawable.baseline_arrow_back_20);
        drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        ab.setHomeAsUpIndicator(drawable);
        this.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
                }
            }
        });
    }

    private void submitForm() {
        String temperature = temperatureEditText.getText().toString();
        int unit = unitSpinner.getSelectedItemPosition()+1;

        if (!temperature.isEmpty()) {
            MediaType mediaType = MediaType.parse("application/json");
            String json = "{"
                    + "\"latitude\":"+currentLocation.getLatitude()+","
                    + "\"longitude\":"+currentLocation.getLongitude()+","
                    + "\"temperature\":"+temperature+","
                    + "\"weatherType\": {"
                    +"\"id\":"+unit+""
                    +"  }"
                    +"}";
            RequestBody body = RequestBody.create(json, mediaType);
            // get the api link configured
            SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences",  MODE_PRIVATE);
            String apilink = sharedPreferences.getString("apiLink", "");
            String url = apilink+"/api/report";
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            String responseCode= "";
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    e.getMessage();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message = getResources().getString(R.string.enter_success_activity_add);
                                Toast.makeText(AddActivity.this, message, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        Log.e("POST_ERROR", "Code d'erreur : " + response.code() +" "+ response.message());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String message = getResources().getString(R.string.enter_echec_activity_add);
                                Toast.makeText(AddActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            } else {
                String message = getResources().getString(R.string.enter_temperature_activity_add);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
    }
}