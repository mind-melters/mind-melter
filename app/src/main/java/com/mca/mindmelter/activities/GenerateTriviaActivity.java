package com.mca.mindmelter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mca.mindmelter.R;
import com.mca.mindmelter.enums.TriviaCategory;
import com.ramotion.circlemenu.CircleMenuView;

public class GenerateTriviaActivity extends AppCompatActivity {
    private TextView artsAndLiteratureLabel;
    private TextView scienceAndNatureLabel;
    private TextView sportsAndLeisureLabel;
    private TextView historyLabel;
    private TextView geographyLabel;
    private TextView entertainmentLabel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_trivia);

        // Initialize views
        artsAndLiteratureLabel = findViewById(R.id.arts_and_literature_label);
        scienceAndNatureLabel = findViewById(R.id.science_and_nature_lable);
        sportsAndLeisureLabel = findViewById(R.id.sports_and_leisure_label);
        historyLabel = findViewById(R.id.history_label);
        geographyLabel = findViewById(R.id.geography_label);
        entertainmentLabel = findViewById(R.id.entertainment_label);
        
        final CircleMenuView menu = findViewById(R.id.circle_menu);
        menu.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
            }

            @Override
            public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                artsAndLiteratureLabel.setVisibility(View.VISIBLE);
                scienceAndNatureLabel.setVisibility(View.VISIBLE);
                sportsAndLeisureLabel.setVisibility(View.VISIBLE);
                historyLabel.setVisibility(View.VISIBLE);
                geographyLabel.setVisibility(View.VISIBLE);
                entertainmentLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                artsAndLiteratureLabel.setVisibility(View.INVISIBLE);
                scienceAndNatureLabel.setVisibility(View.INVISIBLE);
                sportsAndLeisureLabel.setVisibility(View.INVISIBLE);
                historyLabel.setVisibility(View.INVISIBLE);
                geographyLabel.setVisibility(View.INVISIBLE);
                entertainmentLabel.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int index) {
                artsAndLiteratureLabel.setVisibility(View.INVISIBLE);
                scienceAndNatureLabel.setVisibility(View.INVISIBLE);
                sportsAndLeisureLabel.setVisibility(View.INVISIBLE);
                historyLabel.setVisibility(View.INVISIBLE);
                geographyLabel.setVisibility(View.INVISIBLE);
                entertainmentLabel.setVisibility(View.INVISIBLE);

                TriviaCategory category = TriviaCategory.values()[index];
                Intent intent = new Intent(GenerateTriviaActivity.this, TriviaActivity.class);
                intent.putExtra("CATEGORY", category);
                startActivity(intent);
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {

            }
        });

        setUpProfileButton();
    }

    public void setUpProfileButton() {
        findViewById(R.id.triviaActivityProfilePageImageView).setOnClickListener(view -> {
            Intent goToProfilePageIntent = new Intent(GenerateTriviaActivity.this, ProfilePageActivity.class);
            startActivity(goToProfilePageIntent);
        });
    }
}