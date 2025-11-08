const express = require('express');
const router = express.Router();
const {
  createPaymentLink,
  handleWebhook,
  verifyPayment,
  cancelPaymentLink,
  confirmPayment,
  getPendingPayments
} = require('../controllers/payosController');
const { protect } = require('../middleware/auth');

// Create payment link for order
router.post('/orders/:orderId/create-link', protect, createPaymentLink);

// Verify payment status
router.get('/orders/:orderId/verify', protect, verifyPayment);

// Cancel payment link
router.post('/orders/:orderId/cancel-link', protect, cancelPaymentLink);

// Confirm payment success (from frontend after payment)
router.post('/orders/:orderId/confirm-payment', protect, confirmPayment);

// Get pending payments for user (for notifications)
router.get('/users/:userId/pending-payments', protect, getPendingPayments);

// Webhook endpoint (no auth required - PayOS will call this)
// PayOS gửi JSON body, đã được parse bởi express.json() middleware ở server.js
// Body đã là object rồi, không cần parse lại
router.post('/webhook', handleWebhook);

module.exports = router;

