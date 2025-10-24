const express = require('express');
const router = express.Router();
const {
  getAllStores,
  getStoreById,
  createStore,
  updateStore,
  deleteStore,
  getNearbyStores
} = require('../controllers/locationController');

// Public routes
router.get('/', getAllStores);
router.post('/nearby', getNearbyStores); // Must be before /:id route
router.get('/nearby', (req, res) => {
  res.status(405).json({
    success: false,
    message: 'Method not allowed. Use POST instead.'
  });
});
router.get('/:id', getStoreById);

// Protected routes (require authentication)
// router.post('/', auth, createStore);
// router.put('/:id', auth, updateStore);
// router.delete('/:id', auth, deleteStore);

// For now, allow all operations (remove auth middleware for testing)
router.post('/', createStore);
router.put('/:id', updateStore);
router.delete('/:id', deleteStore);

module.exports = router;
