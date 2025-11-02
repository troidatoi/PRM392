const express = require('express');
const router = express.Router();
const { getLocations } = require('../controllers/locationController');

// Route to get all location data
router.route('/').get(getLocations);

module.exports = router;
