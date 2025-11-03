# Hướng dẫn Test OpenCage API bằng Postman

Hướng dẫn test các API geocoding bằng Postman với URL và JSON body.

---

## Cấu hình Base URL

**Base URL**: `http://localhost:5001/api/locations`

**Lưu ý**: Thay `5001` bằng PORT của bạn nếu khác (kiểm tra trong `.env` hoặc khi start server)

---

## 1. Test Geocode (Địa chỉ → Tọa độ)

### Request
- **Method**: `POST`
- **URL**: `http://localhost:5001/api/locations/geocode`
- **Headers**: 
  - `Content-Type`: `application/json`
- **Body** (raw JSON):
```json
{
  "address": "Quận 1, TP.HCM"
}
```

### Response mong đợi:
```json
{
  "success": true,
  "data": {
    "latitude": 10.7769,
    "longitude": 106.7009,
    "formattedAddress": "Quận 1, Thành phố Hồ Chí Minh, Việt Nam"
  }
}
```

### Ví dụ khác:
```json
{
  "address": "123 Đường Nguyễn Huệ, Quận 1, TP.HCM"
}
```

```json
{
  "address": "Quận Ba Đình, Hà Nội"
}
```

---

## 2. Test Reverse Geocode (Tọa độ → Địa chỉ)

### Request
- **Method**: `POST`
- **URL**: `http://localhost:5001/api/locations/reverse-geocode`
- **Headers**: 
  - `Content-Type`: `application/json`
- **Body** (raw JSON):
```json
{
  "lat": 10.7769,
  "lng": 106.7009
}
```

### Response mong đợi:
```json
{
  "success": true,
  "data": {
    "formattedAddress": "Quận 1, Thành phố Hồ Chí Minh, Việt Nam",
    "addressComponents": {
      "street": "Đường Nguyễn Huệ",
      "houseNumber": "",
      "district": "Quận 1",
      "city": "Thành phố Hồ Chí Minh",
      "province": "Thành phố Hồ Chí Minh",
      "country": "Việt Nam"
    }
  }
}
```

### Ví dụ khác:
```json
{
  "lat": 21.0285,
  "lng": 105.8542
}
```
(Tọa độ Hà Nội)

---

## 3. Test Tìm Cửa hàng Gần nhất (POST - Với địa chỉ)

### Request
- **Method**: `POST`
- **URL**: `http://localhost:5001/api/locations/nearby`
- **Headers**: 
  - `Content-Type`: `application/json`
- **Body** (raw JSON):
```json
{
  "address": "Quận 1, TP.HCM",
  "radius": 10,
  "limit": 10
}
```

### Response mong đợi:
```json
{
  "success": true,
  "count": 3,
  "userLocation": {
    "latitude": 10.7769,
    "longitude": 106.7009,
    "address": "Quận 1, Thành phố Hồ Chí Minh, Việt Nam"
  },
  "searchRadius": 10,
  "data": [
    {
      "_id": "...",
      "name": "Cửa hàng Quận 1",
      "latitude": 10.7769,
      "longitude": 106.7009,
      "distance": 2.5,
      ...
    }
  ]
}
```

---

## 4. Test Tìm Cửa hàng Gần nhất (POST - Với GPS)

### Request
- **Method**: `POST`
- **URL**: `http://localhost:5001/api/locations/nearby`
- **Headers**: 
  - `Content-Type`: `application/json`
- **Body** (raw JSON):
```json
{
  "lat": 10.7769,
  "lng": 106.7009,
  "radius": 10,
  "limit": 10
}
```

### Response mong đợi:
```json
{
  "success": true,
  "count": 3,
  "userLocation": {
    "latitude": 10.7769,
    "longitude": 106.7009,
    "address": "Quận 1, Thành phố Hồ Chí Minh, Việt Nam"
  },
  "searchRadius": 10,
  "data": [...]
}
```

---

## 5. Test Tìm Cửa hàng Gần nhất (GET - Với địa chỉ)

### Request
- **Method**: `GET`
- **URL**: `http://localhost:5001/api/locations/nearby?address=Quận 1, TP.HCM&radius=10&limit=10`

**Không cần Body**

### Response: Giống như POST

---

## 6. Test Tìm Cửa hàng Gần nhất (GET - Với GPS)

### Request
- **Method**: `GET`
- **URL**: `http://localhost:5001/api/locations/nearby?lat=10.7769&lng=106.7009&radius=10&limit=10`

**Không cần Body**

### Response: Giống như POST

---

## 7. Test Lấy Stores cho Bản đồ (GET - Với địa chỉ)

### Request
- **Method**: `GET`
- **URL**: `http://localhost:5001/api/locations/map?address=Quận 1, TP.HCM&radius=10`

