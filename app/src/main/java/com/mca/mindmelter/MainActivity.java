package com.mca.mindmelter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mca.mindmelter.activities.ChatActivity;
import com.mca.mindmelter.activities.ProfilePageActivity;
import com.mca.mindmelter.utilities.TextToSpeechUtility;

public class MainActivity extends AppCompatActivity {

    private TextToSpeechUtility textToSpeechUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextToSpeechUtility
        textToSpeechUtility = new TextToSpeechUtility(this);

        // Use the utility to announce the page to the user
        textToSpeechUtility.speak("Main Activity");

        setUpProfilePageButton();

        Button btnGotoChat = findViewById(R.id.btn_goto_chat);
        btnGotoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);

                // Add your TriviaId as an extra to the intent
                String triviaId = "241b57fb-02b3-4314-9748-20fe2652e7b0";  // replace with actual triviaId
                intent.putExtra("triviaId", triviaId);

                // Launch the ChatActivity
                startActivity(intent);
            }
        });
    }

    public void setUpProfilePageButton() {
        findViewById(R.id.homePageLearnMoreButton).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(MainActivity.this, ProfilePageActivity.class);
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