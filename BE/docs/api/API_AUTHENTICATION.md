# API Documentation - Authentication & Password Reset

## Tổng quan

Tài liệu này mô tả các endpoint API cho việc xác thực người dùng, bao gồm:
- Đăng ký/Đăng nhập thông thường
- Đăng nhập bằng Google OAuth 2.0
- Quên mật khẩu và reset mật khẩu

## Base URL

```
http://localhost:5001/api/auth
```

## Endpoints

### 1. Đăng ký người dùng

**POST** `/register`

**Request Body:**
```json
{
  "username": "string (required, min: 3, max: 50)",
  "email": "string (required, valid email)",
  "password": "string (required, min: 6)",
  "phoneNumber": "string (optional, 10-11 digits)",
  "address": "string (optional, max: 255)",
  "firstName": "string (optional, max: 50)",
  "lastName": "string (optional, max: 50)"
}
```

**Response:**
```json
{
  "success": true,
  "token": "jwt_token_here",
  "user": {
    "id": "user_id",
    "username": "username",
    "email": "email",
    "role": "customer",
    "isActive": true,
    "googleId": false,
    "profile": {
      "firstName": "string",
      "lastName": "string"
    }
  }
}
```

### 2. Đăng nhập người dùng

**POST** `/login`

**Request Body:**
```json
{
  "usernameOrEmail": "string (required)",
  "password": "string (required)"
}
```

**Response:** Giống như response của `/register`

### 3. Đăng nhập bằng Google

**GET** `/google`

Chuyển hướng người dùng đến trang đăng nhập Google.

**GET** `/google/callback`

Endpoint callback từ Google. Trả về JWT token hoặc chuyển hướng về frontend với lỗi.

**Success Response:**
```json
{
  "success": true,
  "token": "jwt_token_here",
  "user": {
    "id": "user_id",
    "username": "username",
    "email": "email",
    "role": "customer",
    "isActive": true,
    "googleId": true,
    "profile": {
      "firstName": "string",
      "lastName": "string",
      "avatar": "string"
    }
  }
}
```

### 4. Lấy thông tin profile

**GET** `/me`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "success": true,
  "user": {
    "id": "user_id",
    "username": "username",
    "email": "email",
    "role": "customer",
    "profile": {
      "firstName": "string",
      "lastName": "string"
    }
  }
}
```

### 5. Quên mật khẩu

**POST** `/forgot-password`

**Request Body:**
```json
{
  "email": "string (required, valid email)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Email reset mật khẩu đã được gửi"
}
```

### 6. Reset mật khẩu

**PUT** `/reset-password/:resettoken`

**URL Parameters:**
- `resettoken`: Token nhận được từ email

**Request Body:**
```json
{
  "password": "string (required, min: 6)"
}
```

**Response:** Giống như response của `/login`

### 7. Đổi mật khẩu

**PUT** `/me/change-password`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "currentPassword": "string (required)",
  "newPassword": "string (required, min: 6)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công"
}
```

### 8. Đăng xuất

**POST** `/logout`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng xuất thành công"
}
```

## Error Responses

Tất cả các endpoint đều có thể trả về các lỗi sau:

### 400 Bad Request
```json
{
  "success": false,
  "message": "Error message",
  "errors": [
    {
      "field": "field_name",
      "message": "Validation error message"
    }
  ]
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized access"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error"
}
```

## Setup Requirements

### Environment Variables

```env
# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Email Service
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USER=your_email@gmail.com
EMAIL_PASS=your_app_password

# Frontend URL
FRONTEND_URL=http://localhost:3000

# JWT
JWT_SECRET=your_jwt_secret
JWT_EXPIRE=7d
JWT_COOKIE_EXPIRE=7
```

### Google Cloud Console Setup

1. Tạo project mới hoặc chọn project existing
2. Enable Google+ API
3. Tạo OAuth 2.0 credentials
4. Thêm authorized redirect URIs:
   - `http://localhost:5001/api/auth/google/callback` (development)
   - `https://yourdomain.com/api/auth/google/callback` (production)

### Email Service Setup

Để sử dụng Gmail:
1. Bật 2FA cho tài khoản Gmail
2. Tạo App Password trong Security settings
3. Sử dụng App Password trong `EMAIL_PASS`

## Security Considerations

1. **JWT Token**: Lưu trữ an toàn ở client (localStorage/sessionStorage)
2. **HTTPS**: Luôn sử dụng HTTPS trong production
3. **Rate Limiting**: Implement rate limiting cho các endpoint sensitive
4. **CORS**: Cấu hình CORS chính xác cho domain của bạn
5. **Environment Variables**: Không commit file `.env` vào git

## Testing

Chạy test script:
```bash
cd BE
node tests/auth.test.js
```

Test sẽ validate:
- Health check
- User registration
- User login
- Profile access
- Password reset request
- Google OAuth endpoint configuration
