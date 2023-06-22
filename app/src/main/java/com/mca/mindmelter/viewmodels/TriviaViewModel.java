package com.mca.mindmelter.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.datastore.generated.model.Trivia;
import com.amplifyframework.datastore.generated.model.User;
import com.mca.mindmelter.repositories.OpenAiTriviaRepository;
import com.mca.mindmelter.repositories.UserRepository;


public class TriviaViewModel extends AndroidViewModel {
    private static final String TAG = "TriviaViewModel";
    private final OpenAiTriviaRepository openAiTriviaRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<Trivia> triviaLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private LiveData<User> currentUser;

    public TriviaViewModel(Application application) {
        super(application);
        this.openAiTriviaRepository = new OpenAiTriviaRepository(application);
        this.userRepository = new UserRepository(application);
        this.currentUser = userRepository.getCurrentUser();
        isLoadingLiveData.postValue(true);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void loadMostRecentTrivia() {
        isLoadingLiveData.postValue(true);
        openAiTriviaRepository.getMostRecentTrivia(currentUser.getValue().getId(), new OpenAiTriviaRepository.Callback<Trivia>() {
            @Override
            public void onSuccess(Trivia result) {
                triviaLiveData.postValue(result);
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(Throwable throwable) {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public LiveData<Trivia> getTriviaLiveData() {
        return triviaLiveData;
    }

    public LiveData<Boolean> isLoadingLiveData() {
        return isLoadingLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        openAiTriviaRepository.shutdownExecutorService();
    }
}
