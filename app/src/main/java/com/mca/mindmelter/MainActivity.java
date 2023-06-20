package com.mca.mindmelter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Trivia;
import com.amplifyframework.datastore.generated.model.User;
import com.mca.mindmelter.activities.ChatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the current date as java.util.Date
        Date now = new Date();

        // Format the date as a string
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String awsDateTime = dateFormat.format(now);

        // Build Trivia object and save to database
        Trivia testTrivia = Trivia.builder()
                .userId("7c19c275-7b4f-4d02-bea0-3783399ec523")
                .trivia("On June 20, 1782, the Congress adopted the Great Seal of the United States, featuring the bald eagle. The bald eagle was chosen for its strength, longevity, and majestic looks, and has since become a symbol of freedom and the American spirit. The Great Seal is still used today on official documents and government buildings.")
                .createdAt(new Temporal.DateTime(awsDateTime))
                .build();

        Amplify.API.mutate(
                ModelMutation.create(testTrivia),
                response -> {
                    Log.i("TriviaCreation", "Saved item: " + response.getData().getId());
                },
                error -> {
                    Log.e("TriviaCreation", "Could not save item to API/dynamoDB", error);
                }
        );

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
}