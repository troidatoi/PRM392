const mongoose = require('mongoose');

const orderSchema = new mongoose.Schema({
  orderNumber: {
    type: String,
    unique: true,
    required: [true, 'Số đơn hàng là bắt buộc']
  },
  cart: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Cart',
    required: [true, 'Cart là bắt buộc']
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User là bắt buộc']
  },
  paymentMethod: {
    type: String,
    required: [true, 'Phương thức thanh toán là bắt buộc'],
    enum: ['cash', 'bank_transfer', 'vnpay', 'zalopay', 'paypal', 'credit_card']
  },
  billingAddress: {
    fullName: {
      type: String,
      required: [true, 'Họ tên người nhận là bắt buộc'],
      trim: true,
      maxlength: [100, 'Họ tên không được vượt quá 100 ký tự']
    },
    phone: {
      type: String,
      required: [true, 'Số điện thoại là bắt buộc'],
      trim: true,
      match: [/^[0-9]{10,11}$/, 'Số điện thoại không hợp lệ']
    },
    email: {
      type: String,
      trim: true,
      lowercase: true,
      match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Email không hợp lệ']
    },
    address: {
      type: String,
      required: [true, 'Địa chỉ là bắt buộc'],
      trim: true,
      maxlength: [255, 'Địa chỉ không được vượt quá 255 ký tự']
    },
    city: {
      type: String,
      required: [true, 'Thành phố là bắt buộc'],
      trim: true
    },
    district: {
      type: String,
      required: [true, 'Quận/Huyện là bắt buộc'],
      trim: true
    },
    ward: {
      type: String,
      trim: true
    },
    postalCode: {
      type: String,
      trim: true
    }
  },
  shippingAddress: {
    fullName: {
      type: String,
      trim: true,
      maxlength: [100, 'Họ tên không được vượt quá 100 ký tự']
    },
    phone: {
      type: String,
      trim: true,
      match: [/^[0-9]{10,11}$/, 'Số điện thoại không hợp lệ']
    },
    address: {
      type: String,
      trim: true,
      maxlength: [255, 'Địa chỉ không được vượt quá 255 ký tự']
    },
    city: {
      type: String,
      trim: true
    },
    district: {
      type: String,
      trim: true
    },
    ward: {
      type: String,
      trim: true
    },
    postalCode: {
      type: String,
      trim: true
    }
  },
  orderStatus: {
    type: String,
    required: [true, 'Trạng thái đơn hàng là bắt buộc'],
    enum: ['pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded'],
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
  discountAmount: {
    type: Number,
    min: [0, 'Số tiền giảm giá không được âm'],
    default: 0
  },
  finalAmount: {
    type: Number,
    required: [true, 'Số tiền cuối cùng là bắt buộc'],
    min: [0, 'Số tiền cuối cùng không được âm']
  },
  notes: {
    type: String,
    trim: true,
    maxlength: [500, 'Ghi chú không được vượt quá 500 ký tự']
  },
  trackingNumber: {
    type: String,
    trim: true
  },
  estimatedDelivery: {
    type: Date
  },
  deliveredAt: {
    type: Date
  },
  cancelledAt: {
    type: Date
  },
  cancellationReason: {
    type: String,
    trim: true,
    maxlength: [200, 'Lý do hủy không được vượt quá 200 ký tự']
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
    'processing': 'Đang xử lý',
    'shipped': 'Đang giao hàng',
    'delivered': 'Đã giao hàng',
    'cancelled': 'Đã hủy',
    'refunded': 'Đã hoàn tiền'
  };
  return statusMap[this.orderStatus] || this.orderStatus;
});

// Virtual for is delivered
orderSchema.virtual('isDelivered').get(function() {
  return this.orderStatus === 'delivered';
});

// Virtual for is cancelled
orderSchema.virtual('isCancelled').get(function() {
  return this.orderStatus === 'cancelled';
});

// Index for better performance
orderSchema.index({ orderNumber: 1 });
orderSchema.index({ user: 1, orderDate: -1 });
orderSchema.index({ orderStatus: 1, orderDate: -1 });
orderSchema.index({ orderDate: -1 });

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
  this.finalAmount = this.totalAmount + this.shippingFee - this.discountAmount;
  
  next();
});

// Method to update status
orderSchema.methods.updateStatus = function(newStatus, notes = '') {
  this.orderStatus = newStatus;
  
  if (newStatus === 'delivered') {
    this.deliveredAt = new Date();
  } else if (newStatus === 'cancelled') {
    this.cancelledAt = new Date();
    this.cancellationReason = notes;
  }
  
  return this.save();
};

// Method to cancel order
orderSchema.methods.cancel = function(reason = '') {
  if (this.orderStatus === 'delivered') {
    throw new Error('Không thể hủy đơn hàng đã giao');
  }
  
  return this.updateStatus('cancelled', reason);
};

// Method to mark as delivered
orderSchema.methods.markAsDelivered = function() {
  return this.updateStatus('delivered');
};

module.exports = mongoose.model('Order', orderSchema);
