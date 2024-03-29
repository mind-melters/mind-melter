package com.mca.mindmelter.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Chat;
import com.amplifyframework.datastore.generated.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    public static final String TAG = "UserRepository";
    private static UserRepository INSTANCE;
    private final MutableLiveData<User> currentUser;
    private final ExecutorService executorService;


    ArrayList<String> chatTitles = new ArrayList<>();

    private UserRepository(Context context) {
        this.currentUser = new MutableLiveData<>();
        //Init the executor service
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserRepository(context);
        }
        return INSTANCE;
    }

    public List<Chat> getChats() {
        List<Chat> userChats = currentUser.getValue().getChats();

        Collections.sort(userChats, new Comparator<Chat>() {
            public int compare(Chat chat1, Chat chat2) {
                return chat2.getCreatedAt().compareTo(chat1.getCreatedAt());
            }
        });

        return userChats;
    }

    public void loadUser() {
        executorService.submit(() -> {
            Amplify.Auth.fetchUserAttributes(
                    attributes -> {
                        for (AuthUserAttribute attribute : attributes) {
                            if (attribute.getKey().getKeyString().equals("email")) {
                                String email = attribute.getValue();
                                Amplify.API.query(
                                        ModelQuery.list(User.class, User.EMAIL.eq(email)),
                                        response -> {
                                            if (response.hasData()) {
                                                if (response.getData().getItems().iterator().hasNext()) {
                                                    User user = response.getData().getItems().iterator().next();
                                                    currentUser.postValue(user);
                                                } else {
                                                    Log.e(TAG, "User not found");
                                                }
                                            } else if (response.hasErrors()) {
                                                Log.e(TAG, "Error fetching user : " + response.getErrors().get(0).getMessage());
                                            }
                                        },
                                        error -> {
                                            Log.e(TAG, "Failed to fetch user by email", error);
                                        }
                                );
                            }
                        }
                    },
                    error -> Log.e(TAG, "Failed to fetch user attributes.", error)
            );
        });
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

