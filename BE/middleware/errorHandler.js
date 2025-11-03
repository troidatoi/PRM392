const errorHandler = (err, req, res, next) => {
  let error = { ...err };
  error.message = err.message;

  // Log error
  console.error(err);

  // Mongoose bad ObjectId
  if (err.name === 'CastError') {
    const message = 'Resource not found';
    error = { message, statusCode: 404 };
  }

  // Mongoose duplicate key
  if (err.code === 11000) {
    // Try to detect which unique field caused the error and localize message
    const keyPattern = err.keyPattern || (err.keyValue ? Object.keys(err.keyValue).reduce((acc, k) => (acc[k] = 1, acc), {}) : null);
    const keyValue = err.keyValue || {};
    let field = keyPattern ? Object.keys(keyPattern)[0] : (Object.keys(keyValue)[0] || 'field');
    let value = keyValue[field];
    let message;
    switch (field) {
      case 'email':
        message = 'Email đã tồn tại';
        break;
      case 'username':
        message = 'Tên đăng nhập đã tồn tại';
        break;
      case 'phoneNumber':
        message = 'Số điện thoại đã tồn tại';
        break;
      default:
        message = 'Giá trị đã tồn tại';
        break;
    }
    const errors = {};
    errors[field] = { message, path: field, value };
    error = { message, statusCode: 400, errors };
  }

  // Mongoose validation error
  if (err.name === 'ValidationError') {
    // Preserve field-level error details so clients can map errors to inputs
    const messageList = Object.values(err.errors).map(val => val.message);
    const message = messageList.length === 1 ? messageList[0] : messageList;
    error = { message, statusCode: 400, errors: err.errors };
  }

  res.status(error.statusCode || 500).json({
    success: false,
    message: error.message || 'Server Error',
    // Include structured errors when available (e.g., Mongoose ValidationError)
    ...(error.errors ? { errors: error.errors } : {})
  });
};

module.exports = errorHandler;


