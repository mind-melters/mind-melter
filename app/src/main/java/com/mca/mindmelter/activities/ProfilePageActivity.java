package com.mca.mindmelter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.mca.mindmelter.R;
import com.mca.mindmelter.adapters.ChatListRecyclerViewAdapter;

public class ProfilePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);



        setUpSettingsButton();
        setUpRecyclerView();
    }

    public void setUpSettingsButton() {
        findViewById(R.id.mainActivitySettingsImageView).setOnClickListener(view -> {
            Intent goToSettingsPageIntent = new Intent(ProfilePageActivity.this, SettingsPageActivity.class);
            startActivity(goToSettingsPageIntent);
        });
    }

    public void setUpRecyclerView(){
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

        ChatListRecyclerViewAdapter adapter = new ChatListRecyclerViewAdapter();
        chatListRecyclerView.setAdapter(adapter);
    }
}