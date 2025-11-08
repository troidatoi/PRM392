const Inventory = require('../models/Inventory');
const Bike = require('../models/Bike');
const Store = require('../models/Store');
const Order = require('../models/Order');
const OrderDetail = require('../models/OrderDetail');
const mongoose = require('mongoose');

// Get inventory for a product at a specific store
const getInventory = async (req, res) => {
  try {
    const { productId, storeId } = req.query;
    
    const inventory = await Inventory.findOne({
      product: productId,
      store: storeId
    })
    .populate('product', 'name brand price')
    .populate('store', 'name address');
    
    if (!inventory) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy tồn kho cho sản phẩm này'
      });
    }
    
    res.json({
      success: true,
      data: inventory
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get all inventory for a store
const getStoreInventory = async (req, res) => {
  try {
    const { storeId } = req.params;
    const { page = 1, limit = 20, status } = req.query;
    
    const query = { store: storeId };
    
    // Filter by stock status
    if (status) {
      switch (status) {
        case 'low_stock':
          query.stock = { $lte: '$minStock' };
          break;
        case 'out_of_stock':
          query.stock = 0;
          break;
        case 'in_stock':
          query.stock = { $gt: '$minStock' };
          break;
      }
    }
    
    const skip = (page - 1) * limit;
    
    const inventory = await Inventory.find(query)
      .populate('product', 'name brand price images')
      .populate('store', 'name address')
      .sort({ 'product.name': 1 })
      .skip(skip)
      .limit(parseInt(limit));
    
    const total = await Inventory.countDocuments(query);
    
    res.json({
      success: true,
      data: inventory,
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

// Add stock to inventory
const addStock = async (req, res) => {
  try {
    const { productId, storeId, quantity } = req.body;
    
    // Validate required fields
    if (!productId || !storeId || !quantity) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu thông tin bắt buộc: productId, storeId, quantity'
      });
    }
    
    // Validate quantity
    if (quantity <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Số lượng phải lớn hơn 0'
      });
    }
    
    // Check if product exists
    const product = await Bike.findById(productId);
    if (!product) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy sản phẩm'
      });
    }
    
    // Check if store exists
    const store = await Store.findById(storeId);
    if (!store) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy cửa hàng'
      });
    }
    
    let inventory = await Inventory.findOne({
      product: productId,
      store: storeId
    });
    
    if (!inventory) {
      // Create new inventory record
      inventory = new Inventory({
        product: productId,
        store: storeId,
        stock: quantity
      });
    } else {
      // Add to existing inventory
      await inventory.addStock(quantity);
    }
    
    await inventory.save();
    
    // Populate the response
    await inventory.populate('product', 'name brand price');
    await inventory.populate('store', 'name address');
    
    res.json({
      success: true,
      message: 'Nhập kho thành công',
      data: inventory
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Reduce stock from inventory
const reduceStock = async (req, res) => {
  try {
    const { productId, storeId, quantity } = req.body;
    
    // Validate required fields
    if (!productId || !storeId || !quantity) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu thông tin bắt buộc: productId, storeId, quantity'
      });
    }
    
    // Validate quantity
    if (quantity <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Số lượng phải lớn hơn 0'
      });
    }
    
    // Check if product exists
    const product = await Bike.findById(productId);
    if (!product) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy sản phẩm'
      });
    }
    
    // Check if store exists
    const store = await Store.findById(storeId);
    if (!store) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy cửa hàng'
      });
    }
    
    const inventory = await Inventory.findOne({
      product: productId,
      store: storeId
    });
    
    if (!inventory) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy tồn kho cho sản phẩm này tại cửa hàng'
      });
    }
    
    await inventory.reduceStock(quantity);
    
    // Populate the response
    await inventory.populate('product', 'name brand price');
    await inventory.populate('store', 'name address');
    
    res.json({
      success: true,
      message: 'Xuất kho thành công',
      data: inventory
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get low stock items
const getLowStockItems = async (req, res) => {
  try {
    const { storeId } = req.params;
    
    const lowStockItems = await Inventory.getLowStockItems(storeId);
    
    res.json({
      success: true,
      data: lowStockItems
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get out of stock items
const getOutOfStockItems = async (req, res) => {
  try {
    const { storeId } = req.params;
    
    const outOfStockItems = await Inventory.getOutOfStockItems(storeId);
    
    res.json({
      success: true,
      data: outOfStockItems
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Check stock availability
const checkStockAvailability = async (req, res) => {
  try {
    const { productId, storeId, quantity } = req.body;
    
    const inventory = await Inventory.findOne({
      product: productId,
      store: storeId
    });
    
    const isAvailable = inventory ? inventory.isAvailable(parseInt(quantity)) : false;
    
    res.json({
      success: true,
      data: {
        available: isAvailable,
        productId,
        storeId,
        requestedQuantity: parseInt(quantity),
        currentStock: inventory ? inventory.stock : 0
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

// Get all stores with inventory for a specific product
const getProductInventory = async (req, res) => {
  try {
    const { productId } = req.params;
    
    const inventories = await Inventory.find({
      product: productId
    })
    .populate('store', 'name address phone')
    .populate('product', 'name price');
    
    if (!inventories || inventories.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy tồn kho cho sản phẩm này'
      });
    }
    
    res.json({
      success: true,
      data: inventories
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get inventory turnover ratio
const getInventoryTurnover = async (req, res) => {
  try {
    const { startDate, endDate, storeId, limit, minStock, sortBy } = req.query;
    
    // Build date filter for orders
    const orderDateFilter = {};
    if (startDate || endDate) {
      orderDateFilter.orderDate = {};
      if (startDate) {
        orderDateFilter.orderDate.$gte = new Date(startDate);
      }
      if (endDate) {
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);
        orderDateFilter.orderDate.$lte = end;
      }
    }
    
    // Build store filter for orders
    if (storeId) {
      orderDateFilter.store = mongoose.Types.ObjectId.isValid(storeId) 
        ? new mongoose.Types.ObjectId(storeId) 
        : storeId;
    }
    
    // Only count delivered orders for sales
    orderDateFilter.orderStatus = 'delivered';
    
    // Get all products
    const allProducts = await Bike.find({});
    
    // Get sales data from OrderDetails
    const salesData = await Order.aggregate([
      { $match: orderDateFilter },
      { $unwind: '$orderDetails' },
      {
        $lookup: {
          from: 'orderdetails',
          localField: 'orderDetails',
          foreignField: '_id',
          as: 'detail'
        }
      },
      { $unwind: '$detail' },
      {
        $group: {
          _id: {
            product: '$detail.product',
            order: '$_id'
          },
          quantity: { $first: '$detail.quantity' },
          totalPrice: { $first: '$detail.totalPrice' },
          price: { $first: '$detail.price' }
        }
      },
      {
        $group: {
          _id: '$_id.product',
          totalQuantitySold: { $sum: '$quantity' },
          totalOrders: { $sum: 1 }, // Count unique orders
          totalRevenue: { $sum: '$totalPrice' },
          averagePrice: { $avg: '$price' }
        }
      }
    ]);
    
    // Create a map for quick lookup
    const salesMap = {};
    salesData.forEach(item => {
      salesMap[item._id.toString()] = {
        totalQuantitySold: item.totalQuantitySold,
        totalOrders: item.totalOrders,
        totalRevenue: item.totalRevenue,
        averagePrice: item.averagePrice
      };
    });
    
    // Get inventory data (total stock across all stores)
    const inventoryData = await Inventory.aggregate([
      {
        $group: {
          _id: '$product',
          currentStock: { $sum: '$stock' },
          totalStockAcrossStores: { $sum: '$stock' }
        }
      }
    ]);
    
    // Create inventory map
    const inventoryMap = {};
    inventoryData.forEach(item => {
      inventoryMap[item._id.toString()] = {
        currentStock: item.currentStock,
        totalStockAcrossStores: item.totalStockAcrossStores
      };
    });
    
    // Build product turnover list
    const products = [];
    for (const product of allProducts) {
      const productId = product._id.toString();
      const sales = salesMap[productId] || {
        totalQuantitySold: 0,
        totalOrders: 0,
        totalRevenue: 0,
        averagePrice: product.price || 0
      };
      
      const inventory = inventoryMap[productId] || {
        currentStock: 0,
        totalStockAcrossStores: 0
      };
      
      // Calculate turnover ratio
      let turnoverRatio = 0;
      if (inventory.currentStock > 0) {
        turnoverRatio = sales.totalQuantitySold / inventory.currentStock;
      } else if (sales.totalQuantitySold > 0) {
        // Out of stock but had sales - use a very high number instead of Infinity
        turnoverRatio = 999999; // High number to indicate fast turnover
      }
      
      // Calculate days to sell out (if there are sales)
      let daysToSellOut = 0;
      if (sales.totalQuantitySold > 0 && inventory.currentStock > 0) {
        const daysInPeriod = startDate && endDate 
          ? Math.ceil((new Date(endDate) - new Date(startDate)) / (1000 * 60 * 60 * 24))
          : 30; // Default to 30 days
        const avgDailySales = sales.totalQuantitySold / daysInPeriod;
        if (avgDailySales > 0) {
          daysToSellOut = inventory.currentStock / avgDailySales;
        }
      }
      
      // Apply minStock filter
      if (minStock !== undefined && inventory.currentStock < parseInt(minStock)) {
        continue;
      }
      
      // Get image URL
      let imageUrl = null;
      if (product.images && product.images.length > 0) {
        if (typeof product.images[0] === 'string') {
          imageUrl = product.images[0];
        } else if (product.images[0].url) {
          imageUrl = product.images[0].url;
        }
      }
      
      products.push({
        productId: productId,
        productName: product.name || 'N/A',
        productBrand: product.brand || 'N/A',
        productCategory: product.category || 'other',
        imageUrl: imageUrl,
        currentStock: inventory.currentStock,
        totalStockAcrossStores: inventory.totalStockAcrossStores,
        totalQuantitySold: sales.totalQuantitySold,
        totalOrders: sales.totalOrders,
        turnoverRatio: isFinite(turnoverRatio) ? turnoverRatio : 999999,
        daysToSellOut: isFinite(daysToSellOut) ? daysToSellOut : 0,
        totalRevenue: sales.totalRevenue || 0,
        averagePrice: sales.averagePrice || product.price || 0
      });
    }
    
    // Sort products
    let sortField = 'turnoverRatio';
    let sortOrder = -1; // Descending by default
    
    if (sortBy) {
      switch (sortBy) {
        case 'turnover':
          sortField = 'turnoverRatio';
          break;
        case 'stock':
          sortField = 'currentStock';
          break;
        case 'sales':
          sortField = 'totalQuantitySold';
          break;
        case 'name':
          sortField = 'productName';
          sortOrder = 1; // Ascending for name
          break;
        case 'revenue':
          sortField = 'totalRevenue';
          break;
        default:
          sortField = 'turnoverRatio';
      }
    }
    
    products.sort((a, b) => {
      if (sortField === 'productName') {
        return sortOrder === 1 
          ? a.productName.localeCompare(b.productName)
          : b.productName.localeCompare(a.productName);
      }
      return sortOrder === 1 
        ? a[sortField] - b[sortField]
        : b[sortField] - a[sortField];
    });
    
    // Apply limit
    const limitedProducts = limit ? products.slice(0, parseInt(limit)) : products;
    
    // Calculate summary
    const totalProducts = products.length;
    let fastMovingProducts = 0;
    let slowMovingProducts = 0;
    let outOfStockProducts = 0;
    let totalTurnoverRatio = 0;
    let validRatios = 0;
    
    products.forEach(p => {
      if (p.currentStock === 0) {
        outOfStockProducts++;
      } else if (p.turnoverRatio >= 1.5) {
        fastMovingProducts++;
      } else if (p.turnoverRatio < 0.5 && p.turnoverRatio > 0) {
        slowMovingProducts++;
      }
      
      // Only count valid ratios for average (exclude very high numbers used for out-of-stock)
      if (p.turnoverRatio > 0 && p.turnoverRatio < 1000) {
        totalTurnoverRatio += p.turnoverRatio;
        validRatios++;
      }
    });
    
    const averageTurnoverRatio = validRatios > 0 ? totalTurnoverRatio / validRatios : 0;
    
    res.json({
      success: true,
      message: 'Lấy dữ liệu tỷ lệ tồn kho thành công',
      data: {
        products: limitedProducts,
        summary: {
          averageTurnoverRatio: averageTurnoverRatio,
          totalProducts: totalProducts,
          fastMovingProducts: fastMovingProducts,
          slowMovingProducts: slowMovingProducts,
          outOfStockProducts: outOfStockProducts
        },
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

module.exports = {
  getInventory,
  getStoreInventory,
  addStock,
  reduceStock,
  getLowStockItems,
  getOutOfStockItems,
  checkStockAvailability,
  getProductInventory,
  getInventoryTurnover
};