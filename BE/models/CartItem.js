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
  store: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Store',
    required: [true, 'Cửa hàng là bắt buộc']
  },
  quantity: {
    type: Number,
    required: [true, 'Số lượng là bắt buộc'],
    min: [1, 'Số lượng phải lớn hơn 0']
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for total price (quantity * current product price)
cartItemSchema.virtual('totalPrice').get(async function() {
  const Bike = mongoose.model('Bike');
  const product = await Bike.findById(this.product);
  return this.quantity * (product ? product.price : 0);
});

// Index for better performance
cartItemSchema.index({ cart: 1, product: 1 });
cartItemSchema.index({ cart: 1 });

// Pre-save middleware to validate stock
cartItemSchema.pre('save', async function(next) {
  if (this.isModified('quantity') || this.isNew) {
    try {
      const Bike = mongoose.model('Bike');
      const Inventory = mongoose.model('Inventory');
      
      const product = await Bike.findById(this.product);
      
      if (!product) {
        return next(new Error('Sản phẩm không tồn tại'));
      }
      
              // Check inventory at specific store
              const inventory = await Inventory.findOne({
                product: this.product,
                store: this.store
              });
      
      if (!inventory) {
        return next(new Error('Sản phẩm không có trong kho cửa hàng này'));
      }
      
      // Check stock availability
      if (!inventory.isAvailable(this.quantity)) {
        return next(new Error(`Chỉ còn ${inventory.stock} sản phẩm trong kho`));
      }
      
      // No need to set price - will get from product when needed
      
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

module.exports = mongoose.model('CartItem', cartItemSchema);
