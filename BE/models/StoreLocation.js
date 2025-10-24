const mongoose = require('mongoose');

const storeLocationSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Tên cửa hàng là bắt buộc'],
    trim: true,
    maxlength: [100, 'Tên cửa hàng không được vượt quá 100 ký tự']
  },
  latitude: {
    type: Number,
    required: [true, 'Vĩ độ là bắt buộc'],
    min: [-90, 'Vĩ độ không hợp lệ'],
    max: [90, 'Vĩ độ không hợp lệ']
  },
  longitude: {
    type: Number,
    required: [true, 'Kinh độ là bắt buộc'],
    min: [-180, 'Kinh độ không hợp lệ'],
    max: [180, 'Kinh độ không hợp lệ']
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
  },
  phone: {
    type: String,
    trim: true,
    match: [/^[0-9]{10,11}$/, 'Số điện thoại không hợp lệ']
  },
  email: {
    type: String,
    trim: true,
    lowercase: true,
    match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Email không hợp lệ']
  },
  description: {
    type: String,
    trim: true,
    maxlength: [500, 'Mô tả không được vượt quá 500 ký tự']
  },
  storeType: {
    type: String,
    enum: ['main', 'branch', 'warehouse', 'service_center'],
    default: 'branch'
  },
  isActive: {
    type: Boolean,
    default: true
  },
  operatingHours: {
    monday: {
      open: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ mở cửa không hợp lệ']
      },
      close: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ đóng cửa không hợp lệ']
      },
      isOpen: {
        type: Boolean,
        default: true
      }
    },
    tuesday: {
      open: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ mở cửa không hợp lệ']
      },
      close: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ đóng cửa không hợp lệ']
      },
      isOpen: {
        type: Boolean,
        default: true
      }
    },
    wednesday: {
      open: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ mở cửa không hợp lệ']
      },
      close: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ đóng cửa không hợp lệ']
      },
      isOpen: {
        type: Boolean,
        default: true
      }
    },
    thursday: {
      open: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ mở cửa không hợp lệ']
      },
      close: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ đóng cửa không hợp lệ']
      },
      isOpen: {
        type: Boolean,
        default: true
      }
    },
    friday: {
      open: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ mở cửa không hợp lệ']
      },
      close: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ đóng cửa không hợp lệ']
      },
      isOpen: {
        type: Boolean,
        default: true
      }
    },
    saturday: {
      open: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ mở cửa không hợp lệ']
      },
      close: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ đóng cửa không hợp lệ']
      },
      isOpen: {
        type: Boolean,
        default: true
      }
    },
    sunday: {
      open: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ mở cửa không hợp lệ']
      },
      close: {
        type: String,
        trim: true,
        match: [/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Giờ đóng cửa không hợp lệ']
      },
      isOpen: {
        type: Boolean,
        default: true
      }
    }
  },
  services: [{
    type: String,
    enum: ['sales', 'repair', 'maintenance', 'consultation', 'delivery', 'pickup'],
    trim: true
  }],
  amenities: [{
    type: String,
    enum: ['parking', 'wifi', 'restroom', 'waiting_area', 'test_ride', 'coffee'],
    trim: true
  }],
  images: [{
    url: {
      type: String,
      required: true,
      trim: true
    },
    alt: {
      type: String,
      trim: true
    },
    isMain: {
      type: Boolean,
      default: false
    }
  }],
  manager: {
    name: {
      type: String,
      trim: true
    },
    phone: {
      type: String,
      trim: true,
      match: [/^[0-9]{10,11}$/, 'Số điện thoại không hợp lệ']
    },
    email: {
      type: String,
      trim: true,
      lowercase: true,
      match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Email không hợp lệ']
    }
  },
  capacity: {
    maxCustomers: {
      type: Number,
      min: [1, 'Số khách hàng tối đa phải lớn hơn 0']
    },
    maxProducts: {
      type: Number,
      min: [1, 'Số sản phẩm tối đa phải lớn hơn 0']
    }
  },
  rating: {
    average: {
      type: Number,
      min: 0,
      max: 5,
      default: 0
    },
    count: {
      type: Number,
      default: 0
    }
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for full address
storeLocationSchema.virtual('fullAddress').get(function() {
  const parts = [this.address];
  if (this.ward) parts.push(this.ward);
  if (this.district) parts.push(this.district);
  if (this.city) parts.push(this.city);
  return parts.join(', ');
});

// Virtual for is open now
storeLocationSchema.virtual('isOpenNow').get(function() {
  const now = new Date();
  const dayNames = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
  const dayName = dayNames[now.getDay()];
  const currentTime = now.toTimeString().substring(0, 5);
  
  const daySchedule = this.operatingHours[dayName];
  if (!daySchedule || !daySchedule.isOpen) return false;
  
  return currentTime >= daySchedule.open && currentTime <= daySchedule.close;
});

// Virtual for distance (requires calculation with user location)
storeLocationSchema.virtual('distance').get(function() {
  // This would be calculated based on user's current location
  return null;
});

// Index for better performance
storeLocationSchema.index({ latitude: 1, longitude: 1 });
storeLocationSchema.index({ city: 1, district: 1 });
storeLocationSchema.index({ isActive: 1, storeType: 1 });
storeLocationSchema.index({ 'rating.average': -1 });

// 2dsphere index for geospatial queries
storeLocationSchema.index({ location: '2dsphere' });

// Pre-save middleware to create location field for geospatial queries
storeLocationSchema.pre('save', function(next) {
  this.location = {
    type: 'Point',
    coordinates: [this.longitude, this.latitude]
  };
  next();
});

// Method to calculate distance from a point
storeLocationSchema.methods.calculateDistance = function(lat, lng) {
  const R = 6371; // Earth's radius in kilometers
  const dLat = (lat - this.latitude) * Math.PI / 180;
  const dLng = (lng - this.longitude) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(this.latitude * Math.PI / 180) * Math.cos(lat * Math.PI / 180) *
    Math.sin(dLng/2) * Math.sin(dLng/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c; // Distance in kilometers
};

// Method to get operating hours for a specific day
storeLocationSchema.methods.getOperatingHours = function(dayName) {
  return this.operatingHours[dayName] || null;
};

// Method to check if store is open on a specific day and time
storeLocationSchema.methods.isOpenAt = function(dayName, time) {
  const daySchedule = this.operatingHours[dayName];
  if (!daySchedule || !daySchedule.isOpen) return false;
  
  return time >= daySchedule.open && time <= daySchedule.close;
};

// Static method to find stores near a location
storeLocationSchema.statics.findNearby = function(lat, lng, maxDistance = 10) {
  return this.find({
    location: {
      $near: {
        $geometry: {
          type: 'Point',
          coordinates: [lng, lat]
        },
        $maxDistance: maxDistance * 1000 // Convert to meters
      }
    },
    isActive: true
  });
};

// Static method to get active stores
storeLocationSchema.statics.getActiveStores = function() {
  return this.find({ isActive: true }).sort({ 'rating.average': -1 });
};

module.exports = mongoose.model('StoreLocation', storeLocationSchema);
