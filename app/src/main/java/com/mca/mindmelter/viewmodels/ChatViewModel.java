package com.mca.mindmelter.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mca.mindmelter.Repositories.OpenAiChatRepository;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public class ChatViewModel extends ViewModel {

    private final OpenAiChatRepository chatRepository;
    private final MutableLiveData<List<ChatMessage>> chatMessagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

    public ChatViewModel(Application application) {
        super(application);
        chatRepository = new OpenAiChatRepository(application);
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

