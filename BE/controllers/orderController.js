const mongoose = require('mongoose');
const Order = require('../models/Order');
const OrderDetail = require('../models/OrderDetail');
const Payment = require('../models/Payment');
const Cart = require('../models/Cart');
const CartItem = require('../models/CartItem');
const Inventory = require('../models/Inventory');
const Bike = require('../models/Bike');
const Store = require('../models/Store');
const { geocodeAddress } = require('../utils/geocoding');
const { haversineKm, calculateShippingFee } = require('../utils/shipping');

// Create separate orders for each store
const createOrders = async (req, res) => {
  try {
    const { userId, shippingAddress, paymentMethod, notes } = req.body;

    // Validate payment method
    const allowedPaymentMethods = ['vnpay', 'payos', 'cash', 'bank_transfer'];
    if (!allowedPaymentMethods.includes(paymentMethod)) {
      return res.status(400).json({
        success: false,
        message: `Phương thức thanh toán không hợp lệ. Chỉ hỗ trợ: ${allowedPaymentMethods.join(', ')}`
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

    // Geocode shipping address once
    const fullAddress = `${shippingAddress?.address || ''}, ${shippingAddress?.city || ''}`.trim();
    let destinationCoords = null;
    try {
      if (!shippingAddress || !shippingAddress.address || !shippingAddress.city) {
        return res.status(400).json({ success: false, message: 'Thiếu địa chỉ giao hàng' });
      }
      const { latitude, longitude } = await geocodeAddress(fullAddress);
      destinationCoords = { lat: latitude, lng: longitude };
    } catch (geoErr) {
      return res.status(400).json({ success: false, message: geoErr.message });
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
      
      // Load store to get coordinates
      const storeDoc = await Store.findById(storeId);
      let distanceKm = 0;
      let shippingFee = 0;
      if (storeDoc && destinationCoords) {
        distanceKm = haversineKm(
          storeDoc.latitude,
          storeDoc.longitude,
          destinationCoords.lat,
          destinationCoords.lng
        );
        shippingFee = await calculateShippingFee(distanceKm);
      }

      // Create ONE order for THIS store
      const order = new Order({
        user: userId,
        store: storeData.store._id, // Mỗi order chỉ thuộc 1 cửa hàng
        shippingAddress,
        paymentMethod,
        notes: `${notes || ''}`.trim(),
        totalAmount: storeTotal,
        shippingFee: shippingFee,
        discountAmount: 0,
        finalAmount: storeTotal + shippingFee
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
      
      // Create Payment record for online payment methods (payos, vnpay)
      let payment = null;
      if (paymentMethod === 'payos' || paymentMethod === 'vnpay') {
        payment = new Payment({
          order: order._id,
          user: userId,
          amount: order.finalAmount,
          paymentMethod: paymentMethod,
          paymentGateway: paymentMethod,
          paymentStatus: 'pending',
          currency: 'VND'
        });
        await payment.save();
      }
      
      createdOrders.push({
        order,
        orderDetails,
        store: storeData.store,
        shipping: {
          distanceKm: Number(distanceKm.toFixed(2)),
          shippingFee
        },
        payment: payment ? {
          _id: payment._id,
          paymentStatus: payment.paymentStatus,
          paymentMethod: payment.paymentMethod
        } : null
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

// Estimate shipping fee and distance per store without creating orders
const estimateShipping = async (req, res) => {
  try {
    const { userId, shippingAddress } = req.body;

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
          select: 'name address city latitude longitude'
        }
      ]
    });

    if (!cart || cart.items.length === 0) {
      return res.status(400).json({ success: false, message: 'Giỏ hàng trống hoặc không tồn tại' });
    }

    if (!shippingAddress || !shippingAddress.address || !shippingAddress.city) {
      return res.status(400).json({ success: false, message: 'Thiếu địa chỉ giao hàng' });
    }

    const fullAddress = `${shippingAddress.address}, ${shippingAddress.city}`.trim();
    let destinationCoords;
    try {
      const { latitude, longitude } = await geocodeAddress(fullAddress);
      destinationCoords = { lat: latitude, lng: longitude };
    } catch (e) {
      return res.status(400).json({ success: false, message: e.message });
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

    const perStore = [];
    let totalShippingFee = 0;

    for (const [storeId, storeData] of Object.entries(itemsByStore)) {
      const storeDoc = await Store.findById(storeId);
      if (!storeDoc) continue;

      const distanceKm = haversineKm(
        storeDoc.latitude,
        storeDoc.longitude,
        destinationCoords.lat,
        destinationCoords.lng
      );
      const shippingFee = await calculateShippingFee(distanceKm);
      totalShippingFee += shippingFee;

      perStore.push({
        storeId,
        storeName: storeDoc.name,
        distanceKm: Number(distanceKm.toFixed(2)),
        shippingFee
      });
    }

    return res.json({
      success: true,
      message: 'Ước tính phí vận chuyển thành công',
      data: {
        stores: perStore,
        summary: {
          totalStores: perStore.length,
          totalShippingFee
        }
      }
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
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
      .populate('user', 'username email phoneNumber')
      .populate('store', 'name address')
      .sort({ orderDate: -1 })
      .skip(skip)
      .limit(parseInt(limit));
    
    const total = await Order.countDocuments(query);
    
    // Transform orders to include items
    const ordersWithItems = await Promise.all(orders.map(async (order) => {
      const orderDetails = await OrderDetail.find({ order: order._id })
        .populate('product', 'name images price');
      
      return {
        _id: order._id,
        orderNumber: order.orderNumber,
        userId: order.user?._id,
        user: order.user,
        status: order.orderStatus,
        totalAmount: order.totalAmount,
        shippingAddress: order.shippingAddress,
        paymentMethod: order.paymentMethod,
        createdAt: order.orderDate,
        updatedAt: order.updatedAt,
        items: orderDetails.map(detail => ({
          product: detail.product,
          quantity: detail.quantity,
          price: detail.price,
          totalPrice: detail.totalPrice
        }))
      };
    }));
    
    res.json({
      success: true,
      message: 'Lấy danh sách đơn hàng thành công',
      data: ordersWithItems,
      count: ordersWithItems.length,
      total: total,
      page: parseInt(page),
      pages: Math.ceil(total / limit)
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Lỗi server', error: error.message });
  }
};

// Get total revenue
const getTotalRevenue = async (req, res) => {
  try {
    const { startDate, endDate, storeId } = req.query;
    
    // Build query for completed orders (delivered orders)
    const query = { orderStatus: 'delivered' };
    
    // Filter by store if provided
    if (storeId) {
      query.store = mongoose.Types.ObjectId.isValid(storeId) 
        ? new mongoose.Types.ObjectId(storeId) 
        : storeId;
    }
    
    // Filter by date range if provided
    if (startDate || endDate) {
      query.orderDate = {};
      if (startDate) {
        query.orderDate.$gte = new Date(startDate);
      }
      if (endDate) {
        // Set end date to end of day
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);
        query.orderDate.$lte = end;
      }
    }
    
    // Calculate total revenue from finalAmount of delivered orders
    const revenueData = await Order.aggregate([
      { $match: query },
      {
        $group: {
          _id: null,
          totalRevenue: { $sum: '$finalAmount' },
          totalOrders: { $sum: 1 },
          averageOrderValue: { $avg: '$finalAmount' }
        }
      }
    ]);
    
    // Get revenue by store if no specific store is requested
    let revenueByStore = [];
    if (!storeId) {
      const storeQuery = { orderStatus: 'delivered' };
      if (startDate || endDate) {
        storeQuery.orderDate = query.orderDate;
      }
      revenueByStore = await Order.aggregate([
        { $match: storeQuery },
        {
          $group: {
            _id: '$store',
            totalRevenue: { $sum: '$finalAmount' },
            totalOrders: { $sum: 1 }
          }
        },
        {
          $lookup: {
            from: 'stores',
            localField: '_id',
            foreignField: '_id',
            as: 'store'
          }
        },
        {
          $unwind: {
            path: '$store',
            preserveNullAndEmptyArrays: true
          }
        },
        {
          $project: {
            storeId: '$_id',
            storeName: '$store.name',
            totalRevenue: 1,
            totalOrders: 1
          }
        },
        { $sort: { totalRevenue: -1 } }
      ]);
    }
    
    // Get revenue by month for chart data
    const revenueByMonth = await Order.aggregate([
      { $match: query },
      {
        $group: {
          _id: {
            year: { $year: '$orderDate' },
            month: { $month: '$orderDate' }
          },
          totalRevenue: { $sum: '$finalAmount' },
          totalOrders: { $sum: 1 }
        }
      },
      { $sort: { '_id.year': 1, '_id.month': 1 } }
    ]);
    
    const result = revenueData[0] || {
      totalRevenue: 0,
      totalOrders: 0,
      averageOrderValue: 0
    };
    
    res.json({
      success: true,
      message: 'Lấy tổng doanh thu thành công',
      data: {
        totalRevenue: result.totalRevenue || 0,
        totalOrders: result.totalOrders || 0,
        averageOrderValue: result.averageOrderValue || 0,
        revenueByStore: revenueByStore,
        revenueByMonth: revenueByMonth.map(item => ({
          year: item._id.year,
          month: item._id.month,
          totalRevenue: item.totalRevenue,
          totalOrders: item.totalOrders
        })),
        period: {
          startDate: startDate || null,
          endDate: endDate || null,
          storeId: storeId || null
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

// Get orders by day of week
const getOrdersByDayOfWeek = async (req, res) => {
  try {
    const { startDate, endDate, storeId, status } = req.query;
    
    // Build query
    const query = {};
    
    // Filter by status if provided
    if (status) {
      query.orderStatus = status;
    }
    
    // Filter by store if provided
    if (storeId) {
      query.store = mongoose.Types.ObjectId.isValid(storeId) 
        ? new mongoose.Types.ObjectId(storeId) 
        : storeId;
    }
    
    // Filter by date range if provided
    if (startDate || endDate) {
      query.orderDate = {};
      if (startDate) {
        query.orderDate.$gte = new Date(startDate);
      }
      if (endDate) {
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);
        query.orderDate.$lte = end;
      }
    } else {
      // Default to last 7 days if no date range provided
      const defaultEndDate = new Date();
      const defaultStartDate = new Date();
      defaultStartDate.setDate(defaultStartDate.getDate() - 7);
      query.orderDate = {
        $gte: defaultStartDate,
        $lte: defaultEndDate
      };
    }
    
    // Get orders grouped by day of week
    const ordersByDay = await Order.aggregate([
      { $match: query },
      {
        $group: {
          _id: { $dayOfWeek: '$orderDate' },
          totalOrders: { $sum: 1 },
          totalRevenue: { $sum: '$finalAmount' },
          averageOrderValue: { $avg: '$finalAmount' }
        }
      },
      { $sort: { '_id': 1 } }
    ]);
    
    // Map day of week numbers to Vietnamese names
    // MongoDB $dayOfWeek: 1=Sunday, 2=Monday, ..., 7=Saturday
    const dayNames = {
      1: 'Chủ nhật',
      2: 'Thứ hai',
      3: 'Thứ ba',
      4: 'Thứ tư',
      5: 'Thứ năm',
      6: 'Thứ sáu',
      7: 'Thứ bảy'
    };
    
    // Format result
    const formattedData = ordersByDay.map(item => ({
      dayOfWeek: item._id,
      dayName: dayNames[item._id] || `Ngày ${item._id}`,
      totalOrders: item.totalOrders,
      totalRevenue: item.totalRevenue,
      averageOrderValue: item.averageOrderValue || 0
    }));
    
    // Fill in missing days with zero values
    const completeData = [];
    for (let day = 1; day <= 7; day++) {
      const existing = formattedData.find(item => item.dayOfWeek === day);
      if (existing) {
        completeData.push(existing);
      } else {
        completeData.push({
          dayOfWeek: day,
          dayName: dayNames[day],
          totalOrders: 0,
          totalRevenue: 0,
          averageOrderValue: 0
        });
      }
    }
    
    res.json({
      success: true,
      message: 'Lấy đơn hàng theo ngày trong tuần thành công',
      data: {
        ordersByDay: completeData,
        period: {
          startDate: startDate || null,
          endDate: endDate || null,
          storeId: storeId || null,
          status: status || null
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

module.exports = {
  createOrders,
  getUserOrders,
  getOrderDetails,
  updateOrderStatus,
  cancelOrder,
  getOrdersByStore,
  getAllOrders,
  estimateShipping,
  getTotalRevenue,
  getOrdersByDayOfWeek
};
