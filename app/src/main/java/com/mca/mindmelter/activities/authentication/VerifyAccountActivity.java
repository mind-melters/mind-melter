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

        // Get the UserRepository singleton instance
        userRepository = UserRepository.getInstance(this);

        setUpVerificationButton();
    }

    public void setUpVerificationButton() {
        Button verificationButton = findViewById(R.id.verifyAccountActivityVerificationButton);
        Intent callingIntent = getIntent();
        String userEmail = callingIntent.getStringExtra(SignUpActivity.SIGN_UP_EMAIL_TAG);
        String userFullName = callingIntent.getStringExtra(SignUpActivity.SIGN_UP_FULL_NAME_TAG);
        EditText emailEditText = ((EditText) findViewById(R.id.verifyAccountActivityUsernameEditText));
        emailEditText.setText(userEmail);

        verificationButton.setOnClickListener(v -> {
            String verificationNumber = ((EditText) findViewById(R.id.verifyAccountActivityVerificationNumberEditText)).getText().toString();

            Amplify.Auth.confirmSignUp(userEmail,
                    verificationNumber,
                    success -> {
                        Log.i(TAG, "Verification succeeded: " + success.toString());

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

                        Intent goToLoginActivity = new Intent(VerifyAccountActivity.this, LogInActivity.class);
                        goToLoginActivity.putExtra(VERIFICATION_EMAIL_TAG, userEmail);
                        startActivity(goToLoginActivity);
                        finish();
                    },
                    failure -> Log.i(TAG, "Verification failed: " + failure.toString())
            );
        });
    }
}
