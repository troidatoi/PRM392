const axios = require('axios');
const crypto = require('crypto');

/**
 * PayOS Service - Xử lý tích hợp với PayOS API v2
 */
class PayOSService {
  constructor() {
    this.clientId = process.env.PAYOS_CLIENT_ID;
    this.apiKey = process.env.PAYOS_API_KEY;
    this.checksumKey = process.env.PAYOS_CHECKSUM_KEY;
    
    // PayOS API URL - PayOS đã chuyển sang api-merchant.payos.vn
    // Kiểm tra và sử dụng URL từ env, nhưng nếu là api.payos.vn (URL cũ) thì thay thế
    let envBaseUrl = process.env.PAYOS_BASE_URL;
    
    if (envBaseUrl) {
      // Nếu URL cũ (api.payos.vn) thì tự động chuyển sang URL mới
      if (envBaseUrl.includes('api.payos.vn') && !envBaseUrl.includes('api-merchant.payos.vn')) {
        console.warn('⚠️  PayOS URL đã thay đổi! Tự động chuyển từ api.payos.vn sang api-merchant.payos.vn');
        envBaseUrl = envBaseUrl.replace('api.payos.vn', 'api-merchant.payos.vn');
      }
      this.baseUrl = envBaseUrl.endsWith('/v2') ? envBaseUrl : `${envBaseUrl}/v2`;
    } else {
      // URL mặc định mới
      this.baseUrl = 'https://api-merchant.payos.vn/v2';
    }
    
    if (!this.clientId || !this.apiKey || !this.checksumKey) {
      throw new Error('PayOS credentials are missing in environment variables');
    }
    
    console.log('PayOS Service initialized with baseUrl:', this.baseUrl);
  }

  /**
   * Tạo checksum để xác thực request (theo format PayOS)
   * PayOS yêu cầu format: key=value&key2=value2 (sorted alphabetically)
   * Chỉ dùng các field: amount, cancelUrl, description, orderCode, returnUrl
   */
  createPayOSSignature(data) {
    // Chỉ lấy các field cần thiết cho signature
    const signatureData = {
      amount: data.amount,
      cancelUrl: data.cancelUrl,
      description: data.description,
      orderCode: data.orderCode,
      returnUrl: data.returnUrl
    };

    // Sort theo alphabet và tạo string format: key=value&key2=value2
    const sortedKeys = Object.keys(signatureData).sort();
    const dataString = sortedKeys
      .map(key => `${key}=${signatureData[key]}`)
      .join('&');

    // Tạo HMAC SHA256
    return crypto
      .createHmac('sha256', this.checksumKey)
      .update(dataString)
      .digest('hex');
  }

  /**
   * Tạo checksum để xác thực request (legacy - dùng cho webhook verification)
   */
  createChecksum(data) {
    const dataString = JSON.stringify(data);
    return crypto
      .createHmac('sha256', this.checksumKey)
      .update(dataString)
      .digest('hex');
  }

  /**
   * Verify checksum từ webhook
   */
  verifyChecksum(data, checksum) {
    const calculatedChecksum = this.createChecksum(data);
    
    // Debug logging
    console.log('=== Signature Verification ===');
    console.log('Data to verify:', JSON.stringify(data, null, 2));
    console.log('Calculated checksum:', calculatedChecksum);
    console.log('Expected checksum:', checksum);
    console.log('Match:', calculatedChecksum === checksum);
    
    return calculatedChecksum === checksum;
  }

