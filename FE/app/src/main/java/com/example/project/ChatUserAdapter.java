package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {

    private List<ChatUser> chatUsers;
    private OnChatUserClickListener listener;

    public interface OnChatUserClickListener {
        void onChatUserClick(ChatUser chatUser);
    }

    public ChatUserAdapter(List<ChatUser> chatUsers, OnChatUserClickListener listener) {
        this.chatUsers = chatUsers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatUser chatUser = chatUsers.get(position);

        holder.tvUserName.setText(chatUser.getUserName());
        holder.tvLastMessage.setText(chatUser.getLastMessage());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(chatUser.getLastMessageTime()));
        holder.tvMessageTime.setText(time);

        // Show/hide unread badge
        if (chatUser.getUnreadCount() > 0) {
            holder.badgeUnread.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(chatUser.getUnreadCount()));
        } else {
            holder.badgeUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatUserClick(chatUser);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvLastMessage, tvMessageTime, tvUnreadCount;
        CardView badgeUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            badgeUnread = itemView.findViewById(R.id.badgeUnread);
        }
    }
}

