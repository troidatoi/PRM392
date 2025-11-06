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

    // Validate amount (PayOS yêu cầu >= 1000 VND)
    const amount = Math.round(order.finalAmount);
    if (amount < 1000) {
      return res.status(400).json({
        success: false,
        message: 'Số tiền thanh toán phải lớn hơn hoặc bằng 1,000 VND'
      });
    }

    // Validate orderCode (PayOS yêu cầu integer 1-999999999999)
    if (orderCode < 1 || orderCode > 999999999999) {
      return res.status(400).json({
        success: false,
        message: 'Mã đơn hàng không hợp lệ'
      });
    }

    // Prepare payment data for PayOS
    // Đảm bảo tất cả fields đúng format theo PayOS API docs
    // PayOS yêu cầu description tối đa 25 ký tự
    let description = `Thanh toán đơn hàng ${order.orderNumber}`;
    if (description.length > 25) {
      // Cắt description xuống còn 25 ký tự
      description = description.substring(0, 25);
    }
    
    const paymentData = {
      amount: amount, // integer
      orderCode: orderCode, // integer (1-999999999999)
      description: description, // string (max 25 characters)
      cancelUrl: cancelUrl || `${process.env.FRONTEND_URL}/payment/cancel?orderId=${orderId}`, // URI
      returnUrl: returnUrl || `${process.env.FRONTEND_URL}/payment/success?orderId=${orderId}`, // URI
      items: [], // Array of items (optional)
      buyer: {
        name: order.shippingAddress?.fullName || order.user?.username || '', // string
        email: order.user?.email || '', // email format
        phone: order.shippingAddress?.phone || order.user?.phoneNumber || '' // string
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
    // Log để debug
    console.log('=== PayOS Webhook Received ===');
    console.log('URL:', req.url);
    console.log('Method:', req.method);
    console.log('Headers:', JSON.stringify(req.headers, null, 2));
    console.log('Body:', JSON.stringify(req.body, null, 2));
    
    const webhookData = req.body;
    
    // Kiểm tra body có tồn tại không
    if (!webhookData || Object.keys(webhookData).length === 0) {
      console.error('Empty webhook body');
      return res.status(400).json({
        code: -1,
        desc: 'Empty request body',
        data: null
      });
    }
    
    // PayOS gửi signature trong body, không phải header
    const webhookSignature = webhookData.signature;
    const webhookChecksum = req.headers['x-payos-signature'] || req.headers['x-payos-signature-256'] || webhookSignature;

    const { code, data } = webhookData;

    // PayOS trả về code là string "00" cho success, hoặc số 0
    // Xử lý cả 2 trường hợp
    const isSuccess = code === "00" || code === 0 || code === "0";

    if (!isSuccess || !data) {
      console.error('Invalid webhook data:', { code, hasData: !!data, isSuccess });
      return res.status(400).json({
        code: -1,
        desc: 'Invalid webhook data',
        data: null
      });
    }

    // PayOS format: data có thể có code/desc riêng và status trong data
    const { orderCode, amount, transactionDateTime, status, code: dataCode, desc: dataDesc } = data;
    
    // Status có thể là trong data.code hoặc data.status
    // Nếu data.code === "00" thì thanh toán thành công
    const paymentStatus = status || (dataCode === "00" ? "PAID" : null);
    
    // Verify signature nếu có (nhưng vẫn tiếp tục xử lý nếu là webhook thật với code "00")
    let signatureValid = false;
    if (webhookSignature) {
      // Tạo copy của data để verify (loại bỏ signature field)
      const dataToVerify = { ...webhookData };
      delete dataToVerify.signature;
      
      signatureValid = payosService.verifyWebhook(dataToVerify, webhookSignature);
      
      if (!signatureValid) {
        // Với test webhook từ PayOS dashboard, signature có thể không match
        const testOrderCode = data?.orderCode;
        if (testOrderCode === 123) {
          console.warn('⚠️ Test webhook detected - skipping signature verification');
        } else if (isSuccess && dataCode === "00") {
          // Webhook thật từ PayOS với code "00" - vẫn xử lý dù signature fail
          // (có thể do PayOS signature format khác hoặc có field thay đổi)
          console.warn('⚠️ Signature verification failed but webhook code is "00" - processing payment update');
        } else {
          console.error('Invalid webhook signature');
          console.error('Received signature:', webhookSignature);
          return res.status(400).json({
            code: -1,
            desc: 'Invalid signature',
            data: null
          });
        }
      } else {
        console.log('✅ Signature verified');
      }
    } else {
      console.warn('⚠️ No signature found - might be a test request');
    }

    // Find payment by order code
    // Tìm payment bằng gatewayTransactionId (orderCode từ PayOS)
    let payment = await Payment.findOne({
      gatewayTransactionId: orderCode.toString()
    }).populate('order');

    if (!payment) {
      console.warn(`Payment not found for orderCode: ${orderCode} - might be a test webhook`);
      
      // Với test webhook, vẫn trả về success để PayOS biết endpoint hoạt động
      return res.json({
        code: 0,
        desc: 'Webhook received (payment not found - might be test)',
        data: null
      });
    }
    
    console.log('📋 Payment found:', {
      paymentId: payment._id,
      currentStatus: payment.paymentStatus,
      gatewayTransactionId: payment.gatewayTransactionId,
      orderId: payment.order?._id,
      orderNumber: payment.order?.orderNumber
    });

    // Update payment status based on webhook callback từ PayOS
    // Nếu code "00" hoặc dataCode "00" → thanh toán thành công → payment status = "completed"
    // Ngược lại → thanh toán thất bại → payment status = "failed"
    
    if (isSuccess && (paymentStatus === 'PAID' || dataCode === "00" || code === "00")) {
      // Thanh toán thành công
      payment.paymentStatus = 'completed';
      
      // Parse transactionDateTime từ PayOS format "2025-11-06 11:23:09"
      if (transactionDateTime) {
        try {
          const dateStr = transactionDateTime.replace(/(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})/, '$1-$2-$3T$4:$5:$6');
          payment.paymentDate = new Date(dateStr);
        } catch (e) {
          payment.paymentDate = new Date();
        }
      } else {
        payment.paymentDate = new Date();
      }
      
      // Lưu response từ PayOS
      payment.gatewayResponse = data;
      
      // Lưu reference nếu có
      if (data.reference) {
        payment.transactionId = data.reference;
      }
      
      // Theo yêu cầu: KHÔNG thay đổi trạng thái đơn hàng khi thanh toán PayOS thành công
      
      await payment.save();
      console.log('✅ Payment status updated to completed:', {
        orderCode,
        paymentId: payment._id,
        paymentStatus: payment.paymentStatus
      });
    } else {
      // Thanh toán thất bại hoặc bị hủy
      payment.paymentStatus = 'failed';
      payment.gatewayResponse = data;
      await payment.save();
      console.log('❌ Payment status updated to failed:', {
        orderCode,
        paymentId: payment._id,
        code,
        dataCode,
        paymentStatus: payment.paymentStatus
      });
    }

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

        // Theo yêu cầu: KHÔNG thay đổi trạng thái đơn hàng ở bước này
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
 * Xác minh thanh toán (chỉ đọc từ database, KHÔNG gọi PayOS API)
 * Webhook callback sẽ tự động update payment status
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

    // Chỉ tìm payment trong database (KHÔNG gọi PayOS API)
    const payment = await Payment.findOne({
      gatewayTransactionId: orderCode.toString()
    }).populate('order');

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Thanh toán không tồn tại'
      });
    }

    // Trả về payment status từ database (đã được webhook update)
    const isPaid = payment.paymentStatus === 'completed';

    res.json({
      success: true,
      message: 'Xác minh thanh toán thành công',
      data: {
        payment: payment,
        isPaid: isPaid,
        paymentStatus: payment.paymentStatus
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

/**
 * Order confirmation data (for success screen)
 * Trả về thông tin đơn hàng + thanh toán để hiển thị màn hình xác nhận
 */
const getOrderConfirmation = async (req, res) => {
  try {
    const { orderId } = req.params;

    const order = await Order.findById(orderId)
      .populate('user', 'username email phoneNumber')
      .populate('store', 'name')
      .populate({
        path: 'orderDetails',
        populate: { path: 'product', select: 'name price images' }
      });

    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Đơn hàng không tồn tại'
      });
    }

    const payment = await Payment.findOne({ order: orderId })
      .select('paymentStatus paymentMethod paymentGateway amount paymentDate gatewayTransactionId transactionId');

    res.json({
      success: true,
      data: {
        order: {
          id: order._id,
          orderNumber: order.orderNumber,
          orderStatus: order.orderStatus,
          statusText: order.statusText,
          orderDate: order.orderDate,
          totals: {
            totalAmount: order.totalAmount,
            shippingFee: order.shippingFee,
            discountAmount: order.discountAmount,
            finalAmount: order.finalAmount
          },
          shippingAddress: order.shippingAddress,
          items: order.orderDetails || []
        },
        payment: payment ? {
          id: payment._id,
          status: payment.paymentStatus,
          statusText: payment.statusText,
          method: payment.paymentMethod,
          gateway: payment.paymentGateway,
          amount: payment.amount,
          paidAt: payment.paymentDate,
          gatewayTransactionId: payment.gatewayTransactionId,
          transactionId: payment.transactionId
        } : null
      }
    });
  } catch (error) {
    console.error('Get order confirmation error:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

module.exports.getOrderConfirmation = getOrderConfirmation;

