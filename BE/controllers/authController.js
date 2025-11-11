const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const { validationResult } = require('express-validator');
const User = require('../models/User');
const sendEmail = require('../utils/sendEmail');

const signToken = (userId) => {
  return jwt.sign({ id: userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRE || '7d'
  });
};

const sendTokenResponse = (user, statusCode, res) => {
  const token = signToken(user._id);
  
  const options = {
    expires: new Date(
      Date.now() + (process.env.JWT_COOKIE_EXPIRE || 7) * 24 * 60 * 60 * 1000
    ),
    httpOnly: true
  };

  if (process.env.NODE_ENV === 'production') {
    options.secure = true;
  }

  res.status(statusCode)
    .cookie('token', token, options)
    .json({
      success: true,
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,
        isActive: user.isActive,
        googleId: user.googleId ? true : false, // Chỉ trả về boolean để bảo mật
        profile: user.profile
      }
    });
};

exports.register = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ success: false, errors: errors.array() });
    }

    const { username, email, password, phoneNumber, address, firstName, lastName } = req.body;

    // Kiểm tra username đã tồn tại
    const existingUsername = await User.findOne({ username });
    if (existingUsername) {
      return res.status(400).json({ success: false, message: 'Tên đăng nhập đã được sử dụng' });
    }

    // Kiểm tra email đã tồn tại
    const existingEmail = await User.findOne({ email });
    if (existingEmail) {
      return res.status(400).json({ success: false, message: 'Email đã được đăng ký' });
    }

    // Kiểm tra số điện thoại đã tồn tại (nếu có)
    if (phoneNumber) {
      const existingPhone = await User.findOne({ phoneNumber });
      if (existingPhone) {
        return res.status(400).json({ success: false, message: 'Số điện thoại đã được đăng ký' });
      }
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

    // Gửi email chào mừng (không block response nếu thất bại)
    try {
      const userName = firstName && lastName 
        ? `${firstName} ${lastName}` 
        : firstName || lastName || username;
      
      const message = `
Chào mừng ${userName} đến với Electric Bike Shop!

Cảm ơn bạn đã đăng ký tài khoản với chúng tôi. Tài khoản của bạn đã được tạo thành công.

Thông tin tài khoản:
- Tên đăng nhập: ${username}
- Email: ${email}
${phoneNumber ? `- Số điện thoại: ${phoneNumber}` : ''}

Bạn có thể bắt đầu mua sắm ngay bây giờ!

Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.

Trân trọng,
Đội ngũ Electric Bike Shop
      `.trim();

      const htmlMessage = `
        <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif; background-color: #f9f9f9;">
          <div style="background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
            <h2 style="color: #333; text-align: center; margin-bottom: 30px;">
              Chào mừng đến với Electric Bike Shop! 🚴‍♂️
            </h2>
            
            <p style="color: #666; font-size: 16px; line-height: 1.6;">
              Chào <strong>${userName}</strong>,
            </p>
            
            <p style="color: #666; font-size: 16px; line-height: 1.6;">
              Cảm ơn bạn đã đăng ký tài khoản với chúng tôi. Tài khoản của bạn đã được tạo thành công!
            </p>
            
            <div style="background-color: #f5f5f5; padding: 20px; border-radius: 5px; margin: 20px 0;">
              <h3 style="color: #333; margin-top: 0;">Thông tin tài khoản:</h3>
              <p style="color: #666; margin: 5px 0;"><strong>Tên đăng nhập:</strong> ${username}</p>
              <p style="color: #666; margin: 5px 0;"><strong>Email:</strong> ${email}</p>
              ${phoneNumber ? `<p style="color: #666; margin: 5px 0;"><strong>Số điện thoại:</strong> ${phoneNumber}</p>` : ''}
            </div>
            
            <p style="color: #666; font-size: 16px; line-height: 1.6;">
              Bạn có thể bắt đầu mua sắm ngay bây giờ! Hãy khám phá các sản phẩm xe đạp điện tuyệt vời của chúng tôi.
            </p>
            
            <div style="text-align: center; margin: 30px 0;">
              <a href="${process.env.FRONTEND_URL || '#'}" 
                 style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">
                Bắt đầu mua sắm
              </a>
            </div>
            
            <p style="color: #666; font-size: 14px; line-height: 1.6;">
              Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi. Chúng tôi luôn sẵn sàng hỗ trợ bạn!
            </p>
            
            <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
            
            <p style="color: #888; font-size: 12px; text-align: center; margin: 0;">
              Trân trọng,<br>
              <strong>Đội ngũ Electric Bike Shop</strong>
            </p>
          </div>
        </div>
      `;

      await sendEmail({
        email: user.email,
        subject: 'Chào mừng đến với Electric Bike Shop!',
        message,
        html: htmlMessage
      });

      console.log(`Welcome email sent successfully to: ${user.email}`);
    } catch (emailError) {
      // Không block response nếu gửi email thất bại
      console.error('Failed to send welcome email:', emailError);
      // Tiếp tục xử lý đăng ký thành công
    }

    sendTokenResponse(user, 201, res);
  } catch (err) {
    next(err);
  }
};

