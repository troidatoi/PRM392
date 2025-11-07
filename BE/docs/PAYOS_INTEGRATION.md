# Hướng dẫn tích hợp PayOS

## Tổng quan

PayOS đã được tích hợp vào hệ thống thanh toán của dự án. PayOS là cổng thanh toán qua VietQR, cho phép khách hàng thanh toán bằng ứng dụng ngân hàng.

## Cấu hình

### 1. Đăng ký tài khoản PayOS

1. Truy cập https://my.payos.vn và đăng ký tài khoản
2. Xác thực tổ chức của bạn
3. Liên kết tài khoản ngân hàng với PayOS
4. Tạo kênh thanh toán và lấy các thông tin sau:
   - **Client ID**
   - **API Key**
   - **Checksum Key**

### 2. Cấu hình Environment Variables

Thêm các biến môi trường sau vào file `.env`:

```env
# PayOS Configuration
PAYOS_CLIENT_ID=your_client_id_here
PAYOS_API_KEY=your_api_key_here
PAYOS_CHECKSUM_KEY=your_checksum_key_here
PAYOS_BASE_URL=https://api.payos.vn
```

**Lưu ý:** 
- `PAYOS_BASE_URL` mặc định là `https://api.payos.vn`, chỉ cần thay đổi nếu bạn đang test với sandbox
- Không commit file `.env` vào git

### 3. Cấu hình Webhook

1. Đăng nhập vào https://my.payos.vn
2. Vào phần cấu hình kênh thanh toán
3. Thiết lập Webhook URL: `https://your-domain.com/api/payos/webhook`
4. Lưu ý: Webhook URL phải là HTTPS và có thể truy cập công khai

## Luồng thanh toán PayOS

### Bước 1: Tạo đơn hàng

Khi khách hàng chọn phương thức thanh toán PayOS và hoàn tất đơn hàng:

```http
POST /api/orders/create
Content-Type: application/json
Authorization: Bearer {token}

{
  "userId": "user_id",
  "shippingAddress": {
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "address": "123 Đường ABC",
    "city": "Hà Nội"
  },
  "paymentMethod": "payos",
  "notes": "Giao hàng trong giờ hành chính"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo thành công 1 đơn hàng riêng biệt",
  "data": {
    "orders": [
      {
        "order": {
          "_id": "order_id",
          "orderNumber": "ORD202412011234",
          "finalAmount": 15000000,
          "paymentMethod": "payos"
        },
        "payment": {
          "_id": "payment_id",
          "paymentStatus": "pending",
          "paymentMethod": "payos"
        }
      }
    ]
  }
}
```

### Bước 2: Tạo Payment Link

Sau khi tạo đơn hàng, gọi API để tạo payment link:

```http
POST /api/payos/orders/:orderId/create-link
Content-Type: application/json
Authorization: Bearer {token}

{
  "returnUrl": "https://your-app.com/payment/success",
  "cancelUrl": "https://your-app.com/payment/cancel"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo payment link thành công",
  "data": {
    "paymentLink": {
      "checkoutUrl": "https://pay.payos.vn/web/...",
      "qrCode": "data:image/png;base64,...",
      "orderCode": 1234567890,
      "amount": 15000000,
      "description": "Thanh toán đơn hàng ORD202412011234"
    },
    "paymentId": "payment_id",
    "orderId": "order_id"
  }
}
```

### Bước 3: Chuyển hướng khách hàng

Sử dụng `checkoutUrl` hoặc hiển thị `qrCode` để khách hàng thanh toán:

**Option 1: Redirect đến PayOS checkout page**
```javascript
window.location.href = paymentLink.checkoutUrl;
```

**Option 2: Hiển thị QR Code**
```html
<img src={paymentLink.qrCode} alt="PayOS QR Code" />
```

### Bước 4: Xử lý kết quả thanh toán

PayOS sẽ gửi kết quả về theo 2 cách:

#### 4.1. Return URL (Redirect)

Sau khi thanh toán thành công, PayOS sẽ redirect về `returnUrl` với các tham số:
- `code`: Mã kết quả (00 = thành công)
- `desc`: Mô tả
- `data`: Dữ liệu đơn hàng

**Xử lý trên frontend:**
```javascript
// Trong returnUrl page
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');
const orderCode = urlParams.get('orderCode');

if (code === '00') {
  // Thanh toán thành công
  // Gọi API verify để cập nhật trạng thái
  verifyPayment(orderId);
} else {
  // Thanh toán thất bại
  showError('Thanh toán thất bại');
}
```

#### 4.2. Webhook (Backend)

PayOS sẽ gửi POST request đến webhook URL của bạn:

