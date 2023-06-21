package com.mca.mindmelter.activities.all;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.mca.mindmelter.R;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

//        setUpProfilePageButton();
    }

//    public void setUpProfilePageButton() {
//        findViewById(R.id.homePageLearnMoreButton).setOnClickListener(view -> {
//            Intent goToSettingsPageIntent = new Intent(HomePageActivity.this, ProfilePageActivity.class);
//            startActivity(goToSettingsPageIntent);
//        });
//    }
}