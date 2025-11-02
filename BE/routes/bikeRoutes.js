const express = require('express');
const router = express.Router();
const {
  getBikes,
  getBikeById,
  createBike,
  updateBike,
  deleteBike,
  getFeaturedBikes,
  getCategories,
  uploadImages
} = require('../controllers/bikeController');
const { validateBike, validateBikeQuery, validateFeaturedQuery } = require('../middleware/validation');
const { uploadMultiple, handleUploadError } = require('../middleware/upload');
const { protect, authorize } = require('../middleware/auth');

// @route   GET /api/bikes
// @desc    Get all bikes with filtering and pagination
// @access  Public
router.get('/', validateBikeQuery, getBikes);

// @route   POST /api/bikes/upload
// @desc    Upload images to Cloudinary
// @access  Public
router.post('/upload', uploadMultiple, handleUploadError, (req, res, next) => {
  console.log('Route /upload hit!');
  uploadImages(req, res, next);
});

// @route   GET /api/bikes/featured/list
// @desc    Get featured bikes
// @access  Public
router.get('/featured/list', validateFeaturedQuery, getFeaturedBikes);

// @route   GET /api/bikes/categories/list
// @desc    Get all categories with count
// @access  Public
router.get('/categories/list', getCategories);

// @route   POST /api/bikes
// @desc    Create new bike
// @access  Private (Admin/Staff only)
router.post('/', protect, authorize('admin', 'staff'), uploadMultiple, handleUploadError, validateBike, createBike);

// @route   GET /api/bikes/:id
// @desc    Get single bike by ID
// @access  Public
router.get('/:id', getBikeById);

// @route   PUT /api/bikes/:id
// @desc    Update bike
// @access  Private (Admin/Staff only)
router.put('/:id', protect, authorize('admin', 'staff'), uploadMultiple, handleUploadError, validateBike, updateBike);

// @route   DELETE /api/bikes/:id
// @desc    Delete bike
// @access  Private (Admin/Staff only)
router.delete('/:id', protect, authorize('admin', 'staff'), deleteBike);

module.exports = router;
