const express = require('express');
const router = express.Router();
const {
  getBikes,
  getBikeById,
  createBike,
  updateBike,
  deleteBike,
  getFeaturedBikes,
  getCategories
} = require('../controllers/bikeController');
const { validateBike, validateBikeQuery, validateFeaturedQuery } = require('../middleware/validation');
const { uploadMultiple, handleUploadError } = require('../middleware/upload');
const { protect, authorize } = require('../middleware/auth');

// @route   GET /api/bikes
// @desc    Get all bikes with filtering and pagination
// @access  Public
router.get('/', validateBikeQuery, getBikes);

// @route   GET /api/bikes/:id
// @desc    Get single bike by ID
// @access  Public
router.get('/:id', getBikeById);

// @route   POST /api/bikes
// @desc    Create new bike
// @access  Private (Admin/Staff only)
router.post('/', protect, authorize('admin', 'staff'), uploadMultiple, handleUploadError, validateBike, createBike);

// @route   PUT /api/bikes/:id
// @desc    Update bike
// @access  Private (Admin/Staff only)
router.put('/:id', protect, authorize('admin', 'staff'), uploadMultiple, handleUploadError, validateBike, updateBike);

// @route   DELETE /api/bikes/:id
// @desc    Delete bike
// @access  Private (Admin/Staff only)
router.delete('/:id', protect, authorize('admin', 'staff'), deleteBike);

// @route   GET /api/bikes/featured/list
// @desc    Get featured bikes
// @access  Public
router.get('/featured/list', validateFeaturedQuery, getFeaturedBikes);

// @route   GET /api/bikes/categories/list
// @desc    Get all categories with count
// @access  Public
router.get('/categories/list', getCategories);

module.exports = router;
