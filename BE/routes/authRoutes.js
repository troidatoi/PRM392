const express = require('express');
const { body } = require('express-validator');
const { register, login, logout, getMe, updateProfile, changePassword } = require('../controllers/authController');
const { protect } = require('../middleware/auth');

const router = express.Router();

// Auth
router.post(
  '/register',
  [
    body('username').isLength({ min: 3 }).withMessage('Tên đăng nhập tối thiểu 3 ký tự'),
    body('email').isEmail().withMessage('Email không hợp lệ'),
    body('password').isLength({ min: 6 }).withMessage('Mật khẩu tối thiểu 6 ký tự')
  ],
  register
);

router.post(
  '/login',
  [
    body('usernameOrEmail').notEmpty().withMessage('Vui lòng nhập tên đăng nhập hoặc email'),
    body('password').notEmpty().withMessage('Vui lòng nhập mật khẩu')
  ],
  login
);

router.post('/logout', protect, logout);

// Me
router.get('/me', protect, getMe);
router.put('/me', protect, updateProfile);
router.put('/me/change-password', protect, changePassword);

module.exports = router;


