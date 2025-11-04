const Order = require('../models/Order');
const OrderDetail = require('../models/OrderDetail');
const Cart = require('../models/Cart');
const CartItem = require('../models/CartItem');
const Inventory = require('../models/Inventory');
const Bike = require('../models/Bike');

// Create separate orders for each store
const createOrders = async (req, res) => {
  try {
    const { userId, shippingAddress, paymentMethod, notes } = req.body;

    // Validate payment method: VNPay or PayOS
    const validPaymentMethods = ['vnpay', 'payos', 'cash'];
    if (!validPaymentMethods.includes(paymentMethod)) {
      return res.status(400).json({
        success: false,
        message: 'Phương thức thanh toán không hợp lệ. Chỉ hỗ trợ: VNPay, PayOS, hoặc tiền mặt'
      });
    }

    // Get cart with items for user
    const cart = await Cart.findOne({ 
      user: userId, 
      status: 'active' 
    })
    .populate({
      path: 'items',
      populate: [
        {
          path: 'product',
          select: 'name brand price originalPrice'
        },
        {
          path: 'store',
          select: 'name address city'
        }
      ]
    });
    
    if (!cart) {
      return res.status(404).json({
        success: false,
        message: 'Giỏ hàng không tồn tại'
      });
    }
    
    // Check cart status
    if (cart.status !== 'active') {
      return res.status(400).json({
        success: false,
        message: 'Giỏ hàng đã được chuyển thành đơn hàng hoặc không còn hoạt động'
      });
    }
    
    if (cart.items.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Giỏ hàng trống'
      });
    }
    
    // Group items by store
    const itemsByStore = {};
    cart.items.forEach(cartItem => {
      const storeId = cartItem.store._id.toString();
      if (!itemsByStore[storeId]) {
        itemsByStore[storeId] = {
          store: cartItem.store,
          items: []
        };
      }
      itemsByStore[storeId].items.push(cartItem);
    });
    
    // Pre-check inventory shortages before creating orders
    const shortages = [];
    for (const [storeId, storeData] of Object.entries(itemsByStore)) {
      for (const cartItem of storeData.items) {
        const inv = await Inventory.findOne({ product: cartItem.product._id, store: storeId });
        const available = inv ? inv.quantity : 0;
        if (available < cartItem.quantity) {
          shortages.push({
            productId: cartItem.product._id,
            productName: cartItem.product.name,
            storeId,
            requested: cartItem.quantity,
            available
          });
        }
      }
    }
    if (shortages.length > 0) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu tồn kho',
        data: { shortages }
      });
    }

    // Create ONE order for EACH store
    const createdOrders = [];
    
    for (const [storeId, storeData] of Object.entries(itemsByStore)) {
      // Calculate total for this store only
      let storeTotal = 0;
      for (const item of storeData.items) {
        // Get current price from product
        const product = await Bike.findById(item.product);
        const currentPrice = product ? product.price : 0;
        storeTotal += item.quantity * currentPrice;
      }
      
      // Create ONE order for THIS store
      const order = new Order({
        user: userId,
        store: storeData.store._id, // Mỗi order chỉ thuộc 1 cửa hàng
        shippingAddress,
        paymentMethod,
        notes: `${notes || ''}`.trim(),
        totalAmount: storeTotal,
        shippingFee: 0,
        discountAmount: 0,
        finalAmount: storeTotal
      });
      
      await order.save();
      
      // Create order details ONLY for this store's items
      const orderDetails = [];
      
      for (const cartItem of storeData.items) {
        // Get current price from product
        const product = await Bike.findById(cartItem.product);
        const currentPrice = product ? product.price : 0;
        const originalPrice = product ? product.originalPrice : currentPrice;
        
        const orderDetail = new OrderDetail({
          order: order._id,
          product: cartItem.product._id,
          quantity: cartItem.quantity,
          price: currentPrice,
          originalPrice: originalPrice,
          discount: originalPrice - currentPrice,
          totalPrice: cartItem.quantity * currentPrice
        });
        
        await orderDetail.save();
        orderDetails.push(orderDetail);
        
        // Reduce inventory stock for THIS store only
        const inventory = await Inventory.findOne({ product: cartItem.product._id, store: storeId });
        if (inventory) { await inventory.reduceStock(cartItem.quantity); }
      }
      
      // Update order with details
      order.orderDetails = orderDetails.map(detail => detail._id);
      await order.save();
      
      createdOrders.push({
        order,
        orderDetails,
        store: storeData.store
      });
    }
    
    // Clear cart items after creating orders
    await CartItem.deleteMany({ cart: cart._id });
    
    // Reset cart to empty state
    cart.items = [];
    cart.isMultiStore = false;
    await cart.save();
    
    res.json({
      success: true,
      message: `Tạo thành công ${createdOrders.length} đơn hàng riêng biệt`,
      data: {
        orders: createdOrders,
        summary: {
          totalOrders: createdOrders.length,
          totalAmount: createdOrders.reduce((sum, order) => sum + order.order.totalAmount, 0),
          stores: createdOrders.map(order => ({
            storeName: order.store.name,
            orderId: order.order._id,
            totalAmount: order.order.totalAmount
          }))
        }
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get user orders
const getUserOrders = async (req, res) => {
  try {
    const { userId } = req.params;
    const { page = 1, limit = 10, status } = req.query;
    
    const query = { user: userId };
    if (status) query.orderStatus = status;
    
    const skip = (page - 1) * limit;
    
    const orders = await Order.find(query)
      .populate('orderDetails', 'product quantity price totalPrice')
      .populate('orderDetails.product', 'name brand images')
      .sort({ orderDate: -1 })
      .skip(skip)
      .limit(parseInt(limit));
    
    const total = await Order.countDocuments(query);
    
    res.json({
      success: true,
      data: orders,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get order details
const getOrderDetails = async (req, res) => {
  try {
    const { orderId } = req.params;
    
    const order = await Order.findById(orderId)
      .populate('user', 'username email phoneNumber')
      .populate({
        path: 'orderDetails',
        populate: {
          path: 'product',
          select: 'name brand price images specifications'
        }
      });
    
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Đơn hàng không tồn tại'
      });
    }
    
    res.json({
      success: true,
      data: order
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Update order status
const updateOrderStatus = async (req, res) => {
  try {
    const { orderId } = req.params;
    const { status, notes } = req.body;
    
    const order = await Order.findById(orderId);
    
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Đơn hàng không tồn tại'
      });
    }
    
    await order.updateStatus(status, notes);
    
    res.json({
      success: true,
      message: 'Cập nhật trạng thái đơn hàng thành công',
      data: order
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Cancel order
const cancelOrder = async (req, res) => {
  try {
    const { orderId } = req.params;
    const { reason } = req.body;
    
    const order = await Order.findById(orderId);
    
    if (!order) {
      return res.status(404).json({
        success: false,
        message: 'Đơn hàng không tồn tại'
      });
    }
    
    await order.cancel(reason);
    
    // Restore inventory stock
    const orderDetails = await OrderDetail.find({ order: orderId });
    
    for (const detail of orderDetails) {
      const inventory = await Inventory.findOne({
        product: detail.product,
        store: order.store
      });
      
      if (inventory) {
        await inventory.addStock(detail.quantity);
      }
    }
    
    res.json({
      success: true,
      message: 'Hủy đơn hàng thành công',
      data: order
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get orders by store
const getOrdersByStore = async (req, res) => {
  try {
    const { storeId } = req.params;
    const { page = 1, limit = 10, status } = req.query;
    
    const query = { store: storeId };
    if (status) query.orderStatus = status;
    
    const skip = (page - 1) * limit;
    
    const orders = await Order.find(query)
      .populate('user', 'username email phoneNumber')
      .populate('orderDetails', 'product quantity price totalPrice')
      .populate('orderDetails.product', 'name brand images')
      .sort({ orderDate: -1 })
      .skip(skip)
      .limit(parseInt(limit));
    
    const total = await Order.countDocuments(query);
    
    res.json({
      success: true,
      data: orders,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get all orders (admin)
const getAllOrders = async (req, res) => {
  try {
    const { page = 1, limit = 10, status } = req.query;
    const query = {};
    if (status) query.orderStatus = status;
    const skip = (page - 1) * limit;
    const orders = await Order.find(query)
      .populate('user', 'username email')
      .populate('store', 'name')
      .populate('orderDetails', 'product quantity price totalPrice')
      .sort({ orderDate: -1 })
      .skip(skip)
      .limit(parseInt(limit));
    const total = await Order.countDocuments(query);
    res.json({
      success: true,
      data: orders,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

module.exports = {
  createOrders,
  getUserOrders,
  getOrderDetails,
  updateOrderStatus,
  cancelOrder,
  getOrdersByStore,
  getAllOrders
};
