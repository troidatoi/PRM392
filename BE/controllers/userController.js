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