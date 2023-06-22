package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import com.mca.mindmelter.R;
import com.mca.mindmelter.utilities.TextToSpeechUtility;

public class SettingsPageActivity extends AppCompatActivity {

    Switch themeSwitch, ttsSwitch;
    boolean nightMode, ttsMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        themeSwitch = findViewById(R.id.modeSwitch);
        ttsSwitch = findViewById(R.id.switch2);

        setUpThemeSwitch();
        setUpTTSSwitch();
    }

    public void setUpThemeSwitch(){
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("night", false); //light mode is default

        if(nightMode){
            themeSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        themeSwitch.setOnClickListener(view ->{
            if (nightMode){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor = sharedPreferences.edit();
                editor.putBoolean("night", false);
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor = sharedPreferences.edit();
                editor.putBoolean("night", true);
            }
            editor.apply();
        });
    }

    public void setUpTTSSwitch(){
        ttsMode = sharedPreferences.getBoolean("tts", true); //TTS is on by default

        if(!ttsMode){
            ttsSwitch.setChecked(false);
        }
        ttsSwitch.setOnClickListener(view ->{
            if (ttsMode){
                editor = sharedPreferences.edit();
                editor.putBoolean("tts", false);
            }else{
                editor = sharedPreferences.edit();
                editor.putBoolean("tts", true);
            }
            editor.apply();
        });
    }
}
