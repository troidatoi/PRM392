const Store = require('../models/Store');

// @desc    Create new store
// @route   POST /api/locations
// @access  Private/Admin
const createStore = async (req, res) => {
  try {
    const { name, latitude, longitude, address, city, district } = req.body;

    // Check for duplicate store by name and location
    const existingStore = await Store.findOne({
      $or: [
        { name: name },
        {
          latitude: latitude,
          longitude: longitude
        },
        {
          address: address,
          city: city,
          district: district
        }
      ]
    });

    if (existingStore) {
      return res.status(400).json({
        success: false,
        message: 'Cửa hàng đã tồn tại với tên, địa chỉ hoặc vị trí tương tự'
      });
    }

    const store = await Store.create(req.body);

    res.status(201).json({
      success: true,
      data: store
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Update store
// @route   PUT /api/locations/:id
// @access  Private/Admin
const updateStore = async (req, res) => {
  try {
    const store = await Store.findByIdAndUpdate(
      req.params.id,
      req.body,
      {
        new: true,
        runValidators: true
      }
    );

    if (!store) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy cửa hàng'
      });
    }

    res.status(200).json({
      success: true,
      data: store
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Get all store locations
// @route   GET /api/locations
// @access  Public
const getAllStores = async (req, res) => {
  try {
    const {
      city,
      district,
      storeType,
      isActive = true,
      page = 1,
      limit = 10,
      sort = 'rating.average'
    } = req.query;

    // Build filter object
    const filter = { isActive };
    
    if (city) filter.city = new RegExp(city, 'i');
    if (district) filter.district = new RegExp(district, 'i');
    if (storeType) filter.storeType = storeType;

    // Calculate pagination
    const pageNum = parseInt(page);
    const limitNum = parseInt(limit);
    const skip = (pageNum - 1) * limitNum;

    // Build sort object
    const sortObj = {};
    if (sort === 'rating.average') {
      sortObj['rating.average'] = -1;
    } else if (sort === 'name') {
      sortObj.name = 1;
    } else if (sort === 'createdAt') {
      sortObj.createdAt = -1;
    }

    const stores = await Store.find(filter)
      .sort(sortObj)
      .skip(skip)
      .limit(limitNum)
      .select('-__v');

    const total = await Store.countDocuments(filter);

    res.status(200).json({
      success: true,
      count: stores.length,
      total,
      page: pageNum,
      pages: Math.ceil(total / limitNum),
      data: stores
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Get store by ID
// @route   GET /api/locations/:id
// @access  Public
const getStoreById = async (req, res) => {
  try {
    const store = await Store.findById(req.params.id);

    if (!store) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy cửa hàng'
      });
    }

    res.status(200).json({
      success: true,
      data: store
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Delete store
// @route   DELETE /api/locations/:id
// @access  Private/Admin
const deleteStore = async (req, res) => {
  try {
    const store = await Store.findById(req.params.id);

    if (!store) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy cửa hàng'
      });
    }

    await store.deleteOne();

    res.status(200).json({
      success: true,
      message: 'Xóa cửa hàng thành công'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Get stores near a location
// @route   GET /api/locations/nearby
// @access  Public
const getNearbyStores = async (req, res) => {
  try {
    // Only accept request body
    const { lat, lng, radius = 10, limit = 10 } = req.body;

    if (!lat || !lng) {
      return res.status(400).json({
        success: false,
        message: 'Vĩ độ và kinh độ là bắt buộc'
      });
    }

    const latitude = parseFloat(lat);
    const longitude = parseFloat(lng);
    const radiusKm = parseFloat(radius);
    const limitNum = parseInt(limit);

    if (isNaN(latitude) || isNaN(longitude) || isNaN(radiusKm)) {
      return res.status(400).json({
        success: false,
        message: 'Tọa độ và bán kính không hợp lệ'
      });
    }

    // Get all active stores first
    const allStores = await Store.find({ isActive: true }).select('-__v');

    // Calculate distance for each store and filter by radius
    const storesWithDistance = allStores
      .map(store => {
        const distance = store.calculateDistance(latitude, longitude);
        return {
          ...store.toObject(),
          distance: Math.round(distance * 100) / 100 // Round to 2 decimal places
        };
      })
      .filter(store => store.distance <= radiusKm) // Filter by radius
      .sort((a, b) => a.distance - b.distance) // Sort by distance
      .slice(0, limitNum); // Limit results

    res.status(200).json({
      success: true,
      count: storesWithDistance.length,
      userLocation: {
        latitude,
        longitude
      },
      searchRadius: radius,
      data: storesWithDistance
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

module.exports = {
  createStore,
  updateStore,
  getAllStores,
  getStoreById,
  deleteStore,
  getNearbyStores
};
