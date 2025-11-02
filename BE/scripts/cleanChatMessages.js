const mongoose = require('mongoose');
const ChatMessage = require('../models/ChatMessage');
require('dotenv').config();

// Connect to MongoDB
mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
});

const cleanChatMessages = async () => {
  try {
    console.log('🧹 Cleaning all chat messages...');
    
    const result = await ChatMessage.deleteMany({});
    
    console.log(`✅ Deleted ${result.deletedCount} messages`);
    console.log('💡 Now you can test chat from scratch with proper roomId!');
    
  } catch (error) {
    console.error('❌ Cleanup failed:', error);
  } finally {
    await mongoose.connection.close();
    console.log('📊 Database connection closed');
    process.exit(0);
  }
};

cleanChatMessages();
