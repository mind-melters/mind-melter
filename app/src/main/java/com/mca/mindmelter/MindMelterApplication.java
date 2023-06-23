package com.mca.mindmelter;

import android.app.Application;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.mca.mindmelter.repositories.UserRepository;

public class MindMelterApplication extends Application {
    public static final String TAG = "mindmelterapplication";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i(TAG, "Initialized Amplify");

            UserRepository.getInstance(getApplicationContext());
            Log.i(TAG, "Initialized UserRepository");
        } catch (AmplifyException ae) {
            Log.e(TAG, "Error initializing Amplify: " + ae.getMessage(), ae);
        }
    }
}

