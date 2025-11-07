const axios = require('axios');
const crypto = require('crypto');

/**
 * PayOS Service
 * Xử lý các tương tác với PayOS API
 */
class PayOSService {
  constructor() {
    // Lấy thông tin từ environment variables
    this.clientId = process.env.PAYOS_CLIENT_ID;
    this.apiKey = process.env.PAYOS_API_KEY;
    this.checksumKey = process.env.PAYOS_CHECKSUM_KEY;
    // PAYOS_BASE_URL có thể có /v2 ở cuối, cần loại bỏ để tránh duplicate
    let baseUrl = process.env.PAYOS_BASE_URL || 'https://api-merchant.payos.vn';
    // Loại bỏ /v2 nếu có ở cuối vì sẽ thêm vào khi gọi API
    this.baseUrl = baseUrl.replace(/\/v2\/?$/, '');
    
    // Validate required config
    if (!this.clientId || !this.apiKey || !this.checksumKey) {
      console.warn('⚠️ PayOS credentials chưa được cấu hình. Vui lòng thêm PAYOS_CLIENT_ID, PAYOS_API_KEY, PAYOS_CHECKSUM_KEY vào .env');
    }
  }

  /**
   * Tạo signature theo format PayOS yêu cầu
   * Format: amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
   * (sorted alphabetically)
   */
  createSignature(data) {
    const { amount, cancelUrl, description, orderCode, returnUrl } = data;
    
    // Tạo string theo format alphabetically sorted
    const dataString = `amount=${amount}&cancelUrl=${cancelUrl}&description=${description}&orderCode=${orderCode}&returnUrl=${returnUrl}`;
    
    // Tạo HMAC SHA256
    const hmac = crypto.createHmac('sha256', this.checksumKey);
    hmac.update(dataString);
    return hmac.digest('hex');
  }

  /**
   * Tạo checksum từ data (cho webhook verification)
   */
  createChecksum(data) {
    const dataString = JSON.stringify(data);
    const hmac = crypto.createHmac('sha256', this.checksumKey);
    hmac.update(dataString);
    return hmac.digest('hex');
  }

  /**
   * Verify checksum từ webhook
   */
  verifyWebhookChecksum(data, checksum) {
    const calculatedChecksum = this.createChecksum(data);
    return calculatedChecksum === checksum;
  }

  /**
   * Tạo payment link
   * @param {Object} paymentData - Dữ liệu thanh toán
   * @param {Number} paymentData.orderCode - Mã đơn hàng (unique)
   * @param {Number} paymentData.amount - Số tiền (VND)
   * @param {String} paymentData.description - Mô tả đơn hàng
   * @param {String} paymentData.returnUrl - URL redirect sau khi thanh toán thành công
   * @param {String} paymentData.cancelUrl - URL redirect khi hủy thanh toán
   * @param {Object} paymentData.items - Danh sách sản phẩm (optional)
   * @returns {Promise<Object>} Payment link data
   */
  async createPaymentLink(paymentData) {
    try {
      const { 
        orderCode, 
        amount, 
        description, 
        returnUrl, 
        cancelUrl, 
        items = [],
        buyerName,
        buyerPhone,
        buyerEmail,
        buyerAddress
      } = paymentData;

      // Validate required fields
      if (!orderCode || !amount || !description || !returnUrl || !cancelUrl) {
        throw new Error('Thiếu thông tin bắt buộc để tạo payment link');
      }

      // Validate amount (minimum 1000 VND)
      if (amount < 1000) {
        throw new Error('Số tiền tối thiểu là 1,000 VND');
      }

      // Prepare request data (không bao gồm signature trong data để tính signature)
      const requestData = {
        orderCode: parseInt(orderCode),
        amount: parseInt(amount),
        description: description,
        cancelUrl: cancelUrl,
        returnUrl: returnUrl
      };

      // Thêm items nếu có
      if (items && items.length > 0) {
        requestData.items = items.map(item => ({
          name: item.name || 'Sản phẩm',
          quantity: parseInt(item.quantity) || 1,
          price: parseInt(item.price) || 0
        }));
      }

      // Thêm buyer information nếu có (optional - dùng cho hóa đơn điện tử)
      if (buyerName) requestData.buyerName = buyerName;
      if (buyerPhone) requestData.buyerPhone = buyerPhone;
      if (buyerEmail) requestData.buyerEmail = buyerEmail;
      if (buyerAddress) requestData.buyerAddress = buyerAddress;

      // Thêm expiredAt nếu có (optional)
      if (paymentData.expiredAt) {
        requestData.expiredAt = paymentData.expiredAt;
      }

      // Create signature theo format PayOS yêu cầu
      const signature = this.createSignature({
        amount: requestData.amount,
        cancelUrl: requestData.cancelUrl,
        description: requestData.description,
        orderCode: requestData.orderCode,
        returnUrl: requestData.returnUrl
      });
      requestData.signature = signature;

      // Call PayOS API
      const response = await axios.post(
        `${this.baseUrl}/v2/payment-requests`,
        requestData,
        {
          headers: {
            'Content-Type': 'application/json',
            'x-client-id': this.clientId,
            'x-api-key': this.apiKey
          }
        }
      );

      if (response.data && response.data.code === '00') {
        return {
          success: true,
          data: {
            bin: response.data.data.bin,
            accountNumber: response.data.data.accountNumber,
            accountName: response.data.data.accountName,
            amount: response.data.data.amount,
            description: response.data.data.description,
            orderCode: response.data.data.orderCode,
            qrCode: response.data.data.qrCode,
            checkoutUrl: response.data.data.checkoutUrl
          }
        };
      } else {
        throw new Error(response.data?.desc || 'Lỗi khi tạo payment link');
      }
    } catch (error) {
      console.error('PayOS createPaymentLink error:', error.response?.data || error.message);
      throw new Error(error.response?.data?.desc || error.message || 'Lỗi khi tạo payment link PayOS');
    }
  }

