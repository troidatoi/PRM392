// Simple distance and shipping fee utilities
const ShippingRate = require('../models/ShippingRate');

/**
 * Calculate Haversine distance between two coordinates (km)
 * @param {number} lat1
 * @param {number} lon1
 * @param {number} lat2
 * @param {number} lon2
 * @returns {number} distance in kilometers
 */
const haversineKm = (lat1, lon1, lat2, lon2) => {
  const toRad = (deg) => (deg * Math.PI) / 180;
  const R = 6371; // km
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

/**
 * Calculate shipping fee based on distance using DB shipping rates
 * Falls back to env config if no rates in DB
 * @param {number} distanceKm
 * @returns {Promise<number>} fee in VND
 */
const calculateShippingFee = async (distanceKm) => {
  try {
    // Try to use DB rates first
    const fee = await ShippingRate.calculateFee(distanceKm);
    if (fee > 0) {
      return fee;
    }
  } catch (error) {
    console.error('Error calculating shipping fee from DB:', error);
    // Fall through to env-based calculation
  }
  
  // Fallback to env-based calculation
  const perKm = parseInt(process.env.SHIPPING_FEE_PER_KM || '5000', 10);
  const minFee = parseInt(process.env.MIN_SHIPPING_FEE || '0', 10);
  const roundUp = (process.env.ROUND_DISTANCE_UP || 'true').toLowerCase() !== 'false';
  const effectiveDistance = roundUp ? Math.ceil(distanceKm) : Number(distanceKm.toFixed(1));
  const fee = effectiveDistance * perKm;
  return Math.max(minFee, fee);
};

module.exports = {
  haversineKm,
  calculateShippingFee
};


