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
import android.widget.TextView;

import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Chat;
import com.mca.mindmelter.R;
import com.mca.mindmelter.adapters.ChatListRecyclerViewAdapter;
import com.mca.mindmelter.utilities.TextToSpeechUtility;

import java.util.ArrayList;
import java.util.List;

public class ProfilePageActivity extends AppCompatActivity {
    public static final String CHAT_TITLE_EXTRA_TAG = "chatTitle";
    private final String TAG = "ProfilePageActivity";
    public static final String DATABASE_NAME = "chat_title_database";
    List<Chat> chatTitles;
    ChatListRecyclerViewAdapter adapter;
    SharedPreferences preferences;

    // Creating an instance of the TextToSpeechUtility class
    private TextToSpeechUtility ttsUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // Initialize the TextToSpeechUtility object
        ttsUtility = new TextToSpeechUtility(this);

        List<Chat> chatTitles = new ArrayList<>();
        // TODO: Setup Database Query

        chatTitles = new ArrayList<>();

        setUpSettingsButton();
        setUpRecyclerView(chatTitles);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatTitles.clear();

        Amplify.API.query(
                ModelQuery.list(Chat.class),
                success -> {
                    Log.i(TAG, "Read chats successfully!");
                    chatTitles = new ArrayList<>();
                    for (Chat databaseChatTitle : success.getData()) {
                        chatTitles.add(databaseChatTitle);
                    }
                    adapter.notifyDataSetChanged();
                    ttsUtility.speak("Updated chat list successfully");
                },
                failure -> {
                    Log.i(TAG, "Did not read chat successfully!");
                    ttsUtility.speak("Failed to update chat list");
                }
        );

        // TODO: This is for nickname
        // adapter.notifyDataSetChanged();
        // preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // String chatTitle = preferences.getString(SettingsPageActivity.USER_NICKNAME_TAG, "No nickname");
        // ((TextView) findViewById(R.id.mainActivityNicknameTextView)).setText(userNickname + "'s Tasks");
    }

    public void setUpSettingsButton() {
        findViewById(R.id.mainActivitySettingsImageView).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(ProfilePageActivity.this, SettingsPageActivity.class);
            startActivity(goToSettingsPageIntent);
            ttsUtility.speak("Opening settings page");
        });
    }

    public void setUpRecyclerView(List<Chat> chatTitles){
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