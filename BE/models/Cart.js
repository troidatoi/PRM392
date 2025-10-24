const mongoose = require('mongoose');

const cartSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User là bắt buộc']
  },
  status: {
    type: String,
    enum: ['active', 'converted'],
    default: 'active'
  },
  items: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'CartItem'
  }]
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for item count
cartSchema.virtual('itemCount').get(function() {
  return this.items ? this.items.length : 0;
});

// Index for better performance
cartSchema.index({ user: 1, status: 1 });

// Method to mark as converted
cartSchema.methods.markAsConverted = function() {
  this.status = 'converted';
  return this.save();
};

module.exports = mongoose.model('Cart', cartSchema);
