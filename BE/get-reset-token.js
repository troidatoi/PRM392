/**
 * Quick script để lấy reset token mới nhất từ database
 * Dùng cho testing khi không muốn check email
 */

const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('./BE/models/User');

dotenv.config({ path: './BE/.env' });

async function getLatestResetToken() {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log('🔗 Connected to MongoDB');
    
    // Find user by email
    const user = await User.findOne({ email: 'sktkctman2@gmail.com' });
    
    if (!user) {
      console.log('❌ User not found');
      return;
    }
    
    console.log('👤 User found:', user.email);
    
    if (!user.passwordResetToken || !user.passwordResetExpire) {
      console.log('❌ No reset token found. Please request forgot password first.');
      return;
    }
    
    // Check if token is still valid
    const now = new Date();
    const isExpired = user.passwordResetExpire < now;
    
    if (isExpired) {
      console.log('❌ Reset token has expired');
      console.log('Expired at:', user.passwordResetExpire.toISOString());
      console.log('Current time:', now.toISOString());
      return;
    }
    
    // Generate a new token for testing (this will give us the plain token)
    const plainToken = user.getResetPasswordToken();
    await user.save({ validateBeforeSave: false });
    
    console.log('\n🔑 FRESH RESET TOKEN GENERATED:');
    console.log('Plain Token:', plainToken);
    console.log('Expires at:', user.passwordResetExpire.toISOString());
    console.log('Time remaining:', Math.round((user.passwordResetExpire - now) / 1000 / 60), 'minutes');
    
    console.log('\n🔗 TEST URLS:');
    console.log('Web Page:', `http://localhost:5001/reset-password/${plainToken}`);
    console.log('API Test:', `curl -X PUT http://localhost:5001/api/auth/reset-password/${plainToken} -H "Content-Type: application/json" -d '{"password": "freshpassword123"}'`);
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    mongoose.connection.close();
  }
}

getLatestResetToken();
