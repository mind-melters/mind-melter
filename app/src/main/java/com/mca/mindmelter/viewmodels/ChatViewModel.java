package com.mca.mindmelter.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.datastore.generated.model.User;
import com.mca.mindmelter.repositories.OpenAiChatRepository;
import com.mca.mindmelter.repositories.UserRepository;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";
    private final OpenAiChatRepository openAiChatRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<ChatMessage>> chatMessagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private MutableLiveData<User> user = new MutableLiveData<>();
    private String triviaId;

    public ChatViewModel(Application application) {
        super(application);
        this.openAiChatRepository = new OpenAiChatRepository(application);
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
        openAiChatRepository.loadChatHistory(chatId, new OpenAiChatRepository.Callback<List<ChatMessage>>() {
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
        this.triviaId = triviaId;
        isLoadingLiveData.setValue(true);
        openAiChatRepository.loadChatHistoryByTriviaId(triviaId, new OpenAiChatRepository.Callback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                if (result.isEmpty()) {
                    initiateChat();
                } else {
                    chatMessagesLiveData.postValue(result);
                    isLoadingLiveData.postValue(false);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // This is now for unexpected errors, not for the case where there are no chats associated with the triviaId
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void initiateChat() {
        isLoadingLiveData.setValue(true);
        openAiChatRepository.initiateChat(user.getValue(), triviaId, new OpenAiChatRepository.Callback<List<ChatMessage>>() {
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

    public void sendMessage(String messageContent) {
        // Create a new user message
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), messageContent);

        // Add the new message to the current list
        List<ChatMessage> currentMessages = chatMessagesLiveData.getValue();
        currentMessages.add(userMessage);

        // Update the LiveData with the new list
        chatMessagesLiveData.postValue(currentMessages);

        isLoadingLiveData.setValue(true);
        openAiChatRepository.continueChat(user.getValue(), triviaId, currentMessages, new OpenAiChatRepository.Callback<List<ChatMessage>>() {
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

    public LiveData<List<ChatMessage>> getChatMessagesLiveData() {
        return chatMessagesLiveData;
    }

    public LiveData<Boolean> isLoadingLiveData() {
        return isLoadingLiveData;
    }
}

