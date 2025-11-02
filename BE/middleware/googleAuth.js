/**
 * Middleware xử lý lỗi Google OAuth
 */
const handleGoogleAuthError = (error, req, res, next) => {
  console.error('Google Auth Error:', {
    message: error.message,
    stack: error.stack,
    timestamp: new Date().toISOString(),
    ip: req.ip,
    userAgent: req.get('User-Agent')
  });

  // Chuyển hướng về frontend với thông báo lỗi
  const frontendUrl = process.env.FRONTEND_URL || 'http://localhost:3000';
  let errorMessage = 'Có lỗi xảy ra trong quá trình xác thực';

  // Xử lý các loại lỗi cụ thể
  if (error.message.includes('email')) {
    errorMessage = 'Không thể lấy thông tin email từ tài khoản Google';
  } else if (error.message.includes('đã được sử dụng')) {
    errorMessage = 'Tài khoản Google này đã được liên kết với email khác';
  } else if (error.message.includes('đã được liên kết')) {
    errorMessage = 'Email này đã được liên kết với tài khoản Google khác';
  }

  // Encode error message để truyền qua URL
  const encodedError = encodeURIComponent(errorMessage);
  res.redirect(`${frontendUrl}/auth/error?message=${encodedError}`);
};

module.exports = { handleGoogleAuthError };
