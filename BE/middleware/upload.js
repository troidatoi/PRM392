const cloudinary = require('cloudinary').v2;
const multer = require('multer');

// Configure Cloudinary
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME || 'demo',
  api_key: process.env.CLOUDINARY_API_KEY || 'demo',
  api_secret: process.env.CLOUDINARY_API_SECRET || 'demo'
});

// Configure multer for memory storage
const storage = multer.memoryStorage();

const upload = multer({
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024, // 5MB limit
    files: 5 // Maximum 5 files
  },
  fileFilter: (req, file, cb) => {
    // Check file type
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('Chỉ cho phép upload file ảnh'), false);
    }
  }
});

// Middleware for multiple images upload
const uploadMultiple = upload.array('images', 5);

// Error handling middleware
const handleUploadError = (err, req, res, next) => {
  if (err instanceof multer.MulterError) {
    if (err.code === 'LIMIT_FILE_SIZE') {
      return res.status(400).json({
        success: false,
        message: 'Kích thước file quá lớn. Tối đa 5MB'
      });
    }
    if (err.code === 'LIMIT_FILE_COUNT') {
      return res.status(400).json({
        success: false,
        message: 'Số lượng file quá nhiều. Tối đa 5 ảnh'
      });
    }
  }
  
  if (err.message === 'Chỉ cho phép upload file ảnh') {
    return res.status(400).json({
      success: false,
      message: err.message
    });
  }
  
  next(err);
};

// Upload images to Cloudinary
const uploadToCloudinary = async (files) => {
  const uploadPromises = files.map(file => {
    return new Promise((resolve, reject) => {
      const uploadStream = cloudinary.uploader.upload_stream(
        {
          folder: 'bike-rental/bikes',
          transformation: [
            { width: 800, height: 600, crop: 'limit' },
            { quality: 'auto' }
          ]
        },
        (error, result) => {
          if (error) {
            reject(error);
          } else {
            resolve({
              url: result.secure_url,
              alt: file.originalname || 'Bike image',
              public_id: result.public_id
            });
          }
        }
      );
      
      uploadStream.end(file.buffer);
    });
  });
  
  return Promise.all(uploadPromises);
};

module.exports = {
  uploadMultiple,
  handleUploadError,
  uploadToCloudinary,
  cloudinary
};
