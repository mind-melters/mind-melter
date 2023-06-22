package com.mca.mindmelter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.mca.mindmelter.activities.ChatActivity;

import com.mca.mindmelter.activities.ProfilePageActivity;
import com.mca.mindmelter.activities.TriviaActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpProfilePageButton();
        setUpChatActivityButton();

        // This will automatically reroute to the TriviaActivity on App load
        Intent goToTriviaActivityIntent = new Intent(MainActivity.this, TriviaActivity.class);
        startActivity(goToTriviaActivityIntent);
    }

    public void setUpProfilePageButton() {
        findViewById(R.id.activity_trivia_learn_more_button).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(MainActivity.this, ProfilePageActivity.class);
            startActivity(goToSettingsPageIntent);
        });
    }

    public void setUpChatActivityButton() {
        Button btnGotoChat = findViewById(R.id.btn_goto_chat);
        btnGotoChat.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);

                // Add your TriviaId as an extra to the intent
                String triviaId = "241b57fb-02b3-4314-9748-20fe2652e7b0";  // replace with actual triviaId
                intent.putExtra("triviaId", triviaId);

                // Launch the ChatActivity
                startActivity(intent);
        });
    }
}