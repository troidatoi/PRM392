const Inventory = require('../models/Inventory');
const Bike = require('../models/Bike');
const Store = require('../models/Store');

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

module.exports = {
  getInventory,
  getStoreInventory,
  addStock,
  reduceStock,
  getLowStockItems,
  getOutOfStockItems,
  checkStockAvailability,
  getProductInventory
};