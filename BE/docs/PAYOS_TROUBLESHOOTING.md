# PayOS Troubleshooting Guide

## Lỗi: getaddrinfo ENOTFOUND api.payos.vn

### Nguyên nhân
Lỗi này xảy ra khi server không thể resolve DNS cho domain `api.payos.vn`. Có thể do:

1. **URL API sai**: PayOS có thể sử dụng domain khác
2. **Vấn đề kết nối mạng**: Server không có internet hoặc bị firewall chặn
3. **DNS không resolve được**: Domain không tồn tại hoặc bị chặn

### Giải pháp

#### 1. Kiểm tra URL trong .env

Mở file `.env` trong thư mục `BE` và kiểm tra:

```env
PAYOS_BASE_URL=https://api-merchant.payos.vn/v2
```

**Lưu ý**: 
- PayOS có thể sử dụng `api-merchant.payos.vn` thay vì `api.payos.vn`
- URL phải có protocol `https://`
- URL phải có `/v2` ở cuối

#### 2. Các URL có thể thử:

- `https://api-merchant.payos.vn/v2` (URL mới, khuyến nghị)
- `https://api.payos.vn/v2` (URL cũ, có thể không còn hoạt động)

#### 3. Kiểm tra kết nối mạng

Từ server, thử ping hoặc curl:

```bash
# Test DNS resolve
nslookup api-merchant.payos.vn

# Test connection
curl -v https://api-merchant.payos.vn/v2/payment-requests
```

#### 4. Kiểm tra Firewall/Proxy

Đảm bảo server có thể truy cập internet và không bị firewall chặn.

#### 5. Kiểm tra PayOS Dashboard

Đăng nhập vào PayOS Dashboard và xem tài liệu API để xác nhận URL chính xác:
- https://pay.payos.vn
- Kiểm tra phần "API Documentation" hoặc "Integration Guide"

### Cập nhật .env

Nếu URL đúng là `api-merchant.payos.vn`, cập nhật file `.env`:

```env
# PayOS Configuration
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
PAYOS_BASE_URL=https://api-merchant.payos.vn/v2
```

Sau đó restart server:

```bash
npm start
# hoặc
npm run dev
```

### Debug

Service sẽ log URL đang sử dụng khi khởi động:

```
PayOS Service initialized with baseUrl: https://api-merchant.payos.vn/v2
```

Nếu vẫn lỗi, kiểm tra log để xem URL chính xác đang được sử dụng.

### Liên hệ hỗ trợ

Nếu vẫn gặp vấn đề:
1. Kiểm tra PayOS Dashboard để xác nhận URL API
2. Liên hệ PayOS Support: https://payos.vn
3. Kiểm tra tài liệu chính thức: https://payos.vn/docs

