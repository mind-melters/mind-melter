package com.mca.mindmelter.repositories;

import android.content.Context;
import android.util.Log;

import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Chat;
import com.amplifyframework.datastore.generated.model.Trivia;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.datastore.generated.model.User;
import com.mca.mindmelter.R;

import com.mca.mindmelter.exceptions.OpenAiException;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenAiTriviaRepository {
    public static final String TAG = "OpenAiTriviaRepository";
    private final ExecutorService executorService;
    private final String TOKEN;

    public OpenAiTriviaRepository(Context context) {
        //Init the executor service
        this.executorService = Executors.newSingleThreadExecutor();

        this.TOKEN = context.getResources().getString(R.string.openai_api_key);
    }

    public void generateNewTrivia(String userId, String categoryName, Callback<Trivia> triviaCallback, Callback<String> titleCallback) {
        executorService.submit(() -> {
            generateTriviaMessage(categoryName, new Callback<ChatMessage>() {
                @Override
                public void onSuccess(ChatMessage assistantMessage) {
                    Log.i(TAG, "New trivia generated successfully");
                    saveTriviaToDatabase(userId, assistantMessage, triviaCallback);

                    generateChatTitle(assistantMessage.getContent(), new Callback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            titleCallback.onSuccess(result);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            titleCallback.onSuccess("Did You Know?");
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e(TAG, "Error generating trivia", throwable);
                    titleCallback.onSuccess("Did You Know?");
                }
            });
        });
    }


    public void generateTriviaMessage(String categoryName, Callback<ChatMessage> callback) {
        executorService.submit(() -> {
            String token = TOKEN;
            OpenAiService service = null;

            try {
                // Set duration to 20 seconds to avoid a socket exception for long response times
                service = new OpenAiService(token, Duration.ofSeconds(20));

                List<ChatMessage> messages = new ArrayList<>();

                // Construct the system message
                String SEED_MESSAGE_SYSTEM = "Provide an interesting trivia fact related to the category \"" + categoryName + "\".";
                ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), SEED_MESSAGE_SYSTEM);
                messages.add(systemMessage);

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
                    String errorMessage = "Error: No response from OpenAI";
                    Log.e(TAG, errorMessage);
                    callback.onError(new OpenAiException(errorMessage));
                }

                callback.onSuccess(choices.get(0).getMessage());

            } catch (Exception e) {
                Log.e(TAG, "Error generating chat response", e);
            } finally {
                if (service != null) {
                    service.shutdownExecutor();
                }
            }
        });
    }

    public void generateChatTitle(String trivia, Callback<String> callback) {
        executorService.submit(() -> {
            String token = TOKEN;
            OpenAiService service = null;

            try {
                // Set duration to 20 seconds to avoid a socket exception for long response times
                service = new OpenAiService(token, Duration.ofSeconds(20));

                // Create a system message to instruct the model
                String systemMessageContent = "You are an AI designed to generate concise, captivating titles for trivia topics. Here's your current topic: '" + trivia + "' Your task is to create an engaging and succinct title, suitable for a chat and is display-friendly on mobile screens. It should be 4 words or less. Remember, the title should attract the audience and give them a glimpse of the fascinating topic they're about to learn about.";
                List<ChatMessage> messages = new ArrayList<>();
                ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessageContent);
                messages.add(systemMessage);

                // Send the API request
                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                        .builder()
                        .model("gpt-3.5-turbo")
                        .messages(messages)
                        .n(1)
                        .temperature(0.8)
                        .maxTokens(100)
                        .logitBias(new HashMap<>())
                        .build();

                // Extract the message content of the response
                List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();

                if (choices.isEmpty()) {
                    String errorMessage = "Error: No response from OpenAI";
                    Log.e(TAG, errorMessage);
                    callback.onError(new OpenAiException(errorMessage));
                }

                String title = choices.get(0).getMessage().getContent();

                // No matter how I modify the prompt, ChatGPT always wraps a title in quotes. This is added to strip the quotes if they exist
                if (title.startsWith("\"") && title.endsWith("\"")) {
                    title = title.substring(1, title.length() - 1);
                }

                callback.onSuccess(title);

            } catch (Exception e) {
                Log.e(TAG, "Error generating chat title", e);
                callback.onError(e);
            } finally {
                if (service != null) {
                    service.shutdownExecutor();
                }
            }
        });
    }

    private void saveTriviaToDatabase(String userId, ChatMessage assistantMessage, Callback<Trivia> callback) {
        // Get the current date as java.util.Date
        Date now = new Date();

        // Format the date as a string
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String awsDateTime = dateFormat.format(now);

        // Build Trivia object and save to database
        Trivia trivia = Trivia.builder()
                .userId(userId)
                .trivia(assistantMessage.getContent())
                .createdAt(new Temporal.DateTime(awsDateTime))
                .build();

        Amplify.API.mutate(
                ModelMutation.create(trivia),
                response -> {
                    if (response.hasData()) {
                        Log.i(TAG, "Saved item: " + response.getData().getId());
                        callback.onSuccess(trivia);
                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Failed to trivia : " + response.getErrors().get(0).getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Error saving trivia", error);
                }
        );
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
