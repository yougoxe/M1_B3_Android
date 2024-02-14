package com.example.b3_android.service;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ColorService extends AppCompatActivity {

    private int color;

    public ColorService() {}

    public int getColors(SharedPreferences sharedPreferences)
    {
        String codeCouleur = sharedPreferences.getString("mainColor", "");
        this.color =  Color.parseColor("#000000");
        if(!codeCouleur.equals("")){
            this.color = Color.parseColor(codeCouleur);
        }
        return this.color;
    }

    public String getColorString(SharedPreferences sharedPreferences)
    {
        return sharedPreferences.getString("mainColor", "");
    }

    public void setToolbarColor(Toolbar toolbar, int color, Window window)
    {
        toolbar.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public void setNotificationBarColor(int color, Window window)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

}
