package com.mca.mindmelter.activities.all;

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
    public static final String TAG = "LogInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

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
                        Intent goToMainActivity = new Intent(LogInActivity.this, HomePageActivity.class);
                        startActivity(goToMainActivity);
                    },
                    failure -> {
                        Log.i(TAG, "Login failed: " + failure.toString());
                    });
        });
    }

    public void setUpSignUpButton() {
        Button signUpButton = findViewById(R.id.loginActivitySignUpButton);

        signUpButton.setOnClickListener(v -> {
            Intent goToSignUpActivity = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(goToSignUpActivity);
        });
    }
}
