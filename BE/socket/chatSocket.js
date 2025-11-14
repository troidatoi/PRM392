const ChatMessage = require('../models/ChatMessage');
const User = require('../models/User');
const jwt = require('jsonwebtoken');

// Store active users
const activeUsers = new Map();

const setupChatSocket = (io) => {
  // Middleware xác thực socket
  io.use(async (socket, next) => {
    try {
      const token = socket.handshake.auth.token;
      
      if (!token) {
        return next(new Error('Authentication error'));
      }

      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      const user = await User.findById(decoded.id).select('-password');
      
      if (!user) {
        return next(new Error('User not found'));
      }

      socket.userId = user._id.toString();
      socket.user = user;
      next();
    } catch (error) {
      next(new Error('Authentication error'));
    }
  });

  io.on('connection', (socket) => {
    console.log(`User connected: ${socket.userId}`);
    
    // Thêm user vào danh sách active
    activeUsers.set(socket.userId, {
      socketId: socket.id,
      user: socket.user
    });

    // Gửi danh sách users online cho tất cả clients
    io.emit('users:online', Array.from(activeUsers.keys()));

    // User join room (chat với admin hoặc user khác)
    socket.on('chat:join', async (data) => {
      try {
        const { roomId } = data;
        socket.join(roomId);
        console.log(`User ${socket.userId} joined room ${roomId}`);
        
        // Gửi thông báo join room
        socket.to(roomId).emit('user:joined', {
          userId: socket.userId,
          username: socket.user.username,
          timestamp: new Date()
        });
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    // Gửi tin nhắn
    socket.on('message:send', async (data) => {
      try {
        const { roomId, message, messageType = 'text', attachments = [], metadata = {} } = data;

        // Xác định isFromAdmin dựa trên role của user
        const isFromAdmin = socket.user.role === 'admin' || socket.user.role === 'staff';
        
        // Tạo message mới trong database
        const newMessage = await ChatMessage.create({
          user: socket.userId,
          roomId: roomId, // Lưu roomId để có thể query sau
          message,
          messageType,
          attachments,
          metadata,
          sentAt: new Date(),
          isFromAdmin: isFromAdmin // Set isFromAdmin khi tạo
        });

        // Populate thông tin user
        await newMessage.populate('user', 'username email avatar');
        
        // Gửi message tới room
        io.to(roomId).emit('message:received', {
          _id: newMessage._id,
          user: newMessage.user,
          message: newMessage.message,
          messageType: newMessage.messageType,
          attachments: newMessage.attachments,
          metadata: newMessage.metadata,
          sentAt: newMessage.sentAt.getTime(), // Convert Date to timestamp
          isRead: newMessage.isRead,
          isFromAdmin: isFromAdmin // Thêm field isFromAdmin
        });

        // Emit event để cập nhật danh sách chat của admin
        // Chỉ emit cho admins, không phải cho user gửi tin nhắn
        const admins = Array.from(activeUsers.entries())
          .filter(([userId, data]) => data.user.role === 'admin' || data.user.role === 'staff')
          .filter(([userId]) => userId !== socket.userId); // Không gửi cho chính mình
        
        admins.forEach(([adminId, data]) => {
          io.to(data.socketId).emit('chat-list:update', {
            userId: socket.userId,
            user: newMessage.user,
            lastMessage: {
              message: newMessage.message,
              sentAt: newMessage.sentAt.getTime(),
              isFromAdmin: isFromAdmin
            }
          });
        });

        console.log(`Message sent to room ${roomId}`);
      } catch (error) {
        console.error('Error sending message:', error);
        socket.emit('error', { message: error.message });
      }
    });

    // Đánh dấu tin nhắn đã đọc
    socket.on('message:read', async (data) => {
      try {
        const { messageId, roomId } = data;

        const message = await ChatMessage.findByIdAndUpdate(
          messageId,
          { 
            isRead: true,
            readAt: new Date()
          },
          { new: true }
        );

        if (message) {
          io.to(roomId).emit('message:read-status', {
            messageId,
            isRead: true,
            readAt: message.readAt
          });
        }
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    // User đang typing
    socket.on('typing:start', (data) => {
      const { roomId } = data;
      socket.to(roomId).emit('user:typing', {
        userId: socket.userId,
        username: socket.user.username
      });
    });

    // User ngừng typing
    socket.on('typing:stop', (data) => {
      const { roomId } = data;
      socket.to(roomId).emit('user:stop-typing', {
        userId: socket.userId
      });
    });

    // Chỉnh sửa tin nhắn
    socket.on('message:edit', async (data) => {
      try {
        const { messageId, newMessage, roomId } = data;

        const message = await ChatMessage.findOneAndUpdate(
          { _id: messageId, user: socket.userId },
          { 
            message: newMessage,
            isEdited: true,
            editedAt: new Date()
          },
          { new: true }
        ).populate('user', 'username email avatar');

        if (message) {
          io.to(roomId).emit('message:edited', {
            messageId,
            message: message.message,
            isEdited: true,
            editedAt: message.editedAt
          });
        }
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    // Xóa tin nhắn
    socket.on('message:delete', async (data) => {
      try {
        const { messageId, roomId } = data;

        const message = await ChatMessage.findOneAndUpdate(
          { _id: messageId, user: socket.userId },
          { 
            isDeleted: true,
            deletedAt: new Date()
          },
          { new: true }
        );

        if (message) {
          io.to(roomId).emit('message:deleted', {
            messageId,
            isDeleted: true
          });
        }
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    // Reaction tin nhắn
    socket.on('message:react', async (data) => {
      try {
        const { messageId, emoji, roomId } = data;

        const message = await ChatMessage.findById(messageId);
        
        if (message) {
          // Kiểm tra user đã react chưa
          const existingReaction = message.reactions.find(
            r => r.user.toString() === socket.userId
          );

          if (existingReaction) {
            // Cập nhật reaction
            existingReaction.emoji = emoji;
          } else {
            // Thêm reaction mới
            message.reactions.push({
              user: socket.userId,
              emoji,
              createdAt: new Date()
            });
          }

          await message.save();

          io.to(roomId).emit('message:reaction', {
            messageId,
            userId: socket.userId,
            emoji,
            reactions: message.reactions
          });
        }
      } catch (error) {
        socket.emit('error', { message: error.message });
      }
    });

    // Leave room
    socket.on('chat:leave', (data) => {
      const { roomId } = data;
      socket.leave(roomId);
      socket.to(roomId).emit('user:left', {
        userId: socket.userId,
        username: socket.user.username,
        timestamp: new Date()
      });
    });

    // Disconnect
    socket.on('disconnect', () => {
      console.log(`User disconnected: ${socket.userId}`);
      activeUsers.delete(socket.userId);
      io.emit('users:online', Array.from(activeUsers.keys()));
    });
  });
};

module.exports = setupChatSocket;
