const payOSService = require('../utils/payos');
const Payment = require('../models/Payment');
const Order = require('../models/Order');
const OrderDetail = require('../models/OrderDetail');

/**
 * Tạo payment link PayOS cho đơn hàng
 */
const createPaymentLink = async (req, res) => {
  try {
    const { orderId } = req.params;
    let { returnUrl, cancelUrl } = req.body;

    // Nếu không có returnUrl/cancelUrl, sử dụng custom scheme cho Android app
    // Android app sẽ intercept các URL này và xử lý trong WebView
    if (!returnUrl) {
      // Sử dụng custom scheme để app có thể intercept
      // Hoặc dùng URL với query parameter để WebView có thể detect
      returnUrl = `https://payos-payment-success.com/?orderId=${orderId}&status=success`;
    }
    
    // Cancel URL: redirect về app khi hủy thanh toán
    if (!cancelUrl) {
      cancelUrl = `https://payos-payment-cancel.com/?orderId=${orderId}&status=cancel`;
    }

    // Get order with user information
    const order = await Order.findById(orderId)
      .populate('user', 'email username phoneNumber')
      .populate('orderDetails', 'product quantity price totalPrice')
      .populate('orderDetails.product', 'name brand');

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Đơn hàng không tồn tại'
      });
    }

    // Check if order is already paid
    const existingPayment = await Payment.findOne({ order: orderId });
    if (existingPayment && existingPayment.paymentStatus === 'completed') {
      return res.status(400).json({
        success: false,
        message: 'Đơn hàng đã được thanh toán'
      });
    }

    // Check payment method
    if (order.paymentMethod !== 'payos') {
      return res.status(400).json({
        success: false,
        message: 'Đơn hàng không sử dụng phương thức thanh toán PayOS'
      });
    }

    // Prepare items for PayOS
    const items = order.orderDetails.map(detail => ({
      name: detail.product?.name || 'Sản phẩm',
      quantity: detail.quantity || 1,
      price: detail.price || 0
    }));

    // Get buyer information from order
    const buyerName = order.shippingAddress?.fullName || '';
    const buyerPhone = order.shippingAddress?.phone || '';
    const buyerEmail = order.user?.email || '';
    const buyerAddress = order.shippingAddress?.address || '';

    // Create payment link
    // PayOS yêu cầu orderCode là số nguyên dương và unique
    // Sử dụng timestamp + random để đảm bảo unique
    const orderCode = parseInt(Date.now().toString().slice(-9) + Math.floor(Math.random() * 1000).toString().padStart(3, '0'));
    
    // Description - giới hạn 9 ký tự cho tài khoản không liên kết qua PayOS
    // Nhưng nếu đã liên kết thì có thể dài hơn
    const description = `Đơn ${order.orderNumber}`.substring(0, 100); // Giới hạn 100 ký tự để an toàn
    
    const paymentLinkData = await payOSService.createPaymentLink({
      orderCode: orderCode,
      amount: Math.round(order.finalAmount), // Đảm bảo là số nguyên
      description: description,
      returnUrl: returnUrl,
      cancelUrl: cancelUrl,
      items: items,
      buyerName: buyerName,
      buyerPhone: buyerPhone,
      buyerEmail: buyerEmail,
      buyerAddress: buyerAddress
    });

    // Create or update Payment record
    let payment = existingPayment;
    if (!payment) {
      payment = new Payment({
        order: orderId,
        user: order.user,
        amount: order.finalAmount,
        paymentMethod: 'payos',
        paymentGateway: 'payos',
        paymentStatus: 'pending',
        transactionId: orderCode.toString(),
        currency: 'VND'
      });
    } else {
      payment.paymentStatus = 'pending';
      payment.gatewayTransactionId = paymentLinkData.data.orderCode?.toString();
      payment.gatewayResponse = paymentLinkData.data;
    }

    await payment.save();

    res.json({
      success: true,
      message: 'Tạo payment link thành công',
      data: {
        paymentLink: paymentLinkData.data,
        paymentId: payment._id,
        orderId: order._id
      }
    });
  } catch (error) {
    console.error('Create payment link error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi khi tạo payment link'
    });
  }
};

/**
 * Webhook handler từ PayOS
 */
