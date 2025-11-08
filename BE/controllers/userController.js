const { validationResult } = require('express-validator');
const User = require('../models/User');

exports.listUsers = async (req, res, next) => {
  try {
    const users = await User.find().select('-passwordHash');
    res.json({ success: true, count: users.length, users });
  } catch (err) {
    next(err);
  }
};

exports.getUser = async (req, res, next) => {
  try {
    const user = await User.findById(req.params.id).select('-passwordHash');
    if (!user) return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    res.json({ success: true, user });
  } catch (err) {
    next(err);
  }
};

exports.updateUser = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ success: false, errors: errors.array() });
    }

    const updates = {};
    const allowed = ['username', 'email', 'phoneNumber', 'address', 'role', 'isActive'];
    allowed.forEach((k) => {
      if (typeof req.body[k] !== 'undefined') updates[k] = req.body[k];
    });

    const user = await User.findByIdAndUpdate(req.params.id, updates, { new: true, runValidators: true }).select('-passwordHash');
    if (!user) return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    res.json({ success: true, user });
  } catch (err) {
    next(err);
  }
};

exports.deleteUser = async (req, res, next) => {
  try {
    const user = await User.findByIdAndDelete(req.params.id);
    if (!user) return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    res.json({ success: true, message: 'Đã xóa người dùng' });
  } catch (err) {
    next(err);
  }
};

// Get total users count
exports.getTotalUsers = async (req, res, next) => {
  try {
    const { role, isActive } = req.query;
    
    // Build query
    const query = {};
    if (role) {
      query.role = role;
    }
    if (isActive !== undefined) {
      query.isActive = isActive === 'true';
    }
    
    // Count users
    const totalUsers = await User.countDocuments(query);
    
    // Get count by role if no specific role is requested
    let usersByRole = [];
    if (!role) {
      usersByRole = await User.aggregate([
        {
          $group: {
            _id: '$role',
            count: { $sum: 1 }
          }
        },
        { $sort: { count: -1 } }
      ]);
    }
    
    // Get active vs inactive count
    const activeUsers = await User.countDocuments({ ...query, isActive: true });
    const inactiveUsers = await User.countDocuments({ ...query, isActive: false });
    
    res.json({
      success: true,
      message: 'Lấy tổng số người dùng thành công',
      data: {
        totalUsers,
        activeUsers,
        inactiveUsers,
        usersByRole: usersByRole.map(item => ({
          role: item._id,
          count: item.count
        })),
        filters: {
          role: role || null,
          isActive: isActive !== undefined ? isActive === 'true' : null
        }
      }
    });
  } catch (err) {
    next(err);
  }
};