  /**
   * Tạo payment link
   * @param {Object} paymentData - Thông tin thanh toán
   * @param {Number} paymentData.amount - Số tiền (VNĐ)
   * @param {String} paymentData.orderCode - Mã đơn hàng
   * @param {String} paymentData.description - Mô tả đơn hàng
   * @param {String} paymentData.cancelUrl - URL khi hủy thanh toán
   * @param {String} paymentData.returnUrl - URL khi thanh toán thành công
   * @param {Object} paymentData.items - Danh sách sản phẩm (optional)
   * @param {Object} paymentData.buyer - Thông tin người mua (optional)
   */
  async createPaymentLink(paymentData) {
    try {
      const {
        amount,
        orderCode,
        description,
        cancelUrl,
        returnUrl,
        items = [],
        buyer = {}
      } = paymentData;

      // Validate required fields
      if (!amount || !orderCode || !description || !cancelUrl || !returnUrl) {
        throw new Error('Missing required payment fields');
      }

      // Validate amount (minimum 1000 VND)
      if (amount < 1000) {
        throw new Error('Amount must be at least 1000 VND');
      }

      // Prepare request data
      // Đảm bảo tất cả fields đúng data type theo PayOS API docs
      // PayOS yêu cầu description tối đa 25 ký tự
      let paymentDescription = String(description);
      if (paymentDescription.length > 25) {
        paymentDescription = paymentDescription.substring(0, 25);
      }
      
      const requestData = {
        orderCode: Number(orderCode), // integer (required)
        amount: Number(amount), // integer (required, >= 1000)
        description: paymentDescription, // string (required, max 25 characters)
        cancelUrl: String(cancelUrl), // URI (required)
        returnUrl: String(returnUrl), // URI (required)
        items: items.length > 0 ? items.map(item => ({
          name: String(item.name || paymentDescription), // string
          quantity: Number(item.quantity || 1), // integer
          price: Number(item.price || amount) // integer
        })) : [
          {
            name: paymentDescription,
            quantity: 1,
            price: Number(amount)
          }
        ]
      };

      // Chỉ thêm buyer info nếu có
      if (buyer.name) requestData.buyerName = buyer.name;
      if (buyer.email) requestData.buyerEmail = buyer.email;
      if (buyer.phone) {
        // Format số điện thoại: loại bỏ dấu + và khoảng trắng
        requestData.buyerPhone = buyer.phone.replace(/[\s\+]/g, '');
      }
      if (buyer.address) requestData.buyerAddress = buyer.address;
      // Không gửi expiredAt nếu null hoặc undefined
      if (buyer.expiredAt) requestData.expiredAt = buyer.expiredAt;

      // Tạo signature theo format PayOS (key=value&key2=value2 sorted alphabetically)
      // CHỈ dùng 5 field: amount, cancelUrl, description, orderCode, returnUrl
      const signature = this.createPayOSSignature(requestData);
      
      // Thêm signature vào body (theo PayOS docs, signature là field trong body)
      requestData.signature = signature;

      // Log request data để debug
      console.log('=== PayOS Create Payment Link Request ===');
      console.log('Request URL:', `${this.baseUrl}/payment-requests`);
      console.log('Signature Data (for checksum):', {
        amount: requestData.amount,
        cancelUrl: requestData.cancelUrl,
        description: requestData.description,
        orderCode: requestData.orderCode,
        returnUrl: requestData.returnUrl
      });
      console.log('Full Request Data:', JSON.stringify(requestData, null, 2));
      console.log('Signature:', signature);

      // Make API request với timeout
      // PayOS yêu cầu signature trong BODY, không phải header
      const response = await axios.post(
        `${this.baseUrl}/payment-requests`,
        requestData,
        {
          headers: {
            'Content-Type': 'application/json',
            'x-client-id': this.clientId,
            'x-api-key': this.apiKey
          },
          timeout: 30000 // 30 seconds timeout
        }
      );

      // Log response để debug
      console.log('=== PayOS Response ===');
      console.log('Response Status:', response.status);
      console.log('Response Data:', JSON.stringify(response.data, null, 2));

      // PayOS trả về code có thể là số 0 hoặc string "00"
      const isSuccess = response.data && (response.data.code === 0 || response.data.code === "00");
      
      if (isSuccess) {
        return {
          success: true,
          data: {
            bin: response.data.data.bin,
            checkoutUrl: response.data.data.checkoutUrl,
            accountNumber: response.data.data.accountNumber,
            accountName: response.data.data.accountName,
            amount: response.data.data.amount,
            description: response.data.data.description,
            orderCode: response.data.data.orderCode,
            qrCode: response.data.data.qrCode
          }
        };
      } else {
        // Log chi tiết lỗi từ PayOS
        console.error('PayOS API Error Response:', {
          code: response.data?.code,
          desc: response.data?.desc,
          data: response.data?.data
        });
        throw new Error(response.data?.desc || 'Failed to create payment link');
      }
    } catch (error) {
      console.error('PayOS createPaymentLink error:', error.message);
      console.error('Error details:', {
        code: error.code,
        hostname: error.hostname,
        baseUrl: this.baseUrl,
        response: error.response?.data,
        status: error.response?.status,
        statusText: error.response?.statusText
      });
      
      // Log chi tiết response từ PayOS nếu có
      if (error.response?.data) {
        console.error('PayOS Error Response:', JSON.stringify(error.response.data, null, 2));
      }
      
      // Xử lý lỗi DNS/Network
      if (error.code === 'ENOTFOUND' || error.code === 'ECONNREFUSED') {
        return {
          success: false,
          error: `Không thể kết nối đến PayOS API. Vui lòng kiểm tra kết nối mạng và URL: ${this.baseUrl}`
        };
      }
      
      // Trả về error message chi tiết từ PayOS
      const errorMessage = error.response?.data?.desc || error.message || 'Failed to create payment link';
      return {
        success: false,
        error: errorMessage
      };
    }
  }

