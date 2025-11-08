const express = require('express');
const router = express.Router();
const {
  createOrders,
  getUserOrders,
  getOrderDetails,
  updateOrderStatus,
  cancelOrder,
  getOrdersByStore,
  estimateShipping,
  getTotalRevenue
} = require('../controllers/orderController');
const { protect, authorize } = require('../middleware/auth');

// Create orders from cart (split by store)
router.post('/create', protect, createOrders);

// Estimate shipping fee and distance without creating orders
router.post('/estimate', protect, estimateShipping);

// Get user orders
router.get('/user/:userId', protect, getUserOrders);

// Get orders by store
router.get('/store/:storeId', protect, getOrdersByStore);

// Get total revenue (admin only) - must be before /:orderId
router.get('/revenue/total', protect, authorize('admin'), getTotalRevenue);

// Get orders by day of week (admin only) - must be before /:orderId
router.get('/by-day-of-week', protect, authorize('admin'), require('../controllers/orderController').getOrdersByDayOfWeek);

// Get all orders (admin only) - must be before /:orderId
router.get('/', protect, authorize('admin'), require('../controllers/orderController').getAllOrders);

// Get order details - must be last to avoid matching other routes
router.get('/:orderId', protect, getOrderDetails);

// Update order status
router.put('/:orderId/status', protect, updateOrderStatus);

// Cancel order
router.post('/:orderId/cancel', protect, cancelOrder);

module.exports = router;
