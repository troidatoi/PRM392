# Luồng Mua Hàng - Bike E-commerce System

## Tổng quan hệ thống

Hệ thống cho phép người dùng mua xe đạp điện từ nhiều cửa hàng khác nhau. Mỗi cửa hàng có kho riêng và sẽ tạo đơn hàng riêng biệt.

## Các Model chính

### 1. User Model
```javascript
{
  username: String,        // Tên đăng nhập
  passwordHash: String,    // Mật khẩu đã hash
  email: String,          // Email
  phoneNumber: String,     // Số điện thoại
  address: String,        // Địa chỉ
  role: String,           // customer/admin/staff
  profile: {
    firstName: String,
    lastName: String,
    avatar: String
  }
}
```

### 2. Bike Model (Sản phẩm)
```javascript
{
  name: String,           // Tên xe đạp
  brand: String,          // Thương hiệu
  model: String,          // Model
  price: Number,          // Giá bán
  originalPrice: Number,   // Giá gốc
  description: String,     // Mô tả
  images: [String],       // Hình ảnh
  category: String,       // Danh mục
  status: String,        // available/out_of_stock/discontinued
  specifications: Object  // Thông số kỹ thuật
}
```
**Lưu ý:** Bike model KHÔNG lưu stock. Stock được quản lý riêng trong Inventory model.

### 3. Store Model (Cửa hàng)
```javascript
{
  name: String,           // Tên cửa hàng
  address: String,        // Địa chỉ
  city: String,          // Thành phố
  latitude: Number,       // Vĩ độ
  longitude: Number,      // Kinh độ
  phone: String,         // Số điện thoại
  email: String,         // Email
  isActive: Boolean,      // Trạng thái hoạt động
  operatingHours: Object  // Giờ hoạt động
}
```

### 4. Inventory Model (Kho hàng)
```javascript
{
  product: ObjectId,      // Sản phẩm
  store: ObjectId,        // Cửa hàng
  stock: Number,         // Số lượng tồn kho
  minStock: Number       // Số lượng tối thiểu
}
```
**Lưu ý:** Mỗi sản phẩm có thể có stock khác nhau tại từng cửa hàng.

### 5. Cart Model (Giỏ hàng)
```javascript
{
  user: ObjectId,         // Người dùng
  status: String,        // active/converted
  items: [ObjectId],     // Danh sách sản phẩm
  isMultiStore: Boolean  // Có sản phẩm từ nhiều cửa hàng
}
```

### 6. CartItem Model (Sản phẩm trong giỏ)
```javascript
{
  cart: ObjectId,         // Giỏ hàng
  product: ObjectId,      // Sản phẩm
  store: ObjectId,       // Cửa hàng
  quantity: Number       // Số lượng
}
```
**Lưu ý:** CartItem KHÔNG lưu price. Giá sẽ được lấy từ product hiện tại khi tạo order.

### 7. Order Model (Đơn hàng)
```javascript
{
  orderNumber: String,    // Số đơn hàng (tự động tạo)
  user: ObjectId,        // Người dùng
  store: ObjectId,       // Cửa hàng (1 order = 1 store)
  paymentMethod: String,  // Phương thức thanh toán
  shippingAddress: Object, // Địa chỉ giao hàng
  orderStatus: String,   // pending/confirmed/shipped/delivered/cancelled
  totalAmount: Number,   // Tổng tiền
  shippingFee: Number,   // Phí vận chuyển
  discountAmount: Number, // Số tiền giảm giá
  finalAmount: Number,  // Tiền cuối cùng
  orderDetails: [ObjectId], // Chi tiết đơn hàng
  notes: String         // Ghi chú
}
```

### 8. OrderDetail Model (Chi tiết đơn hàng)
```javascript
{
  order: ObjectId,       // Đơn hàng
  product: ObjectId,     // Sản phẩm
  quantity: Number,      // Số lượng
  price: Number,        // Giá tại thời điểm tạo order
  originalPrice: Number, // Giá gốc
  discount: Number,     // Số tiền giảm giá
  totalPrice: Number    // Tổng tiền
}
```

