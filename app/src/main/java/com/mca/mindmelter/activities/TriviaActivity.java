package com.mca.mindmelter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.amplifyframework.datastore.generated.model.Trivia;
import com.mca.mindmelter.MainActivity;
import com.mca.mindmelter.R;
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
        progressBar = findViewById(R.id.progress_bar);
        layoutContent = findViewById(R.id.layout_content);
        triviaTextView = findViewById(R.id.homePageTriviaTextView);
        learnMoreButton = findViewById(R.id.homePageLearnMoreButton);
        retryButton = findViewById(R.id.retryButton);

        // Initialize ViewModel
        triviaViewModel = new TriviaViewModel(getApplication());

        // Observe trivia live data
        triviaViewModel.getTriviaLiveData().observe(this, new Observer<Trivia>() {
            @Override
            public void onChanged(Trivia trivia) {
                if (trivia != null) {
                    triviaTextView.setText(trivia.getTrivia());
                    learnMoreButton.setEnabled(true);
                    retryButton.setVisibility(View.GONE);
                } else {
                    learnMoreButton.setEnabled(false);
                    retryButton.setVisibility(View.VISIBLE);
                }
            }
        });

        // Observe loading live data
        triviaViewModel.isLoadingLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {
                    // If data is loading, show the progress bar and hide content layout
                    progressBar.setVisibility(View.VISIBLE);
                    layoutContent.setVisibility(View.GONE);
                } else {
                    // If data is not loading, hide the progress bar and show content layout
                    progressBar.setVisibility(View.GONE);
                    layoutContent.setVisibility(View.VISIBLE);
                }
            }
        });

        // Load trivia when the activity starts
        triviaViewModel.loadMostRecentTrivia();

        // Set click listener for the Learn More button
        learnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Trivia trivia = triviaViewModel.getTriviaLiveData().getValue();
                if (trivia != null) {
                    Intent intent = new Intent(TriviaActivity.this, ChatActivity.class);
                    intent.putExtra("triviaId", trivia.getId());
                    startActivity(intent);
                }
            }
        });

        // Set click listener for the Retry button
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triviaViewModel.loadMostRecentTrivia();
            }
        });
    }
}
