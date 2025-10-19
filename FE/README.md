# Electric Bike Android App

Ứng dụng Android để bán xe đạp điện, kết nối với Backend Node.js + MongoDB.

## 🚀 Tính năng

- ✅ Hiển thị danh sách xe đạp điện từ API
- ✅ RecyclerView với CardView đẹp mắt
- ✅ Kết nối Backend qua Retrofit
- ✅ Loading states và error handling
- ✅ Bottom Navigation
- ✅ Fragment-based architecture

## 📱 Cấu trúc dự án

```
FE/app/src/main/java/com/example/myapplication/
├── MainActivity.java                 # Activity chính
├── model/
│   ├── Bike.java                    # Model xe đạp điện
│   ├── ApiResponse.java             # Model response từ API
│   └── Category.java                # Model danh mục
├── api/
│   ├── BikeApiService.java          # Interface API service
│   └── ApiClient.java               # Retrofit client
├── repository/
│   └── BikeRepository.java          # Repository pattern
├── adapter/
│   └── BikeAdapter.java             # RecyclerView adapter
└── fragment/
    └── BikeListFragment.java        # Fragment hiển thị danh sách
```

## 🔧 Cài đặt

### 1. Backend
Đảm bảo Backend đang chạy trên `http://localhost:5000`

```bash
cd BE
npm install
node server.js
```

### 2. Android App
1. Mở project trong Android Studio
2. Sync Gradle files
3. Chạy trên emulator hoặc device

### 3. Cấu hình IP
Nếu chạy trên device thật, cập nhật IP trong `ApiClient.java`:

```java
// Thay đổi từ:
private static final String BASE_URL = "http://10.0.2.2:5000/api/";

// Thành IP máy tính của bạn:
private static final String BASE_URL = "http://192.168.1.xxx:5000/api/";
```

## 📊 API Endpoints sử dụng

- `GET /api/bikes` - Lấy danh sách xe đạp điện
- `GET /api/bikes/featured/list` - Lấy xe nổi bật
- `GET /api/bikes/categories/list` - Lấy danh mục
- `POST /api/bikes` - Tạo xe mới (Admin)

## 🎨 UI Components

### Bike Card
- Hình ảnh xe đạp điện
- Tên, thương hiệu, giá
- Rating và số lượng đánh giá
- Badge "Nổi bật" và "Mới"
- Giảm giá (nếu có)

### Features
- Pull-to-refresh
- Loading indicators
- Error handling
- Empty states

## 🔄 Data Flow

1. **BikeListFragment** gọi **BikeRepository**
2. **BikeRepository** sử dụng **BikeApiService** (Retrofit)
3. **ApiClient** tạo Retrofit instance
4. Data được parse thành **Bike** objects
5. **BikeAdapter** hiển thị trong **RecyclerView**

## 🛠 Dependencies

```gradle
// Network
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// UI
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.github.bumptech.glide:glide:4.16.0'

// Navigation
implementation 'androidx.navigation:navigation-fragment:2.5.3'
implementation 'androidx.navigation:navigation-ui:2.5.3'
```

## 🚨 Troubleshooting

### Lỗi kết nối API
1. Kiểm tra Backend có chạy không
2. Kiểm tra IP address trong ApiClient
3. Kiểm tra internet permission
4. Kiểm tra `usesCleartextTraffic="true"`

### Lỗi build
1. Sync Gradle files
2. Clean và Rebuild project
3. Kiểm tra dependencies versions

## 📱 Screenshots

App sẽ hiển thị:
- Danh sách xe đạp điện với hình ảnh
- Thông tin chi tiết: giá, rating, stock
- Loading states khi tải dữ liệu
- Error messages khi có lỗi

## 🔮 Tính năng sắp tới

- [ ] Chi tiết xe đạp điện
- [ ] Tìm kiếm và filter
- [ ] Giỏ hàng
- [ ] User authentication
- [ ] Đặt hàng
- [ ] Push notifications