const express = require('express');
const router = express.Router();
const {
  getChatHistory,
  getConversations,
  markAllAsRead,
  deleteConversation,
  searchMessages,
  getOnlineUsers,
  sendSystemMessage
} = require('../controllers/chatController');
const { protect, authorize } = require('../middleware/auth');

// Tất cả routes đều yêu cầu authentication
router.use(protect);

// Public routes (cho tất cả users đã đăng nhập)
router.get('/messages/:userId', getChatHistory); // Lấy tin nhắn với user cụ thể
router.put('/read-all', markAllAsRead);
router.delete('/conversation/:userId', deleteConversation);
router.get('/search', searchMessages);
router.get('/online-users', getOnlineUsers);

// Admin only routes
router.get('/conversations', authorize('admin'), getConversations); // Danh sách users đã chat
router.post('/system-message', authorize('admin'), sendSystemMessage);

module.exports = router;
