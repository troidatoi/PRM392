const express = require('express');
const passport = require('passport');
const { body } = require('express-validator');
const { 
  register, 
  login, 
  logout, 
  getMe, 
  updateProfile, 
  changePassword,
  googleAuthCallback,
  forgotPassword,
  resetPassword
} = require('../controllers/authController');
const { protect } = require('../middleware/auth');
const { handleGoogleAuthError } = require('../middleware/googleAuth');

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

// Google OAuth routes
router.get('/google', (req, res, next) => {
  // Check if Google OAuth is configured
  if (!process.env.GOOGLE_CLIENT_ID || 
      process.env.GOOGLE_CLIENT_ID === 'temporary_disabled') {
    return res.status(503).json({
      success: false,
      message: 'Google OAuth chưa được cấu hình. Vui lòng liên hệ admin.'
    });
  }
  
  passport.authenticate('google', { 
    scope: ['profile', 'email'],
    session: false
  })(req, res, next);
});

router.get('/google/callback', (req, res, next) => {
  // Check if Google OAuth is configured
  if (!process.env.GOOGLE_CLIENT_ID || 
      process.env.GOOGLE_CLIENT_ID === 'temporary_disabled') {
    return res.status(503).json({
      success: false,
      message: 'Google OAuth chưa được cấu hình'
    });
  }
  
  passport.authenticate('google', { 
    session: false 
  }, (err, user, info) => {
    if (err) {
      return handleGoogleAuthError(err, req, res, next);
    }
    if (!user) {
      const error = new Error(info?.message || 'Xác thực Google thất bại');
      return handleGoogleAuthError(error, req, res, next);
    }
    req.user = user;
    googleAuthCallback(req, res, next);
  })(req, res, next);
});

// Password reset routes
router.post('/forgot-password',
  [
    body('email')
      .isEmail()
      .normalizeEmail()
      .withMessage('Email không hợp lệ')
  ],
  forgotPassword
);

router.put('/reset-password/:resettoken',
  [
    body('password')
      .isLength({ min: 6 })
      .withMessage('Mật khẩu phải có ít nhất 6 ký tự')
  ],
  resetPassword
);

// Me
router.get('/me', protect, getMe);
router.put('/me', protect, updateProfile);
router.put('/me/change-password', protect, changePassword);

module.exports = router;


