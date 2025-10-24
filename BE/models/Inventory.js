const mongoose = require('mongoose');

const inventorySchema = new mongoose.Schema({
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
  stock: {
    type: Number,
    required: [true, 'Số lượng tồn kho là bắt buộc'],
    min: [0, 'Số lượng tồn kho không được âm'],
    default: 0
  },
  minStock: {
    type: Number,
    min: [0, 'Số lượng tối thiểu không được âm'],
    default: 5
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for stock status
inventorySchema.virtual('stockStatus').get(function() {
  if (this.stock <= 0) return 'out_of_stock';
  if (this.stock <= this.minStock) return 'low_stock';
  return 'in_stock';
});

// Virtual for needs restock
inventorySchema.virtual('needsRestock').get(function() {
  return this.stock <= this.minStock;
});

// Index for better performance
inventorySchema.index({ product: 1, store: 1 }, { unique: true });
inventorySchema.index({ store: 1, stock: 1 });
inventorySchema.index({ product: 1, stock: 1 });

// Method to reduce stock
inventorySchema.methods.reduceStock = function(quantity) {
  if (this.stock < quantity) {
    throw new Error(`Chỉ còn ${this.stock} sản phẩm trong kho`);
  }
  
  this.stock -= quantity;
  return this.save();
};

// Method to add stock
inventorySchema.methods.addStock = function(quantity) {
  this.stock += quantity;
  return this.save();
};

// Method to check availability
inventorySchema.methods.isAvailable = function(quantity) {
  return this.stock >= quantity;
};

// Static method to get inventory for product at store
inventorySchema.statics.getInventory = function(productId, storeId) {
  return this.findOne({
    product: productId,
    storeLocation: storeId
  });
};

// Static method to get low stock items for store
inventorySchema.statics.getLowStockItems = function(storeId) {
  return this.find({
    storeLocation: storeId,
    stock: { $lte: '$minStock' }
  })
  .populate('product', 'name brand price')
  .populate('storeLocation', 'name address');
};

// Static method to get out of stock items for store
inventorySchema.statics.getOutOfStockItems = function(storeId) {
  return this.find({
    storeLocation: storeId,
    stock: 0
  })
  .populate('product', 'name brand price')
  .populate('storeLocation', 'name address');
};

module.exports = mongoose.model('Inventory', inventorySchema);
