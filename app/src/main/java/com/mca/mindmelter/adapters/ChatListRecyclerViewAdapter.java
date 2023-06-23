package com.mca.mindmelter.adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Chat;
import com.mca.mindmelter.R;
import com.mca.mindmelter.activities.ChatActivity;
import com.mca.mindmelter.activities.ProfilePageActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListRecyclerViewAdapter.ChatListViewHolder> {
    private List<Chat> chats;

    // Used to click on the recycler view
    Context callingActivity;

    public ChatListRecyclerViewAdapter(List<Chat> chats, Context callingActivity) {
        this.chats = chats;
        this.callingActivity = callingActivity;

    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatFragment = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_list, parent, false);

        return new ChatListViewHolder(chatFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        TextView chatTitleFragmentTextView = (TextView) holder.itemView.findViewById(R.id.chatFragmentTextView);
        // Set name
        Chat chat = chats.get(position);
        String chatTitle = chat.getTitle();
        String chatTitleFragmentText = chatTitle;
        chatTitleFragmentTextView.setText(chatTitleFragmentText);

        // TODO: Intent of click for recycler view to go to ChatActivity

        View chatViewHolder = holder.itemView;
        chatViewHolder.setOnClickListener(view -> {
            Intent goToChatIntent = new Intent(callingActivity, ChatActivity.class);
            goToChatIntent.putExtra(ProfilePageActivity.CHAT_ID_EXTRA_TAG, chat.getId());
            callingActivity.startActivity(goToChatIntent);
        });
    }

    @Override
    public int getItemCount() {

       return chats.size();
    }

    public static class ChatListViewHolder extends RecyclerView.ViewHolder {
        public ChatListViewHolder(View fragmentChatView){

            super(fragmentChatView);
        }
    }
}