# Electric Bike Shop - Backend API

## Tính năng đã implement

### ✅ Authentication Features
- [x] Đăng ký/Đăng nhập thông thường với email/username & password
- [x] Đăng nhập bằng Google OAuth 2.0
- [x] Quên mật khẩu qua email
- [x] Reset mật khẩu với token
- [x] JWT authentication với refresh token
- [x] Profile management
- [x] Change password

### ✅ Security Features
- [x] Password hashing với bcrypt (salt rounds: 12)
- [x] JWT token với expiration
- [x] Input validation với express-validator
- [x] Error handling middleware
- [x] CORS configuration
- [x] Rate limiting ready (configurable)

## Setup Instructions

### 1. Prerequisites
```bash
# Yêu cầu hệ thống
- Node.js >= 14.x
- MongoDB >= 4.x
- npm hoặc yarn
```

### 2. Installation
```bash
# Clone project và navigate to BE folder
cd BE

# Install dependencies
npm install

# Copy environment file
cp .env.example .env
```

### 3. Environment Configuration

Tạo file `.env` với nội dung sau:

```env
# Server Configuration
NODE_ENV=development
PORT=5001

# Database
MONGO_URI=mongodb://localhost:27017/electric-bike-db

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters-long
JWT_EXPIRE=7d
JWT_COOKIE_EXPIRE=7

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your-google-client-id-from-console
GOOGLE_CLIENT_SECRET=your-google-client-secret-from-console

# Email Configuration (Gmail example)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-gmail-app-password
FROM_NAME=Electric Bike Shop

# Frontend URL (for password reset links)
FRONTEND_URL=http://localhost:3000

# Optional: Cloudinary for image uploads
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

### 4. Google OAuth Setup

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project existing
3. Enable APIs:
   - Google+ API
   - Google OAuth2 API
4. Credentials → Create credentials → OAuth 2.0 Client IDs
5. Application type: Web application
6. Authorized redirect URIs:
   - Development: `http://localhost:5001/api/auth/google/callback`
   - Production: `https://yourdomain.com/api/auth/google/callback`
7. Copy Client ID và Client Secret vào file `.env`

### 5. Email Service Setup (Gmail)

1. Bật 2-Factor Authentication cho Gmail account
2. Truy cập Google Account settings
3. Security → App passwords
4. Generate app password cho "Mail"
5. Sử dụng app password trong `EMAIL_PASS` (không phải password thông thường)

### 6. Database Setup

```bash
# Start MongoDB (if using local installation)
mongod

# Hoặc sử dụng MongoDB Atlas (cloud)
# Update MONGO_URI trong .env với connection string từ Atlas
```

### 7. Start Server

```bash
# Development mode (auto-restart on changes)  
npm run dev

# Production mode
npm start
```

Server sẽ chạy tại: `http://localhost:5001`

## API Testing

### Manual Testing

1. **Health Check**
```bash
curl http://localhost:5001/api/health
```

2. **Register User**
```bash
curl -X POST http://localhost:5001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com", 
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

3. **Login**
```bash
curl -X POST http://localhost:5001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "test@example.com",
    "password": "password123"
  }'
```

4. **Google OAuth**
- Truy cập: `http://localhost:5001/api/auth/google`
- Sẽ redirect đến Google login

5. **Forgot Password**
```bash
curl -X POST http://localhost:5001/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

### Automated Testing

```bash
# Run test suite
node tests/auth.test.js
```

## API Documentation

Chi tiết đầy đủ về API endpoints: [API_AUTHENTICATION.md](./docs/API_AUTHENTICATION.md)

## Project Structure

```
BE/
├── config/
│   ├── database.js          # MongoDB connection
│   └── passport.js          # Google OAuth config
├── controllers/
│   └── authController.js    # Auth logic & handlers
├── middleware/
│   ├── auth.js             # JWT authentication
│   ├── errorHandler.js     # Global error handling
│   ├── googleAuth.js       # Google OAuth error handling
│   ├── upload.js           # File upload handling
│   └── validation.js       # Input validation
├── models/
│   └── User.js             # User model with Google OAuth support
├── routes/
│   └── authRoutes.js       # Authentication routes
├── utils/
│   └── sendEmail.js        # Email service utility
├── tests/
│   └── auth.test.js        # Authentication tests
├── docs/
│   └── API_AUTHENTICATION.md # API documentation
├── .env.example            # Environment variables template
└── server.js               # Entry point
```

## Security Best Practices Implemented

1. **Password Security**
   - bcrypt with 12 salt rounds
   - Password validation (min 6 characters)
   - No password storage for Google OAuth users

2. **JWT Security**
   - Strong secret key requirement
   - Token expiration
   - HTTP-only cookies support

3. **Input Validation**
   - express-validator for all inputs
   - Email format validation
   - SQL injection prevention

4. **Error Handling**
   - No sensitive data in error messages
   - Proper HTTP status codes
   - Detailed logging for debugging

5. **OAuth Security**
   - State parameter validation
   - Scope limitation
   - Error handling for failed authentication

## Production Considerations

1. **Environment Variables**
   - Use strong JWT secrets (>32 characters)
   - Set NODE_ENV=production
   - Configure proper CORS origins

2. **Database**
   - Use MongoDB Atlas or properly secured MongoDB
   - Enable authentication
   - Set up proper indexes

3. **Security Headers**
   - Add helmet.js for security headers
   - Implement rate limiting
   - Set up HTTPS

4. **Monitoring**
   - Add logging (Winston)
   - Set up health checks
   - Monitor error rates

## Common Issues & Solutions

### 1. Google OAuth không hoạt động
- Kiểm tra GOOGLE_CLIENT_ID và GOOGLE_CLIENT_SECRET
- Verify redirect URI trong Google Console
- Ensure correct callback URL

### 2. Email không gửi được
- Kiểm tra EMAIL_HOST, EMAIL_PORT settings
- Verify Gmail app password (không phải password thông thường)
- Check firewall/network restrictions

### 3. JWT Token errors
- Ensure JWT_SECRET đủ dài và phức tạp
- Kiểm tra token expiration settings
- Verify Bearer token format in requests

### 4. Database connection issues
- Check MongoDB is running
- Verify MONGO_URI format
- Check network connectivity

## Support

Nếu gặp vấn đề, check:
1. Console logs cho error details
2. API_AUTHENTICATION.md cho API specs
3. Test script results: `node tests/auth.test.js`
