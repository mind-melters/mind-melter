package com.mca.mindmelter.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.datastore.generated.model.User;
import com.mca.mindmelter.Repositories.OpenAiChatRepository;
import com.mca.mindmelter.repositories.UserRepository;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";
    private final OpenAiChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<ChatMessage>> chatMessagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private MutableLiveData<User> user = new MutableLiveData<>();

    public ChatViewModel(Application application) {
        super(application);
        chatRepository = new OpenAiChatRepository(application);
        this.userRepository = new UserRepository(application);
        loadUser();
    }

    private void loadUser() {
        userRepository.getCurrentUser().observeForever(user -> {
            this.user.postValue(user);
        });
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void loadChatHistory(String chatId) {
        isLoadingLiveData.setValue(true);
        chatRepository.loadChatHistory(chatId, new OpenAiChatRepository.Callback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                chatMessagesLiveData.postValue(result);
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(Throwable throwable) {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void loadChatHistoryByTriviaId(String triviaId) {
        isLoadingLiveData.setValue(true);
        chatRepository.loadChatHistoryByTriviaId(triviaId, new OpenAiChatRepository.Callback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                if (result.isEmpty()) {
                    initiateChat(triviaId);
                } else {
                    chatMessagesLiveData.postValue(result);
                    isLoadingLiveData.postValue(false);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void initiateChat(String triviaId) {
        // initiateChat implementation
    }

    public LiveData<List<ChatMessage>> getChatMessagesLiveData() {
        return chatMessagesLiveData;
    }

    public LiveData<Boolean> isLoadingLiveData() {
        return isLoadingLiveData;
    }
}

