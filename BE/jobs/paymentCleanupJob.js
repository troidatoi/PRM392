const Payment = require('../models/Payment');
const Order = require('../models/Order');
const OrderDetail = require('../models/OrderDetail');
const Inventory = require('../models/Inventory');

/**
 * Cron job để tự động hủy payment và order sau 24h nếu payment vẫn pending
 * Chạy mỗi giờ để check và cleanup
 */
const cleanupPendingPayments = async () => {
  try {
    const now = new Date();
    const twentyFourHoursAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000);

    // Tìm tất cả payment pending đã tạo hơn 24h trước
    const pendingPayments = await Payment.find({
      paymentStatus: 'pending',
      paymentMethod: { $in: ['payos', 'vnpay'] }, // Chỉ xử lý online payment
      createdAt: { $lt: twentyFourHoursAgo }
    }).populate('order');

    if (pendingPayments.length === 0) {
      console.log(`[Payment Cleanup] Không có payment nào cần cleanup tại ${now.toISOString()}`);
      return;
    }

    console.log(`[Payment Cleanup] Tìm thấy ${pendingPayments.length} payment cần cleanup`);

    let cancelledCount = 0;
    let errorCount = 0;

    for (const payment of pendingPayments) {
      try {
        // Kiểm tra xem payment đã được completed chưa (tránh race condition)
        const currentPayment = await Payment.findById(payment._id);
        if (!currentPayment || currentPayment.paymentStatus !== 'pending') {
          console.log(`[Payment Cleanup] Payment ${payment._id} đã được cập nhật hoặc không tồn tại, bỏ qua`);
          continue;
        }

        // Lấy order để xử lý
        const order = payment.order 
          ? await Order.findById(payment.order._id)
          : await Order.findOne({ _id: currentPayment.order });

        // Cancel payment - dùng 'cancelled' vì đây là timeout/hết hạn, không phải lỗi kỹ thuật
        currentPayment.paymentStatus = 'cancelled';
        currentPayment.processedAt = new Date();
        currentPayment.notes = 'Tự động hủy sau 24h không thanh toán';
        await currentPayment.save();
        console.log(`[Payment Cleanup] Đã hủy payment ${currentPayment._id}`);

        // Cancel order và restore inventory nếu order còn ở trạng thái awaiting_payment hoặc pending
        if (order && (order.orderStatus === 'awaiting_payment' || order.orderStatus === 'pending')) {
          // Restore inventory trước khi cancel order
          const orderDetails = await OrderDetail.find({ order: order._id });
          
          for (const detail of orderDetails) {
            const inventory = await Inventory.findOne({
              product: detail.product,
              store: order.store
            });
            
            if (inventory) {
              await inventory.addStock(detail.quantity);
              console.log(`[Payment Cleanup] Restored ${detail.quantity} units for product ${detail.product} in store ${order.store}`);
            } else {
              console.warn(`[Payment Cleanup] Không tìm thấy inventory cho product ${detail.product} trong store ${order.store}`);
            }
          }

          // Cancel order
          order.orderStatus = 'cancelled';
          order.notes = 'Tự động hủy: Thanh toán không hoàn tất sau 24h';
          await order.save();
          
          console.log(`[Payment Cleanup] Đã hủy order ${order._id} và restore inventory`);
        } else if (order) {
          console.log(`[Payment Cleanup] Order ${order._id} đã ở trạng thái ${order.orderStatus}, không cần hủy`);
        } else {
          console.warn(`[Payment Cleanup] Không tìm thấy order cho payment ${currentPayment._id}`);
        }

        cancelledCount++;

      } catch (error) {
        errorCount++;
        console.error(`[Payment Cleanup] Lỗi khi xử lý payment ${payment._id}:`, error.message);
        console.error(error.stack);
      }
    }

    console.log(`[Payment Cleanup] Hoàn thành: ${cancelledCount} payment đã hủy, ${errorCount} lỗi`);

  } catch (error) {
    console.error('[Payment Cleanup] Lỗi trong cleanup job:', error);
  }
};

module.exports = cleanupPendingPayments;

