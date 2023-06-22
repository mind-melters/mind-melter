package com.mca.mindmelter.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Chat;
import com.mca.mindmelter.R;

import java.util.List;

public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter {
    public static class ChatListViewHolder extends RecyclerView.ViewHolder {
        public ChatListViewHolder(View fragmentChatView){
            super(fragmentChatView);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatFragment = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_list, parent, false);

        return new ChatListViewHolder(chatFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        // TODO: Hard code chats for testing
        return 10;
    }
}