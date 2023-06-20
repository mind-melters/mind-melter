package com.mca.mindmelter.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.User;

public class UserRepository {
    public static final String TAG = "UserRepository";
    private final MutableLiveData<User> currentUser;

    public UserRepository(Context context) {
        this.currentUser = new MutableLiveData<>();
        loadUser();
    }

    private void loadUser() {
        Amplify.Auth.getCurrentUser(
                authUser -> {
                    Amplify.API.query(
                            ModelQuery.get(User.class, authUser.getUserId()),
                            response -> {
                                if (response.hasData()) {
                                    currentUser.postValue(response.getData());
                                }
                            },
                            error -> Log.e(TAG, "Could not retrieve User data", error)
                    );
                },
                error -> Log.e(TAG, "Auth session failed.", error)
        );
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
}

