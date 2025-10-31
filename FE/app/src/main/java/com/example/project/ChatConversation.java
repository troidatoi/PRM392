package com.example.project;

import com.google.gson.annotations.SerializedName;

public class ChatConversation {
    @SerializedName("_id")
    private String id;
    
    private ChatMessage.User user;
    
    @SerializedName("lastMessage")
    private ChatMessage lastMessage;
    
    @SerializedName("unreadCount")
    private int unreadCount;
    
    @SerializedName("totalMessages")
    private int totalMessages;
    
    public ChatConversation() {
    }
    
    public ChatConversation(String id, ChatMessage.User user, ChatMessage lastMessage, int unreadCount, int totalMessages) {
        this.id = id;
        this.user = user;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.totalMessages = totalMessages;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public ChatMessage.User getUser() {
        return user;
    }
    
    public void setUser(ChatMessage.User user) {
        this.user = user;
    }
    
    public ChatMessage getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(ChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public int getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public int getTotalMessages() {
        return totalMessages;
    }
    
    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }
}
