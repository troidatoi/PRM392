const { body, query } = require('express-validator');

// Validation rules for bike creation and update
const validateBike = [
  body('name')
    .notEmpty()
    .withMessage('Tên xe đạp điện là bắt buộc')
    .isLength({ max: 100 })
    .withMessage('Tên xe đạp điện không được vượt quá 100 ký tự'),
  
  body('brand')
    .notEmpty()
    .withMessage('Thương hiệu là bắt buộc')
    .isLength({ max: 50 })
    .withMessage('Thương hiệu không được vượt quá 50 ký tự'),
  
  body('model')
    .notEmpty()
    .withMessage('Model là bắt buộc')
    .isLength({ max: 50 })
    .withMessage('Model không được vượt quá 50 ký tự'),
  
  body('price')
    .isNumeric()
    .withMessage('Giá phải là số')
    .isFloat({ min: 0 })
    .withMessage('Giá không được âm'),
  
  body('originalPrice')
    .optional()
    .isNumeric()
    .withMessage('Giá gốc phải là số')
    .isFloat({ min: 0 })
    .withMessage('Giá gốc không được âm'),
  
  body('description')
    .notEmpty()
    .withMessage('Mô tả là bắt buộc')
    .isLength({ max: 1000 })
    .withMessage('Mô tả không được vượt quá 1000 ký tự'),
  
  body('category')
    .isIn(['city', 'mountain', 'folding', 'cargo', 'sport', 'other'])
    .withMessage('Danh mục không hợp lệ'),
  
  body('status')
    .optional()
    .isIn(['available', 'out_of_stock', 'discontinued'])
    .withMessage('Trạng thái không hợp lệ'),
  
  body('stock')
    .optional()
    .isInt({ min: 0 })
    .withMessage('Số lượng tồn kho không được âm'),
  
  body('warranty')
    .optional()
    .isString()
    .withMessage('Bảo hành phải là chuỗi'),
  
  body('isFeatured')
    .optional()
    .isBoolean()
    .withMessage('Trạng thái nổi bật phải là boolean'),
  
  body('isNew')
    .optional()
    .isBoolean()
    .withMessage('Trạng thái mới phải là boolean')
];

// Validation rules for query parameters
const validateBikeQuery = [
  query('page')
    .optional()
    .isInt({ min: 1 })
    .withMessage('Trang phải là số nguyên dương'),
  
  query('limit')
    .optional()
    .isInt({ min: 1, max: 100 })
    .withMessage('Giới hạn phải từ 1-100'),
  
  query('category')
    .optional()
    .isIn(['city', 'mountain', 'folding', 'cargo', 'sport', 'other'])
    .withMessage('Danh mục không hợp lệ'),
  
  query('status')
    .optional()
    .isIn(['available', 'out_of_stock', 'discontinued'])
    .withMessage('Trạng thái không hợp lệ'),
  
  query('minPrice')
    .optional()
    .isNumeric()
    .withMessage('Giá tối thiểu phải là số'),
  
  query('maxPrice')
    .optional()
    .isNumeric()
    .withMessage('Giá tối đa phải là số'),
  
  query('brand')
    .optional()
    .isString()
    .withMessage('Thương hiệu phải là chuỗi'),
  
  query('search')
    .optional()
    .isString()
    .withMessage('Từ khóa tìm kiếm phải là chuỗi'),
  
  query('sortBy')
    .optional()
    .isIn(['price_asc', 'price_desc', 'rating', 'name', 'createdAt'])
    .withMessage('Cách sắp xếp không hợp lệ')
];

// Validation for featured bikes query
const validateFeaturedQuery = [
  query('limit')
    .optional()
    .isInt({ min: 1, max: 50 })
    .withMessage('Giới hạn phải từ 1-50')
];

module.exports = {
  validateBike,
  validateBikeQuery,
  validateFeaturedQuery
};


