const mongoose = require('mongoose');
const ChatMessage = require('../models/ChatMessage');
const User = require('../models/User'); // Import User model
require('dotenv').config();

// Connect to MongoDB
mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
});

const migrateChatMessages = async () => {
  try {
    console.log('🚀 Starting migration...');
    
    // Lấy tất cả tin nhắn chưa có roomId
    const messages = await ChatMessage.find({ roomId: { $exists: false } })
      .populate('user', '_id role');
    
    console.log(`Found ${messages.length} messages without roomId`);
    
    let updated = 0;
    
    for (const msg of messages) {
      if (msg.user) {
        // Nếu user là customer, roomId = "admin_customerId"
        // Nếu user là admin, cần tìm xem tin nhắn này thuộc conversation nào
        // Giả sử: tất cả tin nhắn đều trong format "admin_userId"
        
        let roomId;
        if (msg.user.role === 'customer') {
          roomId = `admin_${msg.user._id}`;
        } else if (msg.user.role === 'admin') {
          // Admin gửi tin - cần xác định customer từ context
          // Vì không có thông tin này, ta bỏ qua hoặc set default
          // Tốt nhất là xóa và gửi lại tin nhắn mới
          console.log(`⚠️  Skipping admin message ${msg._id} - cannot determine customer`);
          continue;
        }
        
        if (roomId) {
          await ChatMessage.updateOne(
            { _id: msg._id },
            { $set: { roomId: roomId } }
          );
          updated++;
          console.log(`✅ Updated message ${msg._id} with roomId: ${roomId}`);
        }
      }
    }
    
    console.log(`\n✅ Migration completed!`);
    console.log(`   Updated: ${updated} messages`);
    console.log(`   Skipped: ${messages.length - updated} messages`);
    
  } catch (error) {
    console.error('❌ Migration failed:', error);
  } finally {
    await mongoose.connection.close();
    console.log('📊 Database connection closed');
    process.exit(0);
  }
};

migrateChatMessages();
