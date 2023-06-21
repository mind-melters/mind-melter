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

    /* Our approach for getting trivia is to get the most recent trivia and check that it was created on the same day. If it wasn't, we generate new trivia. If it was, we return it. This way a user will get new trivia if it's a new day, but they can also generate new trivia and have that show up as the most recent trivia throughout the day.

    Our GraphQLRequest only queries the most recent trivia made so that we aren't getting the entire list every time we query the database.
     */
    public void getMostRecentTrivia(String userId, Callback<Trivia> callback) {
        executorService.submit(() -> {

            Amplify.API.query(
                    ModelQuery.list(Trivia.class, Trivia.USER_ID.eq(userId)),
                    response -> {
                        if (response.hasData()) {
                            Log.i(TAG, "All Trivia read successfully");
                            Trivia mostRecentTrivia = null;
                            for (Trivia trivia : response.getData()) {
                                if (mostRecentTrivia == null || trivia.getCreatedAt().compareTo(mostRecentTrivia.getCreatedAt()) > 0) {
                                    mostRecentTrivia = trivia;
                                }
                            }

                            // Check if the most recent trivia was made today. If so, return it. Otherwise, generate new trivia.
                            if (wasGeneratedToday(mostRecentTrivia)) {
                                callback.onSuccess(mostRecentTrivia);
                            } else {
                                generateNewTrivia(userId, new Callback<Trivia>() {
                                    @Override
                                    public void onSuccess(Trivia trivia) {
                                        callback.onSuccess(trivia);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        // Error handled in generateNewTrivia
                                    }
                                });
                            }
                        } else if (response.hasErrors()) {
                            Log.e(TAG, "Failed to load chat history : " + response.getErrors().get(0).getMessage());
                        } else {
                            // No trivia found, generate it and save it to the database
                            generateNewTrivia(userId, new Callback<Trivia>() {
                                @Override
                                public void onSuccess(Trivia trivia) {
                                    callback.onSuccess(trivia);
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    // Error handled in generateNewTrivia
                                }
                            });
                        }
                    },
                    error -> {
                        Log.e(TAG, "Failed to read most recent Trivia", error);
                        callback.onError(error);
                    }
            );
        });
    }

    private boolean wasGeneratedToday(Trivia trivia) {
        Date createdAtDate = trivia.getCreatedAt().toDate(); // Convert Temporal.DateTime to Date

        LocalDate createdDate = createdAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();

        return createdDate.equals(currentDate);
    }

    public void generateNewTrivia(String userId, Callback<Trivia> callback) {
        executorService.submit(() -> {
            generateTriviaMessage(new Callback<ChatMessage>() {
                @Override
                public void onSuccess(ChatMessage assistantMessage) {
                    Log.i(TAG, "New trivia generated successfully");
                    saveTriviaToDatabase(userId, assistantMessage, callback);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e(TAG, "Error generating trivia", throwable);
                }
            });
        });
    }
    public void generateTriviaMessage(Callback<ChatMessage> callback) {
        executorService.submit(() -> {
            String token = TOKEN;
            OpenAiService service = null;

            try {
                // Set duration to 20 seconds to avoid a socket exception for long response times
                service = new OpenAiService(token, Duration.ofSeconds(20));

                List<ChatMessage> messages = new ArrayList<>();

                // Get the current date
                LocalDate currentDate = LocalDate.now();

                // Format the date without the year
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d");
                String formattedDate = currentDate.format(dateFormatter);

                // Construct the system message
                String SEED_MESSAGE_SYSTEM = "Tell me one interesting trivia fact that happened on " + formattedDate + ". Only provide the trivia message along with a little context.";
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
                    Log.e(TAG, "Error: No response from OpenAI");
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
