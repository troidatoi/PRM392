const mongoose = require('mongoose');

const storeSchema = new mongoose.Schema({
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
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for full address
storeSchema.virtual('fullAddress').get(function() {
  return `${this.address}, ${this.city}`;
});

// Virtual for is open now
storeSchema.virtual('isOpenNow').get(function() {
  if (!this.operatingHours) {
    return !!this.isActive;
  }

  // Lấy thời gian hiện tại theo timezone Việt Nam (GMT+7)
  const now = new Date();
  const vietnamOffset = 7 * 60; // GMT+7 in minutes
  const utc = now.getTime() + (now.getTimezoneOffset() * 60000);
  const vietnamTime = new Date(utc + (vietnamOffset * 60000));
  
  const dayNames = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
  const dayName = dayNames[vietnamTime.getDay()];
  
  const daySchedule = this.operatingHours[dayName];
  if (!daySchedule || !daySchedule.isOpen) {
    return false;
  }

  const openTime = daySchedule.open;
  const closeTime = daySchedule.close;
  if (!openTime || !closeTime) {
    return false;
  }

  // Chuyển đổi thời gian sang phút từ 00:00 để so sánh chính xác
  const timeToMinutes = (timeStr) => {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + minutes;
  };

  const currentMinutes = vietnamTime.getHours() * 60 + vietnamTime.getMinutes();
  const openMinutes = timeToMinutes(openTime);
  const closeMinutes = timeToMinutes(closeTime);

  // Xử lý trường hợp đóng cửa sau nửa đêm (ví dụ: 22:00 - 02:00)
  if (closeMinutes < openMinutes) {
    // Cửa hàng mở qua đêm
    return currentMinutes >= openMinutes || currentMinutes <= closeMinutes;
  }

  return currentMinutes >= openMinutes && currentMinutes <= closeMinutes;
});

// Index for better performance
storeSchema.index({ latitude: 1, longitude: 1 });
storeSchema.index({ city: 1 });
storeSchema.index({ isActive: 1 });

// 2dsphere index for geospatial queries
storeSchema.index({ location: '2dsphere' });

// Pre-save middleware to create location field for geospatial queries
storeSchema.pre('save', function(next) {
  this.location = {
    type: 'Point',
    coordinates: [this.longitude, this.latitude]
  };
  next();
});

// Method to calculate distance from a point
storeSchema.methods.calculateDistance = function(lat, lng) {
  const R = 6371; // Earth's radius in kilometers
  const dLat = (lat - this.latitude) * Math.PI / 180;
  const dLng = (lng - this.longitude) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(this.latitude * Math.PI / 180) * Math.cos(lat * Math.PI / 180) *
    Math.sin(dLng/2) * Math.sin(dLng/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c; // Distance in kilometers
};

// Static method to find stores near a location
storeSchema.statics.findNearby = function(lat, lng, maxDistance = 10) {
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
storeSchema.statics.getActiveStores = function() {
  return this.find({ isActive: true });
};

module.exports = mongoose.model('Store', storeSchema);
