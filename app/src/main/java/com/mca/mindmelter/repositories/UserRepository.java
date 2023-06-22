package com.mca.mindmelter.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    public static final String TAG = "UserRepository";
    private final MutableLiveData<User> currentUser;
    private final ExecutorService executorService;



    public UserRepository(Context context) {
        this.currentUser = new MutableLiveData<>();
        //Init the executor service
        this.executorService = Executors.newSingleThreadExecutor();
        loadUser();
    }

    private void loadUser() {
        Amplify.API.query(
                ModelQuery.get(User.class, "7c19c275-7b4f-4d02-bea0-3783399ec523"),
                response -> {
                    if (response.hasData()) {
                        currentUser.postValue(response.getData());
                    }
                },
                error -> Log.e(TAG, "Could not retrieve User data", error)
        );

//        // TODO: Implement this when Auth is incorporated
//        Amplify.Auth.getCurrentUser(
//                authUser -> {
//                    Amplify.API.query(
//                            ModelQuery.get(User.class, authUser.getUserId()),
//                            response -> {
//                                if (response.hasData()) {
//                                    currentUser.postValue(response.getData());
//                                }
//                            },
//                            error -> Log.e(TAG, "Could not retrieve User data", error)
//                    );
//                },
//                error -> Log.e(TAG, "Auth session failed.", error)
//        );
    }

    public void saveUserToDatabase(User user, Callback<User> callback) {
        executorService.submit(() -> Amplify.API.mutate(
                ModelMutation.create(user),
                response -> {
                    if (response.hasData()) {
                        Log.i(TAG, "User saved to database successfully");
                        callback.onSuccess(user);
                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Failed to save user : " + response.getErrors().get(0).getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Error saving user", error);
                }
        ));
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable throwable);
    }

    public void shutdownExecutorService() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