exports.login = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      console.log('Login validation errors:', errors.array());
      return res.status(400).json({ success: false, errors: errors.array() });
    }

    const { usernameOrEmail, password } = req.body;
    console.log('Login attempt for:', usernameOrEmail);
    
    const user = await User.findOne({
      $or: [{ username: usernameOrEmail }, { email: usernameOrEmail }]
    });

    if (!user) {
      console.log('User not found:', usernameOrEmail);
      return res.status(401).json({ success: false, message: 'Tài khoản không tồn tại' });
    }

    // Check if user is active
    if (!user.isActive) {
      console.log('User account is inactive:', usernameOrEmail);
      return res.status(403).json({ success: false, message: 'Tài khoản đã bị khóa' });
    }

    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      console.log('Password mismatch for user:', usernameOrEmail);
      return res.status(401).json({ success: false, message: 'Mật khẩu không đúng' });
    }

    console.log('Login successful for user:', usernameOrEmail);
    await user.updateLastLogin();
    sendTokenResponse(user, 200, res);
  } catch (err) {
    console.error('Login error:', err);
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

// @desc    Google OAuth callback
// @route   GET /api/auth/google/callback
// @access  Public
exports.googleAuthCallback = (req, res, next) => {
  try {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        message: 'Xác thực Google thất bại'
      });
    }
    
    sendTokenResponse(req.user, 200, res);
  } catch (error) {
    console.error('Google callback error:', error);
    next(error);
  }
};

