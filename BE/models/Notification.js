const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User là bắt buộc']
  },
  title: {
    type: String,
    required: [true, 'Tiêu đề là bắt buộc'],
    trim: true,
    maxlength: [100, 'Tiêu đề không được vượt quá 100 ký tự']
  },
  message: {
    type: String,
    required: [true, 'Nội dung là bắt buộc'],
    trim: true,
    maxlength: [500, 'Nội dung không được vượt quá 500 ký tự']
  },
  type: {
    type: String,
    required: [true, 'Loại thông báo là bắt buộc'],
    enum: ['order', 'payment', 'promotion', 'system', 'chat', 'cart', 'product'],
    default: 'system'
  },
  priority: {
    type: String,
    enum: ['low', 'medium', 'high', 'urgent'],
    default: 'medium'
  },
  isRead: {
    type: Boolean,
    default: false
  },
  readAt: {
    type: Date
  },
  actionUrl: {
    type: String,
    trim: true
  },
  actionText: {
    type: String,
    trim: true,
    maxlength: [50, 'Văn bản hành động không được vượt quá 50 ký tự']
  },
  metadata: {
    orderId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Order'
    },
    paymentId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Payment'
    },
    productId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Bike'
    },
    cartId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Cart'
    },
    chatId: {
      type: String,
      trim: true
    },
    customData: {
      type: mongoose.Schema.Types.Mixed
    }
  },
  scheduledAt: {
    type: Date
  },
  sentAt: {
    type: Date
  },
  deliveryStatus: {
    type: String,
    enum: ['pending', 'sent', 'delivered', 'failed'],
    default: 'pending'
  },
  deliveryMethod: {
    type: String,
    enum: ['push', 'email', 'sms', 'in_app'],
    default: 'in_app'
  },
  expiresAt: {
    type: Date
  },
  isActive: {
    type: Boolean,
    default: true
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for time since creation
notificationSchema.virtual('timeAgo').get(function() {
  const now = new Date();
  const diff = now - this.createdAt;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (days > 0) return `${days} ngày trước`;
  if (hours > 0) return `${hours} giờ trước`;
  if (minutes > 0) return `${minutes} phút trước`;
  return 'Vừa xong';
});

// Virtual for is expired
notificationSchema.virtual('isExpired').get(function() {
  return this.expiresAt && this.expiresAt < new Date();
});

// Virtual for is scheduled
notificationSchema.virtual('isScheduled').get(function() {
  return this.scheduledAt && this.scheduledAt > new Date();
});

// Index for better performance
notificationSchema.index({ user: 1, isRead: 1, createdAt: -1 });
notificationSchema.index({ type: 1, priority: 1 });
notificationSchema.index({ deliveryStatus: 1, scheduledAt: 1 });
notificationSchema.index({ expiresAt: 1 });
notificationSchema.index({ createdAt: -1 });

// Pre-save middleware
notificationSchema.pre('save', function(next) {
  // Set default expiration (30 days from now)
  if (!this.expiresAt) {
    this.expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);
  }
  
  // Auto-generate action text based on type
  if (!this.actionText) {
    const actionTextMap = {
      'order': 'Xem đơn hàng',
      'payment': 'Xem thanh toán',
      'promotion': 'Xem khuyến mãi',
      'system': 'Xem chi tiết',
      'chat': 'Mở chat',
      'cart': 'Xem giỏ hàng',
      'product': 'Xem sản phẩm'
    };
    this.actionText = actionTextMap[this.type] || 'Xem chi tiết';
  }
  
  next();
});

// Method to mark as read
notificationSchema.methods.markAsRead = function() {
  this.isRead = true;
  this.readAt = new Date();
  return this.save();
};

// Method to mark as unread
notificationSchema.methods.markAsUnread = function() {
  this.isRead = false;
  this.readAt = undefined;
  return this.save();
};

// Method to mark as sent
notificationSchema.methods.markAsSent = function() {
  this.deliveryStatus = 'sent';
  this.sentAt = new Date();
  return this.save();
};

// Method to mark as delivered
notificationSchema.methods.markAsDelivered = function() {
  this.deliveryStatus = 'delivered';
  return this.save();
};

// Method to mark as failed
notificationSchema.methods.markAsFailed = function() {
  this.deliveryStatus = 'failed';
  return this.save();
};

// Static method to create notification
notificationSchema.statics.createNotification = function(userId, title, message, options = {}) {
  return this.create({
    user: userId,
    title,
    message,
    type: options.type || 'system',
    priority: options.priority || 'medium',
    actionUrl: options.actionUrl,
    actionText: options.actionText,
    metadata: options.metadata || {},
    scheduledAt: options.scheduledAt,
    deliveryMethod: options.deliveryMethod || 'in_app'
  });
};

// Static method to get unread count
notificationSchema.statics.getUnreadCount = function(userId) {
  return this.countDocuments({ 
    user: userId, 
    isRead: false, 
    isActive: true,
    expiresAt: { $gt: new Date() }
  });
};

module.exports = mongoose.model('Notification', notificationSchema);
