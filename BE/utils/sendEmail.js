const nodemailer = require('nodemailer');

const sendEmail = async (options) => {
  try {
    // Validate required options
    if (!options.email || !options.subject || !options.message) {
      throw new Error('Missing required email options: email, subject, message');
    }

    // Validate environment variables
    const requiredEnvVars = ['EMAIL_HOST', 'EMAIL_PORT', 'EMAIL_USER', 'EMAIL_PASS'];
    const missingEnvVars = requiredEnvVars.filter(envVar => !process.env[envVar]);
    
    if (missingEnvVars.length > 0) {
      throw new Error(`Missing required environment variables: ${missingEnvVars.join(', ')}`);
    }

    // Create transporter
    // Nếu sử dụng Gmail, cần dùng App Password hoặc OAuth2
    // Hướng dẫn tạo App Password: https://support.google.com/accounts/answer/185833
    const transporterConfig = {
      host: process.env.EMAIL_HOST,
      port: parseInt(process.env.EMAIL_PORT),
      secure: process.env.EMAIL_PORT === '465', // true for 465, false for other ports
      auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
      },
      // Cấu hình bổ sung cho production
      pool: true, // Sử dụng connection pool
      maxConnections: 5,
      maxMessages: 100,
      rateDelta: 1000, // Giới hạn rate
      rateLimit: 5,
      // Timeout settings
      connectionTimeout: 60000, // 60s
      greetingTimeout: 30000, // 30s
      socketTimeout: 60000 // 60s
    };

    // Nếu là Gmail, thêm tls config
    if (process.env.EMAIL_HOST && process.env.EMAIL_HOST.includes('gmail.com')) {
      transporterConfig.tls = {
        rejectUnauthorized: false
      };
      // Gmail yêu cầu port 587 với secure: false hoặc port 465 với secure: true
      if (parseInt(process.env.EMAIL_PORT) === 587) {
        transporterConfig.secure = false;
        transporterConfig.requireTLS = true;
      }
    }

    const transporter = nodemailer.createTransport(transporterConfig);

    // Verify transporter configuration
    await transporter.verify();

    // Mail options with proper formatting
    const mailOptions = {
      from: {
        name: process.env.FROM_NAME || 'Electric Bike Shop',
        address: process.env.EMAIL_USER
      },
      to: options.email,
      subject: options.subject,
      text: options.message,
      html: options.html || `<p>${options.message.replace(/\n/g, '<br>')}</p>`,
      // Anti-spam headers
      headers: {
        'X-Priority': '3',
        'X-MSMail-Priority': 'Normal',
        'Importance': 'Normal'
      }
    };

    // Send email with retry logic
    let lastError;
    const maxRetries = 3;
    
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        const info = await transporter.sendMail(mailOptions);
        
        console.log(`Email sent successfully on attempt ${attempt}:`, {
          messageId: info.messageId,
          to: options.email,
          subject: options.subject,
          timestamp: new Date().toISOString()
        });
        
        return {
          success: true,
          messageId: info.messageId,
          attempt
        };
      } catch (error) {
        lastError = error;
        console.error(`Email send attempt ${attempt} failed:`, {
          error: error.message,
          code: error.code,
          to: options.email,
          subject: options.subject
        });
        
        // Nếu là lỗi Gmail authentication, hiển thị hướng dẫn
        if (error.code === 'EAUTH' && process.env.EMAIL_HOST && process.env.EMAIL_HOST.includes('gmail.com')) {
          console.error('\n⚠️  LỖI XÁC THỰC GMAIL:');
          console.error('Gmail yêu cầu sử dụng App Password thay vì mật khẩu thông thường.');
          console.error('Hướng dẫn tạo App Password:');
          console.error('1. Vào https://myaccount.google.com/security');
          console.error('2. Bật 2-Step Verification (nếu chưa bật)');
          console.error('3. Vào "App passwords" và tạo mật khẩu mới');
          console.error('4. Sử dụng mật khẩu đó trong EMAIL_PASS (16 ký tự, không có khoảng trắng)');
          console.error('5. Đảm bảo EMAIL_PORT=587 và EMAIL_HOST=smtp.gmail.com\n');
        }
        
        // Wait before retry (exponential backoff)
        if (attempt < maxRetries) {
          const delay = Math.pow(2, attempt) * 1000; // 2s, 4s, 8s
          await new Promise(resolve => setTimeout(resolve, delay));
        }
      }
    }
    
    // If all retries failed
    throw new Error(`Failed to send email after ${maxRetries} attempts: ${lastError.message}`);
    
  } catch (error) {
    console.error('Email service error:', {
      error: error.message,
      stack: error.stack,
      to: options?.email,
      subject: options?.subject,
      timestamp: new Date().toISOString()
    });
    
    throw error;
  }
};

module.exports = sendEmail;
