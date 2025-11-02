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
    const transporter = nodemailer.createTransport({
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
    });

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
