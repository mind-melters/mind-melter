package com.mca.mindmelter.activities.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
import com.mca.mindmelter.R;
import com.mca.mindmelter.utilities.TextToSpeechUtility;

public class SignUpActivity extends AppCompatActivity {
    public static final String TAG = "SignupActivity";
    public static final String SIGN_UP_EMAIL_TAG = "Signup_Email_Tag";
    public static final String SIGN_UP_FULL_NAME_TAG = "Signup_Full_Name_Tag";

    // Creating an instance of the TextToSpeechUtility class
    private TextToSpeechUtility ttsUtility;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize the TextToSpeechUtility object
        ttsUtility = new TextToSpeechUtility(this);
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);

        setUpSignUpButton();
        setUpLogInButton();
    }

    public void setUpSignUpButton() {
        Button signUpButton = findViewById(R.id.SignUpActivitySignUpButton);

        signUpButton.setOnClickListener(v -> {
            String userFullName = ((EditText) findViewById(R.id.SignUpActivityEditTextFullName)).getText().toString();
            String userEmail = ((EditText) findViewById(R.id.SignUpActivityEditTextTextEmailAddress)).getText().toString();
            String userPassword = ((EditText) findViewById(R.id.SignUpActivityeditTextTextPassword)).getText().toString();

            // Amplify User Sign Up code block
            Amplify.Auth.signUp(userEmail, // user email address as username in Cognito calls
                    userPassword, // Cognito's default password policy is 8 characters, no other requirements
                    AuthSignUpOptions.builder()
                            .userAttribute(AuthUserAttributeKey.email(), userEmail)
                            .userAttribute(AuthUserAttributeKey.name(), userFullName)
                            .build(),
                    good -> {
                        Log.i(TAG, "Signup succeeded: " + good.toString());

                        // move to the verify account activity and pass the email as an intent extra
                        Intent goToVerificationIntent = new Intent(SignUpActivity.this, VerifyAccountActivity.class);
                        goToVerificationIntent.putExtra(SIGN_UP_EMAIL_TAG, userEmail);
                        goToVerificationIntent.putExtra(SIGN_UP_FULL_NAME_TAG, userFullName);
                        startActivity(goToVerificationIntent);

                        if(sharedPreferences.getBoolean("tts", false)){
                            ttsUtility.speak("Signup successful. Verification required.");
                        }
                    },
                    bad -> {
                        Log.i(TAG, "Signup failed with username: " + userEmail + "with this message: " + bad.toString());
                        if(sharedPreferences.getBoolean("tts", false)){
                            ttsUtility.speak("Signup failed. Please try again.");
                        }
                    }
            );
        });
    }

    public void setUpLogInButton() {
        TextView logInText = findViewById(R.id.SignUpActivityAlreadyHaveAnAccountTextView);
        logInText.setOnClickListener(v -> {
            Intent goToLogInIntent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(goToLogInIntent);
            if(sharedPreferences.getBoolean("tts", false)){
                ttsUtility.speak("Redirecting to login page.");
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
