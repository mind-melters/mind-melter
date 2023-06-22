package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.mca.mindmelter.R;
import com.mca.mindmelter.adapters.ChatAdapter;
import com.mca.mindmelter.utilities.TextToSpeechUtility;
import com.mca.mindmelter.viewmodels.ChatViewModel;

public class ChatActivity extends AppCompatActivity {
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;
    private TextToSpeechUtility textToSpeechUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        EditText inputText = findViewById(R.id.message_input);
        ImageButton speakMessageButton = findViewById(R.id.speak_message_button);
        ProgressBar loadingIndicator = findViewById(R.id.loading_indicator);
        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Initialize TextToSpeechUtility
        textToSpeechUtility = new TextToSpeechUtility(this);

        // Initialize RecyclerView and its adapter
        chatAdapter = new ChatAdapter();
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Observe LiveData from ViewModel
        viewModel.getChatMessagesLiveData().observe(this, chatMessages -> {
            chatAdapter.submitList(chatMessages);
            recyclerView.smoothScrollToPosition(chatMessages.size() - 1);

            // Get the state of the TTS switch
            SharedPreferences sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
            boolean ttsMode = sharedPreferences.getBoolean("tts", true); //TTS is on by default

            // If the TTS mode is enabled, and the last message is not from the user, use the TTS utility to read out the message
            if (ttsMode && !chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).isFromBot()) {
                textToSpeechUtility.speak(chatMessages.get(chatMessages.size() - 1).getText());
            }
        });

        // Disable the input while data is loading and show progress bar
        viewModel.isLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                loadingIndicator.setVisibility(View.VISIBLE);
                inputText.setEnabled(false);
                speakMessageButton.setEnabled(false);
            } else {
                loadingIndicator.setVisibility(View.GONE);
                inputText.setEnabled(true);
                speakMessageButton.setEnabled(true);
            }
        });

        // Handle the intent extras
        String triviaId = getIntent().getStringExtra("triviaId");
        String chatId = getIntent().getStringExtra("chatId");

        // Observe User object and load chats when User is ready
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                if (triviaId != null) {
                    viewModel.loadChatHistoryByTriviaId(triviaId);
                } else if (chatId != null) {
                    viewModel.loadChatHistory(chatId);
                }
            }
        });

        speakMessageButton.setOnClickListener(v -> {
            // TODO: Implement speech-to-text
        });

        // Handle message sending
        inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String userMessage = inputText.getText().toString();
                if (!userMessage.isEmpty()) {
                    viewModel.sendMessage(userMessage);

                    // Reset input text
                    inputText.setText("");

                    // Hide the keyboard after sending a message
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // Handle scrolling to the bottom of the recycler view when the keyboard pops up
        View rootView = findViewById(android.R.id.content);
        rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                recyclerView.post(() -> {
                    int lastPosition = recyclerView.getAdapter().getItemCount() - 1;
                    recyclerView.scrollToPosition(lastPosition);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure to shutdown TextToSpeech
        textToSpeechUtility.shutdown();
    }
}