  /**
   * Lấy thông tin thanh toán
   * @param {Number} orderCode - Mã đơn hàng
   */
  async getPaymentInfo(orderCode) {
    try {
      const response = await axios.get(
        `${this.baseUrl}/payment-requests/${orderCode}`,
        {
          headers: {
            'x-client-id': this.clientId,
            'x-api-key': this.apiKey
          },
          timeout: 30000
        }
      );

      // PayOS trả về code có thể là số 0 hoặc string "00"
      const isSuccess = response.data && (response.data.code === 0 || response.data.code === "00");
      
      if (isSuccess) {
        return {
          success: true,
          data: response.data.data
        };
      } else {
        // Chỉ throw error nếu thực sự có lỗi (không phải "success")
        const errorDesc = response.data?.desc;
        if (errorDesc && errorDesc.toLowerCase() !== 'success') {
          throw new Error(errorDesc);
        } else {
          // Nếu desc là "success" nhưng code không match, có thể do format khác
          // Trả về data nếu có
          if (response.data?.data) {
            return {
              success: true,
              data: response.data.data
            };
          }
          throw new Error('Invalid response format from PayOS');
        }
      }
    } catch (error) {
      // Chỉ log error nếu không phải "success"
      const errorMessage = error.response?.data?.desc || error.message || 'Failed to get payment info';
      if (errorMessage.toLowerCase() !== 'success') {
        console.error('PayOS getPaymentInfo error:', errorMessage);
      }
      return {
        success: false,
        error: errorMessage
      };
    }
  }

  /**
   * Hủy payment link
   * @param {Number} orderCode - Mã đơn hàng
   * @param {String} cancellationReason - Lý do hủy
   */
  async cancelPaymentLink(orderCode, cancellationReason = '') {
    try {
      const requestData = {
        cancellationReason: cancellationReason || 'Hủy thanh toán'
      };

      const response = await axios.delete(
        `${this.baseUrl}/payment-requests/${orderCode}`,
        {
          data: requestData,
          headers: {
            'Content-Type': 'application/json',
            'x-client-id': this.clientId,
            'x-api-key': this.apiKey
          },
          timeout: 30000
        }
      );

      if (response.data && response.data.code === 0) {
        return {
          success: true,
          data: response.data.data
        };
      } else {
        throw new Error(response.data?.desc || 'Failed to cancel payment link');
      }
    } catch (error) {
      console.error('PayOS cancelPaymentLink error:', error.response?.data || error.message);
      return {
        success: false,
        error: error.response?.data?.desc || error.message || 'Failed to cancel payment link'
      };
    }
  }

  /**
   * Xác minh webhook từ PayOS
   * @param {Object} webhookData - Dữ liệu webhook
   * @param {String} webhookChecksum - Checksum từ header
   */
  verifyWebhook(webhookData, webhookChecksum) {
    try {
      return this.verifyChecksum(webhookData, webhookChecksum);
    } catch (error) {
      console.error('PayOS verifyWebhook error:', error.message);
      return false;
    }
  }

  /**
   * Xác minh thanh toán thành công
   * @param {Object} webhookData - Dữ liệu webhook
   */
  isPaymentSuccess(webhookData) {
    return webhookData && webhookData.status === 'PAID';
  }
}

module.exports = new PayOSService();

