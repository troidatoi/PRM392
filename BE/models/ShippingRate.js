const mongoose = require('mongoose');

const shippingRateSchema = new mongoose.Schema({
  minDistance: {
    type: Number,
    required: [true, 'Khoảng cách tối thiểu là bắt buộc'],
    min: [0, 'Khoảng cách tối thiểu không được âm']
  },
  maxDistance: {
    type: Number,
    required: false,
    default: null,
    min: [0, 'Khoảng cách tối đa không được âm'],
    validate: {
      validator: function(value) {
        return value === null || value === undefined || value > this.minDistance;
      },
      message: 'Khoảng cách tối đa phải lớn hơn khoảng cách tối thiểu'
    }
  },
  pricePerKm: {
    type: Number,
    required: [true, 'Đơn giá mỗi km là bắt buộc'],
    min: [0, 'Đơn giá không được âm']
  },
  note: {
    type: String,
    trim: true,
    maxlength: [255, 'Ghi chú không được vượt quá 255 ký tự']
  },
  isActive: {
    type: Boolean,
    default: true
  },
  order: {
    type: Number,
    required: true,
    default: 0
  }
}, {
  timestamps: true
});

// Index for efficient querying
shippingRateSchema.index({ minDistance: 1, maxDistance: 1 });
shippingRateSchema.index({ isActive: 1, order: 1 });

// Static method to get active rates sorted by distance
shippingRateSchema.statics.getActiveRates = function() {
  return this.find({ isActive: true }).sort({ minDistance: 1 });
};

// Method to calculate shipping fee based on distance (tiered pricing)
// Calculates fee for each segment of the distance
// Example: 30km = (0-3km: 3×5000) + (3-10km: 7×3000) + (10-30km: 20×2000) = 76000₫
shippingRateSchema.statics.calculateFee = async function(distanceKm) {
  const rates = await this.getActiveRates();
  
  if (rates.length === 0) {
    return 0;
  }
  
  // Round up distance to next km
  const roundedDistance = Math.ceil(distanceKm);
  let totalFee = 0;
  let remainingDistance = roundedDistance;
  
  // Calculate fee for each segment
  for (let i = 0; i < rates.length; i++) {
    if (remainingDistance <= 0) break;
    
    const rate = rates[i];
    const maxDist = rate.maxDistance === null ? Infinity : rate.maxDistance;
    
    // Determine segment boundaries
    let segmentStart = rate.minDistance === 0 ? 0 : rate.minDistance;
    let segmentEnd = maxDist;
    
    // Check if this segment applies
    if (roundedDistance <= segmentStart) {
      continue; // Skip segments that are beyond the distance
    }
    
    // Calculate the distance covered in this segment
    const actualEnd = Math.min(roundedDistance, segmentEnd);
    const segmentDistance = actualEnd - segmentStart;
    
    if (segmentDistance > 0) {
      totalFee += segmentDistance * rate.pricePerKm;
      remainingDistance -= segmentDistance;
    }
    
    // If this is the last rate with no maxDistance, apply to any remaining distance
    if (rate.maxDistance === null && remainingDistance > 0) {
      totalFee += remainingDistance * rate.pricePerKm;
      break;
    }
  }
  
  return totalFee;
};

module.exports = mongoose.model('ShippingRate', shippingRateSchema);

