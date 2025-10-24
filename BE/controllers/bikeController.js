const Bike = require('../models/Bike');
const { validationResult } = require('express-validator');
const { uploadToCloudinary } = require('../middleware/upload');

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

    // Process uploaded images
    let images = [];
    if (req.files && req.files.length > 0) {
      try {
        images = await uploadToCloudinary(req.files);
      } catch (uploadError) {
        console.error('Error uploading images:', uploadError);
        return res.status(500).json({
          success: false,
          message: 'Lỗi khi upload ảnh lên Cloudinary'
        });
      }
    }

    // Process specifications
    const specifications = {};
    if (req.body.battery) specifications.battery = req.body.battery;
    if (req.body.motor) specifications.motor = req.body.motor;
    if (req.body.range) specifications.range = req.body.range;
    if (req.body.maxSpeed) specifications.maxSpeed = req.body.maxSpeed;
    if (req.body.weight) specifications.weight = req.body.weight;
    if (req.body.chargingTime) specifications.chargingTime = req.body.chargingTime;

    // Process features (split by newline)
    let features = [];
    if (req.body.features) {
      features = req.body.features.split('\n').filter(feature => feature.trim() !== '');
    }

    // Process tags (split by comma)
    let tags = [];
    if (req.body.tags) {
      tags = req.body.tags.split(',').filter(tag => tag.trim() !== '');
    }

    // Process original price
    let originalPrice = 0;
    if (req.body.originalPrice && req.body.originalPrice !== '0') {
      originalPrice = parseFloat(req.body.originalPrice);
    }

    // Create bike data with all fields
    const bikeData = {
      name: req.body.name,
      brand: req.body.brand,
      model: req.body.model,
      price: parseFloat(req.body.price),
      description: req.body.description,
      stock: parseInt(req.body.stock),
      category: req.body.category,
      status: req.body.status,
      specifications: Object.keys(specifications).length > 0 ? specifications : undefined,
      features: features.length > 0 ? features : undefined,
      warranty: req.body.warranty || '12 tháng',
      originalPrice: originalPrice > 0 ? originalPrice : undefined,
      tags: tags.length > 0 ? tags : undefined,
      images: images
    };

    const bike = new Bike(bikeData);
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

    // Find existing bike
    const existingBike = await Bike.findById(req.params.id);
    if (!existingBike) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy xe đạp điện'
      });
    }

    // Process new uploaded images if any
    let newImages = [];
    if (req.files && req.files.length > 0) {
      try {
        newImages = await uploadToCloudinary(req.files);
      } catch (uploadError) {
        console.error('Error uploading new images:', uploadError);
        return res.status(500).json({
          success: false,
          message: 'Lỗi khi upload ảnh mới lên Cloudinary'
        });
      }
    }

    // Combine existing images with new images
    let allImages = [...existingBike.images];
    if (newImages.length > 0) {
      allImages = [...allImages, ...newImages];
    }

    // Create update data
    const updateData = {
      ...req.body,
      images: allImages
    };

    const bike = await Bike.findByIdAndUpdate(
      req.params.id,
      updateData,
      { new: true, runValidators: true }
    ).select('-__v');

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

