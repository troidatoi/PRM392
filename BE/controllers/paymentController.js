const Payment = require('../models/Payment');
const Order = require('../models/Order');
const payosService = require('../utils/payosService');

/**
 * Tạo payment link với PayOS
 */
const createPaymentLink = async (req, res) => {
  try {
    const { orderId } = req.params;
    const { returnUrl, cancelUrl } = req.body;

    // Validate order exists
    const order = await Order.findById(orderId)
      .populate('user', 'email username phoneNumber')
      .populate('store', 'name');

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Đơn hàng không tồn tại'
      });
    }

    // Check if order already has a payment
    let payment = await Payment.findOne({ order: orderId });

    // If payment doesn't exist, create one
    if (!payment) {
      payment = new Payment({
        order: orderId,
        user: order.user._id,
        amount: order.finalAmount,
        paymentStatus: 'pending',
        paymentMethod: 'payos',
        paymentGateway: 'PayOS',
        currency: 'VND'
      });
      await payment.save();
    }

    // Check if payment is already completed
    if (payment.paymentStatus === 'completed') {
      return res.status(400).json({
        success: false,
        message: 'Đơn hàng đã được thanh toán'
      });
    }

    // Generate unique order code for PayOS
    // PayOS requires orderCode to be a unique integer between 1 and 999999999999
    // Use timestamp + random number to ensure uniqueness
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 1000);
    const orderCode = parseInt(`${timestamp}${random}`.slice(-12)); // Max 12 digits

    // Prepare payment data for PayOS
    const paymentData = {
      amount: Math.round(order.finalAmount),
      orderCode: orderCode,
      description: `Thanh toán đơn hàng ${order.orderNumber}`,
      cancelUrl: cancelUrl || `${process.env.FRONTEND_URL}/payment/cancel?orderId=${orderId}`,
      returnUrl: returnUrl || `${process.env.FRONTEND_URL}/payment/success?orderId=${orderId}`,
      items: [],
      buyer: {
        name: order.shippingAddress?.fullName || order.user?.username || '',
        email: order.user?.email || '',
        phone: order.shippingAddress?.phone || order.user?.phoneNumber || ''
      }
    };

    // Create payment link via PayOS
    const payosResult = await payosService.createPaymentLink(paymentData);

    if (!payosResult.success) {
      return res.status(400).json({
        success: false,
        message: 'Không thể tạo link thanh toán',
        error: payosResult.error
      });
    }

    // Update payment record
    payment.gatewayTransactionId = payosResult.data.orderCode.toString();
    payment.gatewayResponse = payosResult.data;
    payment.paymentStatus = 'processing';
    await payment.save();

    res.json({
      success: true,
      message: 'Tạo link thanh toán thành công',
      data: {
        paymentId: payment._id,
        checkoutUrl: payosResult.data.checkoutUrl,
        qrCode: payosResult.data.qrCode,
        orderCode: payosResult.data.orderCode,
        amount: payosResult.data.amount,
        accountNumber: payosResult.data.accountNumber,
        accountName: payosResult.data.accountName
      }
    });
  } catch (error) {
    console.error('Create payment link error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

/**
 * Webhook handler cho PayOS
 */
const handlePayOSWebhook = async (req, res) => {
  try {
    const webhookData = req.body;
    const webhookChecksum = req.headers['x-payos-signature'] || req.headers['x-payos-signature-256'];

    // Verify webhook signature
    if (!payosService.verifyWebhook(webhookData, webhookChecksum)) {
      console.error('Invalid webhook signature');
      return res.status(400).json({
        code: -1,
        desc: 'Invalid signature',
        data: null
      });
    }

    const { code, data } = webhookData;

    if (code !== 0 || !data) {
      return res.status(400).json({
        code: -1,
        desc: 'Invalid webhook data',
        data: null
      });
    }

    const { orderCode, status, amount, transactionDateTime } = data;

    // Find payment by order code
    const payment = await Payment.findOne({
      gatewayTransactionId: orderCode.toString()
    }).populate('order');

    if (!payment) {
      console.error(`Payment not found for orderCode: ${orderCode}`);
      return res.status(404).json({
        code: -1,
        desc: 'Payment not found',
        data: null
      });
    }

    // Update payment status based on webhook
    if (status === 'PAID') {
      payment.paymentStatus = 'completed';
      payment.paymentDate = new Date(transactionDateTime || Date.now());
      payment.gatewayResponse = data;
      
      // Update order status
      if (payment.order) {
        payment.order.orderStatus = 'confirmed';
        await payment.order.save();
      }
    } else if (status === 'CANCELLED') {
      payment.paymentStatus = 'cancelled';
      payment.gatewayResponse = data;
    } else if (status === 'EXPIRED') {
      payment.paymentStatus = 'failed';
      payment.gatewayResponse = data;
    }

    await payment.save();

    res.json({
      code: 0,
      desc: 'Success',
      data: null
    });
  } catch (error) {
    console.error('PayOS webhook error:', error);
    res.status(500).json({
      code: -1,
      desc: 'Server error',
      data: null
    });
  }
};

/**
 * Lấy thông tin thanh toán
 */
const getPaymentInfo = async (req, res) => {
  try {
    const { paymentId } = req.params;

    const payment = await Payment.findById(paymentId)
      .populate('order', 'orderNumber orderStatus totalAmount finalAmount')
      .populate('user', 'username email');

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Thanh toán không tồn tại'
      });
    }

    // If payment has gateway transaction ID, get latest info from PayOS
    if (payment.gatewayTransactionId && payment.paymentMethod === 'payos') {
      const payosInfo = await payosService.getPaymentInfo(
        parseInt(payment.gatewayTransactionId)
      );

      if (payosInfo.success) {
        // Update payment status if changed
        if (payosInfo.data.status === 'PAID' && payment.paymentStatus !== 'completed') {
          payment.paymentStatus = 'completed';
          payment.paymentDate = new Date(payosInfo.data.transactionDateTime || Date.now());
          await payment.save();

          // Update order status
          if (payment.order) {
            payment.order.orderStatus = 'confirmed';
            await payment.order.save();
          }
        }

        payment.gatewayResponse = payosInfo.data;
      }
    }

    res.json({
      success: true,
      data: payment
    });
  } catch (error) {
    console.error('Get payment info error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

/**
 * Lấy thông tin thanh toán theo order
 */
const getPaymentByOrder = async (req, res) => {
  try {
    const { orderId } = req.params;

    const payment = await Payment.findOne({ order: orderId })
      .populate('order', 'orderNumber orderStatus totalAmount finalAmount')
      .populate('user', 'username email');

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Thanh toán không tồn tại cho đơn hàng này'
      });
    }

    res.json({
      success: true,
      data: payment
    });
  } catch (error) {
    console.error('Get payment by order error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

/**
 * Hủy payment link
 */
const cancelPaymentLink = async (req, res) => {
  try {
    const { paymentId } = req.params;
    const { reason } = req.body;

    const payment = await Payment.findById(paymentId);

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Thanh toán không tồn tại'
      });
    }

    if (payment.paymentStatus === 'completed') {
      return res.status(400).json({
        success: false,
        message: 'Không thể hủy thanh toán đã thành công'
      });
    }

    if (payment.gatewayTransactionId && payment.paymentMethod === 'payos') {
      const cancelResult = await payosService.cancelPaymentLink(
        parseInt(payment.gatewayTransactionId),
        reason || 'Hủy thanh toán'
      );

      if (!cancelResult.success) {
        return res.status(400).json({
          success: false,
          message: 'Không thể hủy link thanh toán',
          error: cancelResult.error
        });
      }
    }

    payment.paymentStatus = 'cancelled';
    await payment.save();

    res.json({
      success: true,
      message: 'Hủy link thanh toán thành công',
      data: payment
    });
  } catch (error) {
    console.error('Cancel payment link error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

/**
 * Xác minh thanh toán (khi user return từ PayOS)
 */
const verifyPayment = async (req, res) => {
  try {
    const { orderCode } = req.query;

    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu mã đơn hàng'
      });
    }

    // Get payment info from PayOS
    const payosInfo = await payosService.getPaymentInfo(parseInt(orderCode));

    if (!payosInfo.success) {
      return res.status(400).json({
        success: false,
        message: 'Không thể xác minh thanh toán',
        error: payosInfo.error
      });
    }

    // Find payment in database
    const payment = await Payment.findOne({
      gatewayTransactionId: orderCode.toString()
    }).populate('order');

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Thanh toán không tồn tại'
      });
    }

    // Update payment status if paid
    if (payosInfo.data.status === 'PAID' && payment.paymentStatus !== 'completed') {
      payment.paymentStatus = 'completed';
      payment.paymentDate = new Date(payosInfo.data.transactionDateTime || Date.now());
      payment.gatewayResponse = payosInfo.data;
      await payment.save();

      // Update order status
      if (payment.order) {
        payment.order.orderStatus = 'confirmed';
        await payment.order.save();
      }
    }

    res.json({
      success: true,
      message: 'Xác minh thanh toán thành công',
      data: {
        payment: payment,
        payosStatus: payosInfo.data.status,
        isPaid: payosInfo.data.status === 'PAID'
      }
    });
  } catch (error) {
    console.error('Verify payment error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

module.exports = {
  createPaymentLink,
  handlePayOSWebhook,
  getPaymentInfo,
  getPaymentByOrder,
  cancelPaymentLink,
  verifyPayment
};

