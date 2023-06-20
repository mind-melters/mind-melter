package com.mca.mindmelter.Repositories;

import android.content.Context;
import android.util.Log;

import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Trivia;
import com.amplifyframework.api.graphql.GraphQLRequest;
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
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenAiTriviaRepository {
    public static final String TAG = "OpenAI API Trivia Service";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final String TOKEN;

    Trivia mostRecentTrivia;
    AuthUser user;

    public OpenAiTriviaRepository(Context context) {
        this.TOKEN = context.getResources().getString(R.string.openai_api_key);
        // Get the userId
        Amplify.Auth.getCurrentUser(
                user -> {
                    this.user = user; // User is signed in
                },
                error -> {
                    Log.e(TAG, "User not signed in. Stopping the service...");
                }
        );
    }

    /* Our approach for getting trivia is to get the most recent trivia and check that it was created on the same day. If it wasn't, we generate new trivia. If it was, we return it. This way a user will get new trivia if it's a new day, but they can also generate new trivia and have that show up as the most recent trivia throughout the day.

    Our GraphQLRequest only queries the most recent trivia made so that we aren't getting the entire list every time we query the database.
     */
    public void getMostRecentTrivia(Callback<String> callback) {
        executorService.submit(() -> {
            GraphQLRequest<Trivia> request = getMostRecentTriviaRequest();

            Amplify.API.query(
                    request,
                    response -> {
                        Log.i(TAG, "Read most recent Trivia successfully");
                        if(response.getData() != null) {
                            Trivia trivia = response.getData();

                            if (trivia != null) {
                                mostRecentTrivia = trivia;
                            }

                            callback.onSuccess(mostRecentTrivia.getTrivia());
                        } else {
                            // No trivia found, generate it and save it to the database
                            ChatMessage assistantMessage = generateTriviaMessage();

                            if (assistantMessage != null) {
                                saveTriviaToDatabase(assistantMessage, new Callback<String>() {
                                    @Override
                                    public void onSuccess(String trivia) {
                                        callback.onSuccess(trivia);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        callback.onError(e);
                                    }
                                });
                            }
                        }
                    },
                    error -> {
                        Log.e(TAG, "Failed to read most recent Trivia", error);
                        callback.onError(error);
                    }
            );
        });
    }

    // Define the GraphQL request which gets the most recent Trivia
    private GraphQLRequest<Trivia> getMostRecentTriviaRequest() {
        String document = "query getMostRecentTrivia($limit: Int, $userId: ID) {\n" +
                "  listTrivias(filter: {userId: {eq: $userId}}, $limit, sortDirection: DESC, sortKey: \"createdAt\") {\n" +
                "    items {\n" +
                "      id\n" +
                "      trivia\n" +
                "      createdAt\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // Variables for the GraphQL request
        Map<String, Object> variables = new HashMap<>();
        variables.put("limit", 1);
        variables.put("userId", this.user.getUserId());

        return new SimpleGraphQLRequest<>(
                document,
                variables,
                Trivia.class,
                new GsonVariablesSerializer()
        );
    }

    private boolean wasGeneratedToday(Trivia trivia) {
        Date createdAtDate = trivia.getCreatedAt().toDate(); // Convert Temporal.DateTime to Date

        LocalDate createdDate = createdAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();

        return createdDate.equals(currentDate);
    }

    public void generateNewTrivia(Callback<String> callback) {
        executorService.submit(() -> {
            ChatMessage assistantMessage = generateTriviaMessage();

            if (assistantMessage != null) {
                saveTriviaToDatabase(assistantMessage, callback);
            } else {
                callback.onError(new Exception("Failed to generate trivia message."));
            }
        });
    }

    public ChatMessage generateTriviaMessage() {
        ChatMessage response = null;
        OpenAiService service = null;

        try {
            // Set duration to 20 seconds to avoid a socket exception for long response times
            service = new OpenAiService(TOKEN, Duration.ofSeconds(20));

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

            response = choices.get(0).getMessage();
        } catch (Exception e) {
            Log.e(TAG, "Error generating OpenAI chat message", e);
        } finally {
            if (service != null) {
                service.shutdownExecutor();
            }
        }

        return response;
    }

    private void saveTriviaToDatabase(ChatMessage assistantMessage, Callback<String> callback) {
        // Get the current date as java.util.Date
        Date now = new Date();

        // Format the date as a string
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String awsDateTime = dateFormat.format(now);

        // Build Trivia object and save to database
        this.mostRecentTrivia = Trivia.builder()
                .userId(user.getUserId())
                .trivia(assistantMessage.getContent())
                .createdAt(new Temporal.DateTime(awsDateTime))
                .build();

        Amplify.API.mutate(
                ModelMutation.create(this.mostRecentTrivia),
                response -> {
                    Log.i(TAG, "Saved item: " + response.getData().getId());
                    callback.onSuccess(mostRecentTrivia.getTrivia());
                },
                error -> {
                    Log.e(TAG, "Could not save item to API/dynamoDB", error);
                    callback.onError(error);
                }
        );
    }


    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