  /**
   * Lấy thông tin payment link
   * @param {Number} orderCode - Mã đơn hàng
   * @returns {Promise<Object>} Payment link info
   */
  async getPaymentLinkInfo(orderCode) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/v2/payment-requests/${orderCode}`,
        {
          headers: {
            'x-client-id': this.clientId,
            'x-api-key': this.apiKey
          }
        }
      );

      if (response.data && response.data.code === '00') {
        return {
          success: true,
          data: response.data.data
        };
      } else {
        throw new Error(response.data?.desc || 'Không tìm thấy payment link');
      }
    } catch (error) {
      console.error('PayOS getPaymentLinkInfo error:', error.response?.data || error.message);
      throw new Error(error.response?.data?.desc || error.message || 'Lỗi khi lấy thông tin payment link');
    }
  }

  /**
   * Hủy payment link
   * @param {Number} orderCode - Mã đơn hàng
   * @param {String} cancellationReason - Lý do hủy
   * @returns {Promise<Object>} Cancellation result
   */
  async cancelPaymentLink(orderCode, cancellationReason = 'Khách hàng hủy') {
    try {
      const requestData = {
        cancellationReason: cancellationReason
      };

      const checksum = this.createChecksum(requestData);
      requestData.signature = checksum;

      const response = await axios.delete(
        `${this.baseUrl}/v2/payment-requests/${orderCode}`,
        {
          headers: {
            'Content-Type': 'application/json',
            'x-client-id': this.clientId,
            'x-api-key': this.apiKey
          },
          data: requestData
        }
      );

      if (response.data && response.data.code === '00') {
        return {
          success: true,
          message: 'Đã hủy payment link thành công'
        };
      } else {
        throw new Error(response.data?.desc || 'Lỗi khi hủy payment link');
      }
    } catch (error) {
      console.error('PayOS cancelPaymentLink error:', error.response?.data || error.message);
      throw new Error(error.response?.data?.desc || error.message || 'Lỗi khi hủy payment link');
    }
  }

  /**
   * Xác thực webhook từ PayOS
   * PayOS webhook signature được tạo từ data object, sắp xếp theo alphabet và tạo HMAC SHA256
   * @param {Object} webhookData - Dữ liệu webhook từ PayOS
   * @param {String} receivedSignature - Signature nhận được từ PayOS (có thể trong body hoặc header)
   * @returns {Boolean} True nếu hợp lệ
   */
  verifyWebhook(webhookData, receivedSignature = null) {
    try {
      // PayOS có thể gửi signature trong body hoặc header
      const signature = receivedSignature || webhookData.signature;
      
      // PayOS webhook format: { code, desc, data, signature }
      // Signature được tạo từ data object
      const data = webhookData.data;
      
      if (!data || !signature) {
        console.error('Missing data or signature in webhook', {
          hasData: !!data,
          hasSignature: !!signature,
          webhookKeys: Object.keys(webhookData)
        });
        return false;
      }

      // Tạo signature từ data object theo format PayOS yêu cầu
      // Sắp xếp các key theo alphabet và tạo chuỗi key=value&key=value
      // Chỉ lấy các field trong data object, không bao gồm signature
      const sortedKeys = Object.keys(data).sort();
      const dataString = sortedKeys
        .map(key => {
          const value = data[key];
          // Xử lý các kiểu dữ liệu khác nhau
          if (value === null || value === undefined) {
            return `${key}=`;
          }
          // Chuyển đổi giá trị thành string
          const stringValue = typeof value === 'object' ? JSON.stringify(value) : String(value);
          return `${key}=${stringValue}`;
        })
        .join('&');

      // Tạo HMAC SHA256 signature
      const hmac = crypto.createHmac('sha256', this.checksumKey);
      hmac.update(dataString);
      const calculatedSignature = hmac.digest('hex');

      // So sánh signature (case-insensitive để an toàn)
      const isValid = calculatedSignature.toLowerCase() === signature.toLowerCase();
      
      if (!isValid) {
        console.error('Signature mismatch:', {
          received: signature,
          calculated: calculatedSignature,
          dataString: dataString,
          data: JSON.stringify(data, null, 2),
          sortedKeys: sortedKeys,
          checksumKeyLength: this.checksumKey ? this.checksumKey.length : 0
        });
        
        // Thử verify với format khác nếu có rawBodyString
        if (rawBodyString) {
          console.log('Attempting alternative verification with raw body...');
        }
      } else {
        console.log('Webhook signature verified successfully');
      }

      return isValid;
    } catch (error) {
      console.error('PayOS verifyWebhook error:', error.message, error.stack);
      return false;
    }
  }
}

module.exports = new PayOSService();

