package com.mca.mindmelter.Repositories;

import android.content.Context;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenAiChatRepository {
    private final ExecutorService executorService;
    private final String TOKEN;

    public OpenAiChatRepository(Context context) {
        //Init the executor service
        this.executorService = Executors.newFixedThreadPool(2);

        this.TOKEN = context.getResources().getString(R.string.openai_api_key);
    }

    public void loadChatHistory(String chatId, Callback<List<ChatMessage>> callback) {
        executorService.submit(() -> Amplify.API.query(
                ModelQuery.get(Chat.class, chatId),
                response -> {
                    List<ChatMessage> messages = new ArrayList<>();
                    if (response.hasData()) {
                        Chat chat = response.getData();
                        List<String> messagesJson = chat.getMessages();
                        Gson gson = new Gson();
                        Type type = new TypeToken<ChatMessage>() {}.getType();
                        for (String messageJson : messagesJson) {
                            ChatMessage message = gson.fromJson(messageJson, type);
                            messages.add(message);
                        }

                        callback.onSuccess(messages);
                    } else if (response.hasErrors()) {
                        callback.onError(new Throwable(response.getErrors().get(0).getMessage()));
                    }
                },
                error -> callback.onError(error)
        ));
    }

    public void initiateChat(Trivia trivia, User user, Callback<List<ChatMessage>> callback)  {
        // Create a system message to set the context
        String systemMessageContent = "You are an AI trained to provide detailed explanations and facilitate learning. The current topic is the following trivia fact: '" + trivia.getTrivia() + "'. Please state the trivia fact in quotes and then prompt the user using their first name to ask any questions related to the topic. The user's full name is " + user.getFullName() + ". Please provide a detailed explanations to any subsequent inquiries from the user. If the conversation strays off-topic, kindly steer it back towards the trivia topic at hand. Please prioritize clarity, friendliness, and accuracy in your responses.";
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessageContent);
        messages.add(systemMessage);

        generateChatResponse(messages, newMessage -> {
            if (newMessage != null) {
                messages.add(newMessage);
                saveChatHistory(messages, user, chat -> {
                    callback.onSuccess(Arrays.asList(systemMessage, newMessage));
                });
            }
        });
    }

    public void addMessage(User user, List<ChatMessage> messages, ChatMessage newMessage, Callback<List<ChatMessage>> callback) {
        messages.add(newMessage);

        generateChatResponse(messages, assistantMessage -> {
            if (assistantMessage != null) {
                messages.add(assistantMessage);
                updateChatHistory(user, messages, chat -> {
                    callback.onSuccess(Arrays.asList(newMessage, assistantMessage));
                });
            }
        });
    }

    private void generateChatResponse(List<ChatMessage> messages, Callback<ChatMessage> callback) {
        executorService.submit(() -> {
            String token = TOKEN;
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
                    callback.onError(new Exception("Error: No response from OpenAI"));
                }

                callback.onSuccess(choices.get(0).getMessage());

            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (service != null) {
                    service.shutdownExecutor();
                }
            }
        });
    }

    private void saveChatHistory(List<ChatMessage> messages, User user, Callback<Chat> callback) {
        Gson gson = new Gson();
        Type type = new TypeToken<ChatMessage>() {}.getType();
        List<String> jsonMessages = new ArrayList<>();
        for (ChatMessage message : messages) {
            String jsonMessage = gson.toJson(message, type);
            jsonMessages.add(jsonMessage);
        }

        // Get the current date as java.util.Date
        Date now = new Date();

        // Format the date as a string
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String awsDateTime = dateFormat.format(now);

        Chat chat = Chat.builder()
                .userId(user.getId())
                .createdAt(new Temporal.DateTime(awsDateTime))
                .messages(jsonMessages)
                .build();

        executorService.submit(() -> Amplify.API.mutate(
                ModelMutation.create(chat),
                response -> callback.onSuccess(response.getData()),
                error -> callback.onError(error)
        ));
    }

    private void updateChatHistory(User user, List<ChatMessage> messages, Callback<Chat> callback) {
        Gson gson = new Gson();
        Type type = new TypeToken<ChatMessage>() {}.getType();
        List<String> jsonMessages = new ArrayList<>();
        for (ChatMessage message : messages) {
            String jsonMessage = gson.toJson(message, type);
            jsonMessages.add(jsonMessage);
        }

        // Update the Chat object with the new messages
        Chat chat = Chat.builder()
                .userId(user.getId())
                .createdAt(user.getCreatedAt()) // Preserve the original creation timestamp
                .id(user.getId()) // Preserve the existing ID
                .messages(jsonMessages) // Update the messages
                .build();

        executorService.submit(() -> Amplify.API.mutate(
                ModelMutation.update(chat),
                response -> callback.onSuccess(response.getData()),
                error -> callback.onError(error)
        ));
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable throwable);
    }
}