## Luồng mua hàng chi tiết

### Bước 1: Đăng nhập/Đăng ký
```
POST /api/auth/login
{
  "username": "user123",
  "password": "password123"
}
```

### Bước 2: Xem danh sách sản phẩm
```
GET /api/bikes?page=1&limit=20&category=city&brand=yamaha
```

### Bước 3: Xem chi tiết sản phẩm
```
GET /api/bikes/:productId
```

### Bước 4: Tạo giỏ hàng
```
POST /api/cart/create
{
  "userId": "user123"
}
```

### Bước 5: Kiểm tra tồn kho tại cửa hàng
```
POST /api/inventory/check-availability
{
  "productId": "bike001",
  "storeId": "store_hanoi",
  "quantity": 2
}
```

### Bước 6: Thêm sản phẩm vào giỏ hàng
```
POST /api/cart/add-item
{
  "cartId": "cart123",
  "productId": "bike001",
  "storeId": "store_hanoi",
  "quantity": 1
}
```

**Xử lý:**
- Tạo CartItem với store cụ thể
- Kiểm tra inventory tại cửa hàng đó
- KHÔNG lưu giá - sẽ lấy từ product khi tạo order
- Validate số lượng có đủ không

### Bước 7: Xem giỏ hàng (nhóm theo cửa hàng)
```
GET /api/cart/user/:userId
```

**Response:**
```json
{
  "success": true,
  "data": {
    "cart": {
      "_id": "cart123",
      "isMultiStore": true,
      "itemCount": 2,
      "storeCount": 2,
      "grandTotal": 27000000
    },
    "itemsByStore": [
      {
        "store": {
          "_id": "store_hanoi",
          "name": "Cửa hàng Hà Nội",
          "city": "Hà Nội"
        },
        "items": [
          {
            "_id": "cartitem001",
            "product": {
              "_id": "bike001",
              "name": "Yamaha Ego S",
              "brand": "Yamaha",
              "price": 15000000,
              "originalPrice": 18000000,
              "stock": 10,
              "images": ["image1.jpg"]
            },
            "quantity": 1,
            "totalPrice": 15000000
          }
        ],
        "storeTotal": 15000000
      },
      {
        "store": {
          "_id": "store_hcm",
          "name": "Cửa hàng TP.HCM",
          "city": "TP.HCM"
        },
        "items": [
          {
            "_id": "cartitem002",
            "product": {
              "_id": "bike002",
              "name": "Honda PCX",
              "brand": "Honda",
              "price": 12000000,
              "originalPrice": 14000000,
              "stock": 5,
              "images": ["image2.jpg"]
            },
            "quantity": 1,
            "totalPrice": 12000000
          }
        ],
        "storeTotal": 12000000
      }
    ],
    "summary": {
      "totalStores": 2,
      "totalItems": 2,
      "grandTotal": 27000000
    }
  }
}
```

### Bước 8: Thanh toán (Tạo nhiều đơn hàng)
```
POST /api/orders/create
{
  "userId": "user123",
  "cartId": "cart123",
  "shippingAddress": {
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "address": "789 Đường ABC",
    "city": "Hà Nội"
  },
  "paymentMethod": "vnpay",
  "notes": "Giao hàng trong giờ hành chính"
}
```

**Xử lý:**
1. Lấy giỏ hàng và nhóm sản phẩm theo cửa hàng
2. Tạo **1 Order riêng** cho **mỗi cửa hàng**
3. Lấy giá hiện tại từ product cho mỗi sản phẩm
4. Tạo OrderDetail với giá tại thời điểm tạo order
5. Giảm stock trong Inventory của từng cửa hàng
6. Đánh dấu Cart là "converted"

