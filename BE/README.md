# Electric Bike Backend API

Backend API cho ứng dụng bán xe đạp điện được xây dựng với Node.js, Express và MongoDB.

## Cài đặt

1. Cài đặt dependencies:
```bash
npm install
```

2. Tạo file `.env` từ mẫu:
```bash
cp .env.example .env
```

3. Cập nhật các biến môi trường trong file `.env`:
   - `MONGODB_URI`: Đường dẫn kết nối MongoDB
   - `PORT`: Port chạy server (mặc định: 5000)
   - `JWT_SECRET`: Secret key cho JWT

4. Đảm bảo MongoDB đang chạy trên máy local hoặc cập nhật `MONGODB_URI` để kết nối với MongoDB cloud.

## Chạy ứng dụng

### Development mode:
```bash
npm run dev
```

### Production mode:
```bash
npm start
```

Server sẽ chạy trên port 5000 (hoặc port được cấu hình trong .env).

## API Endpoints

### Bikes

#### Lấy danh sách xe đạp điện
```
GET /api/bikes
```

Query parameters:
- `page`: Số trang (mặc định: 1)
- `limit`: Số item mỗi trang (mặc định: 10, tối đa: 100)
- `category`: Lọc theo danh mục (city, mountain, folding, cargo, sport, other)
- `status`: Lọc theo trạng thái (available, out_of_stock, discontinued)
- `minPrice`: Giá tối thiểu
- `maxPrice`: Giá tối đa
- `brand`: Lọc theo thương hiệu
- `search`: Tìm kiếm theo tên, thương hiệu, model
- `sortBy`: Sắp xếp (price_asc, price_desc, rating, name)

#### Lấy thông tin chi tiết xe đạp điện
```
GET /api/bikes/:id
```

#### Tạo xe đạp điện mới
```
POST /api/bikes
```

Body (JSON):
```json
{
  "name": "Tên xe đạp điện",
  "brand": "Thương hiệu",
  "model": "Model",
  "price": 15000000,
  "originalPrice": 18000000,
  "description": "Mô tả chi tiết",
  "specifications": {
    "battery": "48V 20Ah",
    "motor": "500W",
    "range": "80km",
    "maxSpeed": "25km/h",
    "weight": "25kg",
    "chargingTime": "6-8 giờ"
  },
  "images": [
    {
      "url": "https://example.com/image1.jpg",
      "alt": "Hình ảnh xe đạp điện"
    }
  ],
  "colors": [
    {
      "name": "Đỏ",
      "hex": "#FF0000"
    }
  ],
  "category": "city",
  "stock": 10,
  "features": ["Khóa thông minh", "Đèn LED", "Phanh đĩa"],
  "warranty": "12 tháng",
  "isFeatured": true,
  "isNew": true
}
```

#### Cập nhật xe đạp điện
```
PUT /api/bikes/:id
```

#### Xóa xe đạp điện
```
DELETE /api/bikes/:id
```

#### Lấy danh sách xe nổi bật
```
GET /api/bikes/featured/list
```

Query parameters:
- `limit`: Số lượng xe (mặc định: 8)

#### Lấy danh sách danh mục
```
GET /api/bikes/categories/list
```

### Health Check
```
GET /api/health
```

## Cấu trúc dự án

```
BE/
├── config/
│   ├── database.js          # Cấu hình kết nối MongoDB
│   └── .env                 # Biến môi trường (không commit)
├── middleware/
│   └── errorHandler.js      # Xử lý lỗi
├── models/
│   └── Bike.js              # Model xe đạp điện
├── routes/
│   └── bikeRoutes.js        # Routes cho API xe đạp điện
├── server.js                # File chính của server
├── package.json
└── README.md
```

## Tính năng

- ✅ CRUD operations cho xe đạp điện
- ✅ Tìm kiếm và lọc nâng cao
- ✅ Phân trang
- ✅ Validation dữ liệu
- ✅ Xử lý lỗi
- ✅ CORS support
- ✅ Text search với MongoDB
- ✅ Virtual fields (discount percentage)
- ✅ Auto-generated tags
- ✅ Rating system
- ✅ Image management
- ✅ Color variants
- ✅ Stock management

## Các tính năng sắp tới

- [ ] Authentication & Authorization
- [ ] User management
- [ ] Order management
- [ ] Payment integration
- [ ] File upload
- [ ] Email notifications
- [ ] Admin dashboard
- [ ] API documentation với Swagger


