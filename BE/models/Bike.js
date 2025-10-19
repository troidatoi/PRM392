const mongoose = require('mongoose');

const bikeSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Tên xe đạp điện là bắt buộc'],
    trim: true,
    maxlength: [100, 'Tên xe đạp điện không được vượt quá 100 ký tự']
  },
  brand: {
    type: String,
    required: [true, 'Thương hiệu là bắt buộc'],
    trim: true,
    maxlength: [50, 'Thương hiệu không được vượt quá 50 ký tự']
  },
  model: {
    type: String,
    required: [true, 'Model là bắt buộc'],
    trim: true,
    maxlength: [50, 'Model không được vượt quá 50 ký tự']
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
  description: {
    type: String,
    required: [true, 'Mô tả là bắt buộc'],
    trim: true,
    maxlength: [1000, 'Mô tả không được vượt quá 1000 ký tự']
  },
  specifications: {
    battery: {
      type: String,
      trim: true
    },
    motor: {
      type: String,
      trim: true
    },
    range: {
      type: String,
      trim: true
    },
    maxSpeed: {
      type: String,
      trim: true
    },
    weight: {
      type: String,
      trim: true
    },
    chargingTime: {
      type: String,
      trim: true
    }
  },
  images: [{
    url: {
      type: String,
      required: true
    },
    alt: {
      type: String,
      default: ''
    }
  }],
  colors: [{
    name: {
      type: String,
      required: true
    },
    hex: {
      type: String,
      required: true
    }
  }],
  category: {
    type: String,
    required: [true, 'Danh mục là bắt buộc'],
    enum: ['city', 'mountain', 'folding', 'cargo', 'sport', 'other'],
    default: 'city'
  },
  status: {
    type: String,
    enum: ['available', 'out_of_stock', 'discontinued'],
    default: 'available'
  },
  stock: {
    type: Number,
    default: 0,
    min: [0, 'Số lượng tồn kho không được âm']
  },
  features: [{
    type: String,
    trim: true
  }],
  warranty: {
    type: String,
    trim: true,
    default: '12 tháng'
  },
  rating: {
    average: {
      type: Number,
      default: 0,
      min: 0,
      max: 5
    },
    count: {
      type: Number,
      default: 0
    }
  },
  isFeatured: {
    type: Boolean,
    default: false
  },
  isNew: {
    type: Boolean,
    default: true
  },
  tags: [{
    type: String,
    trim: true
  }]
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for discount percentage
bikeSchema.virtual('discountPercentage').get(function() {
  if (this.originalPrice && this.originalPrice > this.price) {
    return Math.round(((this.originalPrice - this.price) / this.originalPrice) * 100);
  }
  return 0;
});

// Index for better search performance
bikeSchema.index({ name: 'text', brand: 'text', model: 'text', description: 'text' });
bikeSchema.index({ category: 1, status: 1 });
bikeSchema.index({ price: 1 });
bikeSchema.index({ 'rating.average': -1 });

// Pre-save middleware
bikeSchema.pre('save', function(next) {
  // Auto-generate tags from name, brand, and category
  if (this.isModified('name') || this.isModified('brand') || this.isModified('category')) {
    this.tags = [
      ...this.name.toLowerCase().split(' '),
      this.brand.toLowerCase(),
      this.category.toLowerCase()
    ].filter(tag => tag.length > 2);
  }
  next();
});

module.exports = mongoose.model('Bike', bikeSchema);


