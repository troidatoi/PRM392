const mongoose = require('mongoose');
require('dotenv').config();

const connectDB = require('../config/database');
const Store = require('../models/Store');

async function checkStores() {
  try {
    await connectDB();
    console.log('Connected to database\n');
    
    const allStores = await Store.find({}).select('name isActive city createdAt');
    console.log(`Total stores in database: ${allStores.length}\n`);
    
    allStores.forEach((store, index) => {
      console.log(`${index + 1}. ${store.name}`);
      console.log(`   - Active: ${store.isActive}`);
      console.log(`   - City: ${store.city}`);
      console.log(`   - ID: ${store._id}`);
      console.log('');
    });
    
    const activeStores = await Store.countDocuments({ isActive: true });
    const inactiveStores = await Store.countDocuments({ isActive: false });
    
    console.log(`\nSummary:`);
    console.log(`- Active stores: ${activeStores}`);
    console.log(`- Inactive stores: ${inactiveStores}`);
    console.log(`- Total: ${allStores.length}`);
    
    process.exit(0);
  } catch (error) {
    console.error('Error:', error);
    process.exit(1);
  }
}

checkStores();
