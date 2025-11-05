const express = require('express');
const router = express.Router();
const {
  getInventory,
  getStoreInventory,
  addStock,
  reduceStock,
  getLowStockItems,
  getOutOfStockItems,
  checkStockAvailability,
  getProductInventory
} = require('../controllers/inventoryController');
const { protect } = require('../middleware/auth');

// Get inventory for a specific product at a store
router.get('/product', getInventory);

// Get all stores with inventory for a specific product
router.get('/product/:productId', getProductInventory);

// Get all inventory for a store
router.get('/store/:storeId', getStoreInventory);

// Add stock to inventory
router.post('/add', protect, addStock);

// Reduce stock from inventory
router.post('/reduce', protect, reduceStock);

// Get low stock items
router.get('/store/:storeId/low-stock', getLowStockItems);

// Get out of stock items
router.get('/store/:storeId/out-of-stock', getOutOfStockItems);

// Check stock availability
router.post('/check-availability', checkStockAvailability);

module.exports = router;