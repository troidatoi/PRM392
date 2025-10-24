const express = require('express');
const router = express.Router();
const {
  createOrders,
  getUserOrders,
  getOrderDetails,
  updateOrderStatus,
  cancelOrder,
  getOrdersByStore
} = require('../controllers/orderController');
const { protect } = require('../middleware/auth');

// Create orders from cart (split by store)
router.post('/create', protect, createOrders);

// Get user orders
router.get('/user/:userId', protect, getUserOrders);

// Get order details
router.get('/:orderId', protect, getOrderDetails);

// Update order status
router.put('/:orderId/status', protect, updateOrderStatus);

// Cancel order
router.post('/:orderId/cancel', protect, cancelOrder);

// Get orders by store
router.get('/store/:storeId', protect, getOrdersByStore);

module.exports = router;
