package com.example.b3_android;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.b3_android.databinding.ActivityAddBinding;

public class AddActivity extends AppCompatActivity {

    private EditText temperatureEditText;
    private Spinner unitSpinner;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // get form
        temperatureEditText = findViewById(R.id.temperatureEditText);
        unitSpinner = findViewById(R.id.unitSpinner);
        submitButton = findViewById(R.id.submitButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weather_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        this.configureToolbar();

        // form button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void configureToolbar(){
        // back to home button in the navbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_add_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        Drawable drawable = getResources().getDrawable(R.drawable.baseline_arrow_back_20);
        drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        ab.setHomeAsUpIndicator(drawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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