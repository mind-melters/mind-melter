package com.mca.mindmelter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.mca.mindmelter.activities.all.HomePageActivity;
import com.mca.mindmelter.activities.all.ProfilePageActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpProfilePageButton();
    }

    public void setUpProfilePageButton() {
        findViewById(R.id.homePageLearnMoreButton).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(MainActivity.this, ProfilePageActivity.class);
            startActivity(goToSettingsPageIntent);
        });
    }
}