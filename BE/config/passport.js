const GoogleStrategy = require('passport-google-oauth20').Strategy;
const User = require('../models/User');

module.exports = function(passport) {
  // Chỉ enable Google OAuth nếu có credentials
  if (process.env.GOOGLE_CLIENT_ID && 
      process.env.GOOGLE_CLIENT_SECRET && 
      process.env.GOOGLE_CLIENT_ID !== 'temporary_disabled') {
    
    passport.use(new GoogleStrategy({
      clientID: process.env.GOOGLE_CLIENT_ID,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET,
      callbackURL: '/api/auth/google/callback'
    },
  async (accessToken, refreshToken, profile, done) => {
    try {
      // Kiểm tra dữ liệu từ Google
      if (!profile.emails || !profile.emails[0] || !profile.emails[0].value) {
        return done(new Error('Không thể lấy email từ tài khoản Google'), null);
      }

      const email = profile.emails[0].value.toLowerCase();
      const googleId = profile.id;
      const displayName = profile.displayName || `User${googleId}`;

      // Tìm user bằng email trước
      let user = await User.findOne({ email });

      if (user) {
        // Nếu user tồn tại nhưng chưa liên kết Google
        if (!user.googleId) {
          user.googleId = googleId;
          await user.save();
          console.log(`Liên kết Google ID cho user existing: ${email}`);
        }
        // Nếu đã có Google ID khác
        else if (user.googleId !== googleId) {
          return done(new Error('Email này đã được liên kết với tài khoản Google khác'), null);
        }
        
        // Cập nhật last login
        await user.updateLastLogin();
        return done(null, user);
      } else {
        // Kiểm tra xem Google ID đã được sử dụng chưa
        const existingGoogleUser = await User.findOne({ googleId });
        if (existingGoogleUser) {
          return done(new Error('Tài khoản Google này đã được sử dụng'), null);
        }

        // Tạo user mới
        const newUser = {
          googleId,
          username: `google_${googleId}`, // Đảm bảo unique
          email,
          role: 'customer',
          isActive: true,
          profile: {
            firstName: profile.name?.givenName || '',
            lastName: profile.name?.familyName || '',
            avatar: profile.photos?.[0]?.value || ''
          },
          lastLogin: new Date()
        };

        user = await User.create(newUser);
        console.log(`Tạo user mới từ Google: ${email}`);
        return done(null, user);
      }
    } catch (error) {
      console.error('Google OAuth error:', error);
      
      // Xử lý lỗi cụ thể
      if (error.code === 11000) {
        if (error.keyPattern?.username) {
          return done(new Error('Tên đăng nhập đã tồn tại'), null);
        }
        if (error.keyPattern?.email) {
          return done(new Error('Email đã được sử dụng'), null);
        }
        if (error.keyPattern?.googleId) {
          return done(new Error('Tài khoản Google đã được liên kết'), null);
        }
      }
      
      return done(new Error('Có lỗi xảy ra trong quá trình xác thực Google'), null);
    }
  }));

  // Serialize user for session
  passport.serializeUser((user, done) => {
    done(null, user._id);
  });

    // Deserialize user from session
    passport.deserializeUser(async (id, done) => {
      try {
        const user = await User.findById(id).select('-passwordHash');
        done(null, user);
      } catch (error) {
        done(error, null);
      }
    });
  } else {
    console.log('⚠️  Google OAuth disabled - Missing GOOGLE_CLIENT_ID or GOOGLE_CLIENT_SECRET');
  }
};
