const axios = require('axios');

// Test data cho xe đạp điện mới
const newBike = {
  name: "Xe đạp điện Xiaomi Mi Electric Scooter 3",
  brand: "Xiaomi",
  model: "Mi Electric Scooter 3",
  price: 8500000,
  originalPrice: 9500000,
  description: "Xe đạp điện Xiaomi Mi Electric Scooter 3 với thiết kế gọn nhẹ, phù hợp cho việc di chuyển trong thành phố. Pin lithium-ion 36V 7.5Ah cho phép di chuyển xa lên đến 30km sau mỗi lần sạc.",
  specifications: {
    battery: "36V 7.5Ah Lithium-ion",
    motor: "250W",
    range: "30km",
    maxSpeed: "25km/h",
    weight: "12.5kg",
    chargingTime: "5-6 giờ"
  },
  images: [
    {
      url: "https://example.com/xiaomi-scooter-3-1.jpg",
      alt: "Xiaomi Mi Electric Scooter 3 - Góc trước"
    },
    {
      url: "https://example.com/xiaomi-scooter-3-2.jpg",
      alt: "Xiaomi Mi Electric Scooter 3 - Góc bên"
    }
  ],
  colors: [
    {
      name: "Đen",
      hex: "#000000"
    },
    {
      name: "Trắng",
      hex: "#FFFFFF"
    }
  ],
  category: "city",
  status: "available",
  stock: 20,
  features: [
    "Thiết kế gọn nhẹ",
    "Đèn LED",
    "Phanh đĩa",
    "Màn hình LED",
    "Chống nước IP54",
    "Ứng dụng thông minh"
  ],
  warranty: "12 tháng",
  rating: {
    average: 4.3,
    count: 156
  },
  isFeatured: true,
  isNew: true
};

// Function để test tạo xe đạp điện mới
async function createNewBike() {
  try {
    console.log('🚀 Đang tạo xe đạp điện mới...');
    console.log('📝 Dữ liệu:', JSON.stringify(newBike, null, 2));
    
    const response = await axios.post('http://localhost:5000/api/bikes', newBike, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    console.log('✅ Tạo thành công!');
    console.log('📊 Response:', JSON.stringify(response.data, null, 2));
    
    return response.data;
  } catch (error) {
    console.error('❌ Lỗi khi tạo xe đạp điện:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', JSON.stringify(error.response.data, null, 2));
    } else {
      console.error('Error:', error.message);
    }
  }
}

// Function để test lấy danh sách xe đạp điện
async function getBikes() {
  try {
    console.log('\n🔍 Đang lấy danh sách xe đạp điện...');
    
    const response = await axios.get('http://localhost:5000/api/bikes');
    
    console.log('✅ Lấy danh sách thành công!');
    console.log(`📊 Tổng số xe: ${response.data.pagination.totalItems}`);
    console.log(`📄 Trang hiện tại: ${response.data.pagination.currentPage}`);
    console.log('🚲 Danh sách xe:');
    
    response.data.data.forEach((bike, index) => {
      console.log(`${index + 1}. ${bike.name} - ${bike.brand} - ${bike.price.toLocaleString('vi-VN')} VND`);
    });
    
    return response.data;
  } catch (error) {
    console.error('❌ Lỗi khi lấy danh sách xe đạp điện:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', JSON.stringify(error.response.data, null, 2));
    } else {
      console.error('Error:', error.message);
    }
  }
}

// Function để test lấy xe nổi bật
async function getFeaturedBikes() {
  try {
    console.log('\n⭐ Đang lấy danh sách xe nổi bật...');
    
    const response = await axios.get('http://localhost:5000/api/bikes/featured/list');
    
    console.log('✅ Lấy xe nổi bật thành công!');
    console.log(`📊 Số xe nổi bật: ${response.data.data.length}`);
    console.log('⭐ Xe nổi bật:');
    
    response.data.data.forEach((bike, index) => {
      console.log(`${index + 1}. ${bike.name} - ${bike.brand} - Rating: ${bike.rating.average}/5`);
    });
    
    return response.data;
  } catch (error) {
    console.error('❌ Lỗi khi lấy xe nổi bật:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', JSON.stringify(error.response.data, null, 2));
    } else {
      console.error('Error:', error.message);
    }
  }
}

// Function để test lấy danh mục
async function getCategories() {
  try {
    console.log('\n📂 Đang lấy danh sách danh mục...');
    
    const response = await axios.get('http://localhost:5000/api/bikes/categories/list');
    
    console.log('✅ Lấy danh mục thành công!');
    console.log('📂 Danh mục:');
    
    response.data.data.forEach((category, index) => {
      console.log(`${index + 1}. ${category.name} - ${category.count} xe (${category.availableCount} có sẵn)`);
    });
    
    return response.data;
  } catch (error) {
    console.error('❌ Lỗi khi lấy danh mục:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', JSON.stringify(error.response.data, null, 2));
    } else {
      console.error('Error:', error.message);
    }
  }
}

// Chạy tất cả tests
async function runTests() {
  console.log('🧪 Bắt đầu test API...\n');
  
  // Test tạo xe mới
  await createNewBike();
  
  // Test lấy danh sách
  await getBikes();
  
  // Test lấy xe nổi bật
  await getFeaturedBikes();
  
  // Test lấy danh mục
  await getCategories();
  
  console.log('\n🎉 Hoàn thành tất cả tests!');
}

// Chạy nếu file được gọi trực tiếp
if (require.main === module) {
  runTests();
}

module.exports = {
  createNewBike,
  getBikes,
  getFeaturedBikes,
  getCategories,
  runTests
};


