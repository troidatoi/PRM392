const express = require('express');
const router = express.Router();
const {
  getAllStores,
  getStoreById,
  createStore,
  updateStore,
  deleteStore,
  getNearbyStores,
  geocodeLocation,
  reverseGeocodeLocation,
  getStoresForMap
} = require('../controllers/storeController');

// Public routes
router.get('/', getAllStores);

// Geocoding routes (must be before /:id route)
router.post('/geocode', geocodeLocation);
router.post('/reverse-geocode', reverseGeocodeLocation);

// Map and nearby stores routes (must be before /:id route)
router.get('/map', getStoresForMap);
router.get('/nearby', getNearbyStores); // Support GET with query params
router.post('/nearby', getNearbyStores); // Support POST with body

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
