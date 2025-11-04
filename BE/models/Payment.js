const mongoose = require('mongoose');

const paymentSchema = new mongoose.Schema({
  order: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Order',
    required: [true, 'Đơn hàng là bắt buộc']
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User là bắt buộc']
  },
  amount: {
    type: Number,
    required: [true, 'Số tiền là bắt buộc'],
    min: [0, 'Số tiền không được âm']
  },
  paymentDate: {
    type: Date,
    default: Date.now
  },
  paymentStatus: {
    type: String,
    required: [true, 'Trạng thái thanh toán là bắt buộc'],
    enum: ['pending', 'processing', 'completed', 'failed', 'cancelled', 'refunded'],
    default: 'pending'
  },
  paymentMethod: {
    type: String,
    required: [true, 'Phương thức thanh toán là bắt buộc'],
    enum: ['cash', 'bank_transfer', 'vnpay', 'zalopay', 'paypal', 'credit_card', 'momo', 'payos']
  },
  paymentGateway: {
    type: String,
    trim: true
  },
  transactionId: {
    type: String,
    unique: true,
    sparse: true,
    trim: true
  },
  gatewayTransactionId: {
    type: String,
    trim: true
  },
  gatewayResponse: {
    type: mongoose.Schema.Types.Mixed
  },
  paymentDetails: {
    bankCode: {
      type: String,
      trim: true
    },
    bankName: {
      type: String,
      trim: true
    },
    cardNumber: {
      type: String,
      trim: true
    },
    cardType: {
      type: String,
      trim: true
    },
    expiryDate: {
      type: String,
      trim: true
    }
  },
  currency: {
    type: String,
    default: 'VND',
    enum: ['VND', 'USD', 'EUR']
  },
  exchangeRate: {
    type: Number,
    min: [0, 'Tỷ giá không được âm'],
    default: 1
  },
  fees: {
    gatewayFee: {
      type: Number,
      min: [0, 'Phí gateway không được âm'],
      default: 0
    },
    processingFee: {
      type: Number,
      min: [0, 'Phí xử lý không được âm'],
      default: 0
    },
    totalFees: {
      type: Number,
      min: [0, 'Tổng phí không được âm'],
      default: 0
    }
  },
  refundDetails: {
    refundAmount: {
      type: Number,
      min: [0, 'Số tiền hoàn không được âm'],
      default: 0
    },
    refundDate: {
      type: Date
    },
    refundReason: {
      type: String,
      trim: true,
      maxlength: [200, 'Lý do hoàn tiền không được vượt quá 200 ký tự']
    },
    refundTransactionId: {
      type: String,
      trim: true
    }
  },
  notes: {
    type: String,
    trim: true,
    maxlength: [500, 'Ghi chú không được vượt quá 500 ký tự']
  },
  processedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  processedAt: {
    type: Date
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for payment status in Vietnamese
paymentSchema.virtual('statusText').get(function() {
  const statusMap = {
    'pending': 'Chờ thanh toán',
    'processing': 'Đang xử lý',
    'completed': 'Thanh toán thành công',
    'failed': 'Thanh toán thất bại',
    'cancelled': 'Đã hủy',
    'refunded': 'Đã hoàn tiền'
  };
  return statusMap[this.paymentStatus] || this.paymentStatus;
});

// Virtual for net amount (amount - fees)
paymentSchema.virtual('netAmount').get(function() {
  return this.amount - this.fees.totalFees;
});

// Virtual for is successful
paymentSchema.virtual('isSuccessful').get(function() {
  return this.paymentStatus === 'completed';
});

// Virtual for is refunded
paymentSchema.virtual('isRefunded').get(function() {
  return this.paymentStatus === 'refunded';
});

// Index for better performance
paymentSchema.index({ order: 1 });
paymentSchema.index({ user: 1, paymentDate: -1 });
paymentSchema.index({ paymentStatus: 1, paymentDate: -1 });
paymentSchema.index({ transactionId: 1 });
paymentSchema.index({ gatewayTransactionId: 1 });
paymentSchema.index({ paymentDate: -1 });

// Pre-save middleware to calculate total fees
paymentSchema.pre('save', function(next) {
  this.fees.totalFees = this.fees.gatewayFee + this.fees.processingFee;
  next();
});

// Method to mark as completed
paymentSchema.methods.markAsCompleted = function(gatewayResponse = {}) {
  this.paymentStatus = 'completed';
  this.gatewayResponse = gatewayResponse;
  this.processedAt = new Date();
  return this.save();
};

// Method to mark as failed
paymentSchema.methods.markAsFailed = function(gatewayResponse = {}) {
  this.paymentStatus = 'failed';
  this.gatewayResponse = gatewayResponse;
  this.processedAt = new Date();
  return this.save();
};

// Method to process refund
paymentSchema.methods.processRefund = function(refundAmount, reason = '', transactionId = '') {
  if (this.paymentStatus !== 'completed') {
    throw new Error('Chỉ có thể hoàn tiền cho thanh toán đã thành công');
  }
  
  if (refundAmount > this.amount) {
    throw new Error('Số tiền hoàn không được vượt quá số tiền thanh toán');
  }
  
  this.paymentStatus = 'refunded';
  this.refundDetails.refundAmount = refundAmount;
  this.refundDetails.refundDate = new Date();
  this.refundDetails.refundReason = reason;
  this.refundDetails.refundTransactionId = transactionId;
  
  return this.save();
};

// Method to cancel payment
paymentSchema.methods.cancel = function() {
  if (this.paymentStatus === 'completed') {
    throw new Error('Không thể hủy thanh toán đã thành công');
  }
  
  this.paymentStatus = 'cancelled';
  this.processedAt = new Date();
  return this.save();
};

module.exports = mongoose.model('Payment', paymentSchema);
