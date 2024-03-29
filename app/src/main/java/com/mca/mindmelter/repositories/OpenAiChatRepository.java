package com.mca.mindmelter.repositories;

import android.content.Context;
import android.util.Log;

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

import com.mca.mindmelter.exceptions.OpenAiException;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenAiChatRepository {
    public static final String TAG = "OpenAiChatRepository";
    private final ExecutorService executorService;
    private final String TOKEN;

    public OpenAiChatRepository(Context context) {
        //Init the executor service
        this.executorService = Executors.newFixedThreadPool(2);

        this.TOKEN = context.getResources().getString(R.string.openai_api_key);
    }

    public void loadChatHistory(String chatId, Callback<Chat> callback) {
        executorService.submit(() -> Amplify.API.query(
                ModelQuery.get(Chat.class, chatId),
                response -> {
                    if (response.hasData()) {
                        Chat chat = response.getData();

                        callback.onSuccess(chat);
                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Failed to load chat history : " + response.getErrors().get(0).getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Failed to load chat history : " + error.getMessage(), error);
                }
        ));
    }

    public void loadChatHistoryByTriviaId(String triviaId, Callback<Chat> callback) {
        executorService.submit(() -> Amplify.API.query(
                ModelQuery.list(Chat.class, Chat.TRIVIA_ID.eq(triviaId)),
                response -> {
                    if (response.hasData()) {
                        Chat chat = null;

                        Iterator<Chat> iterator = response.getData().iterator();
                        if (iterator.hasNext()) {
                            chat = iterator.next();
                        }

                        callback.onSuccess(chat); // Even if chat object is null, we call onSuccess
                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Failed to load chat history : " + response.getErrors().get(0).getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Failed to load chat history : " + error.getMessage(), error);
                }
        ));
    }

    public void initiateChat(User user, String triviaId, String title, Callback<Chat> callback) {
        executorService.submit(() -> Amplify.API.query(
                ModelQuery.get(Trivia.class, triviaId),
                response -> {
                    if (response.hasData()) {
                        Trivia trivia = response.getData();
                        List<ChatMessage> messages = new ArrayList<>();

                        String systemMessageContent = "You are an AI chatbot specialized in providing succinct yet insightful explanations, primarily facilitating learning about a specific piece of trivia. Start by presenting the trivia fact in quotes. Use only the user's FIRST name to personalize the interaction. Their FULL name is " + user.getFullName() + ". Again, only the first name when addressing the user. Encourage the user to ask any questions that are related to the topic. In all of your responses, prioritize conciseness, accuracy, and a positive learning atmosphere. Should the user deviate from the subject, use polite and engaging techniques to redirect the conversation back to the trivia topic. Remember, your main purpose is to keep the discussion focused and educational, yet enjoyable. Here is your trivia fact:\n\n\"" +  trivia.getTrivia() + "\"\n\nNow, prompt the user to ask any question related to this topic. Be ready to deliver concise, accurate, and enthusiastic answers. If the discussion veers off-topic, gently guide it back to the main subject.";

                        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessageContent);
                        messages.add(systemMessage);

                        generateChatResponse(messages, new Callback<ChatMessage>() {
                            @Override
                            public void onSuccess(ChatMessage assistantMessage) {
                                if (assistantMessage != null) {
                                    messages.add(assistantMessage);
                                    saveChatHistory(user, trivia.getId(), title, messages, new Callback<Chat>() {
                                        @Override
                                        public void onSuccess(Chat chat) {
                                            callback.onSuccess(chat);
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            Log.e(TAG, "Failed to save chat history.", throwable);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Log.e(TAG, "Failed to generate chat response.", throwable);
                            }
                        });

                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Failed to get trivia : " + response.getErrors().get(0).getMessage());
                    }
                },
                error -> Log.e(TAG, "Failed to get trivia : " + error.getMessage(), error)
        ));
    }

    public void continueChat(Chat chat, List<ChatMessage> messages, Callback<Chat> callback) {
        generateChatResponse(messages, new Callback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage assistantMessage) {
                if (assistantMessage != null) {
                    messages.add(assistantMessage);
                    updateChatHistory(chat, messages, new Callback<Chat>() {
                        @Override
                        public void onSuccess(Chat updatedChat) {
                            callback.onSuccess(updatedChat);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e(TAG, "Failed to update chat history.", throwable);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Failed to generate chat response.", throwable);
            }
        });
    }


    public void generateChatResponse(List<ChatMessage> messages, Callback<ChatMessage> callback) {
        executorService.submit(() -> {
            String token = TOKEN;
            OpenAiService service = null;

            try {
                // Set duration to 60 seconds to avoid a socket exception for long response times
                service = new OpenAiService(token, Duration.ofSeconds(60));

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

    private void saveChatHistory(User user, String triviaId, String title, List<ChatMessage> messages, Callback<Chat> callback) {
        Gson gson = new Gson();
        Type type = new TypeToken<ChatMessage>() {}.getType();
        List<String> jsonMessages = new ArrayList<>();
        for (ChatMessage message : messages) {
            String jsonMessage = gson.toJson(message, type);
            jsonMessages.add(jsonMessage);
        }

        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String awsDateTime = dateFormat.format(now);

        Chat chat = Chat.builder()
                .userId(user.getId())
                .triviaId(triviaId)
                .title(title)
                .createdAt(new Temporal.DateTime(awsDateTime))
                .messages(jsonMessages)
                .build();

        executorService.submit(() -> Amplify.API.mutate(
                ModelMutation.create(chat),
                response -> {
                    if (response.hasData()) {
                        callback.onSuccess(chat);
                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Failed to save chat history : " + response.getErrors().get(0).getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Error saving chat history", error);
                }
        ));
    }

    private void updateChatHistory(Chat chat, List<ChatMessage> messages, Callback<Chat> callback) {
        Gson gson = new Gson();
        Type type = new TypeToken<ChatMessage>() {}.getType();
        List<String> jsonMessages = new ArrayList<>();
        for (ChatMessage message : messages) {
            String jsonMessage = gson.toJson(message, type);
            jsonMessages.add(jsonMessage);
        }

        // Update the Chat object with the new messages
        Chat updatedChat = chat.copyOfBuilder()
                .messages(jsonMessages) // Update the messages
                .build();

        executorService.submit(() -> Amplify.API.mutate(
                ModelMutation.update(updatedChat),
                response -> {
                    if (response.hasData()) {
                        callback.onSuccess(response.getData());
                    } else if (response.hasErrors()) {
                        Log.e(TAG, "Failed to update chat history : " + response.getErrors().get(0).getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Error updating chat history", error);
                }
        ));
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
