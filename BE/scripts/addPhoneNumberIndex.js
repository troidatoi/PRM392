const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('../models/User');

// Load env vars
dotenv.config();

const addPhoneNumberIndex = async () => {
  try {
    // Connect to database
    await mongoose.connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });

    console.log('Connected to MongoDB');

    // Kiểm tra và xóa các số điện thoại trùng lặp nếu có
    const duplicates = await User.aggregate([
      { $match: { phoneNumber: { $ne: null, $ne: '' } } },
      { $group: { _id: '$phoneNumber', count: { $sum: 1 }, users: { $push: '$_id' } } },
      { $match: { count: { $gt: 1 } } }
    ]);

    if (duplicates.length > 0) {
      console.log(`Found ${duplicates.length} duplicate phone numbers:`);
      for (const dup of duplicates) {
        console.log(`  - Phone: ${dup._id}, Count: ${dup.count}`);
        // Giữ user đầu tiên, xóa phoneNumber của các user khác
        const usersToUpdate = dup.users.slice(1);
        await User.updateMany(
          { _id: { $in: usersToUpdate } },
          { $unset: { phoneNumber: '' } }
        );
        console.log(`    Cleared phoneNumber for ${usersToUpdate.length} duplicate users`);
      }
    } else {
      console.log('No duplicate phone numbers found');
    }

    // Xóa các index cũ cho phoneNumber nếu có
    try {
      await User.collection.dropIndex('phoneNumber_1');
      console.log('Dropped old phoneNumber index');
    } catch (error) {
      console.log('No old phoneNumber index to drop');
    }

    // Tạo unique sparse index cho phoneNumber
    await User.collection.createIndex(
      { phoneNumber: 1 },
      { unique: true, sparse: true }
    );
    console.log('Created unique sparse index for phoneNumber');

    console.log('Phone number index migration completed successfully!');
    process.exit(0);
  } catch (error) {
    console.error('Error during migration:', error);
    process.exit(1);
  }
};

// Run if called directly
if (require.main === module) {
  addPhoneNumberIndex();
}

module.exports = addPhoneNumberIndex;
