const mongoose = require('mongoose');
const dotenv = require('dotenv');
const Bike = require('../models/Bike');
const sampleBikes = require('../data/sampleBikes');

// Load env vars
dotenv.config({ path: './config.env' });

const seedDatabase = async () => {
  try {
    // Connect to database
    await mongoose.connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });

    console.log('Connected to MongoDB');

    // Clear existing bikes
    await Bike.deleteMany({});
    console.log('Cleared existing bikes');

    // Insert sample bikes
    const bikes = await Bike.insertMany(sampleBikes);
    console.log(`Inserted ${bikes.length} sample bikes`);

    // Create text index for search
    await Bike.collection.createIndex({ 
      name: 'text', 
      brand: 'text', 
      model: 'text', 
      description: 'text' 
    });
    console.log('Created text index for search');

    console.log('Database seeded successfully!');
    process.exit(0);
  } catch (error) {
    console.error('Error seeding database:', error);
    process.exit(1);
  }
};

// Run if called directly
if (require.main === module) {
  seedDatabase();
}

module.exports = seedDatabase;


