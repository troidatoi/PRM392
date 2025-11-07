# Hướng dẫn Import Shipping Rates vào MongoDB

## Files có sẵn:

- `shippingRates.json` - File đầy đủ với date fields (cho MongoDB Compass)
- `shippingRates-simple.json` - File đơn giản không có date (khuyên dùng cho mongoimport)

## Cách 1: Sử dụng MongoDB Compass (GUI)

1. Mở MongoDB Compass
2. Kết nối đến database của bạn
3. Chọn database (ví dụ: `your_database_name`)
4. Click vào collection `shippingrates` (hoặc tạo mới nếu chưa có)
5. Click vào tab "Documents"
6. Click nút "Import Data" hoặc "Add Data" > "Import File"
7. Chọn file `shippingRates-simple.json` (hoặc `shippingRates.json`)
8. Chọn format: JSON
9. Click "Import"

## Cách 2: Sử dụng MongoDB Shell (mongosh)

```bash
mongosh "mongodb://localhost:27017/your_database_name"

# Import file
mongoimport --db your_database_name --collection shippingrates --file BE/scripts/shippingRates-simple.json --jsonArray
```

## Cách 3: Sử dụng mongoimport (command line)

```bash
# Windows
mongoimport --db your_database_name --collection shippingrates --file BE\scripts\shippingRates-simple.json --jsonArray

# Linux/Mac
mongoimport --db your_database_name --collection shippingrates --file BE/scripts/shippingRates-simple.json --jsonArray
```

## Cách 4: Sử dụng MongoDB Atlas (Cloud)

1. Đăng nhập vào MongoDB Atlas
2. Chọn cluster của bạn
3. Click "Browse Collections"
4. Chọn database và collection `shippingrates`
5. Click "INSERT DOCUMENT"
6. Copy nội dung từ file JSON và paste vào
7. Hoặc click "Import File" và chọn file `shippingRates-simple.json`

## Lưu ý:

- Thay `your_database_name` bằng tên database thực tế của bạn
- Collection name trong MongoDB sẽ là `shippingrates` (lowercase, plural) dựa trên model `ShippingRate`
- Nếu collection đã có dữ liệu, bạn có thể xóa trước hoặc cập nhật các document hiện có

## Kiểm tra sau khi import:

```javascript
// Trong MongoDB Shell
db.shippingrates.find().pretty()
```

Bạn sẽ thấy 4 documents với các mốc khoảng cách:
- 0-3km: 5.000₫/km
- 3-10km: 3.000₫/km  
- 10-50km: 2.000₫/km
- >50km: 1.000₫/km

