const mongoose = require('mongoose');

const cartSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User là bắt buộc']
  },
  totalPrice: {
    type: Number,
    required: [true, 'Tổng giá là bắt buộc'],
    min: [0, 'Tổng giá không được âm'],
    default: 0
  },
  status: {
    type: String,
    enum: ['active', 'abandoned', 'converted', 'expired'],
    default: 'active'
  },
  items: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'CartItem'
  }],
  expiresAt: {
    type: Date,
    default: function() {
      // Cart expires after 30 days
      return new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);
    }
  },
  metadata: {
    sessionId: {
      type: String,
      trim: true
    },
    userAgent: {
      type: String,
      trim: true
    },
    ipAddress: {
      type: String,
      trim: true
    }
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for item count
cartSchema.virtual('itemCount').get(function() {
  return this.items ? this.items.length : 0;
});

// Virtual for is expired
cartSchema.virtual('isExpired').get(function() {
  return this.expiresAt && this.expiresAt < new Date();
});

// Index for better performance
cartSchema.index({ user: 1, status: 1 });
cartSchema.index({ status: 1, expiresAt: 1 });
cartSchema.index({ 'metadata.sessionId': 1 });

// Pre-save middleware to calculate total price
cartSchema.pre('save', async function(next) {
  if (this.isModified('items') || this.isNew) {
    try {
      const CartItem = mongoose.model('CartItem');
      const cartItems = await CartItem.find({ _id: { $in: this.items } });
      
      this.totalPrice = cartItems.reduce((total, item) => {
        return total + (item.price * item.quantity);
      }, 0);
    } catch (error) {
      // If CartItem model doesn't exist yet, skip calculation
      console.log('CartItem model not found, skipping total calculation');
    }
  }
  next();
});

// Method to add item to cart
cartSchema.methods.addItem = function(productId, quantity, price) {
  // This would typically be handled by CartItem model
  // but we can add the item reference here
  return this.items.push(productId);
};

// Method to remove item from cart
cartSchema.methods.removeItem = function(itemId) {
  this.items = this.items.filter(id => !id.equals(itemId));
  return this.save();
};

// Method to clear cart
cartSchema.methods.clear = function() {
  this.items = [];
  this.totalPrice = 0;
  return this.save();
};

// Method to mark as converted
cartSchema.methods.markAsConverted = function() {
  this.status = 'converted';
  return this.save();
};

// Method to mark as abandoned
cartSchema.methods.markAsAbandoned = function() {
  this.status = 'abandoned';
  return this.save();
};

module.exports = mongoose.model('Cart', cartSchema);
