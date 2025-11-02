const path = require('path');
const fs = require('fs');

// @desc    Get all location data from JSON file
// @route   GET /api/locations
// @access  Public
exports.getLocations = (req, res, next) => {
  try {
    const dataPath = path.join(__dirname, '..', 'data', 'locations.json');
    const locations = JSON.parse(fs.readFileSync(dataPath, 'utf-8'));

    res.status(200).json({
      success: true,
      count: locations.length,
      data: locations
    });
  } catch (error) {
    console.error('Could not read or parse locations.json:', error);
    // Pass a generic error to the error handler
    next(new Error('Could not retrieve location data at this time.'));
  }
};
