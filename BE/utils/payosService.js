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
   * Tạo checksum để xác thực request
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
      const requestData = {
        orderCode: Number(orderCode),
        amount: Number(amount),
        description: description,
        cancelUrl: cancelUrl,
        returnUrl: returnUrl,
        items: items.length > 0 ? items : [
          {
            name: description,
            quantity: 1,
            price: amount
          }
        ],
        buyerName: buyer.name || '',
        buyerEmail: buyer.email || '',
        buyerPhone: buyer.phone || '',
        buyerAddress: buyer.address || '',
        expiredAt: buyer.expiredAt || null
      };

      // Create checksum
      const checksum = this.createChecksum(requestData);

      // Make API request với timeout
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

      if (response.data && response.data.code === 0) {
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
        throw new Error(response.data?.desc || 'Failed to create payment link');
      }
    } catch (error) {
      console.error('PayOS createPaymentLink error:', error.message);
      console.error('Error details:', {
        code: error.code,
        hostname: error.hostname,
        baseUrl: this.baseUrl,
        response: error.response?.data
      });
      
      // Xử lý lỗi DNS/Network
      if (error.code === 'ENOTFOUND' || error.code === 'ECONNREFUSED') {
        return {
          success: false,
          error: `Không thể kết nối đến PayOS API. Vui lòng kiểm tra kết nối mạng và URL: ${this.baseUrl}`
        };
      }
      
      return {
        success: false,
        error: error.response?.data?.desc || error.message || 'Failed to create payment link'
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

      if (response.data && response.data.code === 0) {
        return {
          success: true,
          data: response.data.data
        };
      } else {
        throw new Error(response.data?.desc || 'Failed to get payment info');
      }
    } catch (error) {
      console.error('PayOS getPaymentInfo error:', error.response?.data || error.message);
      return {
        success: false,
        error: error.response?.data?.desc || error.message || 'Failed to get payment info'
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

