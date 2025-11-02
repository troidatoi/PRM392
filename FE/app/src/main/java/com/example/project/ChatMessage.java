package com.example.project;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class ChatMessage {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("user")
    private Object userRaw; // Can be String (ID) or User object
    
    private transient User user; // User object (parsed from userRaw)
    
    private String message;
    
    @SerializedName("messageType")
    private String messageType;
    
    @SerializedName("sentAt")
    private long sentAt; // Receive as timestamp (long) from API
    
    @SerializedName("isRead")
    private boolean isRead;
    
    @SerializedName("isEdited")
    private boolean isEdited;
    
    @SerializedName("isDeleted")
    private boolean isDeleted;
    
    // Legacy fields for compatibility
    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private long timestamp;
    private boolean isFromUser;

    public ChatMessage() {
    }

    // Legacy constructor
    public ChatMessage(String id, String senderId, String senderName, String receiverId, 
                      String message, long timestamp, boolean isFromUser) {
        this.messageId = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.isFromUser = isFromUser;
    }

    // Getters and Setters
    public String getId() {
        return id != null ? id : messageId;
    }

    public void setId(String id) {
        this.id = id;
        this.messageId = id;
    }

    public User getUser() {
        if (user == null && userRaw != null) {
            parseUserFromRaw();
        }
        return user;
    }
    
    private void parseUserFromRaw() {
        android.util.Log.d("ChatMessage", "parseUserFromRaw - userRaw type: " + (userRaw != null ? userRaw.getClass().getName() : "null"));

        if (userRaw instanceof String) {
            // user is just an ID, create minimal User object
            user = new User();
            user.setId((String) userRaw);
            senderId = (String) userRaw;
            android.util.Log.d("ChatMessage", "Parsed as String ID: " + senderId);
        } else if (userRaw instanceof User) {
            user = (User) userRaw;
            if (user != null) {
                senderId = user.getId();
                senderName = user.getUsername();
                android.util.Log.d("ChatMessage", "Parsed as User object - ID: " + senderId);
            }
        } else if (userRaw instanceof com.google.gson.internal.LinkedTreeMap) {
            // Gson parsed it as a Map, convert to User
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                String json = gson.toJson(userRaw);
                android.util.Log.d("ChatMessage", "Parsed as LinkedTreeMap - JSON: " + json);
                user = gson.fromJson(json, User.class);
                if (user != null) {
                    senderId = user.getId();
                    senderName = user.getUsername();
                    android.util.Log.d("ChatMessage", "Converted to User - ID: " + senderId);
                }
            } catch (Exception e) {
                android.util.Log.e("ChatMessage", "Error parsing LinkedTreeMap", e);
                e.printStackTrace();
            }
        }
    }

    public void setUser(User user) {
        this.user = user;
        this.userRaw = user;
        if (user != null) {
            this.senderId = user.getId();
            this.senderName = user.getUsername();
        }
    }
    
    public String getUserId() {
        if (userRaw instanceof String) {
            return (String) userRaw;
        } else if (user != null) {
            return user.getId();
        } else if (senderId != null) {
            return senderId;
        }
        return null;
    }

    public String getSenderId() {
        // Try to get user ID from various sources
        // First, check if userRaw is a String (just the ID)
        if (userRaw instanceof String) {
            return (String) userRaw;
        }
        
        // Second, parse userRaw if not done yet
        if (user == null && userRaw != null) {
            parseUserFromRaw();
        }
        
        // Third, try to get from user object
        if (user != null && user.getId() != null) {
            return user.getId();
        }
        
        // Fourth, try getUserId() method
        String userId = getUserId();
        if (userId != null) {
            return userId;
        }
        
        // Finally, return senderId field
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        User userObj = getUser();
        if (userObj != null && userObj.getUsername() != null) {
            return userObj.getUsername();
        }
        return senderName != null ? senderName : "Unknown";
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType != null ? messageType : "text";
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public long getTimestamp() {
        // Return sentAt if available, otherwise fallback to timestamp
        return sentAt != 0 ? sentAt : timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.sentAt = timestamp;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
        this.timestamp = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }

    public String getMessageId() {
        return getId();
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
        this.id = messageId;
    }
    
    // Inner User class for API response
    public static class User {
        @SerializedName("_id")
        private String id;
        private String username;
        private String email;
        private String avatar;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getAvatar() {
            return avatar;
        }
        
        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}

