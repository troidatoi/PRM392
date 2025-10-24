const express = require('express');
const router = express.Router();
const {
  createCart,
  addItemToCart,
  removeItemFromCart,
  updateCartItemQuantity,
  clearCart,
  getCart,
  updateItemPrice,
  checkPriceChanges,
  lockAllPrices,
  getPriceHistory
} = require('../controllers/cartController');
const { protect } = require('../middleware/auth');

// Create new cart
router.post('/create', protect, createCart);

// Add item to cart
router.post('/add-item', protect, addItemToCart);

// Remove item from cart
router.delete('/remove-item/:itemId', protect, removeItemFromCart);

// Update cart item quantity
router.put('/update-quantity/:itemId', protect, updateCartItemQuantity);

// Clear cart
router.delete('/clear/:userId', protect, clearCart);

// Get cart with updated prices
router.get('/user/:userId', protect, getCart);

// Update item price (lock/unlock/update)
router.put('/item/:itemId/price', protect, updateItemPrice);

// Check price changes before checkout
router.get('/user/:userId/price-changes', protect, checkPriceChanges);

// Lock all prices in cart
router.post('/user/:userId/lock-prices', protect, lockAllPrices);

// Get price history for an item
router.get('/item/:itemId/price-history', protect, getPriceHistory);

module.exports = router;



