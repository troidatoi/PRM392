/**
 * Quick setup test script
 * Chạy: node setup-test.js
 */

require('dotenv').config();

console.log('🔍 Kiểm tra cấu hình environment variables...\n');

// Check required variables
const requiredVars = {
  'GOOGLE_CLIENT_ID': process.env.GOOGLE_CLIENT_ID,
  'GOOGLE_CLIENT_SECRET': process.env.GOOGLE_CLIENT_SECRET,
  'EMAIL_USER': process.env.EMAIL_USER,
  'EMAIL_PASS': process.env.EMAIL_PASS,
  'JWT_SECRET': process.env.JWT_SECRET,
  'MONGO_URI': process.env.MONGO_URI
};

let allConfigured = true;

Object.entries(requiredVars).forEach(([key, value]) => {
  if (!value || value.includes('your_') || value.includes('temp') || value === 'temporary_disabled') {
    console.log(`❌ ${key}: Chưa được cấu hình`);
    allConfigured = false;
  } else {
    console.log(`✅ ${key}: Đã cấu hình`);
  }
});

console.log('\n📋 Hướng dẫn setup:');

if (!requiredVars.GOOGLE_CLIENT_ID || requiredVars.GOOGLE_CLIENT_ID.includes('your_')) {
  console.log(`
🔧 Google OAuth Setup:
1. Truy cập: https://console.cloud.google.com/
2. Tạo project mới hoặc chọn project existing
3. APIs & Services → Credentials → Create OAuth 2.0 Client ID  
4. Web application, Authorized redirect URIs: http://localhost:5000/api/auth/google/callback
5. Copy Client ID và Secret vào file .env
  `);
}

if (!requiredVars.EMAIL_USER || requiredVars.EMAIL_USER.includes('temp')) {
  console.log(`
📧 Gmail App Password Setup:
1. Bật 2FA cho Gmail: https://myaccount.google.com/security
2. Tạo App Password: Security → App passwords → Mail → Other
3. Copy 16-character password vào EMAIL_PASS trong .env
4. Điền Gmail address vào EMAIL_USER trong .env
  `);
}

if (allConfigured) {
  console.log('\n🎉 Tất cả cấu hình đã sẵn sàng! Có thể chạy npm run dev');
} else {
  console.log('\n⚠️  Vui lòng hoàn thành cấu hình trước khi chạy server');
}

console.log('\n🚀 Sau khi cấu hình xong, test bằng: npm run dev');
