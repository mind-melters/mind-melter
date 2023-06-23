package com.mca.mindmelter.activities.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.mca.mindmelter.R;
import com.mca.mindmelter.activities.TriviaActivity;
import com.mca.mindmelter.utilities.TextToSpeechUtility;

public class LogInActivity extends AppCompatActivity {
    public static final String TAG = "LogInActivity";

    // Creating an instance of the TextToSpeechUtility class
    private TextToSpeechUtility ttsUtility;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Initialize the TextToSpeechUtility object
        ttsUtility = new TextToSpeechUtility(this);
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);

        setUpLoginButton();
        setUpSignUpButton();
    }

    public void setUpLoginButton() {
        Intent callingIntent = getIntent();
        String userEmail = callingIntent.getStringExtra(VerifyAccountActivity.VERIFICATION_EMAIL_TAG);
        EditText userEmailEditText = findViewById(R.id.loginActivityEditTextTextEmailAddress);
        if (userEmail != null) {
            userEmailEditText.setText(userEmail);
        }
        EditText userPasswordEditText = findViewById(R.id.loginActivityeditTextTextPassword);
        Button loginButton = findViewById(R.id.logInActivityLogInButton);

        loginButton.setOnClickListener(v -> {
            // Amplify User Login code block
            Amplify.Auth.signIn(userEmailEditText.getText().toString(),
                    userPasswordEditText.getText().toString(),
                    success -> {
                        Log.i(TAG, "Login succeeded: " + success.toString());
                        Intent goToTriviaActivity = new Intent(LogInActivity.this, TriviaActivity.class);
                        startActivity(goToTriviaActivity);
                        if(sharedPreferences.getBoolean("tts", false)){
                            ttsUtility.speak("Login successful. Welcome back!");
                        }
                    },
                    failure -> {
                        Log.i(TAG, "Login failed: " + failure.toString());
                        if(sharedPreferences.getBoolean("tts", false)){
                            ttsUtility.speak("Login failed. Please check your User name and password  and try again.");
                        }
                    });
        });
    }

    public void setUpSignUpButton() {
        TextView signUpText = findViewById(R.id.logInActivityDontHaveAnAccountTextView);

        signUpText.setOnClickListener(v -> {
            Intent goToSignUpActivity = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(goToSignUpActivity);
            if(sharedPreferences.getBoolean("tts", false)){
                ttsUtility.speak("Redirecting to sign up page.");
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Don't forget to shutdown tts!
        ttsUtility.shutdown();
        super.onDestroy();
    }
}
