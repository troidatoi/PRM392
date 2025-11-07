const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('./models/User'); // Corrected path

// Load .env file from the root
dotenv.config();

async function getResetTokenForUser() {
  const email = process.argv[2];

  if (!email) {
    console.log('\nUsage: node get-reset-token.js <user_email@example.com>\n');
    process.exit(1);
  }

  try {
    if (!process.env.MONGODB_URI) {
        throw new Error('MONGODB_URI not found in .env file');
    }
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('🔗 Connected to MongoDB');
    
    const user = await User.findOne({ email });
    
    if (!user) {
      console.log(`❌ User with email "${email}" not found.`);
      return;
    }
    
    console.log('👤 User found:', user.email);
    
    // Generate a new reset token
    const plainToken = user.getResetPasswordToken();
    await user.save({ validateBeforeSave: false });
    
    const now = new Date();
    console.log('\n🔑 FRESH RESET TOKEN GENERATED:');
    console.log('Token:', plainToken);
    console.log('Expires:', user.passwordResetExpire.toLocaleString());
    console.log('Time remaining:', Math.round((user.passwordResetExpire - now) / 1000 / 60), 'minutes');
    
    console.log('\n🔗 USE THIS TOKEN TO TEST THE RESET PASSWORD API.');
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await mongoose.connection.close();
    console.log('\n🔌 Disconnected from MongoDB.');
  }
}

getResetTokenForUser();