// @desc    Forgot password
// @route   POST /api/auth/forgot-password
// @access  Public
exports.forgotPassword = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ 
        success: false, 
        errors: errors.array() 
      });
    }

    const { email } = req.body;
    
    if (!email) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp email'
      });
    }

    const user = await User.findOne({ email: email.toLowerCase() });
    
    // Không tiết lộ thông tin user có tồn tại hay không để bảo mật
    if (!user) {
      return res.status(200).json({
        success: true,
        message: 'Nếu email tồn tại trong hệ thống, bạn sẽ nhận được link reset mật khẩu'
      });
    }

    // Kiểm tra xem user có đăng ký qua Google không
    if (user.googleId && !user.passwordHash) {
      return res.status(400).json({
        success: false,
        message: 'Tài khoản này được đăng ký qua Google. Vui lòng đăng nhập bằng Google'
      });
    }

    // Generate reset token
    const resetToken = user.getResetPasswordToken();
    await user.save({ validateBeforeSave: false });

    // Create reset url
    const resetUrl = `${process.env.FRONTEND_URL}/reset-password/${resetToken}`;
    
    const message = `
Bạn đã yêu cầu reset mật khẩu. Vui lòng click vào link bên dưới để đặt lại mật khẩu:

${resetUrl}

Link này sẽ hết hạn sau 10 phút.

Nếu bạn không yêu cầu reset mật khẩu, vui lòng bỏ qua email này.
    `.trim();

    const htmlMessage = `
      <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
        <h2 style="color: #333; text-align: center;">Reset Mật Khẩu</h2>
        <p>Bạn đã yêu cầu reset mật khẩu. Vui lòng click vào nút bên dưới để đặt lại mật khẩu:</p>
        <div style="text-align: center; margin: 30px 0;">
          <a href="${resetUrl}" 
             style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
            Reset Mật Khẩu
          </a>
        </div>
        <p style="color: #666; font-size: 14px;">
          Link này sẽ hết hạn sau 10 phút.<br>
          Nếu bạn không yêu cầu reset mật khẩu, vui lòng bỏ qua email này.
        </p>
        <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
        <p style="color: #888; font-size: 12px; text-align: center;">
          Electric Bike Shop - Hệ thống quản lý xe điện
        </p>
      </div>
    `;

    try {
      await sendEmail({
        email: user.email,
        subject: 'Reset mật khẩu - Electric Bike Shop',
        message,
        html: htmlMessage
      });

      console.log(`Password reset email sent to: ${user.email}`);
      
      res.status(200).json({
        success: true,
        message: 'Email reset mật khẩu đã được gửi'
      });
    } catch (emailError) {
      console.error('Failed to send reset email:', emailError);
      
      // Clear reset token if email fails
      user.passwordResetToken = undefined;
      user.passwordResetExpire = undefined;
      await user.save({ validateBeforeSave: false });

      return res.status(500).json({
        success: false,
        message: 'Không thể gửi email. Vui lòng thử lại sau'
      });
    }
  } catch (error) {
    console.error('Forgot password error:', error);
    next(error);
  }
};

// @desc    Reset password
// @route   PUT /api/auth/reset-password/:resettoken
// @access  Public
exports.resetPassword = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ 
        success: false, 
        errors: errors.array() 
      });
    }

    const { password } = req.body;
    const { resettoken } = req.params;

    if (!password) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp mật khẩu mới'
      });
    }

    if (!resettoken) {
      return res.status(400).json({
        success: false,
        message: 'Token reset không hợp lệ'
      });
    }

    // Get hashed token
    const resetPasswordToken = crypto
      .createHash('sha256')
      .update(resettoken)
      .digest('hex');

    const user = await User.findOne({
      passwordResetToken: resetPasswordToken,
      passwordResetExpire: { $gt: Date.now() }
    });

    if (!user) {
      return res.status(400).json({
        success: false,
        message: 'Token không hợp lệ hoặc đã hết hạn'
      });
    }

    // Set new password
    user.passwordHash = password;
    user.passwordResetToken = undefined;
    user.passwordResetExpire = undefined;
    
    await user.save();

    console.log(`Password reset successful for user: ${user.email}`);
    
    sendTokenResponse(user, 200, res);
  } catch (error) {
    console.error('Reset password error:', error);
    next(error);
  }
};

// @desc    Check if username, email or phone is already registered
// @route   GET /api/auth/check-duplicate
// @access  Public
exports.checkDuplicate = async (req, res, next) => {
  try {
    const { username, email, phoneNumber } = req.query;

    if (!username && !email && !phoneNumber) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp username, email hoặc số điện thoại'
      });
    }

    const result = {
      usernameExists: false,
      emailExists: false,
      phoneExists: false
    };

    if (username) {
      const existingUsername = await User.findOne({ username });
      result.usernameExists = !!existingUsername;
    }

    if (email) {
      const existingEmail = await User.findOne({ email: email.toLowerCase() });
      result.emailExists = !!existingEmail;
    }

    if (phoneNumber) {
      const existingPhone = await User.findOne({ phoneNumber });
      result.phoneExists = !!existingPhone;
    }

    res.status(200).json({
      success: true,
      data: result
    });
  } catch (error) {
    console.error('Check duplicate error:', error);
    next(error);
  }
};


