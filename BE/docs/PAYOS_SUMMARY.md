# Tóm tắt tích hợp PayOS

## Các file đã tạo/cập nhật

### 1. Models
- ✅ `BE/models/Payment.js` - Thêm 'payos' vào enum paymentMethod
- ✅ `BE/models/Order.js` - Thêm 'payos' vào enum paymentMethod

### 2. Services
- ✅ `BE/utils/payos.js` - Service xử lý PayOS API:
  - `createPaymentLink()` - Tạo payment link
  - `getPaymentLinkInfo()` - Lấy thông tin payment link
  - `cancelPaymentLink()` - Hủy payment link
  - `verifyWebhook()` - Xác thực webhook signature
  - `createChecksum()` - Tạo checksum cho request

### 3. Controllers
- ✅ `BE/controllers/payosController.js` - Controller xử lý PayOS:
  - `createPaymentLink` - Tạo payment link cho đơn hàng
  - `handleWebhook` - Xử lý webhook từ PayOS
  - `verifyPayment` - Xác thực trạng thái thanh toán
  - `cancelPaymentLink` - Hủy payment link

### 4. Routes
- ✅ `BE/routes/payosRoutes.js` - Routes cho PayOS:
  - `POST /api/payos/orders/:orderId/create-link` - Tạo payment link
  - `GET /api/payos/orders/:orderId/verify` - Verify payment
  - `POST /api/payos/orders/:orderId/cancel-link` - Hủy payment link
  - `POST /api/payos/webhook` - Webhook handler

### 5. Order Controller
- ✅ `BE/controllers/orderController.js` - Cập nhật:
  - Hỗ trợ paymentMethod 'payos' (thay vì chỉ 'vnpay')
  - Tự động tạo Payment record khi tạo order với payos/vnpay

### 6. Server
- ✅ `BE/server.js` - Đăng ký PayOS routes

### 7. Documentation
- ✅ `BE/docs/PAYOS_INTEGRATION.md` - Hướng dẫn chi tiết tích hợp PayOS

## Luồng thanh toán PayOS

1. **Tạo đơn hàng** (`POST /api/orders/create`)
   - User chọn paymentMethod: 'payos'
   - Hệ thống tạo Order và Payment record (status: pending)

2. **Tạo payment link** (`POST /api/payos/orders/:orderId/create-link`)
   - Gọi PayOS API để tạo payment link
   - Lưu transactionId vào Payment record
   - Trả về checkoutUrl và qrCode

3. **Khách hàng thanh toán**
   - Redirect đến checkoutUrl hoặc quét QR Code
   - Thanh toán qua ứng dụng ngân hàng

4. **Xử lý kết quả**
   - PayOS redirect về returnUrl
   - PayOS gửi webhook đến `/api/payos/webhook`
   - Hệ thống tự động cập nhật Payment và Order status

5. **Verify payment** (optional)
   - `GET /api/payos/orders/:orderId/verify`
   - Kiểm tra trạng thái thanh toán từ PayOS

## Cấu hình cần thiết

Thêm vào file `.env`:

```env
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
PAYOS_BASE_URL=https://api.payos.vn
```

## Testing

1. Đăng ký tài khoản PayOS tại https://my.payos.vn
2. Lấy credentials từ kênh thanh toán
3. Cấu hình webhook URL trên PayOS dashboard
4. Test với đơn hàng thật hoặc sandbox

## Lưu ý

- PayOS yêu cầu orderCode là số nguyên dương và unique
- Số tiền tối thiểu: 1,000 VND
- Webhook URL phải là HTTPS và có thể truy cập công khai
- Luôn verify webhook signature để đảm bảo an toàn

## API Endpoints

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| POST | `/api/payos/orders/:orderId/create-link` | ✅ | Tạo payment link |
| GET | `/api/payos/orders/:orderId/verify` | ✅ | Verify payment status |
| POST | `/api/payos/orders/:orderId/cancel-link` | ✅ | Hủy payment link |
| POST | `/api/payos/webhook` | ❌ | Webhook handler |

## Next Steps

1. Cấu hình PayOS credentials trong `.env`
2. Test tạo payment link
3. Cấu hình webhook URL trên PayOS dashboard
4. Test luồng thanh toán đầy đủ
5. Tích hợp vào frontend (nếu cần)

