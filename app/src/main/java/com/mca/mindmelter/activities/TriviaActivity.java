package com.mca.mindmelter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.datastore.generated.model.Trivia;

import com.mca.mindmelter.R;
import com.mca.mindmelter.enums.TriviaCategory;
import com.mca.mindmelter.viewmodels.TriviaViewModel;

public class TriviaActivity extends AppCompatActivity {

    private TriviaViewModel triviaViewModel;
    private ProgressBar progressBar;
    private LinearLayout layoutContent;
    private TextView triviaTextView;
    private Button learnMoreButton;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        // Initialize views
        progressBar = findViewById(R.id.activity_trivia_progress_bar);
        layoutContent = findViewById(R.id.activity_trivia_layout_content);
        triviaTextView = findViewById(R.id.activity_trivia_text_view);
        learnMoreButton = findViewById(R.id.activity_trivia_learn_more_button);
        retryButton = findViewById(R.id.activity_trivia_retry_button);

        // Initialize ViewModel
        triviaViewModel = new TriviaViewModel(getApplication());

        // Observe loading live data
        triviaViewModel.isLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                // If data is loading, show the progress bar and hide content layout
                progressBar.setVisibility(View.VISIBLE);
                layoutContent.setVisibility(View.GONE);
            } else {
                // If data is not loading, hide the progress bar and show content layout
                progressBar.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
            }
        });

        // Observe trivia live data
        triviaViewModel.getTriviaLiveData().observe(this, trivia -> {
            if (trivia != null) {
                triviaTextView.setText(trivia.getTrivia());
                learnMoreButton.setEnabled(true);
                retryButton.setVisibility(View.GONE);
            } else {
                learnMoreButton.setEnabled(false);
                retryButton.setVisibility(View.VISIBLE);
            }
        });

        // Get the trivia category
        TriviaCategory category = (TriviaCategory) getIntent().getSerializableExtra("CATEGORY");
        String categoryName = category.getDisplayName();

        // Observe User object and generate trivia when User is ready
        triviaViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                triviaViewModel.generateTrivia(categoryName);
            }
        });

        // Set click listener for the Learn More button
        learnMoreButton.setOnClickListener(view -> {
            Trivia trivia = triviaViewModel.getTriviaLiveData().getValue();
            if (trivia != null) {
                Intent intent = new Intent(TriviaActivity.this, ChatActivity.class);
                intent.putExtra("triviaId", trivia.getId());
                startActivity(intent);
            }
        });

        // Set click listener for the Retry button
        retryButton.setOnClickListener(v -> triviaViewModel.generateTrivia(categoryName));

        setUpProfilePageButton();
    }
    public void setUpProfilePageButton() {
        findViewById(R.id.activity_trivia_profile_button).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(TriviaActivity.this, ProfilePageActivity.class);
            startActivity(goToSettingsPageIntent);
        });
    }
}