**Response:**
```json
{
  "success": true,
  "message": "Tạo thành công 2 đơn hàng riêng biệt",
  "data": {
    "orders": [
      {
        "order": {
          "_id": "order001",
          "orderNumber": "ORD202412011234",
          "storeLocation": "store_hanoi",
          "totalAmount": 15000000,
          "finalAmount": 15000000,
          "orderStatus": "pending"
        },
        "orderDetails": [...],
        "store": {
          "name": "Cửa hàng Hà Nội"
        }
      },
      {
        "order": {
          "_id": "order002", 
          "orderNumber": "ORD202412015678",
          "storeLocation": "store_hcm",
          "totalAmount": 12000000,
          "finalAmount": 12000000,
          "orderStatus": "pending"
        },
        "orderDetails": [...],
        "store": {
          "name": "Cửa hàng TP.HCM"
        }
      }
    ],
    "summary": {
      "totalOrders": 2,
      "totalAmount": 27000000,
      "stores": [
        {
          "storeName": "Cửa hàng Hà Nội",
          "orderId": "order001",
          "totalAmount": 15000000
        },
        {
          "storeName": "Cửa hàng TP.HCM", 
          "orderId": "order002",
          "totalAmount": 12000000
        }
      ]
    }
  }
}
```

### Bước 9: Xem đơn hàng của user
```
GET /api/orders/user/:userId?page=1&limit=10
```

### Bước 10: Xem chi tiết đơn hàng
```
GET /api/orders/:orderId
```

### Bước 11: Cập nhật trạng thái đơn hàng (Admin)
```
PUT /api/orders/:orderId/status
{
  "status": "confirmed"
}
```

### Bước 12: Hủy đơn hàng (nếu cần)
```
POST /api/orders/:orderId/cancel
{
  "reason": "Khách hàng không muốn mua nữa"
}
```

**Xử lý:**
- Cập nhật orderStatus = "cancelled"
- Hoàn lại stock vào Inventory của cửa hàng

## Luồng xử lý Cart sau khi tạo Order

### Trước khi tạo Order:
```json
{
  "cart": {
    "_id": "cart123",
    "user": "user123",
    "status": "active",  // ← Trạng thái ban đầu
    "items": ["item1", "item2"],
    "isMultiStore": true
  }
}
```

### Sau khi tạo Order thành công:
```json
{
  "cart": {
    "_id": "cart123",
    "user": "user123", 
    "status": "converted",  // ← Đã chuyển thành đơn hàng
    "items": ["item1", "item2"],
    "isMultiStore": true
  }
}
```

### Khi user xem cart lần sau:
```json
{
  "success": true,
  "data": null,
  "message": "Giỏ hàng trống"
}
```

**Lý do:** Hệ thống chỉ tìm cart có status = "active". Cart đã "converted" sẽ không hiển thị.

### Nếu user muốn mua tiếp:
```
POST /api/cart/create
{
  "userId": "user123"
}
```
**Sẽ tạo cart mới với status = "active"**

## Quản lý kho hàng

### Xem tồn kho của cửa hàng
```
GET /api/inventory/store/:storeId?page=1&limit=20&status=low_stock
```

### Nhập kho
```
POST /api/inventory/product/:productId/store/:storeId/add
{
  "quantity": 50
}
```

### Xuất kho
```
POST /api/inventory/product/:productId/store/:storeId/reduce
{
  "quantity": 10
}
```

### Xem sản phẩm sắp hết hàng
```
GET /api/inventory/store/:storeId/low-stock
```

### Xem sản phẩm hết hàng
```
GET /api/inventory/store/:storeId/out-of-stock
```

## Đặc điểm quan trọng

### 1. Multi-Store Support
- Mỗi CartItem có storeLocation riêng
- Khi thanh toán tự động chia thành nhiều Order
- Mỗi Order chỉ thuộc 1 cửa hàng

### 2. Inventory Management
- Mỗi cửa hàng có kho riêng (Inventory)
- Kiểm tra tồn kho trước khi thêm vào giỏ
- Giảm kho khi tạo Order thành công

### 3. Price Locking
- Giá được khóa khi thêm vào giỏ hàng
- Không thay đổi giá trong quá trình checkout

