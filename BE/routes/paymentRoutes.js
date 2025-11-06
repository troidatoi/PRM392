const express = require('express');
const router = express.Router();
const {
  createPaymentLink,
  handlePayOSWebhook,
  getPaymentInfo,
  getPaymentByOrder,
  cancelPaymentLink,
  verifyPayment
} = require('../controllers/paymentController');
const { protect, authorize } = require('../middleware/auth');

// Webhook từ PayOS (không cần authentication)
// Route alias cho BACKEND_WEBHOOK_URL (dùng cho PayOS settings)
router.post('/webhook', handlePayOSWebhook);
router.post('/webhook/payos', handlePayOSWebhook);

// Tạo payment link cho đơn hàng
router.post('/order/:orderId/create-link', protect, createPaymentLink);

// Xác minh thanh toán (khi user return từ PayOS)
router.get('/verify', verifyPayment);

// Lấy thông tin thanh toán theo ID
router.get('/:paymentId', protect, getPaymentInfo);

// Lấy thông tin thanh toán theo order
router.get('/order/:orderId', protect, getPaymentByOrder);

// Hủy payment link
router.post('/:paymentId/cancel', protect, cancelPaymentLink);

module.exports = router;

