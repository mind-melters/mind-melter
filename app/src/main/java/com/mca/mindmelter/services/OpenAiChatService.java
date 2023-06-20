package com.mca.mindmelter.services;

import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Chat;
import com.amplifyframework.datastore.generated.model.Trivia;
import com.amplifyframework.datastore.generated.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mca.mindmelter.R;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class OpenAiChatService extends Service{
    public static final String TAG = "OpenAI API Chat Service";
    private Chat chat;
    // We use LiveData to efficiently render new messages.
    private MutableLiveData<List<ChatMessage>> messagesLiveData;
    private List<ChatMessage> messages;
    // This class needs the User object and not just AuthUser, as we're using the fullname field in our prompt to ChatGPT. We have to query the database for this information, so we use LiveData and observe it in the Chat activity. Once the 'User' object is available, the observer is notified.
    private MutableLiveData<User> userLiveData;
    private User user;

    @Override
    public void onCreate() {
        super.onCreate();
        messages = new ArrayList<>();
        messagesLiveData = new MutableLiveData<>();
        userLiveData = new MutableLiveData<>();

        // Check that user is logged in and, if so, get the User object from the database.
        Amplify.Auth.getCurrentUser(
                authUser -> {
                    Amplify.API.query(
                            ModelQuery.get(User.class, authUser.getUserId()),
                            response -> {
                                if (response.hasData()) {
                                    this.user = response.getData();
                                    userLiveData.postValue(this.user);
                                }
                            },
                            error -> Log.e(TAG, "Could not retrieve User data", error)
                    );
                },
                error -> {
                    Log.e(TAG, "User not signed in. Stopping the service...");
                    stopSelf();  // Stops the service
                }
        );
    }

    public LiveData<List<ChatMessage>> loadChatHistory(String chatId) {
        Amplify.API.query(
                ModelQuery.get(Chat.class, chatId),
                response -> {
                    if (response.hasData()) {
                        this.chat = response.getData();
                        List<String> messagesJson = this.chat.getMessages();
                        Gson gson = new Gson();
                        Type type = new TypeToken<ChatMessage>() {}.getType();
                        for (String messageJson : messagesJson) {
                            ChatMessage message = gson.fromJson(messageJson, type);
                            this.messages.add(message);
                        }

                        messagesLiveData.postValue(new ArrayList<>(this.messages)); // Emit the full list
                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Could not load chat", new Throwable(response.getErrors().get(0).getMessage()));
                    }
                },
                error -> Log.e(TAG, "Could not load chat", error)
        );

        return messagesLiveData;
    }

    public LiveData<List<ChatMessage>> initiateChat(Trivia trivia) {
        // Create a system message to set the context
        String systemMessageContent = "You are an AI trained to provide detailed explanations and facilitate learning. The current topic is the following trivia fact: '" + trivia.getTrivia() + "'. Please state the trivia fact in quotes and then prompt the user using their first name to ask any questions related to the topic. The user's full name is " + user.getFullName() + ". Please provide a detailed explanations to any subsequent inquiries from the user. If the conversation strays off-topic, kindly steer it back towards the trivia topic at hand. Please prioritize clarity, friendliness, and accuracy in your responses.";
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessageContent);
        messages.add(systemMessage);

        ChatMessage assistantMessage = generateChatResponse();

        if (assistantMessage != null) {
            this.messages.add(assistantMessage);
            saveChatHistory();
            messagesLiveData.setValue(new ArrayList<>(this.messages)); // Emit the full list
        }

        return messagesLiveData;
    }

    public void addMessage(ChatMessage newMessage) {
        this.messages.add(newMessage);

        ChatMessage assistantMessage = generateChatResponse();
        if (assistantMessage != null) {
            this.messages.add(assistantMessage);
            updateChatHistory();

            messagesLiveData.setValue(Arrays.asList(newMessage, assistantMessage)); // Emit only the new messages
        }
    }

    private ChatMessage generateChatResponse() {
        ChatMessage response = null;

        String token = getResources().getString(R.string.openai_api_key);
        OpenAiService service = null;

        try {
            // Set duration to 20 seconds to avoid a socket exception for long response times
            service = new OpenAiService(token, Duration.ofSeconds(20));

            // Send the API request
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .n(1)
                    .temperature(0.8)
                    .maxTokens(1000)
                    .logitBias(new HashMap<>())
                    .build();

            // Extract the message content of the response
            List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();

            if (choices.isEmpty()) {
                Log.e(TAG, "Error: No response from OpenAI");
            }

            String content = choices.get(0).getMessage().getContent();

            response = new ChatMessage(ChatMessageRole.ASSISTANT.value(), content);
        } catch (Exception e) {
            Log.e(TAG, "Error generating OpenAI chat message", e);
        } finally {
            if (service != null) {
                service.shutdownExecutor();
            }
        }

        return response;
    }

    private void saveChatHistory() {
        Gson gson = new Gson();
        Type type = new TypeToken<ChatMessage>() {}.getType();
        List<String> jsonMessages = new ArrayList<>();
        for (ChatMessage message : this.messages) {
            String jsonMessage = gson.toJson(message, type);
            jsonMessages.add(jsonMessage);
        }

        // Get the current date as java.util.Date
        Date now = new Date();

        // Format the date as a string
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String awsDateTime = dateFormat.format(now);

        this.chat = Chat.builder()
                .userId(this.user.getId())
                .createdAt(new Temporal.DateTime(awsDateTime))
                .messages(jsonMessages)
                .build();

        Amplify.API.mutate(
                ModelMutation.create(this.chat),
                response -> Log.i(TAG, "Saved chat: " + response.getData().getId()),
                error -> Log.e(TAG, "Could not save chat to API/dynamoDB", error)
        );
    }

    private void updateChatHistory() {
        Gson gson = new Gson();
        Type type = new TypeToken<ChatMessage>() {}.getType();
        List<String> jsonMessages = new ArrayList<>();
        for (ChatMessage message : this.messages) {
            String jsonMessage = gson.toJson(message, type);
            jsonMessages.add(jsonMessage);
        }

        // Update the Chat object with the new messages
        this.chat = Chat.builder()
                .userId(this.chat.getUserId()) // Preserve the existing user ID
                .createdAt(this.chat.getCreatedAt()) // Preserve the original creation timestamp
                .id(this.chat.getId()) // Preserve the existing ID
                .messages(jsonMessages) // Update the messages
                .build();

        // Update the chat in the database
        Amplify.API.mutate(
                ModelMutation.update(this.chat),
                response -> Log.i(TAG, "Updated chat: " + response.getData().getId()),
                error -> Log.e(TAG, "Could not update chat", error)
        );
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messagesLiveData;
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
