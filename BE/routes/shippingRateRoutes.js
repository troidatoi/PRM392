const express = require('express');
const router = express.Router();
const {
  getAllShippingRates,
  getShippingRateById,
  createShippingRate,
  updateShippingRate,
  deleteShippingRate,
  calculateShippingFee
} = require('../controllers/shippingRateController');

// Public routes
router.get('/', getAllShippingRates);
router.get('/:id', getShippingRateById);
router.post('/calculate', calculateShippingFee);

// Admin routes (can add auth middleware later)
router.post('/', createShippingRate);
router.put('/:id', updateShippingRate);
router.delete('/:id', deleteShippingRate);

module.exports = router;

