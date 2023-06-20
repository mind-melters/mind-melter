package com.mca.mindmelter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mca.mindmelter.R;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> chatMessages;

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_ASSISTANT = 1;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getRole().equals(ChatMessageRole.USER.value()) ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_message_item_user, parent, false);
        } else { // viewType == VIEW_TYPE_ASSISTANT
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_message_item_assistant, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.bind(chatMessage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatMessageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatMessageTextView = itemView.findViewById(R.id.message_content);
        }

        void bind(ChatMessage chatMessage) {
            chatMessageTextView.setText(chatMessage.getContent());
        }
    }
}
