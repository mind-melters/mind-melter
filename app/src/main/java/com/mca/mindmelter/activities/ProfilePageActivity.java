package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;

import android.util.Log;

import android.view.View;

import com.amplifyframework.datastore.generated.model.Chat;
import com.mca.mindmelter.R;
import com.mca.mindmelter.adapters.ChatListRecyclerViewAdapter;

import com.mca.mindmelter.utilities.TextToSpeechUtility;

import com.mca.mindmelter.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ProfilePageActivity extends AppCompatActivity {
    public static final String CHAT_ID_EXTRA_TAG = "chatId";

    private final String TAG = "ProfilePageActivity";
    public static final String DATABASE_NAME = "chat_title_database";
    private List<Chat> chats;
    ChatListRecyclerViewAdapter adapter;
    SharedPreferences preferences;

    // Creating an instance of the TextToSpeechUtility class
    private TextToSpeechUtility ttsUtility;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // Initialize the TextToSpeechUtility object
        ttsUtility = new TextToSpeechUtility(this);

        List<Chat> chatTitles = new ArrayList<>();
        // TODO: Setup Database Query

        // Get the UserRepository singleton instance
        userRepository = UserRepository.getInstance(this);
        userRepository.loadUser();

        userRepository.getCurrentUser().observe(this, user -> {
            if (user != null) {
                chats = userRepository.getChats();
                setUpRecyclerView(chats);
            }
        });

        setUpSettingsButton();
        setUpTriviaButton();
    }

    public void setUpSettingsButton() {
        findViewById(R.id.mainActivitySettingsImageView).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(ProfilePageActivity.this, SettingsPageActivity.class);
            startActivity(goToSettingsPageIntent);
            ttsUtility.speak("Opening settings page");
        });
    }

    public void setUpTriviaButton() {
        findViewById(R.id.mindMelterHomeImageView).setOnClickListener(view -> {
            Intent goToTriviaPageIntent = new Intent(ProfilePageActivity.this, GenerateTriviaActivity.class);
            startActivity(goToTriviaPageIntent);
        });
    }

    public void setUpRecyclerView(List<Chat> chats){
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

        ChatListRecyclerViewAdapter adapter = new ChatListRecyclerViewAdapter(chats, this);
        chatListRecyclerView.setAdapter(adapter);
    }
}
