package com.mca.mindmelter.activities.authentication;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;
import com.mca.mindmelter.R;
import com.mca.mindmelter.repositories.UserRepository;

public class VerifyAccountActivity extends AppCompatActivity {
    public static final String TAG = "VerifyAccountActivity";
    public static final String VERIFICATION_EMAIL_TAG = "VERIFICATION_EMAIL_TAG";

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);
        this.userRepository = new UserRepository(getApplication());

        setUpVerificationButton();
    }

    public void setUpVerificationButton() {
        Button verificationButton = findViewById(R.id.verifyAccountActivityVerificationButton);
        // need to grab email from calling intent
        Intent callingIntent = getIntent();
        String userEmail = callingIntent.getStringExtra(SignUpActivity.SIGN_UP_EMAIL_TAG);
        String userFullName = callingIntent.getStringExtra(SignUpActivity.SIGN_UP_FULL_NAME_TAG);
        EditText emailEditText = ((EditText) findViewById(R.id.verifyAccountActivityUsernameEditText));
        emailEditText.setText(userEmail);

        verificationButton.setOnClickListener(v -> {
            String verificationNumber = ((EditText) findViewById(R.id.verifyAccountActivityVerificationNumberEditText)).getText().toString();
            // Amplify User Confirmation code block
            Amplify.Auth.confirmSignUp(userEmail,
                    verificationNumber, // this is the verification code from th verification email
                    success -> {
                        Log.i(TAG, "Verification succeeded: " + success.toString());

                        // create the user
                        User user = User.builder()
                                .fullName(userFullName)
                                .email(userEmail)
                                .build();

                        userRepository.saveUserToDatabase(user, new UserRepository.Callback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                Log.i(TAG, "User successfully saved to database");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Log.e(TAG, "Failed to save user to database", throwable);
                            }
                        });

                        // make an intent to move to login page and pass the user's email
                        Intent goToLoginActivity = new Intent(VerifyAccountActivity.this, LogInActivity.class);
                        goToLoginActivity.putExtra(VERIFICATION_EMAIL_TAG, userEmail);
                        startActivity(goToLoginActivity);
                        finish();
                    },
                    failure -> {
                        Log.i(TAG, "Verification failed: " + failure.toString());
                    }
            );
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRepository.shutdownExecutorService();
    }

}

