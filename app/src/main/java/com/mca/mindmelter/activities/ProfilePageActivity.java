package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import android.util.Log;

import android.view.View;

import com.mca.mindmelter.R;
import com.mca.mindmelter.adapters.ChatListRecyclerViewAdapter;

import com.mca.mindmelter.utilities.TextToSpeechUtility;

import com.mca.mindmelter.repositories.UserRepository;
import com.mca.mindmelter.viewmodels.ChatViewModel;


import java.util.ArrayList;

public class ProfilePageActivity extends AppCompatActivity {
    public static final String CHAT_TITLE_EXTRA_TAG = "chatTitle";

    private final String TAG = "ProfilePageActivity";
    public static final String DATABASE_NAME = "chat_title_database";
    List<Chat> chatTitles;
    ChatListRecyclerViewAdapter adapter;
    SharedPreferences preferences;

    // Creating an instance of the TextToSpeechUtility class
    private TextToSpeechUtility ttsUtility;

    public UserRepository userRepository = new UserRepository(this);
    private ChatViewModel viewModel;
    private ArrayList<String> chatTitles; // Updated variable type


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);


        // Initialize the TextToSpeechUtility object
        ttsUtility = new TextToSpeechUtility(this);

        List<Chat> chatTitles = new ArrayList<>();
        // TODO: Setup Database Query

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);


        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                chatTitles = userRepository.recyclerViewChats(user); // Assign the List<Chat> directly
                setUpRecyclerView(chatTitles); // Move the setup to the observer callback
            }
        });

        setUpSettingsButton();
    }



    public void setUpSettingsButton() {
        findViewById(R.id.mainActivitySettingsImageView).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(ProfilePageActivity.this, SettingsPageActivity.class);
            startActivity(goToSettingsPageIntent);
            ttsUtility.speak("Opening settings page");
        });
    }

    public void setUpRecyclerView(ArrayList<String> chatTitles){
        RecyclerView chatListRecyclerView = findViewById(R.id.profilePageChatsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        chatListRecyclerView.setLayoutManager(layoutManager);

        // Add item decoration with desired spacing
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing); // Define your desired spacing value
        chatListRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = spacingInPixels;

                // Add top margin only for the first item to remove unwanted space
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = spacingInPixels;
                } else {
                    outRect.top = 0;
                }
            }
        });

        ChatListRecyclerViewAdapter adapter = new ChatListRecyclerViewAdapter(chatTitles, this);
        chatListRecyclerView.setAdapter(adapter);
    }
}