const mongoose = require('mongoose');

const orderDetailSchema = new mongoose.Schema({
  order: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Order',
    required: [true, 'Đơn hàng là bắt buộc']
  },
  product: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Bike',
    required: [true, 'Sản phẩm là bắt buộc']
  },
  quantity: {
    type: Number,
    required: [true, 'Số lượng là bắt buộc'],
    min: [1, 'Số lượng phải lớn hơn 0']
  },
  price: {
    type: Number,
    required: [true, 'Giá là bắt buộc'],
    min: [0, 'Giá không được âm']
  },
  totalPrice: {
    type: Number,
    required: [true, 'Tổng tiền là bắt buộc'],
    min: [0, 'Tổng tiền không được âm']
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Index for better performance
orderDetailSchema.index({ order: 1 });
orderDetailSchema.index({ product: 1 });

// Pre-save middleware to calculate total price
orderDetailSchema.pre('save', function(next) {
  this.totalPrice = this.quantity * this.price;
  next();
});

module.exports = mongoose.model('OrderDetail', orderDetailSchema);
