
# Kế hoạch Implementation: Xác thực Google & Quên mật khẩu

Tài liệu này định nghĩa các bước cần thiết để implement hai tính năng mới cho hệ thống backend: (1) Đăng nhập/Đăng ký bằng tài khoản Google và (2) Chức năng "Quên mật khẩu".

## Bối cảnh hiện tại

- **Xác thực**: Hệ thống đang sử dụng xác thực dựa trên email/username và mật khẩu với JWT (JSON Web Tokens).
- **Controller**: `controllers/authController.js` chứa logic chính cho `register` và `login`.
- **Model**: `models/User.js` định nghĩa schema cho User, bao gồm `username`, `email`, `password`, và `role`. Mật khẩu được hash bằng `bcryptjs`.
- **Middleware**: `middleware/auth.js` chứa hàm `protect` để xác thực token và `authorize` để phân quyền dựa trên vai trò.
- **Thiếu sót**: Model `User` hiện chưa có các trường để lưu `googleId` hoặc các token/thời gian hết hạn cho việc reset mật khẩu.

---

## Phần 1: Tính năng Xác thực bằng Google (Login with Google)

### A. Logic Implementation

Mục tiêu là cho phép người dùng đăng nhập hoặc đăng ký thông qua tài khoản Google. Hệ thống sẽ tự động tạo tài khoản mới nếu email Google chưa tồn tại, hoặc đăng nhập nếu đã tồn tại.

Chúng ta sẽ sử dụng `passport` và `passport-google-oauth20` để xử lý luồng OAuth 2.0.

### B. Các bước thực hiện

#### 1. Cài đặt & Cấu hình

1.  **Cài đặt thư viện**:
    ```bash
    npm install passport passport-google-oauth20
    ```
2.  **Cập nhật file `.env`**: Lấy `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` từ Google Cloud Console và thêm vào file `.env`.
    ```env
    GOOGLE_CLIENT_ID=your_google_client_id
    GOOGLE_CLIENT_SECRET=your_google_client_secret
    ```
3.  **Cập nhật Model `User`**: Mở `models/User.js` và thêm trường `googleId`. Mật khẩu sẽ không bắt buộc nếu user đăng ký qua Google.
    ```javascript
    // Thêm vào UserSchema
    googleId: {
      type: String,
    },
    // Sửa trường password
    password: {
      type: String,
      // select: false không còn cần thiết nếu không có required
    },
    ```
4.  **Tạo file cấu hình Passport**: Tạo file mới `config/passport.js`.
    ```javascript
    const GoogleStrategy = require('passport-google-oauth20').Strategy;
    const User = require('../models/User');

    module.exports = function(passport) {
      passport.use(new GoogleStrategy({
        clientID: process.env.GOOGLE_CLIENT_ID,
        clientSecret: process.env.GOOGLE_CLIENT_SECRET,
        callbackURL: '/api/auth/google/callback'
      },
      async (accessToken, refreshToken, profile, done) => {
        const newUser = {
          googleId: profile.id,
          username: profile.displayName,
          email: profile.emails[0].value,
          // Thêm các trường khác nếu cần
        };

        try {
          let user = await User.findOne({ email: profile.emails[0].value });

          if (user) {
            // Nếu user tồn tại, chỉ cần đảm bảo googleId được liên kết
            if (!user.googleId) {
              user.googleId = profile.id;
              await user.save();
            }
            done(null, user);
          } else {
            // Nếu không, tạo user mới
            user = await User.create(newUser);
            done(null, user);
          }
        } catch (err) {
          console.error(err);
          done(err, null);
        }
      }));
    };
    ```

#### 2. Tích hợp vào App

1.  **Tích hợp vào `server.js`**:
    ```javascript
    const passport = require('passport');
    // ...
    // Sau dòng app = express();
    app.use(passport.initialize());
    require('./config/passport')(passport);
    ```
2.  **Thêm Routes vào `routes/authRoutes.js`**:
    ```javascript
    const passport = require('passport');
    const { googleAuthCallback } = require('../controllers/authController'); // Sẽ tạo hàm này

    // Route để bắt đầu luồng xác thực Google
    router.get('/google', passport.authenticate('google', { scope: ['profile', 'email'] }));

    // Route callback Google sẽ gọi lại
    router.get('/google/callback', passport.authenticate('google', { failureRedirect: '/', session: false }), googleAuthCallback);
    ```
3.  **Thêm Controller `googleAuthCallback`**: Trong `controllers/authController.js`, tạo hàm để gửi token về cho client sau khi xác thực thành công.
    ```javascript
    // (Hàm sendTokenResponse đã có sẵn)
    exports.googleAuthCallback = (req, res, next) => {
      sendTokenResponse(req.user, 200, res);
    };
    ```

### C. Kế hoạch Testing

