package com.mca.mindmelter.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.datastore.generated.model.Chat;
import com.amplifyframework.datastore.generated.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mca.mindmelter.repositories.OpenAiChatRepository;
import com.mca.mindmelter.repositories.UserRepository;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";
    private final OpenAiChatRepository openAiChatRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<ChatMessage>> chatMessagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private LiveData<User> currentUser;
    private Chat currentChat;
    private String triviaId;

    public ChatViewModel(Application application) {
        super(application);
        this.openAiChatRepository = new OpenAiChatRepository(application);
        this.userRepository = new UserRepository(application);
        this.currentUser = userRepository.getCurrentUser();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void loadChatHistory(String chatId) {
        isLoadingLiveData.postValue(true);
        openAiChatRepository.loadChatHistory(chatId, new OpenAiChatRepository.Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                currentChat = result;
                chatMessagesLiveData.postValue(getMessages(currentChat));
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
        isLoadingLiveData.postValue(true);
        openAiChatRepository.loadChatHistoryByTriviaId(triviaId, new OpenAiChatRepository.Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                if (result == null) {
                    initiateChat();
                } else {
                    currentChat = result;
                    chatMessagesLiveData.postValue(getMessages(currentChat));
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
        isLoadingLiveData.postValue(true);
        openAiChatRepository.initiateChat(currentUser.getValue(), triviaId, new OpenAiChatRepository.Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                currentChat = result;
                chatMessagesLiveData.postValue(getMessages(currentChat));
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

        isLoadingLiveData.postValue(true);
        openAiChatRepository.continueChat(currentChat, currentMessages, new OpenAiChatRepository.Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                currentChat = result;
                chatMessagesLiveData.postValue(getMessages(currentChat));
                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(Throwable throwable) {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    private List<ChatMessage> getMessages(Chat chat) {
        List<ChatMessage> messages = new ArrayList<>();
        List<String> messagesJson = chat.getMessages();
        Gson gson = new Gson();
        Type type = new TypeToken<ChatMessage>() {}.getType();
        for (String messageJson : messagesJson) {
            ChatMessage message = gson.fromJson(messageJson, type);
            messages.add(message);
        }

        return messages;
    }

    public LiveData<List<ChatMessage>> getChatMessagesLiveData() {
        return chatMessagesLiveData;
    }

    public LiveData<Boolean> isLoadingLiveData() {
        return isLoadingLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        openAiChatRepository.shutdownExecutorService();
    }
}