const handleWebhook = async (req, res) => {
  try {
    const webhookData = req.body;
    
    // PayOS có thể gửi signature trong header hoặc body
    // Kiểm tra header trước
    const signatureFromHeader = req.headers['x-payos-signature'] || req.headers['x-signature'];
    const signature = signatureFromHeader || webhookData.signature;

    // Log để debug
    console.log('Webhook received:', {
      hasBody: !!webhookData,
      hasSignature: !!signature,
      signatureSource: signatureFromHeader ? 'header' : 'body',
      bodyKeys: Object.keys(webhookData)
    });

    // Verify webhook signature
    // Body đã được parse bởi express.json() middleware
    const isValid = payOSService.verifyWebhook(webhookData, signature);
    if (!isValid) {
      console.error('Invalid webhook signature', {
        receivedSignature: signature,
        webhookData: JSON.stringify(webhookData, null, 2)
      });
      return res.status(400).json({
        success: false,
        message: 'Invalid webhook signature'
      });
    }

    const { code, desc, data } = webhookData;

    // Find payment by orderCode
    const orderCode = data?.orderCode?.toString();
    if (!orderCode) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu orderCode trong webhook data'
      });
    }

    // Tìm Payment bằng nhiều cách:
    // 1. Tìm bằng transactionId (orderCode từ PayOS)
    // 2. Tìm bằng gatewayTransactionId
    // 3. Tìm thông qua Order nếu có orderNumber trong data
    let payment = await Payment.findOne({ 
      $or: [
        { transactionId: orderCode },
        { gatewayTransactionId: orderCode }
      ]
    }).populate('order');

    // Nếu không tìm thấy, thử tìm qua Order nếu có thông tin order
    if (!payment && data?.reference) {
      // PayOS có thể gửi reference là orderNumber
      const order = await Order.findOne({ orderNumber: data.reference });
      if (order) {
        payment = await Payment.findOne({ order: order._id }).populate('order');
      }
    }

    // Nếu vẫn không tìm thấy, log để debug
    if (!payment) {
      console.error(`Payment not found for orderCode: ${orderCode}`, {
        webhookData: JSON.stringify(webhookData, null, 2),
        searchAttempts: {
          transactionId: orderCode,
          gatewayTransactionId: orderCode,
          reference: data?.reference
        }
      });
      
      // Vẫn trả về success để PayOS không retry
      // Nhưng log để admin biết
      return res.json({
        success: true,
        message: 'Webhook received but payment not found',
        warning: `Payment not found for orderCode: ${orderCode}. This might be a test webhook or payment was not created.`
      });
    }

    // Update payment status based on webhook code
    if (code === '00') {
      // Payment successful
      payment.paymentStatus = 'completed';
      payment.gatewayResponse = data;
      payment.processedAt = new Date();
      await payment.save();

      // Chuyển order status từ awaiting_payment sang pending khi payment thành công
      if (payment.order && payment.order.orderStatus === 'awaiting_payment') {
        payment.order.orderStatus = 'pending';
        await payment.order.save();
        console.log(`Order ${payment.order._id} status updated from awaiting_payment to pending`);
      }

      console.log(`Payment completed for orderCode: ${orderCode}`);
    } else {
      // Payment failed or cancelled
      payment.paymentStatus = 'failed';
      payment.gatewayResponse = { code, desc, data };
      payment.processedAt = new Date();
      await payment.save();

      console.log(`Payment failed for orderCode: ${orderCode}, reason: ${desc}`);
    }

    // Return success to PayOS
    res.json({
      success: true,
      message: 'Webhook processed successfully'
    });
  } catch (error) {
    console.error('Webhook handler error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi khi xử lý webhook'
    });
  }
};

/**
 * Verify payment status
 */
