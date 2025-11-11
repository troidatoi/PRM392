const Store = require('../models/Store');
const { geocodeAddress, reverseGeocode } = require('../utils/geocoding');

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
      isActive,
      page = 1,
      limit = 10,
      sort = 'rating.average'
    } = req.query;

    // Build filter object
    const filter = {};
    
    // Only filter by isActive if explicitly provided
    if (isActive !== undefined && isActive !== null && isActive !== '') {
      filter.isActive = isActive === 'true' || isActive === true;
    }
    
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

// @desc    Geocode address to coordinates
// @route   POST /api/locations/geocode
// @access  Public
const geocodeLocation = async (req, res) => {
  try {
    const { address } = req.body;

    if (!address || typeof address !== 'string' || address.trim() === '') {
      return res.status(400).json({
        success: false,
        message: 'Địa chỉ là bắt buộc'
      });
    }

    const geocodeResult = await geocodeAddress(address.trim());

    res.status(200).json({
      success: true,
      data: geocodeResult
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message || 'Không thể geocode địa chỉ'
    });
  }
};

// @desc    Reverse geocode coordinates to address
// @route   POST /api/locations/reverse-geocode
// @access  Public
const reverseGeocodeLocation = async (req, res) => {
  try {
    const { lat, lng } = req.body;

    if (!lat || !lng) {
      return res.status(400).json({
        success: false,
        message: 'Vĩ độ và kinh độ là bắt buộc'
      });
    }

    const latitude = parseFloat(lat);
    const longitude = parseFloat(lng);

    if (isNaN(latitude) || isNaN(longitude)) {
      return res.status(400).json({
        success: false,
        message: 'Tọa độ không hợp lệ'
      });
    }

    const addressResult = await reverseGeocode(latitude, longitude);

    res.status(200).json({
      success: true,
      data: addressResult
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message || 'Không thể reverse geocode tọa độ'
    });
  }
};

