const mongoose = require('mongoose');

const cartItemSchema = new mongoose.Schema({
  cart: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Cart',
    required: [true, 'Cart là bắt buộc']
  },
  product: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Bike',
    required: [true, 'Sản phẩm là bắt buộc']
  },
  quantity: {
    type: Number,
    required: [true, 'Số lượng là bắt buộc'],
    min: [1, 'Số lượng phải lớn hơn 0'],
    max: [999, 'Số lượng không được vượt quá 999']
  },
  price: {
    type: Number,
    required: [true, 'Giá là bắt buộc'],
    min: [0, 'Giá không được âm']
  },
  originalPrice: {
    type: Number,
    min: [0, 'Giá gốc không được âm']
  },
  discount: {
    type: Number,
    min: [0, 'Giảm giá không được âm'],
    max: [100, 'Giảm giá không được vượt quá 100%'],
    default: 0
  },
  addedAt: {
    type: Date,
    default: Date.now
  },
  notes: {
    type: String,
    trim: true,
    maxlength: [200, 'Ghi chú không được vượt quá 200 ký tự']
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for total price (quantity * price)
cartItemSchema.virtual('totalPrice').get(function() {
  return this.quantity * this.price;
});

// Virtual for discount amount
cartItemSchema.virtual('discountAmount').get(function() {
  if (this.originalPrice && this.originalPrice > this.price) {
    return (this.originalPrice - this.price) * this.quantity;
  }
  return 0;
});

// Virtual for savings percentage
cartItemSchema.virtual('savingsPercentage').get(function() {
  if (this.originalPrice && this.originalPrice > this.price) {
    return Math.round(((this.originalPrice - this.price) / this.originalPrice) * 100);
  }
  return 0;
});

// Index for better performance
cartItemSchema.index({ cart: 1, product: 1 });
cartItemSchema.index({ cart: 1 });
cartItemSchema.index({ product: 1 });

// Pre-save middleware to validate stock
cartItemSchema.pre('save', async function(next) {
  if (this.isModified('quantity') || this.isNew) {
    try {
      const Bike = mongoose.model('Bike');
      const product = await Bike.findById(this.product);
      
      if (!product) {
        return next(new Error('Sản phẩm không tồn tại'));
      }
      
      if (product.stock < this.quantity) {
        return next(new Error(`Chỉ còn ${product.stock} sản phẩm trong kho`));
      }
      
      // Update price if not set
      if (!this.price) {
        this.price = product.price;
      }
      
      if (!this.originalPrice) {
        this.originalPrice = product.originalPrice || product.price;
      }
      
    } catch (error) {
      return next(error);
    }
  }
  next();
});

// Method to update quantity
cartItemSchema.methods.updateQuantity = function(newQuantity) {
  if (newQuantity <= 0) {
    throw new Error('Số lượng phải lớn hơn 0');
  }
  this.quantity = newQuantity;
  return this.save();
};

// Method to calculate total
cartItemSchema.methods.calculateTotal = function() {
  return this.quantity * this.price;
};

module.exports = mongoose.model('CartItem', cartItemSchema);
