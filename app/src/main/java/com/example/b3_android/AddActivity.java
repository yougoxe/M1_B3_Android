package com.example.b3_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.example.b3_android.service.ColorService;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

public class AddActivity extends AppCompatActivity {

    private EditText temperatureEditText;
    private Spinner unitSpinner;
    private Button submitButton;
    int color;
    private ColorService colorService = new ColorService();
    private Toolbar toolbar;

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

    private void submitForm() {
        String temperature = temperatureEditText.getText().toString();
        String unit = unitSpinner.getSelectedItem().toString();

        if (!temperature.isEmpty()) {
            // Traitement de la température ici
            Toast.makeText(this, "Température : " + temperature + " " + unit, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Veuillez saisir une température", Toast.LENGTH_SHORT).show();
        }
    }
}