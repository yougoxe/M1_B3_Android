package com.example.b3_android.service;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

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

}
