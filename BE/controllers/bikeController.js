const Bike = require('../models/Bike');
const { validationResult } = require('express-validator');

// @desc    Get all bikes with filtering and pagination
// @route   GET /api/bikes
// @access  Public
const getBikes = async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Dữ liệu không hợp lệ',
        errors: errors.array()
      });
    }

    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    // Build filter object
    const filter = {};
    
    if (req.query.category) {
      filter.category = req.query.category;
    }
    
    if (req.query.status) {
      filter.status = req.query.status;
    }
    
    if (req.query.brand) {
      filter.brand = new RegExp(req.query.brand, 'i');
    }
    
    if (req.query.minPrice || req.query.maxPrice) {
      filter.price = {};
      if (req.query.minPrice) {
        filter.price.$gte = parseFloat(req.query.minPrice);
      }
      if (req.query.maxPrice) {
        filter.price.$lte = parseFloat(req.query.maxPrice);
      }
    }
    
    if (req.query.search) {
      filter.$text = { $search: req.query.search };
    }

    // Build sort object
    let sort = { createdAt: -1 };
    if (req.query.sortBy) {
      switch (req.query.sortBy) {
        case 'price_asc':
          sort = { price: 1 };
          break;
        case 'price_desc':
          sort = { price: -1 };
          break;
        case 'rating':
          sort = { 'rating.average': -1 };
          break;
        case 'name':
          sort = { name: 1 };
          break;
        default:
          sort = { createdAt: -1 };
      }
    }

    const bikes = await Bike.find(filter)
      .sort(sort)
      .skip(skip)
      .limit(limit)
      .select('-__v');

    const total = await Bike.countDocuments(filter);
    const totalPages = Math.ceil(total / limit);

    res.json({
      success: true,
      data: bikes,
      pagination: {
        currentPage: page,
        totalPages,
        totalItems: total,
        itemsPerPage: limit,
        hasNextPage: page < totalPages,
        hasPrevPage: page > 1
      }
    });
  } catch (error) {
    console.error('Error fetching bikes:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi lấy danh sách xe đạp điện'
    });
  }
};

// @desc    Get single bike by ID
// @route   GET /api/bikes/:id
// @access  Public
const getBikeById = async (req, res) => {
  try {
    const bike = await Bike.findById(req.params.id).select('-__v');
    
    if (!bike) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy xe đạp điện'
      });
    }

    res.json({
      success: true,
      data: bike
    });
  } catch (error) {
    console.error('Error fetching bike:', error);
    if (error.name === 'CastError') {
      return res.status(400).json({
        success: false,
        message: 'ID không hợp lệ'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi lấy thông tin xe đạp điện'
    });
  }
};

// @desc    Create new bike
// @route   POST /api/bikes
// @access  Private (Admin only - sẽ implement sau)
const createBike = async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Dữ liệu không hợp lệ',
        errors: errors.array()
      });
    }

    const bike = new Bike(req.body);
    await bike.save();

    res.status(201).json({
      success: true,
      message: 'Tạo xe đạp điện thành công',
      data: bike
    });
  } catch (error) {
    console.error('Error creating bike:', error);
    if (error.name === 'ValidationError') {
      return res.status(400).json({
        success: false,
        message: 'Dữ liệu không hợp lệ',
        errors: Object.values(error.errors).map(err => ({
          field: err.path,
          message: err.message
        }))
      });
    }
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi tạo xe đạp điện'
    });
  }
};

// @desc    Update bike
// @route   PUT /api/bikes/:id
// @access  Private (Admin only - sẽ implement sau)
const updateBike = async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({
        success: false,
        message: 'Dữ liệu không hợp lệ',
        errors: errors.array()
      });
    }

    const bike = await Bike.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true, runValidators: true }
    ).select('-__v');

    if (!bike) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy xe đạp điện'
      });
    }

    res.json({
      success: true,
      message: 'Cập nhật xe đạp điện thành công',
      data: bike
    });
  } catch (error) {
    console.error('Error updating bike:', error);
    if (error.name === 'CastError') {
      return res.status(400).json({
        success: false,
        message: 'ID không hợp lệ'
      });
    }
    if (error.name === 'ValidationError') {
      return res.status(400).json({
        success: false,
        message: 'Dữ liệu không hợp lệ',
        errors: Object.values(error.errors).map(err => ({
          field: err.path,
          message: err.message
        }))
      });
    }
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi cập nhật xe đạp điện'
    });
  }
};

// @desc    Delete bike
// @route   DELETE /api/bikes/:id
// @access  Private (Admin only - sẽ implement sau)
const deleteBike = async (req, res) => {
  try {
    const bike = await Bike.findByIdAndDelete(req.params.id);

    if (!bike) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy xe đạp điện'
      });
    }

    res.json({
      success: true,
      message: 'Xóa xe đạp điện thành công'
    });
  } catch (error) {
    console.error('Error deleting bike:', error);
    if (error.name === 'CastError') {
      return res.status(400).json({
        success: false,
        message: 'ID không hợp lệ'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi xóa xe đạp điện'
    });
  }
};

// @desc    Get featured bikes
// @route   GET /api/bikes/featured/list
// @access  Public
const getFeaturedBikes = async (req, res) => {
  try {
    const limit = parseInt(req.query.limit) || 8;
    
    const bikes = await Bike.find({ 
      isFeatured: true, 
      status: 'available' 
    })
    .sort({ 'rating.average': -1, createdAt: -1 })
    .limit(limit)
    .select('-__v');

    res.json({
      success: true,
      data: bikes
    });
  } catch (error) {
    console.error('Error fetching featured bikes:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi lấy danh sách xe nổi bật'
    });
  }
};

// @desc    Get all categories with count
// @route   GET /api/bikes/categories/list
// @access  Public
const getCategories = async (req, res) => {
  try {
    const categories = await Bike.aggregate([
      {
        $group: {
          _id: '$category',
          count: { $sum: 1 },
          availableCount: {
            $sum: { $cond: [{ $eq: ['$status', 'available'] }, 1, 0] }
          }
        }
      },
      {
        $sort: { count: -1 }
      }
    ]);

    const categoryNames = {
      city: 'Xe đạp điện thành phố',
      mountain: 'Xe đạp điện leo núi',
      folding: 'Xe đạp điện gấp',
      cargo: 'Xe đạp điện chở hàng',
      sport: 'Xe đạp điện thể thao',
      other: 'Khác'
    };

    const formattedCategories = categories.map(cat => ({
      id: cat._id,
      name: categoryNames[cat._id] || cat._id,
      count: cat.count,
      availableCount: cat.availableCount
    }));

    res.json({
      success: true,
      data: formattedCategories
    });
  } catch (error) {
    console.error('Error fetching categories:', error);
    res.status(500).json({
      success: false,
      message: 'Lỗi server khi lấy danh sách danh mục'
    });
  }
};

module.exports = {
  getBikes,
  getBikeById,
  createBike,
  updateBike,
  deleteBike,
  getFeaturedBikes,
  getCategories
};