const verifyPayment = async (req, res) => {
  try {
    const { orderId } = req.params;

    // Get payment
    const payment = await Payment.findOne({ order: orderId })
      .populate('order');

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy payment'
      });
    }

    // If payment is already completed, return status
    if (payment.paymentStatus === 'completed') {
      return res.json({
        success: true,
        data: {
          paymentStatus: payment.paymentStatus,
          orderStatus: payment.order?.orderStatus,
          payment
        }
      });
    }

    // Get payment link info from PayOS
    if (payment.transactionId) {
      try {
        const orderCode = parseInt(payment.transactionId);
        const paymentLinkInfo = await payOSService.getPaymentLinkInfo(orderCode);

        if (paymentLinkInfo.success && paymentLinkInfo.data) {
          const payOSData = paymentLinkInfo.data;

          // Update payment status if changed
          if (payOSData.status === 'PAID' && payment.paymentStatus !== 'completed') {
            payment.paymentStatus = 'completed';
            payment.gatewayResponse = payOSData;
            payment.processedAt = new Date();
            await payment.save();

            // Chuyển order status từ awaiting_payment sang pending khi payment thành công
            if (payment.order && payment.order.orderStatus === 'awaiting_payment') {
              payment.order.orderStatus = 'pending';
              await payment.order.save();
              console.log(`Order ${payment.order._id} status updated from awaiting_payment to pending`);
            }
          } else if (payOSData.status === 'CANCELLED' && payment.paymentStatus !== 'cancelled') {
            payment.paymentStatus = 'cancelled';
            payment.gatewayResponse = payOSData;
            await payment.save();
          }
        }
      } catch (error) {
        console.error('Error verifying payment with PayOS:', error.message);
        // Continue to return current payment status
      }
    }

    res.json({
      success: true,
      data: {
        paymentStatus: payment.paymentStatus,
        orderStatus: payment.order?.orderStatus,
        payment
      }
    });
  } catch (error) {
    console.error('Verify payment error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi khi xác thực payment'
    });
  }
};

/**
 * Cancel payment link
 */
const cancelPaymentLink = async (req, res) => {
  try {
    const { orderId } = req.params;
    const { reason } = req.body;

    // Get payment
    const payment = await Payment.findOne({ order: orderId });

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy payment'
      });
    }

    if (payment.paymentStatus === 'completed') {
      return res.status(400).json({
        success: false,
        message: 'Không thể hủy payment đã hoàn thành'
      });
    }

    // Cancel payment link on PayOS
    if (payment.transactionId) {
      try {
        const orderCode = parseInt(payment.transactionId);
        await payOSService.cancelPaymentLink(orderCode, reason || 'Khách hàng hủy');
      } catch (error) {
        console.error('Error cancelling payment link on PayOS:', error.message);
        // Continue to update local payment status
      }
    }

    // Update payment status
    payment.paymentStatus = 'cancelled';
    await payment.save();

    res.json({
      success: true,
      message: 'Đã hủy payment link thành công',
      data: payment
    });
  } catch (error) {
    console.error('Cancel payment link error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi khi hủy payment link'
    });
  }
};

/**
 * Confirm payment success từ frontend
 * Được gọi khi user thanh toán thành công trong WebView
 */
const confirmPayment = async (req, res) => {
  try {
    const { orderId } = req.params;
    const { code, orderCode, status } = req.body;

    // Get order
    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Đơn hàng không tồn tại'
      });
    }

    // Find payment
    let payment = await Payment.findOne({ order: orderId });
    
    if (!payment) {
      // Nếu chưa có payment record, tạo mới
      payment = new Payment({
        order: orderId,
        user: order.user,
        amount: order.finalAmount,
        paymentMethod: 'payos',
        paymentGateway: 'payos',
        paymentStatus: status || 'completed',
        transactionId: orderCode?.toString() || order.orderNumber,
        currency: 'VND'
      });
    } else {
      // Cập nhật payment status
      payment.paymentStatus = status || 'completed';
      payment.gatewayTransactionId = orderCode?.toString();
      payment.processedAt = new Date();
      if (code) {
        payment.gatewayResponse = { code, orderCode };
      }
    }

    await payment.save();

    // Chuyển order status từ awaiting_payment sang pending khi payment thành công
    if (payment.paymentStatus === 'completed' && order.orderStatus === 'awaiting_payment') {
      order.orderStatus = 'pending';
      await order.save();
      console.log(`Order ${order._id} status updated from awaiting_payment to pending`);
    }

    res.json({
      success: true,
      message: 'Đã cập nhật trạng thái thanh toán',
      data: {
        payment: {
          _id: payment._id,
          paymentStatus: payment.paymentStatus,
          amount: payment.amount
        },
        order: {
          _id: order._id,
          orderStatus: order.orderStatus,
          orderNumber: order.orderNumber
        }
      }
    });
  } catch (error) {
    console.error('Confirm payment error:', error);
    res.status(500).json({
      success: false,
      message: error.message || 'Lỗi khi cập nhật trạng thái thanh toán'
    });
  }
};

module.exports = {
  createPaymentLink,
  handleWebhook,
  verifyPayment,
  cancelPaymentLink,
  confirmPayment
};

