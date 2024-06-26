package com.example.b3_android;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.b3_android.service.ColorService;

public class SettingActivity extends AppCompatActivity {

    int color;
    private String colorCode;
    private EditText apiLinkEditText;
    private SharedPreferences sharedPreferences;

    private Spinner mainColorSpinner;

    private ColorService colorService = new ColorService();

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        apiLinkEditText = findViewById(R.id.api_link_edit_text);
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String apiLink = sharedPreferences.getString("apiLink", "");
        this.colorCode = sharedPreferences.getString("mainColor", "");
        this.color = this.colorService.getColors(sharedPreferences);

        this.toolbar = (Toolbar) findViewById(R.id.activity_add_toolbar);
        this.configureToolbar();
        Window window = getWindow();
        this.colorService.setToolbarColor(this.toolbar, color,window);


        apiLinkEditText.setText(apiLink);

        apiLinkEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apiLink = apiLinkEditText.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("apiLink", apiLink);
                editor.apply();
            }
        });

        //setup spinner data
        mainColorSpinner = findViewById(R.id.main_color_spinner);
        String[] color_string_array = getResources().getStringArray(R.array.color_spinner_activity_setting);
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, color_string_array);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mainColorSpinner.setAdapter(colorAdapter);

        mainColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean needReload = false;
                switch (parent.getSelectedItemPosition()) {
                    case 0:
                        break;
                    case 1:
                        colorCode = "#000000";
                        needReload = true;
                        break;
                    case 2:
                        colorCode = "#FF0000";
                        needReload = true;
                        break;
                    case 3:
                        colorCode = "#00FF00";
                        needReload = true;
                        break;
                    case 4:
                        colorCode = "#0000FF";
                        needReload = true;
                        break;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("mainColor", colorCode);
                editor.apply();
                if(needReload){
                    requestReloadMainActivity();
                    recreate();
                    mainColorSpinner.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void requestReloadMainActivity() {
        Intent intent = new Intent("com.example.app.RELOAD_MAIN_ACTIVITY");
        sendBroadcast(intent);
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
                finish();
            }
        });
    }
}