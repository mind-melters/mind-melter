package com.mca.mindmelter.activities.authentification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.mca.mindmelter.MainActivity;
import com.mca.mindmelter.R;

public class LogInActivity extends AppCompatActivity {
    public static final String TAG = "LogInAcivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        setUpLoginButton();

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
                        Intent goToMainActivity = new Intent(LogInActivity.this, MainActivity.class);
                        startActivity(goToMainActivity);
                    },
                    failure -> {
                        Log.i(TAG, "Login failed: " + failure.toString());
                    });
        });
    }
