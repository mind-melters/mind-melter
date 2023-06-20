package com.mca.mindmelter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mca.mindmelter.R;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class ChatAdapter extends ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder> {
    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_ASSISTANT = 1;

    public ChatAdapter() {
        super(DIFF_CALLBACK);
    }

    static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatMessage>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessage oldChatMessage, @NonNull ChatMessage newChatMessage) {
                    // Compare role and content for simplicity
                    return oldChatMessage.getRole().equals(newChatMessage.getRole()) &&
                            oldChatMessage.getContent().equals(newChatMessage.getContent());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage oldChatMessage, @NonNull ChatMessage newChatMessage) {
                    return oldChatMessage.getRole().equals(newChatMessage.getRole()) &&
                            oldChatMessage.getContent().equals(newChatMessage.getContent());
                }
            };


    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        return message.getRole().equals(ChatMessageRole.USER.value()) ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        ChatMessage chatMessage = getItem(position);
        holder.bind(chatMessage);
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
