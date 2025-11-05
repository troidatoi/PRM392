const ShippingRate = require('../models/ShippingRate');

// @desc    Get all shipping rates
// @route   GET /api/shipping-rates
// @access  Public (or Admin for all, Public for active only)
const getAllShippingRates = async (req, res) => {
  try {
    const { activeOnly = 'false' } = req.query;
    const filter = activeOnly === 'true' ? { isActive: true } : {};
    
    const rates = await ShippingRate.find(filter).sort({ minDistance: 1 });
    
    res.status(200).json({
      success: true,
      data: rates
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Get single shipping rate
// @route   GET /api/shipping-rates/:id
// @access  Public
const getShippingRateById = async (req, res) => {
  try {
    const rate = await ShippingRate.findById(req.params.id);
    
    if (!rate) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy bảng giá phí ship'
      });
    }
    
    res.status(200).json({
      success: true,
      data: rate
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Create shipping rate
// @route   POST /api/shipping-rates
// @access  Private/Admin
const createShippingRate = async (req, res) => {
  try {
    const { minDistance, maxDistance, pricePerKm, note, order } = req.body;
    
    // Validate overlapping ranges
    const existingRates = await ShippingRate.find({ isActive: true });
    for (const existing of existingRates) {
      const existingMax = existing.maxDistance === null ? Infinity : existing.maxDistance;
      const newMax = maxDistance === null ? Infinity : maxDistance;
      
      if (
        (minDistance >= existing.minDistance && minDistance <= existingMax) ||
        (newMax >= existing.minDistance && newMax <= existingMax) ||
        (minDistance <= existing.minDistance && newMax >= existingMax)
      ) {
        return res.status(400).json({
          success: false,
          message: `Khoảng cách này trùng với khoảng cách đã có: ${existing.minDistance} - ${existing.maxDistance || '∞'} km`
        });
      }
    }
    
    const rate = await ShippingRate.create({
      minDistance,
      maxDistance: maxDistance || null,
      pricePerKm,
      note,
      order: order || 0
    });
    
    res.status(201).json({
      success: true,
      data: rate
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Update shipping rate (only price, note, and isActive can be updated)
// @route   PUT /api/shipping-rates/:id
// @access  Private/Admin
const updateShippingRate = async (req, res) => {
  try {
    const { pricePerKm, note, isActive } = req.body;
    
    // Find the current rate
    const currentRate = await ShippingRate.findById(req.params.id);
    if (!currentRate) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy bảng giá phí ship'
      });
    }
    
    // Only allow updating pricePerKm, note, and isActive
    // minDistance, maxDistance, and order are fixed
    const updateData = {};
    if (pricePerKm !== undefined) {
      updateData.pricePerKm = pricePerKm;
    }
    if (note !== undefined) {
      updateData.note = note;
    }
    if (isActive !== undefined) {
      updateData.isActive = isActive;
    }
    
    const rate = await ShippingRate.findByIdAndUpdate(
      req.params.id,
      updateData,
      {
        new: true,
        runValidators: true
      }
    );
    
    if (!rate) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy bảng giá phí ship'
      });
    }
    
    res.status(200).json({
      success: true,
      data: rate
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Delete shipping rate
// @route   DELETE /api/shipping-rates/:id
// @access  Private/Admin
const deleteShippingRate = async (req, res) => {
  try {
    const rate = await ShippingRate.findByIdAndDelete(req.params.id);
    
    if (!rate) {
      return res.status(404).json({
        success: false,
        message: 'Không tìm thấy bảng giá phí ship'
      });
    }
    
    res.status(200).json({
      success: true,
      message: 'Đã xóa bảng giá phí ship',
      data: rate
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

// @desc    Calculate shipping fee for a distance
// @route   POST /api/shipping-rates/calculate
// @access  Public
const calculateShippingFee = async (req, res) => {
  try {
    const { distanceKm } = req.body;
    
    if (!distanceKm || distanceKm < 0) {
      return res.status(400).json({
        success: false,
        message: 'Khoảng cách không hợp lệ'
      });
    }
    
    const fee = await ShippingRate.calculateFee(distanceKm);
    
    res.status(200).json({
      success: true,
      data: {
        distanceKm,
        roundedDistanceKm: Math.ceil(distanceKm),
        shippingFee: fee
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || 'Server Error'
    });
  }
};

module.exports = {
  getAllShippingRates,
  getShippingRateById,
  createShippingRate,
  updateShippingRate,
  deleteShippingRate,
  calculateShippingFee
};