```http
POST /api/payos/webhook
Content-Type: application/json

{
  "code": "00",
  "desc": "Thành công",
  "data": {
    "orderCode": 1234567890,
    "amount": 15000000,
    "description": "Thanh toán đơn hàng ORD202412011234",
    "accountNumber": "970422",
    "accountName": "NGUYEN VAN A",
    "reference": "ref123",
    "transactionDateTime": "2024-12-01T10:30:00Z"
  },
  "signature": "checksum_signature"
}
```

**Lưu ý:** Webhook handler sẽ tự động:
- Xác thực signature
- Cập nhật trạng thái payment
- Cập nhật trạng thái order thành "confirmed" nếu thanh toán thành công

### Bước 5: Verify Payment Status (Optional)

Nếu cần kiểm tra trạng thái thanh toán:

```http
GET /api/payos/orders/:orderId/verify
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "paymentStatus": "completed",
    "orderStatus": "confirmed",
    "payment": {
      "_id": "payment_id",
      "amount": 15000000,
      "paymentStatus": "completed",
      "paymentMethod": "payos"
    }
  }
}
```

## API Endpoints

### 1. Tạo Payment Link
```http
POST /api/payos/orders/:orderId/create-link
Authorization: Bearer {token}
Body: { returnUrl, cancelUrl }
```

### 2. Verify Payment
```http
GET /api/payos/orders/:orderId/verify
Authorization: Bearer {token}
```

### 3. Cancel Payment Link
```http
POST /api/payos/orders/:orderId/cancel-link
Authorization: Bearer {token}
Body: { reason? }
```

### 4. Webhook Handler
```http
POST /api/payos/webhook
Content-Type: application/json
Body: PayOS webhook data
```

## Xử lý lỗi

### Lỗi thường gặp

1. **Invalid webhook signature**
   - Kiểm tra `PAYOS_CHECKSUM_KEY` trong `.env`
   - Đảm bảo webhook data không bị thay đổi

2. **Payment link creation failed**
   - Kiểm tra `PAYOS_CLIENT_ID` và `PAYOS_API_KEY`
   - Đảm bảo số tiền >= 1,000 VND
   - Kiểm tra orderCode không trùng lặp

3. **Order not found**
   - Đảm bảo orderId hợp lệ
   - Kiểm tra order đã được tạo thành công

## Testing

### Test với PayOS Sandbox

1. Sử dụng sandbox credentials từ PayOS
2. Set `PAYOS_BASE_URL=https://api-sandbox.payos.vn` (nếu có)
3. Test với số tiền nhỏ

### Test Webhook locally

Sử dụng ngrok để expose local server:

```bash
ngrok http 5000
```

Sau đó cấu hình webhook URL trên PayOS dashboard:
```
https://your-ngrok-url.ngrok.io/api/payos/webhook
```

## Lưu ý quan trọng

1. **Security:**
   - Không commit credentials vào git
   - Luôn verify webhook signature
   - Sử dụng HTTPS cho production

2. **Error Handling:**
   - Luôn xử lý các trường hợp lỗi
   - Log các transaction quan trọng
   - Có cơ chế retry cho webhook

3. **Order Status:**
   - Order status chỉ được cập nhật khi payment thành công
   - Nếu payment thất bại, order vẫn ở trạng thái "pending"

4. **Idempotency:**
   - Mỗi order chỉ nên tạo 1 payment link
   - Nếu payment link đã tồn tại, có thể reuse hoặc tạo mới

## Ví dụ Frontend Integration

```javascript
// 1. Tạo đơn hàng
const createOrder = async (orderData) => {
  const response = await fetch('/api/orders/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      ...orderData,
      paymentMethod: 'payos'
    })
  });
  return response.json();
};

// 2. Tạo payment link
const createPaymentLink = async (orderId, returnUrl, cancelUrl) => {
  const response = await fetch(`/api/payos/orders/${orderId}/create-link`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ returnUrl, cancelUrl })
  });
  return response.json();
};

// 3. Xử lý thanh toán
const handlePayment = async () => {
  // Tạo đơn hàng
  const orderResult = await createOrder(orderData);
  const orderId = orderResult.data.orders[0].order._id;
  
  // Tạo payment link
  const paymentLinkResult = await createPaymentLink(
    orderId,
    'https://your-app.com/payment/success',
    'https://your-app.com/payment/cancel'
  );
  
  // Redirect đến PayOS
  window.location.href = paymentLinkResult.data.paymentLink.checkoutUrl;
  
  // Hoặc hiển thị QR Code
  // setQrCode(paymentLinkResult.data.paymentLink.qrCode);
};
```

## Tài liệu tham khảo

- [PayOS Documentation](https://payos.vn/docs/)
- [PayOS API Reference](https://payos.vn/docs/api-reference/)
- [PayOS GitHub Examples](https://github.com/payosHQ)

