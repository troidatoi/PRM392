const express = require('express');
const router = express.Router();
const { getLocations } = require('../controllers/locationController');

// @route   GET /api/locations
// @desc    Get all location data
// @access  Public
router.get('/', getLocations);

module.exports = router;