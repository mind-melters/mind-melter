package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mca.mindmelter.R;
import com.mca.mindmelter.adapters.ChatAdapter;
import com.mca.mindmelter.viewmodels.ChatViewModel;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        // Initialize RecyclerView and its adapter
        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);
        chatAdapter = new ChatAdapter();
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Observe LiveData from ViewModel
        viewModel.getChatMessages().observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> chatMessages) {
                chatAdapter.submitList(chatMessages); // Update UI with new data
            }
        });

        // Handle message sending
        EditText inputText = findViewById(R.id.input_text);
        Button sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userMessage = inputText.getText().toString();
                if (!userMessage.isEmpty()) {
                    viewModel.sendMessage(userMessage);
                    inputText.setText("");
                }
            }
        });
    }
}