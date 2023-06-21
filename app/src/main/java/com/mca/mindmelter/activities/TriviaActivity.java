package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.mca.mindmelter.R;

public class TriviaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

//        setUpProfilePageButton();
    }

//    public void setUpProfilePageButton() {
//        findViewById(R.id.homePageLearnMoreButton).setOnClickListener(view -> {
//            Intent goToSettingsPageIntent = new Intent(HomePageActivity.this, ProfilePageActivity.class);
//            startActivity(goToSettingsPageIntent);
//        });
//    }
}