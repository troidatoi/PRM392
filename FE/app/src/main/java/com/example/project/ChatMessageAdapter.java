package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messages;
    private boolean isUserView; // true if user view, false if admin view

    public ChatMessageAdapter(List<ChatMessage> messages, boolean isUserView) {
        this.messages = messages;
        this.isUserView = isUserView;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);

        // For user: sent messages are from user, received are from admin
        // For admin: sent messages are from admin, received are from user
        if (isUserView) {
            return message.isFromUser() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        } else {
            return message.isFromUser() ? VIEW_TYPE_RECEIVED : VIEW_TYPE_SENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time;
        try {
            long timestamp = message.getTimestamp();
            if (timestamp > 0) {
                time = sdf.format(new Date(timestamp));
            } else {
                time = "N/A";
            }
        } catch (Exception e) {
            time = "N/A";
            e.printStackTrace();
        }

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).tvMessage.setText(message.getMessage());
            ((SentMessageViewHolder) holder).tvTimestamp.setText(time);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).tvMessage.setText(message.getMessage());
            ((ReceivedMessageViewHolder) holder).tvTimestamp.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

