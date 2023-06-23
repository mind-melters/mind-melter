package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mca.mindmelter.R;
import com.mca.mindmelter.adapters.ChatAdapter;
import com.mca.mindmelter.viewmodels.ChatViewModel;

public class ChatActivity extends AppCompatActivity {
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;
    private EditText inputText;
    private ImageButton speakMessageButton;
    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;
    private ProgressBar initialLoadingIndicator;
    private LinearLayout retryLayout;
    private Button retryButton;
    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        inputText = findViewById(R.id.message_input);
        speakMessageButton = findViewById(R.id.speak_message_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        recyclerView = findViewById(R.id.chat_recycler_view);
        initialLoadingIndicator = findViewById(R.id.initial_loading_indicator);
        retryLayout = findViewById(R.id.retry_layout);
        retryButton = findViewById(R.id.retry_button);
        errorMessage = findViewById(R.id.error_message);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Initialize RecyclerView and its adapter
        chatAdapter = new ChatAdapter();
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Observe LiveData from ViewModel
        viewModel.getChatMessagesLiveData().observe(this, chatMessages -> {
            chatAdapter.submitList(chatMessages);
            recyclerView.scrollToPosition(chatMessages.size() - 1);
        });

        // Disable the input while data is initial loading and initial show progress bar
        viewModel.isInitialLoadingLiveData().observe(this, isInitialLoading -> {
            if (isInitialLoading) {
                initialLoadingIndicator.setVisibility(View.VISIBLE);
                inputText.setEnabled(false);
                speakMessageButton.setEnabled(false);
            } else {
                initialLoadingIndicator.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                inputText.setEnabled(true);
                speakMessageButton.setEnabled(true);
            }
        });

        // Disable the input while if there was an error and the retry button is visible
        viewModel.isRetryVisibleLiveData().observe(this, isRetryVisible -> {
            if (isRetryVisible) {
                retryLayout.setVisibility(View.VISIBLE);
                inputText.setEnabled(false);
                speakMessageButton.setEnabled(false);
            } else {
                retryLayout.setVisibility(View.GONE);
                inputText.setEnabled(true);
                speakMessageButton.setEnabled(true);
            }
        });

        // Disable the input while while response is loading and show message progress bar
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

        retryButton.setOnClickListener(v -> {
            viewModel.retry();
        });

        // Handle the intent extras
        String triviaId = getIntent().getStringExtra("triviaId");
        String title = getIntent().getStringExtra("title");
        String chatId = getIntent().getStringExtra("chatId");

        // Observe User object and load chats when User is ready
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                if (triviaId != null) {
                    viewModel.loadChatHistoryByTriviaId(triviaId, title);
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
}
