# PayOS Integration Guide

## Tổng quan

Dự án đã được tích hợp với PayOS để xử lý thanh toán trực tuyến. PayOS là một cổng thanh toán phổ biến tại Việt Nam, hỗ trợ nhiều phương thức thanh toán như thẻ ATM nội địa, ví điện tử, và thẻ quốc tế.

## Cấu hình

### Environment Variables

Đảm bảo các biến môi trường sau được cấu hình trong file `.env`:

```env
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
PAYOS_BASE_URL=https://api-merchant.payos.vn/v2
BACKEND_WEBHOOK_URL=https://your-backend-url.com/api/payments/webhook
FRONTEND_URL=your_frontend_url
```

### Models

- **Payment Model**: Đã được cập nhật để hỗ trợ `payos` trong enum `paymentMethod`
- **Order Model**: Đã được cập nhật để hỗ trợ `payos` trong enum `paymentMethod`

## API Endpoints

### 1. Tạo Payment Link

**POST** `/api/payments/order/:orderId/create-link`

Tạo link thanh toán PayOS cho một đơn hàng.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "returnUrl": "https://yourfrontend.com/payment/success?orderId=xxx",
  "cancelUrl": "https://yourfrontend.com/payment/cancel?orderId=xxx"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo link thanh toán thành công",
  "data": {
    "paymentId": "payment_id",
    "checkoutUrl": "https://pay.payos.vn/web/...",
    "qrCode": "data:image/png;base64,...",
    "orderCode": 123456789,
    "amount": 1000000,
    "accountNumber": "1234567890",
    "accountName": "PAYOS"
  }
}
```

### 2. Webhook Handler

**POST** `/api/payments/webhook/payos`

Endpoint để nhận webhook từ PayOS khi thanh toán thay đổi trạng thái.

**Headers:**
```
x-payos-signature: <checksum>
```

**Request Body:** (Từ PayOS)
```json
{
  "code": 0,
  "desc": "Success",
  "data": {
    "orderCode": 123456789,
    "amount": 1000000,
    "status": "PAID",
    "transactionDateTime": "2024-01-01T00:00:00Z"
  }
}
```

**Response:**
```json
{
  "code": 0,
  "desc": "Success",
  "data": null
}
```

### 3. Xác minh Thanh toán

**GET** `/api/payments/verify?orderCode=123456789`

Xác minh trạng thái thanh toán khi user quay lại từ PayOS.

**Response:**
```json
{
  "success": true,
  "message": "Xác minh thanh toán thành công",
  "data": {
    "payment": {...},
    "payosStatus": "PAID",
    "isPaid": true
  }
}
```

### 4. Lấy Thông tin Thanh toán

**GET** `/api/payments/:paymentId`

Lấy thông tin chi tiết của một payment.

**GET** `/api/payments/order/:orderId`

Lấy thông tin thanh toán theo order ID.

### 5. Hủy Payment Link

**POST** `/api/payments/:paymentId/cancel`

Hủy link thanh toán (chỉ áp dụng cho payment chưa thanh toán).

**Request Body:**
```json
{
  "reason": "Người dùng hủy thanh toán"
}
```

## Flow Thanh toán

### 1. Tạo đơn hàng với PayOS

```javascript
// POST /api/orders/create
{
  "userId": "user_id",
  "shippingAddress": {...},
  "paymentMethod": "payos",
  "notes": "..."
}
```

### 2. Tạo payment link

```javascript
// POST /api/payments/order/:orderId/create-link
{
  "returnUrl": "https://frontend.com/payment/success",
  "cancelUrl": "https://frontend.com/payment/cancel"
}
```

### 3. Redirect user đến checkoutUrl

```javascript
// Redirect user đến data.checkoutUrl từ response ở bước 2
window.location.href = data.checkoutUrl;
```

### 4. User thanh toán trên PayOS

User sẽ được redirect đến PayOS để thực hiện thanh toán.

### 5. PayOS gửi webhook

PayOS sẽ tự động gửi webhook đến `/api/payments/webhook/payos` khi thanh toán thay đổi trạng thái.

### 6. User được redirect về returnUrl

Sau khi thanh toán, user sẽ được redirect về `returnUrl` với query params `orderCode`.

### 7. Xác minh thanh toán

```javascript
// GET /api/payments/verify?orderCode=123456789
// Kiểm tra trạng thái thanh toán và cập nhật UI
```

## Trạng thái Thanh toán

- `pending`: Chờ thanh toán
- `processing`: Đang xử lý (đã tạo payment link)
- `completed`: Thanh toán thành công
- `failed`: Thanh toán thất bại
- `cancelled`: Đã hủy
- `refunded`: Đã hoàn tiền

## Trạng thái PayOS

- `PAID`: Đã thanh toán
- `CANCELLED`: Đã hủy
- `EXPIRED`: Hết hạn
- `PENDING`: Đang chờ

## Xử lý Lỗi

### Common Errors

1. **Invalid signature**: Webhook signature không hợp lệ
2. **Payment not found**: Không tìm thấy payment với orderCode
3. **Amount too small**: Số tiền phải >= 1000 VND
4. **Payment already completed**: Payment đã được thanh toán

## Testing

### Test với PayOS Sandbox

1. Đăng ký tài khoản PayOS tại https://pay.payos.vn
2. Lấy credentials từ PayOS Dashboard
3. Cấu hình webhook URL trong PayOS Dashboard: Sử dụng giá trị từ `BACKEND_WEBHOOK_URL` (ví dụ: `https://your-backend.com/api/payments/webhook`)
4. Test với số tiền nhỏ (>= 1000 VND)

### Test Webhook Locally

Sử dụng ngrok hoặc cloudflare tunnel để expose local server:

```bash
# Ngrok
ngrok http 5000

# Hoặc cloudflare tunnel (đã có trong project)
# Cập nhật BACKEND_WEBHOOK_URL trong .env với URL đầy đủ
```

## Security

1. **Checksum Verification**: Tất cả webhook từ PayOS đều được verify checksum
2. **Authentication**: Các endpoint (trừ webhook) đều yêu cầu authentication
3. **HTTPS**: Production environment phải sử dụng HTTPS
4. **Webhook URL**: Đảm bảo webhook URL được bảo vệ và chỉ PayOS có thể gọi

## Notes

- PayOS orderCode phải là số nguyên duy nhất từ 1 đến 999999999999
- Payment link có thời gian hết hạn (mặc định là 15 phút)
- Webhook có thể được gọi nhiều lần, cần đảm bảo idempotency
- Luôn verify webhook signature trước khi xử lý

## Support

Nếu gặp vấn đề, tham khảo:
- PayOS Documentation: https://payos.vn/docs
- PayOS Dashboard: https://pay.payos.vn