// @desc    Get stores near a location (supports both coordinates and address)
// @route   GET /api/locations/nearby
// @route   POST /api/locations/nearby
// @access  Public
const getNearbyStores = async (req, res) => {
  try {
    // Support both query params (GET) and body (POST)
    const { lat, lng, address, radius = 10, limit = 20 } = req.method === 'GET' ? req.query : req.body;

    let latitude, longitude, userAddress = null;

    // If address is provided, geocode it first
    if (address && (!lat || !lng)) {
      try {
        const geocodeResult = await geocodeAddress(address.trim());
        latitude = geocodeResult.latitude;
        longitude = geocodeResult.longitude;
        userAddress = geocodeResult.formattedAddress;
      } catch (error) {
        return res.status(400).json({
          success: false,
          message: error.message || 'Không thể tìm địa chỉ'
        });
      }
    } 
    // Otherwise use coordinates
    else if (lat && lng) {
      latitude = parseFloat(lat);
      longitude = parseFloat(lng);

      if (isNaN(latitude) || isNaN(longitude)) {
        return res.status(400).json({
          success: false,
          message: 'Tọa độ không hợp lệ'
        });
      }

      // Try to get address from coordinates
      try {
        const addressResult = await reverseGeocode(latitude, longitude);
        userAddress = addressResult.formattedAddress;
      } catch (error) {
        // If reverse geocode fails, continue without address
        console.log('Reverse geocode failed:', error.message);
      }
    } 
    else {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp địa chỉ hoặc tọa độ (lat, lng)'
      });
    }

    const radiusKm = parseFloat(radius);
    const limitNum = parseInt(limit);

    if (isNaN(radiusKm) || radiusKm <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Bán kính tìm kiếm không hợp lệ'
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
        longitude,
        address: userAddress
      },
      searchRadius: radiusKm,
      data: storesWithDistance
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Get stores optimized for map display
// @route   GET /api/locations/map
// @access  Public
const getStoresForMap = async (req, res) => {
  try {
    const { lat, lng, address, radius = 50 } = req.query;

    let latitude, longitude;

    // If address is provided, geocode it
    if (address && (!lat || !lng)) {
      try {
        const geocodeResult = await geocodeAddress(address.trim());
        latitude = geocodeResult.latitude;
        longitude = geocodeResult.longitude;
      } catch (error) {
        return res.status(400).json({
          success: false,
          message: error.message || 'Không thể tìm địa chỉ'
        });
      }
    } 
    // Use coordinates if provided
    else if (lat && lng) {
      latitude = parseFloat(lat);
      longitude = parseFloat(lng);

      if (isNaN(latitude) || isNaN(longitude)) {
        return res.status(400).json({
          success: false,
          message: 'Tọa độ không hợp lệ'
        });
      }
    }

    // Get all active stores
    const resolveIsOpenNow = (store) => {
      if (!store || !store.operatingHours) {
        return !!store?.isActive;
      }

      // Lấy thời gian hiện tại theo timezone Việt Nam (GMT+7)
      const now = new Date();
      const vietnamOffset = 7 * 60; // GMT+7 in minutes
      const utc = now.getTime() + (now.getTimezoneOffset() * 60000);
      const vietnamTime = new Date(utc + (vietnamOffset * 60000));

      const dayNames = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
      const dayName = dayNames[vietnamTime.getDay()];

      const daySchedule = store.operatingHours[dayName];
      if (!daySchedule || !daySchedule.isOpen) {
        return false;
      }

      const openTime = daySchedule.open;
      const closeTime = daySchedule.close;
      if (!openTime || !closeTime) {
        return false;
      }

      // Chuyển đổi thời gian sang phút từ 00:00 để so sánh chính xác
      const timeToMinutes = (timeStr) => {
        const [hours, minutes] = timeStr.split(':').map(Number);
        return hours * 60 + minutes;
      };

      const currentMinutes = vietnamTime.getHours() * 60 + vietnamTime.getMinutes();
      const openMinutes = timeToMinutes(openTime);
      const closeMinutes = timeToMinutes(closeTime);

      // Xử lý trường hợp đóng cửa sau nửa đêm (ví dụ: 22:00 - 02:00)
      if (closeMinutes < openMinutes) {
        // Cửa hàng mở qua đêm
        return currentMinutes >= openMinutes || currentMinutes <= closeMinutes;
      }

      return currentMinutes >= openMinutes && currentMinutes <= closeMinutes;
    };

    let allStores = await Store.find({ isActive: true })
      .select('name latitude longitude address city phone operatingHours isActive')
      .lean();

    allStores = allStores.map(store => ({
      ...store,
      isOpenNow: resolveIsOpenNow(store)
    }));

    // If location provided, calculate distances and filter by radius
    if (latitude && longitude) {
      const radiusKm = parseFloat(radius);
      // Helper function to calculate distance (same logic as Store model)
      const calculateDistance = (storeLat, storeLng, userLat, userLng) => {
        const R = 6371; // Earth's radius in kilometers
        const dLat = (userLat - storeLat) * Math.PI / 180;
        const dLng = (userLng - storeLng) * Math.PI / 180;
        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
          Math.cos(storeLat * Math.PI / 180) * Math.cos(userLat * Math.PI / 180) *
          Math.sin(dLng/2) * Math.sin(dLng/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c; // Distance in kilometers
      };

      allStores = allStores
        .map(store => {
          const distance = calculateDistance(store.latitude, store.longitude, latitude, longitude);
          return {
            ...store,
            distance: Math.round(distance * 100) / 100
          };
        })
        .filter(store => store.distance <= radiusKm)
        .sort((a, b) => a.distance - b.distance);
    }

    // Format response for map markers
    const storesForMap = allStores.map(store => ({
      id: store._id,
      name: store.name,
      latitude: store.latitude,
      longitude: store.longitude,
      address: store.address,
      city: store.city,
      phone: store.phone,
      isActive: store.isActive,
      operatingHours: store.operatingHours || null,
      isOpenNow: store.isOpenNow,
      distance: store.distance || null
    }));

    res.status(200).json({
      success: true,
      count: storesForMap.length,
      userLocation: latitude && longitude ? { latitude, longitude } : null,
      searchRadius: latitude && longitude ? parseFloat(radius) : null,
      data: storesForMap
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
  getNearbyStores,
  geocodeLocation,
  reverseGeocodeLocation,
  getStoresForMap
};
