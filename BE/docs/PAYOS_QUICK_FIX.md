# PayOS Quick Fix

## ✅ Kết quả test kết nối:

- ✅ `api-merchant.payos.vn` - **HOẠT ĐỘNG BÌNH THƯỜNG**
- ❌ `api.payos.vn` - **KHÔNG CÒN HOẠT ĐỘNG**

## 🔧 Giải pháp:

**KHÔNG CẦN TẮT FIREWALL!** 

Vấn đề là server đang dùng URL cũ. Chỉ cần:

1. **Restart server** để load URL mới từ `.env`:
   ```bash
   # Dừng server (Ctrl+C)
   npm start
   # hoặc
   npm run dev
   ```

2. **Kiểm tra log** khi server khởi động:
   ```
   PayOS Service initialized with baseUrl: https://api-merchant.payos.vn/v2
   ```

3. **Test lại** - Lỗi sẽ hết sau khi restart.

## 📝 Lưu ý:

- `.env` đã có URL đúng: `PAYOS_BASE_URL=https://api-merchant.payos.vn/v2`
- Code đã tự động convert URL cũ → mới
- **KHÔNG** cần tắt firewall hoặc thay đổi cấu hình mạng

