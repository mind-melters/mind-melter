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
    private final MutableLiveData<Trivia> triviaLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> triviaTitleLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private LiveData<User> currentUser;

    public TriviaViewModel(Application application) {
        super(application);
        this.openAiTriviaRepository = new OpenAiTriviaRepository(application);
        this.currentUser = UserRepository.getInstance(application).getCurrentUser();
        isLoadingLiveData.postValue(true);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void generateTrivia(String categoryName) {
        isLoadingLiveData.postValue(true);

        openAiTriviaRepository.generateNewTrivia(currentUser.getValue().getId(), categoryName,
                new OpenAiTriviaRepository.Callback<Trivia>() {
                    @Override
                    public void onSuccess(Trivia result) {
                        triviaLiveData.postValue(result);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        isLoadingLiveData.postValue(false);
                    }
                },
                new OpenAiTriviaRepository.Callback<String>() {
                    @Override
                    public void onSuccess(String title) {
                        triviaTitleLiveData.postValue(title);
                        isLoadingLiveData.postValue(false);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        isLoadingLiveData.postValue(false);
                    }
                }
        );
    }


    public LiveData<Trivia> getTriviaLiveData() {
        return triviaLiveData;
    }

    public LiveData<String> getTriviaTitleLiveData() {
        return triviaTitleLiveData;
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
