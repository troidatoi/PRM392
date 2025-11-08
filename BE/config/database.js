const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/bike_shop';
    const conn = await mongoose.connect(mongoUri, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
      serverSelectionTimeoutMS: 5000, // Timeout after 5s instead of 30s
    });

    console.log(`MongoDB Connected: ${conn.connection.host}`);
  } catch (error) {
    console.error('\n❌ Database connection error:', error.message);
    
    // Check if it's an IP whitelist issue
    if (error.message && error.message.includes('whitelist')) {
      console.error('\n⚠️  LỖI: IP address của bạn chưa được whitelist trong MongoDB Atlas');
      console.error('📝 Cách khắc phục:');
      console.error('1. Đăng nhập vào MongoDB Atlas: https://cloud.mongodb.com/');
      console.error('2. Vào Network Access (hoặc IP Access List)');
      console.error('3. Click "Add IP Address"');
      console.error('4. Chọn "Add Current IP Address" hoặc nhập IP của bạn');
      console.error('5. Hoặc chọn "Allow Access from Anywhere" (0.0.0.0/0) - chỉ dùng cho development');
      console.error('\n💡 Lưu ý: Sau khi thêm IP, có thể mất vài phút để áp dụng.\n');
    } else if (error.message && error.message.includes('authentication')) {
      console.error('\n⚠️  LỖI: Xác thực MongoDB thất bại');
      console.error('📝 Kiểm tra lại username và password trong MONGODB_URI\n');
    } else {
      console.error('\n⚠️  LỖI: Không thể kết nối đến MongoDB');
      console.error('📝 Kiểm tra lại:');
      console.error('   - MongoDB có đang chạy không?');
      console.error('   - MONGODB_URI trong file .env có đúng không?');
      console.error('   - Kết nối internet có ổn định không?\n');
    }
    
    process.exit(1);
  }
};

module.exports = connectDB;

