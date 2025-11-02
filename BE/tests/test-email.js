/**
 * Script test email service riêng
 * Chạy: node test-email.js
 */

const dotenv = require('dotenv');
const sendEmail = require('../utils/sendEmail');

// Load environment variables
dotenv.config();

async function testEmail() {
  console.log('🧪 Testing email service...\n');
  
  // Check environment variables
  console.log('📋 Email configuration:');
  console.log('EMAIL_HOST:', process.env.EMAIL_HOST);
  console.log('EMAIL_PORT:', process.env.EMAIL_PORT);
  console.log('EMAIL_USER:', process.env.EMAIL_USER);
  console.log('EMAIL_PASS:', process.env.EMAIL_PASS ? '***configured***' : 'NOT SET');
  console.log('');
  
  try {
    const result = await sendEmail({
      email: process.env.EMAIL_USER, // Gửi về chính email của mình để test
      subject: 'Test Email Service - Electric Bike Backend',
      message: `
Đây là email test từ hệ thống Electric Bike Backend.

Nếu bạn nhận được email này, có nghĩa là email service đã hoạt động tốt!

Thời gian test: ${new Date().toLocaleString('vi-VN')}
      `.trim(),
      html: `
        <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
          <h2 style="color: #28a745; text-align: center;">✅ Email Service Test</h2>
          <p>Đây là email test từ hệ thống <strong>Electric Bike Backend</strong>.</p>
          <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
            <p><strong>✅ Email service hoạt động tốt!</strong></p>
            <p>Thời gian test: ${new Date().toLocaleString('vi-VN')}</p>
          </div>
          <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
          <p style="color: #888; font-size: 12px; text-align: center;">
            Electric Bike Shop - Backend Email Service
          </p>
        </div>
      `
    });
    
    console.log('✅ Email sent successfully!');
    console.log('Message ID:', result.messageId);
    console.log('Attempt:', result.attempt);
    console.log('\n📧 Hãy kiểm tra hộp thư của bạn!');
    
  } catch (error) {
    console.error('❌ Email failed:', error.message);
    
    // Gợi ý fix lỗi
    console.log('\n🔧 Gợi ý fix lỗi:');
    
    if (error.message.includes('authentication')) {
      console.log('- Kiểm tra lại EMAIL_USER và EMAIL_PASS');
      console.log('- Tạo lại Gmail App Password: https://myaccount.google.com/apppasswords');
    }
    
    if (error.message.includes('connection')) {
      console.log('- Kiểm tra kết nối internet');
      console.log('- Kiểm tra EMAIL_HOST và EMAIL_PORT');
    }
    
    if (error.message.includes('Missing required')) {
      console.log('- Kiểm tra các biến môi trường trong file .env');
    }
  }
}

// Run test
if (require.main === module) {
  testEmail().catch(console.error);
}

module.exports = { testEmail };
