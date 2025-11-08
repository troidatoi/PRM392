const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const dotenv = require('dotenv');
const passport = require('passport');
const connectDB = require('./config/database');
const errorHandler = require('./middleware/errorHandler');
const setupChatSocket = require('./socket/chatSocket');
const cleanupPendingPayments = require('./jobs/paymentCleanupJob');

// Load env vars
dotenv.config();

// Connect to database
connectDB();

const app = express();
const server = http.createServer(app);

// Setup Socket.IO
const io = new Server(server, {
  cors: {
    origin: process.env.NODE_ENV === 'production' 
      ? ['https://yourdomain.com'] 
      : ['http://localhost:3000', 'http://localhost:3001', 'http://10.0.2.2:5000'],
    credentials: true,
    methods: ['GET', 'POST']
  },
  transports: ['websocket', 'polling']
});

// Setup chat socket handlers
setupChatSocket(io);

// Make io accessible to routes
app.set('io', io);

// Passport config
app.use(passport.initialize());
require('./config/passport')(passport);

// Body parser
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Enable CORS
app.use(cors({
  origin: process.env.NODE_ENV === 'production' 
    ? ['https://yourdomain.com'] 
    : true, // Allow all origins in development (including Android emulator)
  credentials: true
}));

// Serve static files from public directory
app.use(express.static('public'));

// Routes
const bikeRoutes = require('./routes/bikeRoutes');
app.use('/api/bikes', bikeRoutes);
console.log('✅ Bike routes registered: POST /api/bikes/upload available');
app.use('/api/auth', require('./routes/authRoutes'));
app.use('/api/users', require('./routes/userRoutes'));
app.use('/api/locations', require('./routes/storeRoutes')); // Store/Location routes
app.use('/api/stores', require('./routes/storeRoutes')); // Alias for backward compatibility
app.use('/api/cart', require('./routes/cartRoutes')); // Cart routes
app.use('/api/orders', require('./routes/orderRoutes')); // Order routes
app.use('/api/inventory', require('./routes/inventoryRoutes')); // Inventory routes
app.use('/api/chat', require('./routes/chatRoutes')); // Chat routes
app.use('/api/shipping-rates', require('./routes/shippingRateRoutes')); // Shipping rates routes
app.use('/api/payos', require('./routes/payosRoutes')); // PayOS payment routes
// Alias webhook route để phù hợp với BACKEND_WEBHOOK_URL trong .env
app.use('/api/payments', require('./routes/payosRoutes')); // PayOS payment routes (alias)

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    message: 'Server is running',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV
  });
});

// Reset password page route
app.get('/reset-password/:token', (req, res) => {
  res.sendFile(__dirname + '/public/reset-password.html');
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'Route not found'
  });
});

// Error handler
app.use(errorHandler);

const PORT = process.env.PORT || 5000;

server.listen(PORT, () => {
  console.log(`Server running in ${process.env.NODE_ENV} mode on port ${PORT}`);
  console.log(`WebSocket server is ready`);
  
  // Start payment cleanup cron job
  // Chạy mỗi giờ để check và cleanup payment pending sau 24h
  const CLEANUP_INTERVAL = 60 * 60 * 1000; // 1 giờ = 60 phút * 60 giây * 1000ms
  
  // Chạy ngay lần đầu sau khi server start (đợi 5 phút để DB sẵn sàng)
  setTimeout(() => {
    console.log('[Payment Cleanup] Bắt đầu cleanup job lần đầu...');
    cleanupPendingPayments();
  }, 5 * 60 * 1000); // 5 phút
  
  // Sau đó chạy định kỳ mỗi giờ
  setInterval(() => {
    console.log('[Payment Cleanup] Chạy cleanup job định kỳ...');
    cleanupPendingPayments();
  }, CLEANUP_INTERVAL);
  
  console.log('[Payment Cleanup] Cron job đã được khởi động - chạy mỗi giờ');
});

// Handle unhandled promise rejections
process.on('unhandledRejection', (err, promise) => {
  console.log(`Error: ${err.message}`);
  // Close server & exit process
  server.close(() => {
    process.exit(1);
  });
});

// Handle uncaught exceptions
process.on('uncaughtException', (err) => {
  console.log(`Error: ${err.message}`);
  process.exit(1);
});


