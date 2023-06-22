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
    private final MutableLiveData<Boolean> isInitialLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRetryVisibleLiveData = new MutableLiveData<>();

    private LiveData<User> currentUser;
    private Chat currentChat;
    private String triviaId;

    // This is how we keep track of failed operations so that the retry button will do the right thing.
    private ChatOperation lastFailedOperation;
    public enum ChatOperation {
        LOAD_CHAT_HISTORY,
        LOAD_CHAT_HISTORY_BY_TRIVIA_ID,
        INITIATE_CHAT,
        SEND_MESSAGE
    }

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
        isInitialLoadingLiveData.postValue(true);
        isRetryVisibleLiveData.postValue(false);

        openAiChatRepository.loadChatHistory(chatId, new OpenAiChatRepository.Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                currentChat = result;
                chatMessagesLiveData.postValue(getMessages(currentChat));
                isInitialLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(Throwable throwable) {
                isRetryVisibleLiveData.postValue(true);
                isInitialLoadingLiveData.postValue(false);
                lastFailedOperation = ChatOperation.LOAD_CHAT_HISTORY;
            }
        });
    }

    public void loadChatHistoryByTriviaId(String triviaId) {
        this.triviaId = triviaId;
        isInitialLoadingLiveData.postValue(true);
        isRetryVisibleLiveData.postValue(false);

        openAiChatRepository.loadChatHistoryByTriviaId(triviaId, new OpenAiChatRepository.Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                if (result == null) {
                    initiateChat();
                } else {
                    currentChat = result;
                    chatMessagesLiveData.postValue(getMessages(currentChat));
                    isInitialLoadingLiveData.postValue(false);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // This is now for unexpected errors, not for the case where there are no chats associated with the triviaId
                isRetryVisibleLiveData.postValue(true);
                isInitialLoadingLiveData.postValue(false);
                lastFailedOperation = ChatOperation.LOAD_CHAT_HISTORY_BY_TRIVIA_ID;
            }
        });
    }

    public void initiateChat() {
        isInitialLoadingLiveData.postValue(true);
        isRetryVisibleLiveData.postValue(false);

        openAiChatRepository.initiateChat(currentUser.getValue(), triviaId, new OpenAiChatRepository.Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                currentChat = result;
                chatMessagesLiveData.postValue(getMessages(currentChat));
                isInitialLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(Throwable throwable) {
                isRetryVisibleLiveData.postValue(true);
                isInitialLoadingLiveData.postValue(false);
                lastFailedOperation = ChatOperation.INITIATE_CHAT;
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

        continueChat(currentMessages);
    }

    private void continueChat(List<ChatMessage> currentMessages) {
        isLoadingLiveData.postValue(true);
        isRetryVisibleLiveData.postValue(false);

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
                isRetryVisibleLiveData.postValue(true);
                lastFailedOperation = ChatOperation.SEND_MESSAGE;
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

            // We filter out the system messages so that they don't show up in the chat.
            if (!message.getRole().equals(ChatMessageRole.SYSTEM.value())) {
                messages.add(message);
            }
        }

        return messages;
    }

    public void retry() {
        switch (lastFailedOperation) {
            case LOAD_CHAT_HISTORY:
                loadChatHistory(currentChat.getId());
                break;
            case LOAD_CHAT_HISTORY_BY_TRIVIA_ID:
                loadChatHistoryByTriviaId(triviaId);
                break;
            case INITIATE_CHAT:
                initiateChat();
                break;
            case SEND_MESSAGE:
                continueChat(chatMessagesLiveData.getValue());
                break;
        }
    }

    public LiveData<List<ChatMessage>> getChatMessagesLiveData() {
        return chatMessagesLiveData;
    }

    public LiveData<Boolean> isInitialLoadingLiveData() {
        return isInitialLoadingLiveData;
    }

    public LiveData<Boolean> isLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<Boolean> isRetryVisibleLiveData() {
        return isRetryVisibleLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        openAiChatRepository.shutdownExecutorService();
    }
}

