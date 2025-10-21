const jwt = require('jsonwebtoken');
const { validationResult } = require('express-validator');
const User = require('../models/User');

const signToken = (userId) => {
  return jwt.sign({ id: userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRE || '7d'
  });
};

exports.register = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ success: false, errors: errors.array() });
    }

    const { username, email, password, phoneNumber, address, firstName, lastName } = req.body;

    const existing = await User.findOne({ $or: [{ username }, { email }] });
    if (existing) {
      return res.status(400).json({ success: false, message: 'Tên đăng nhập hoặc email đã tồn tại' });
    }

    const user = await User.create({
      username,
      email,
      passwordHash: password,
      phoneNumber: phoneNumber || undefined,
      address: address || undefined,
      profile: {
        firstName: firstName || undefined,
        lastName: lastName || undefined
      }
    });

    const token = signToken(user._id);
    res.status(201).json({
      success: true,
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,
        isActive: user.isActive
      }
    });
  } catch (err) {
    next(err);
  }
};

exports.login = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ success: false, errors: errors.array() });
    }

    const { usernameOrEmail, password } = req.body;
    const user = await User.findOne({
      $or: [{ username: usernameOrEmail }, { email: usernameOrEmail }]
    });

    if (!user) {
      return res.status(401).json({ success: false, message: 'Tài khoản không tồn tại' });
    }

    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({ success: false, message: 'Mật khẩu không đúng' });
    }

    await user.updateLastLogin();
    const token = signToken(user._id);
    res.json({
      success: true,
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        role: user.role
      }
    });
  } catch (err) {
    next(err);
  }
};

exports.logout = async (req, res) => {
  // JWT stateless: client should discard token. Provide 200 OK.
  res.json({ success: true, message: 'Đăng xuất thành công' });
};

exports.getMe = async (req, res) => {
  res.json({ success: true, user: req.user });
};

exports.updateProfile = async (req, res, next) => {
  try {
    const updates = {};
    const allowed = ['phoneNumber', 'address'];
    allowed.forEach((k) => {
      if (typeof req.body[k] !== 'undefined') updates[k] = req.body[k];
    });

    const profileAllowed = ['firstName', 'lastName', 'avatar', 'dateOfBirth'];
    profileAllowed.forEach((k) => {
      if (typeof req.body[k] !== 'undefined') {
        if (!updates.profile) updates.profile = {};
        updates.profile[k] = req.body[k];
      }
    });

    const user = await User.findByIdAndUpdate(req.user._id, updates, { new: true, runValidators: true }).select('-passwordHash');
    res.json({ success: true, user });
  } catch (err) {
    next(err);
  }
};

exports.changePassword = async (req, res, next) => {
  try {
    const { currentPassword, newPassword } = req.body;
    const user = await User.findById(req.user._id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }
    const isMatch = await user.comparePassword(currentPassword);
    if (!isMatch) {
      return res.status(400).json({ success: false, message: 'Mật khẩu hiện tại không đúng' });
    }
    user.passwordHash = newPassword;
    await user.save();
    res.json({ success: true, message: 'Đổi mật khẩu thành công' });
  } catch (err) {
    next(err);
  }
};


