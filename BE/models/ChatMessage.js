const mongoose = require('mongoose');

const chatMessageSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User là bắt buộc']
  },
  roomId: {
    type: String,
    trim: true,
    index: true // Thêm index để query nhanh hơn
  },
  message: {
    type: String,
    required: [true, 'Tin nhắn là bắt buộc'],
    trim: true,
    maxlength: [1000, 'Tin nhắn không được vượt quá 1000 ký tự']
  },
  messageType: {
    type: String,
    enum: ['text', 'image', 'file', 'system', 'order', 'product'],
    default: 'text'
  },
  sentAt: {
    type: Date,
    default: Date.now
  },
  isRead: {
    type: Boolean,
    default: false
  },
  readAt: {
    type: Date
  },
  isEdited: {
    type: Boolean,
    default: false
  },
  editedAt: {
    type: Date
  },
  isDeleted: {
    type: Boolean,
    default: false
  },
  deletedAt: {
    type: Date
  },
  attachments: [{
    type: {
      type: String,
      enum: ['image', 'file', 'document'],
      required: true
    },
    url: {
      type: String,
      required: true,
      trim: true
    },
    filename: {
      type: String,
      trim: true
    },
    size: {
      type: Number,
      min: [0, 'Kích thước file không được âm']
    },
    mimeType: {
      type: String,
      trim: true
    }
  }],
  metadata: {
    orderId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Order'
    },
    productId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Bike'
    },
    replyTo: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'ChatMessage'
    },
    customData: {
      type: mongoose.Schema.Types.Mixed
    }
  },
  reactions: [{
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true
    },
    emoji: {
      type: String,
      required: true,
      trim: true
    },
    createdAt: {
      type: Date,
      default: Date.now
    }
  }],
  isFromAdmin: {
    type: Boolean,
    default: false
  },
  adminUser: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  priority: {
    type: String,
    enum: ['low', 'normal', 'high', 'urgent'],
    default: 'normal'
  },
  tags: [{
    type: String,
    trim: true
  }],
  isArchived: {
    type: Boolean,
    default: false
  },
  archivedAt: {
    type: Date
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for time since sent
chatMessageSchema.virtual('timeAgo').get(function() {
  const now = new Date();
  const diff = now - this.sentAt;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (days > 0) return `${days} ngày trước`;
  if (hours > 0) return `${hours} giờ trước`;
  if (minutes > 0) return `${minutes} phút trước`;
  return 'Vừa xong';
});

// Virtual for reaction count
chatMessageSchema.virtual('reactionCount').get(function() {
  return this.reactions ? this.reactions.length : 0;
});

// Virtual for has attachments
chatMessageSchema.virtual('hasAttachments').get(function() {
  return this.attachments && this.attachments.length > 0;
});

// Index for better performance
chatMessageSchema.index({ user: 1, sentAt: -1 });
chatMessageSchema.index({ sentAt: -1 });
chatMessageSchema.index({ isRead: 1, sentAt: -1 });
chatMessageSchema.index({ messageType: 1, sentAt: -1 });
chatMessageSchema.index({ isFromAdmin: 1, sentAt: -1 });
chatMessageSchema.index({ priority: 1, sentAt: -1 });
chatMessageSchema.index({ isArchived: 1, sentAt: -1 });

// Pre-save middleware
chatMessageSchema.pre('save', function(next) {
  // Auto-generate tags from message content
  if (this.isModified('message') && this.messageType === 'text') {
    const words = this.message.toLowerCase()
      .replace(/[^\w\s]/g, '')
      .split(/\s+/)
      .filter(word => word.length > 3);
    
    this.tags = [...new Set(words)].slice(0, 5); // Max 5 tags
  }
  
  next();
});

// Method to mark as read
chatMessageSchema.methods.markAsRead = function() {
  this.isRead = true;
  this.readAt = new Date();
  return this.save();
};

// Method to edit message
chatMessageSchema.methods.editMessage = function(newMessage) {
  if (this.isDeleted) {
    throw new Error('Không thể chỉnh sửa tin nhắn đã xóa');
  }
  
  this.message = newMessage;
  this.isEdited = true;
  this.editedAt = new Date();
  return this.save();
};

// Method to delete message
chatMessageSchema.methods.deleteMessage = function() {
  this.isDeleted = true;
  this.deletedAt = new Date();
  return this.save();
};

// Method to add reaction
chatMessageSchema.methods.addReaction = function(userId, emoji) {
  if (this.isDeleted) {
    throw new Error('Không thể thêm reaction cho tin nhắn đã xóa');
  }
  
  // Remove existing reaction from same user
  this.reactions = this.reactions.filter(r => !r.user.equals(userId));
  
  // Add new reaction
  this.reactions.push({
    user: userId,
    emoji: emoji,
    createdAt: new Date()
  });
  
  return this.save();
};

// Method to remove reaction
chatMessageSchema.methods.removeReaction = function(userId) {
  this.reactions = this.reactions.filter(r => !r.user.equals(userId));
  return this.save();
};

// Method to archive message
chatMessageSchema.methods.archive = function() {
  this.isArchived = true;
  this.archivedAt = new Date();
  return this.save();
};

// Static method to get unread count for user
chatMessageSchema.statics.getUnreadCount = function(userId) {
  return this.countDocuments({ 
    user: userId, 
    isRead: false, 
    isDeleted: false,
    isFromAdmin: true
  });
};

// Static method to get recent messages
chatMessageSchema.statics.getRecentMessages = function(userId, limit = 50) {
  return this.find({ 
    user: userId,
    isDeleted: false,
    isArchived: false
  })
  .populate('user', 'username profile.firstName profile.lastName')
  .populate('metadata.orderId', 'orderNumber')
  .populate('metadata.productId', 'name price')
  .sort({ sentAt: -1 })
  .limit(limit);
};

module.exports = mongoose.model('ChatMessage', chatMessageSchema);
