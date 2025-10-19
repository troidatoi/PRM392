// Sample data để test API
const sampleBikes = [
  {
    name: "Xe đạp điện VinFast Klara S",
    brand: "VinFast",
    model: "Klara S",
    price: 15900000,
    originalPrice: 18900000,
    description: "Xe đạp điện VinFast Klara S với thiết kế hiện đại, phù hợp cho việc di chuyển trong thành phố. Pin lithium-ion 48V 20Ah cho phép di chuyển xa lên đến 80km sau mỗi lần sạc.",
    specifications: {
      battery: "48V 20Ah Lithium-ion",
      motor: "500W",
      range: "80km",
      maxSpeed: "25km/h",
      weight: "25kg",
      chargingTime: "6-8 giờ"
    },
    images: [
      {
        url: "https://example.com/vinfast-klara-s-1.jpg",
        alt: "VinFast Klara S - Góc trước"
      },
      {
        url: "https://example.com/vinfast-klara-s-2.jpg",
        alt: "VinFast Klara S - Góc bên"
      }
    ],
    colors: [
      {
        name: "Đỏ",
        hex: "#FF0000"
      },
      {
        name: "Xanh dương",
        hex: "#0066CC"
      },
      {
        name: "Trắng",
        hex: "#FFFFFF"
      }
    ],
    category: "city",
    status: "available",
    stock: 15,
    features: [
      "Khóa thông minh",
      "Đèn LED siêu sáng",
      "Phanh đĩa thủy lực",
      "Màn hình LCD",
      "Chế độ tiết kiệm pin",
      "Chống nước IPX4"
    ],
    warranty: "24 tháng",
    rating: {
      average: 4.5,
      count: 128
    },
    isFeatured: true,
    isNew: true,
    tags: ["vinfast", "klara", "city", "thành phố"]
  },
  {
    name: "Xe đạp điện Yadea C1S",
    brand: "Yadea",
    model: "C1S",
    price: 12900000,
    originalPrice: 14900000,
    description: "Xe đạp điện Yadea C1S với thiết kế gọn gàng, phù hợp cho nữ giới. Động cơ 350W mạnh mẽ, pin 48V 12Ah cho phép di chuyển 60km sau mỗi lần sạc.",
    specifications: {
      battery: "48V 12Ah Lithium-ion",
      motor: "350W",
      range: "60km",
      maxSpeed: "25km/h",
      weight: "22kg",
      chargingTime: "4-6 giờ"
    },
    images: [
      {
        url: "https://example.com/yadea-c1s-1.jpg",
        alt: "Yadea C1S - Thiết kế gọn gàng"
      }
    ],
    colors: [
      {
        name: "Hồng",
        hex: "#FF69B4"
      },
      {
        name: "Xanh lá",
        hex: "#00FF00"
      }
    ],
    category: "city",
    status: "available",
    stock: 8,
    features: [
      "Thiết kế gọn gàng",
      "Đèn LED",
      "Phanh đĩa",
      "Màn hình LED",
      "Chống nước cơ bản"
    ],
    warranty: "12 tháng",
    rating: {
      average: 4.2,
      count: 89
    },
    isFeatured: false,
    isNew: true,
    tags: ["yadea", "c1s", "city", "nữ"]
  },
  {
    name: "Xe đạp điện Giant Explore E+ 2",
    brand: "Giant",
    model: "Explore E+ 2",
    price: 45000000,
    originalPrice: 50000000,
    description: "Xe đạp điện Giant Explore E+ 2 là dòng xe cao cấp với khung nhôm siêu nhẹ, hệ thống truyền động Shimano 10 tốc độ. Phù hợp cho việc khám phá và leo núi.",
    specifications: {
      battery: "36V 500Wh Lithium-ion",
      motor: "250W Yamaha",
      range: "120km",
      maxSpeed: "25km/h",
      weight: "18kg",
      chargingTime: "3-4 giờ"
    },
    images: [
      {
        url: "https://example.com/giant-explore-e2-1.jpg",
        alt: "Giant Explore E+ 2 - Khung nhôm"
      }
    ],
    colors: [
      {
        name: "Đen",
        hex: "#000000"
      },
      {
        name: "Xám",
        hex: "#808080"
      }
    ],
    category: "mountain",
    status: "available",
    stock: 3,
    features: [
      "Khung nhôm siêu nhẹ",
      "Hệ thống truyền động Shimano",
      "Phanh đĩa thủy lực",
      "Lốp chuyên dụng",
      "Hệ thống treo trước",
      "Màn hình LCD thông minh"
    ],
    warranty: "36 tháng",
    rating: {
      average: 4.8,
      count: 45
    },
    isFeatured: true,
    isNew: false,
    tags: ["giant", "explore", "mountain", "leo núi", "cao cấp"]
  },
  {
    name: "Xe đạp điện Brompton Electric",
    brand: "Brompton",
    model: "Electric",
    price: 85000000,
    originalPrice: 95000000,
    description: "Xe đạp điện Brompton Electric với khả năng gấp gọn trong 20 giây. Thiết kế độc đáo của Anh, phù hợp cho việc di chuyển đa phương tiện.",
    specifications: {
      battery: "36V 300Wh Lithium-ion",
      motor: "250W",
      range: "70km",
      maxSpeed: "25km/h",
      weight: "16.5kg",
      chargingTime: "4-5 giờ"
    },
    images: [
      {
        url: "https://example.com/brompton-electric-1.jpg",
        alt: "Brompton Electric - Gấp gọn"
      }
    ],
    colors: [
      {
        name: "Đen",
        hex: "#000000"
      },
      {
        name: "Xanh navy",
        hex: "#000080"
      }
    ],
    category: "folding",
    status: "available",
    stock: 2,
    features: [
      "Gấp gọn trong 20 giây",
      "Khung thép cao cấp",
      "Bánh xe 16 inch",
      "Túi xách tích hợp",
      "Đèn LED tích hợp",
      "Khóa thông minh"
    ],
    warranty: "24 tháng",
    rating: {
      average: 4.7,
      count: 67
    },
    isFeatured: true,
    isNew: false,
    tags: ["brompton", "electric", "folding", "gấp gọn", "anh"]
  },
  {
    name: "Xe đạp điện Rad Power Bikes RadWagon 4",
    brand: "Rad Power Bikes",
    model: "RadWagon 4",
    price: 32000000,
    originalPrice: 35000000,
    description: "Xe đạp điện RadWagon 4 với khả năng chở hàng lên đến 150kg. Thiết kế chắc chắn, phù hợp cho việc vận chuyển hàng hóa và gia đình.",
    specifications: {
      battery: "48V 14Ah Lithium-ion",
      motor: "750W",
      range: "80km",
      maxSpeed: "25km/h",
      weight: "35kg",
      chargingTime: "6-8 giờ"
    },
    images: [
      {
        url: "https://example.com/radwagon-4-1.jpg",
        alt: "RadWagon 4 - Chở hàng"
      }
    ],
    colors: [
      {
        name: "Đen",
        hex: "#000000"
      },
      {
        name: "Xanh dương",
        hex: "#0066CC"
      }
    ],
    category: "cargo",
    status: "available",
    stock: 5,
    features: [
      "Chở hàng lên đến 150kg",
      "Động cơ 750W mạnh mẽ",
      "Phanh đĩa thủy lực",
      "Lốp chuyên dụng",
      "Giỏ hàng tích hợp",
      "Đèn LED siêu sáng"
    ],
    warranty: "12 tháng",
    rating: {
      average: 4.4,
      count: 34
    },
    isFeatured: false,
    isNew: true,
    tags: ["rad power", "radwagon", "cargo", "chở hàng", "gia đình"]
  }
];

module.exports = sampleBikes;


