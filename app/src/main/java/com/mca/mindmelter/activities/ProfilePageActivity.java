package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.mca.mindmelter.R;
import com.mca.mindmelter.utilities.TextToSpeechUtility;

public class ProfilePageActivity extends AppCompatActivity {

    private static final String TAG = "ProfilePageActivity";
    private TextToSpeechUtility textToSpeechUtility;
    private SharedPreferences sharedPreferences;
    private boolean ttsMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        setUpSettingsButton();

        // Initialize TextToSpeechUtility
        textToSpeechUtility = new TextToSpeechUtility(this);

        // Check shared preferences for TTS state
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        ttsMode = sharedPreferences.getBoolean("tts", true); //TTS is on by default

        // If TTS mode is enabled, use the utility to announce the page to the user
        if (ttsMode) {
            textToSpeechUtility.speak("Profile Page Activity");
        }
    }

    public void setUpSettingsButton() {
        findViewById(R.id.mainActivitySettingsImageView).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(ProfilePageActivity.this, SettingsPageActivity.class);
            startActivity(goToSettingsPageIntent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure to shutdown TextToSpeech
        textToSpeechUtility.shutdown();
    }
}