### 4. Order Status Flow
```
pending → confirmed → shipped → delivered
                ↓
            cancelled
```

### 5. Error Handling
- Kiểm tra tồn kho trước khi thêm vào giỏ
- Validate địa chỉ giao hàng
- Xử lý lỗi thanh toán

## API Endpoints Summary

### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập

### Products
- `GET /api/bikes` - Danh sách sản phẩm
- `GET /api/bikes/:id` - Chi tiết sản phẩm

### Cart
- `POST /api/cart/create` - Tạo giỏ hàng
- `GET /api/cart/user/:userId` - Xem giỏ hàng
- `POST /api/cart/add-item` - Thêm sản phẩm
- `PUT /api/cart/update-quantity/:itemId` - Cập nhật số lượng
- `DELETE /api/cart/remove-item/:itemId` - Xóa sản phẩm
- `DELETE /api/cart/clear/:userId` - Xóa tất cả sản phẩm

### Orders
- `POST /api/orders/create` - Tạo đơn hàng
- `GET /api/orders/user/:userId` - Đơn hàng của user
- `GET /api/orders/:orderId` - Chi tiết đơn hàng
- `PUT /api/orders/:orderId/status` - Cập nhật trạng thái
- `POST /api/orders/:orderId/cancel` - Hủy đơn hàng

### Inventory
- `GET /api/inventory/store/:storeId` - Tồn kho cửa hàng
- `POST /api/inventory/add` - Nhập kho
- `POST /api/inventory/reduce` - Xuất kho
- `POST /api/inventory/check-availability` - Kiểm tra tồn kho
- `GET /api/inventory/store/:storeId/low-stock` - Sản phẩm sắp hết hàng
- `GET /api/inventory/store/:storeId/out-of-stock` - Sản phẩm hết hàng

### Stores
- `GET /api/stores` - Danh sách cửa hàng
- `GET /api/stores/:id` - Chi tiết cửa hàng
- `POST /api/stores` - Tạo cửa hàng

## Database Relationships

```
User (1) ←→ (N) Cart
Cart (1) ←→ (N) CartItem
CartItem (N) ←→ (1) Bike
CartItem (N) ←→ (1) Store

User (1) ←→ (N) Order
Order (1) ←→ (1) Store
Order (1) ←→ (N) OrderDetail
OrderDetail (N) ←→ (1) Bike

Bike (1) ←→ (N) Inventory
Store (1) ←→ (N) Inventory
```

## Lưu ý cho Developer

1. **Luôn kiểm tra tồn kho** trước khi thêm vào giỏ
2. **Mỗi Order chỉ thuộc 1 cửa hàng** - không được mix
3. **KHÔNG lưu giá trong Cart** - lấy giá từ product khi tạo order
4. **Giảm kho ngay** khi tạo Order thành công
5. **Hoàn lại kho** khi hủy đơn hàng
6. **Validate địa chỉ** trước khi tạo Order
7. **Cart status = "converted"** sau khi tạo order thành công
8. **User cần tạo cart mới** để mua tiếp
9. **Xử lý lỗi** một cách graceful
10. **Log các hoạt động** quan trọng

## Testing Scenarios

### Test Case 1: Mua hàng từ 1 cửa hàng
1. User thêm 2 sản phẩm từ cùng 1 cửa hàng
2. Thanh toán → Tạo 1 Order duy nhất

### Test Case 2: Mua hàng từ nhiều cửa hàng
1. User thêm sản phẩm từ 2 cửa hàng khác nhau
2. Thanh toán → Tạo 2 Order riêng biệt

### Test Case 3: Hết hàng
1. User thêm sản phẩm đã hết hàng
2. System trả về lỗi "Chỉ còn X sản phẩm"

### Test Case 4: Hủy đơn hàng
1. User tạo Order thành công
2. User hủy Order
3. System hoàn lại kho hàng

---

**Tài liệu này sẽ giúp team hiểu rõ luồng mua hàng và cách implement các tính năng.**



