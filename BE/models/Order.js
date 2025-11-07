const mongoose = require('mongoose');

const orderSchema = new mongoose.Schema({
  orderNumber: {
    type: String,
    unique: true
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User là bắt buộc']
  },
  store: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Store',
    required: [true, 'Cửa hàng là bắt buộc']
  },
  paymentMethod: {
    type: String,
    required: [true, 'Phương thức thanh toán là bắt buộc'],
    enum: ['cash', 'bank_transfer', 'vnpay', 'zalopay', 'paypal', 'credit_card', 'payos']
  },
  shippingAddress: {
    fullName: {
      type: String,
      required: [true, 'Họ tên người nhận là bắt buộc'],
      trim: true
    },
    phone: {
      type: String,
      required: [true, 'Số điện thoại là bắt buộc'],
      trim: true
    },
    address: {
      type: String,
      required: [true, 'Địa chỉ là bắt buộc'],
      trim: true
    },
    city: {
      type: String,
      required: [true, 'Thành phố là bắt buộc'],
      trim: true
    }
  },
  orderStatus: {
    type: String,
    enum: ['pending', 'confirmed', 'shipped', 'delivered', 'cancelled'],
    default: 'pending'
  },
  orderDate: {
    type: Date,
    default: Date.now
  },
  totalAmount: {
    type: Number,
    required: [true, 'Tổng tiền là bắt buộc'],
    min: [0, 'Tổng tiền không được âm']
  },
  shippingFee: {
    type: Number,
    min: [0, 'Phí vận chuyển không được âm'],
    default: 0
  },
  finalAmount: {
    type: Number,
    required: [true, 'Số tiền cuối cùng là bắt buộc'],
    min: [0, 'Số tiền cuối cùng không được âm']
  },
  discountAmount: {
    type: Number,
    min: [0, 'Số tiền giảm giá không được âm'],
    default: 0
  },
  orderDetails: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'OrderDetail'
  }],
  notes: {
    type: String,
    trim: true
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for order status in Vietnamese
orderSchema.virtual('statusText').get(function() {
  const statusMap = {
    'pending': 'Chờ xác nhận',
    'confirmed': 'Đã xác nhận',
    'shipped': 'Đang giao hàng',
    'delivered': 'Đã giao hàng',
    'cancelled': 'Đã hủy'
  };
  return statusMap[this.orderStatus] || this.orderStatus;
});

// Index for better performance
orderSchema.index({ orderNumber: 1 });
orderSchema.index({ user: 1, orderDate: -1 });
orderSchema.index({ store: 1, orderDate: -1 });
orderSchema.index({ orderStatus: 1 });

// Pre-save middleware to generate order number
orderSchema.pre('save', async function(next) {
  if (this.isNew && !this.orderNumber) {
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    // Generate random 4-digit number
    const randomNum = Math.floor(1000 + Math.random() * 9000);
    this.orderNumber = `ORD${year}${month}${day}${randomNum}`;
  }
  
  // Calculate final amount
  this.finalAmount = this.totalAmount + this.shippingFee;
  
  next();
});

// Method to update status
orderSchema.methods.updateStatus = function(newStatus, notes) {
  this.orderStatus = newStatus;
  if (notes) {
    this.notes = notes;
  }
  return this.save();
};

// Method to cancel order
orderSchema.methods.cancel = function(reason) {
  this.orderStatus = 'cancelled';
  if (reason) {
    this.notes = reason;
  }
  return this.save();
};

module.exports = mongoose.model('Order', orderSchema);
