package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.mca.mindmelter.R;
import com.mca.mindmelter.utilities.TextToSpeechUtility;

public class SettingsPageActivity extends AppCompatActivity {
    public static final String TTS_SWITCH_TAG = "TTS_SWITCH_TAG";
    public static final String USER_NICKNAME_TAG = "USER_NICKNAME_TAG";
    public static final String DARK_MODE_SWITCH_TAG = "DARK_MODE_SWITCH_TAG";
    private TextToSpeechUtility ttsUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        ttsUtility = new TextToSpeechUtility(this);

        setUpModeSwitch();
        setUpTTSSwitch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ttsUtility.shutdown();
    }

    public void setUpModeSwitch() {
        SwitchCompat modeSwitch = findViewById(R.id.modeSwitch);
        SharedPreferences sharedPreferences = getSharedPreferences(USER_NICKNAME_TAG, MODE_PRIVATE);
        boolean isDarkModeOn = sharedPreferences.getBoolean(DARK_MODE_SWITCH_TAG, false);
        modeSwitch.setChecked(isDarkModeOn);
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DARK_MODE_SWITCH_TAG, isChecked);
            editor.apply();

            // Toggle between light and dark mode based on isChecked
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    public void setUpTTSSwitch() {
        SwitchCompat ttsSwitch = findViewById(R.id.ttsSwitch);
        SharedPreferences sharedPreferences = getSharedPreferences(USER_NICKNAME_TAG, MODE_PRIVATE);
        ttsSwitch.setChecked(sharedPreferences.getBoolean(TTS_SWITCH_TAG, false));
        ttsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(TTS_SWITCH_TAG, isChecked);
            editor.apply();

            if (isChecked) {
                String ttsText = "Text to speech is turned on.";
                ttsUtility.speak(ttsText);
            }
        });
    }

}