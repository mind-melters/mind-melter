package com.mca.mindmelter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.amplifyframework.core.Amplify;
import com.mca.mindmelter.activities.ChatActivity;

import com.mca.mindmelter.activities.GenerateTriviaActivity;
import com.mca.mindmelter.activities.ProfilePageActivity;
import com.mca.mindmelter.activities.TriviaActivity;
import com.mca.mindmelter.activities.authentication.LogInActivity;
import com.mca.mindmelter.repositories.UserRepository;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Amplify.Auth.getCurrentUser(
                success -> {
                    Log.i(TAG, "User authenticated with username: " + success.getUsername());
                    UserRepository.getInstance(this).loadUser();
                    // if the user is authenticated, start the TriviaActivity
                    startActivity(new Intent(MainActivity.this, GenerateTriviaActivity.class));
                },
                failure -> {
                    Log.i(TAG, "There is no current authenticated user");
                    // if the user is not authenticated, start the LogInActivity
                    startActivity(new Intent(MainActivity.this, LogInActivity.class));
                }
        );
        finish();
    }
}