**Không cần Body**

### Response mong đợi:
```json
{
  "success": true,
  "count": 5,
  "userLocation": {
    "latitude": 10.7769,
    "longitude": 106.7009
  },
  "searchRadius": 10,
  "data": [
    {
      "id": "...",
      "name": "Cửa hàng Quận 1",
      "latitude": 10.7769,
      "longitude": 106.7009,
      "address": "123 Đường ABC",
      "city": "TP.HCM",
      "phone": "0123456789",
      "isOpenNow": true,
      "distance": 2.5
    }
  ]
}
```

---

## 8. Test Lấy Stores cho Bản đồ (GET - Với GPS)

### Request
- **Method**: `GET`
- **URL**: `http://localhost:5001/api/locations/map?lat=10.7769&lng=106.7009&radius=10`

**Không cần Body**

### Response: Giống như trên

---

## 9. Test Lấy Tất cả Stores cho Bản đồ (Không lọc)

### Request
- **Method**: `GET`
- **URL**: `http://localhost:5001/api/locations/map`

**Không cần Body, không cần Query params**

### Response:
```json
{
  "success": true,
  "count": 10,
  "userLocation": null,
  "searchRadius": null,
  "data": [
    {
      "id": "...",
      "name": "Cửa hàng...",
      "latitude": ...,
      "longitude": ...,
      "distance": null
    }
  ]
}
```

---

## Cách Test trong Postman

### Bước 1: Tạo Request mới
1. Mở Postman
2. Click **"New"** → **"HTTP Request"**
3. Chọn **Method**: `POST` hoặc `GET`

### Bước 2: Nhập URL
- Copy URL vào ô **"Enter request URL"**
- Ví dụ: `http://localhost:5001/api/locations/geocode`

### Bước 3: Thêm Headers (cho POST)
1. Click tab **"Headers"**
2. Thêm:
   - **Key**: `Content-Type`
   - **Value**: `application/json`

### Bước 4: Thêm Body (cho POST)
1. Click tab **"Body"**
2. Chọn **"raw"**
3. Chọn **"JSON"** từ dropdown
4. Paste JSON body vào

### Bước 5: Gửi Request
- Click nút **"Send"** (màu xanh)

### Bước 6: Xem Response
- Response sẽ hiện ở phần dưới
- Kiểm tra Status: `200 OK`
- Kiểm tra `"success": true`

---

## Ví dụ Test Đầy đủ trong Postman

### Test 1: Geocode
```
Method: POST
URL: http://localhost:5001/api/locations/geocode
Headers: Content-Type: application/json
Body:
{
  "address": "Quận 1, TP.HCM"
}
```

### Test 2: Reverse Geocode
```
Method: POST
URL: http://localhost:5001/api/locations/reverse-geocode
Headers: Content-Type: application/json
Body:
{
  "lat": 10.7769,
  "lng": 106.7009
}
```

### Test 3: Tìm cửa hàng (POST với địa chỉ)
```
Method: POST
URL: http://localhost:5001/api/locations/nearby
Headers: Content-Type: application/json
Body:
{
  "address": "Quận 1, TP.HCM",
  "radius": 10,
  "limit": 10
}
```

### Test 4: Tìm cửa hàng (GET với GPS)
```
Method: GET
URL: http://localhost:5001/api/locations/nearby?lat=10.7769&lng=106.7009&radius=10
```

---

## Lưu ý

### Nếu lỗi 404:
- Kiểm tra server đã chạy chưa: `npm start`
- Kiểm tra PORT có đúng không (mặc định 5001)

### Nếu lỗi "OpenCage API key chưa được cấu hình":
- Kiểm tra file `.env` có `OPENCAGE_API_KEY=...`
- Restart server sau khi thêm API key

### Nếu lỗi 500:
- Kiểm tra console log của server để xem lỗi chi tiết
- Đảm bảo OpenCage API key hợp lệ

---

## Collection Postman (Optional)

Bạn có thể tạo Collection trong Postman:

1. **New** → **Collection** → Đặt tên: "OpenCage API"
2. Tạo từng request như trên
3. **Save** để dùng lại sau

---

## Tóm tắt

### POST Requests (Cần Body):
1. ✅ `/geocode` - Geocode địa chỉ
2. ✅ `/reverse-geocode` - Reverse geocode tọa độ
3. ✅ `/nearby` - Tìm cửa hàng (có thể dùng GET hoặc POST)

### GET Requests (Không cần Body):
1. ✅ `/nearby?address=...` hoặc `/nearby?lat=...&lng=...`
2. ✅ `/map?address=...` hoặc `/map?lat=...&lng=...`
3. ✅ `/map` - Tất cả stores

**🎉 Sẵn sàng test!**

