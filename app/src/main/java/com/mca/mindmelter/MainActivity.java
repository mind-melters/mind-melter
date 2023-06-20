package com.mca.mindmelter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;
import com.mca.mindmelter.activities.ChatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGotoChat = findViewById(R.id.btn_goto_chat);
        btnGotoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);

                // Add your TriviaId as an extra to the intent
                String triviaId = "yourTriviaId";  // replace with actual triviaId
                intent.putExtra("triviaId", triviaId);

                // Launch the ChatActivity
                startActivity(intent);
            }
        });
    }
}