1.  **Khởi động**: Chạy server backend.
2.  **Bắt đầu**: Mở trình duyệt và truy cập `http://localhost:5001/api/auth/google`.
3.  **Xác thực**: Trình duyệt sẽ chuyển hướng đến trang đăng nhập của Google. Tiến hành đăng nhập và chấp thuận quyền.
4.  **Nhận Token**: Google sẽ chuyển hướng về `api/auth/google/callback`. Controller `googleAuthCallback` sẽ được thực thi và trả về một JSON chứa token.
5.  **Kiểm tra Token**: Dùng token vừa nhận được để gọi một API được bảo vệ (ví dụ: `GET /api/auth/me`) và xác nhận rằng nó hoạt động.

---

## Phần 2: Tính năng Quên mật khẩu

### A. Logic Implementation

Luồng này cho phép người dùng yêu cầu reset mật khẩu qua email.
1.  User yêu cầu reset, cung cấp email.
2.  Backend tạo một token reset, gửi link chứa token này đến email của user.
3.  User click vào link, được dẫn đến trang nhập mật khẩu mới.
4.  User gửi token và mật khẩu mới đến backend để hoàn tất.

### B. Các bước thực hiện

#### 1. Cài đặt & Cấu hình

1.  **Cài đặt thư viện**:
    ```bash
    npm install nodemailer
    ```
2.  **Cập nhật file `.env`**: Thêm thông tin cấu hình dịch vụ email.
    ```env
    EMAIL_HOST=smtp.example.com
    EMAIL_PORT=587
    EMAIL_USER=your_email_user
    EMAIL_PASS=your_email_password
    FRONTEND_URL=http://localhost:3000 // Địa chỉ của frontend
    ```
3.  **Cập nhật Model `User`**: Mở `models/User.js`, thêm các trường để lưu token reset.
    ```javascript
    // Thêm vào UserSchema
    passwordResetToken: String,
    passwordResetExpire: Date,
    ```
    Thêm một method vào `UserSchema.methods` để tạo token:
    ```javascript
    const crypto = require('crypto');
    // ... trong UserSchema.methods
    userSchema.methods.getResetPasswordToken = function() {
      const resetToken = crypto.randomBytes(20).toString('hex');

      this.passwordResetToken = crypto
        .createHash('sha256')
        .update(resetToken)
        .digest('hex');

      this.passwordResetExpire = Date.now() + 10 * 60 * 1000; // 10 phút

      return resetToken;
    }
    ```

#### 2. Tích hợp vào App

1.  **Tạo Email Utility**: Tạo file `utils/sendEmail.js` để xử lý việc gửi mail.
2.  **Thêm Routes vào `routes/authRoutes.js`**:
    ```javascript
    const { forgotPassword, resetPassword } = require('../controllers/authController');

    router.post('/forgot-password', forgotPassword);
    router.put('/reset-password/:resettoken', resetPassword);
    ```
3.  **Thêm Controllers**: Trong `controllers/authController.js`, thêm 2 hàm:
    *   `forgotPassword`:
        1.  Tìm user bằng email. Nếu không có, vẫn trả về thông báo thành công để tránh lộ thông tin.
        2.  Gọi `user.getResetPasswordToken()` để tạo token.
        3.  Lưu lại user.
        4.  Tạo reset URL (`${process.env.FRONTEND_URL}/reset-password/${resetToken}`).
        5.  Gửi email chứa URL này cho user.
    *   `resetPassword`:
        1.  Lấy `resettoken` từ `req.params`. Hash nó lại.
        2.  Tìm user có `passwordResetToken` khớp và chưa hết hạn.
        3.  Nếu không tìm thấy, báo lỗi.
        4.  Lấy `password` mới từ `req.body`, hash và cập nhật cho user.
        5.  Xóa `passwordResetToken` và `passwordResetExpire`.
        6.  Lưu user và gửi token đăng nhập mới.

### C. Kế hoạch Testing

1.  **Yêu cầu Reset**: Dùng `curl` gọi `POST /api/auth/forgot-password` với email của một user đã tồn tại.
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"email": "test@example.com"}' http://localhost:5001/api/auth/forgot-password
    ```
2.  **Lấy Token**:
    *   **Cách 1 (Thực tế)**: Kiểm tra hộp thư email "test@example.com" để nhận link reset và lấy token từ URL.
    *   **Cách 2 (Dev)**: Trong quá trình code, cho `console.log(resetToken)` trong hàm `forgotPassword` để lấy token trực tiếp từ terminal cho nhanh.
3.  **Reset Mật khẩu**: Dùng `curl` gọi `PUT /api/auth/reset-password/:resettoken` với token vừa lấy và mật khẩu mới.
    ```bash
    curl -X PUT -H "Content-Type: application/json" -d '{"password": "newpassword123"}' http://localhost:5001/api/auth/reset-password/your_reset_token_here
    ```
4.  **Xác thực**: Thử đăng nhập bằng tài khoản `test@example.com` và mật khẩu `newpassword123` để xác nhận việc reset đã thành công.
