const ChatMessage = require('../models/ChatMessage');
const User = require('../models/User');

// @desc    Lấy lịch sử chat giữa user và admin
// @route   GET /api/chat/messages/:userId
// @access  Private
exports.getChatHistory = async (req, res, next) => {
  try {
    const { userId } = req.params;
    const { limit = 50, before } = req.query;

    // Tạo roomId theo format: "admin_userId"
    const roomId = `admin_${userId}`;
    
    const query = {
      roomId: roomId, // Lấy tất cả tin nhắn trong room này
      isDeleted: false
    };
    
    if (before) {
      query.sentAt = { $lt: new Date(before) };
    }

    // Lấy tin nhắn mới nhất trước (descending), sau đó reverse để hiển thị đúng thứ tự
    const messages = await ChatMessage.find(query)
      .populate('user', 'username email avatar')
      .sort({ sentAt: -1 }) // Descending order (newest first) - để lấy N tin nhắn mới nhất
      .limit(parseInt(limit));

    console.log(`Found ${messages.length} messages in room ${roomId}`);

    // Reverse array để hiển thị đúng thứ tự (oldest first) cho client
    messages.reverse();

    // Convert sentAt to timestamp for consistency with Socket.IO
    const formattedMessages = messages.map(msg => ({
      _id: msg._id,
      user: msg.user,
      message: msg.message,
      messageType: msg.messageType,
      attachments: msg.attachments,
      metadata: msg.metadata,
      sentAt: msg.sentAt.getTime(), // Convert Date to timestamp
      isRead: msg.isRead,
      isEdited: msg.isEdited,
      isDeleted: msg.isDeleted,
      isFromAdmin: msg.isFromAdmin || false // Thêm field isFromAdmin
    }));

    res.status(200).json({
      success: true,
      count: formattedMessages.length,
      data: formattedMessages
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Lấy danh sách users đã chat với admin
// @route   GET /api/chat/conversations
// @access  Private (Admin only)
exports.getConversations = async (req, res, next) => {
  try {
    console.log('GET /api/chat/conversations - Admin:', req.user.role);
    
    // Lấy danh sách unique rooms (conversations)
    const conversations = await ChatMessage.aggregate([
      {
        $match: {
          isDeleted: false,
          roomId: { $regex: '^admin_' } // Chỉ lấy các room của admin
        }
      },
      {
        $sort: { sentAt: -1 }
      },
      {
        // Group theo roomId để mỗi room là 1 conversation riêng biệt
        $group: {
          _id: '$roomId', // Group theo roomId thay vì user
          lastMessage: { $first: '$$ROOT' },
          unreadCount: {
            $sum: {
              $cond: [{ $eq: ['$isRead', false] }, 1, 0]
            }
          },
          totalMessages: { $sum: 1 }
        }
      },
      {
        $addFields: {
          // Extract userId from roomId (admin_userId)
          userId: { $substr: ['$_id', 6, -1] }
        }
      },
      {
        $addFields: {
          // Convert string to ObjectId
          userObjectId: { $toObjectId: '$userId' }
        }
      },
      {
        $lookup: {
          from: 'users',
          localField: 'userObjectId',
          foreignField: '_id',
          as: 'user'
        }
      },
      {
        $unwind: {
          path: '$user',
          preserveNullAndEmptyArrays: false // Chỉ lấy conversations có user hợp lệ
        }
      },
      {
        // Loại trừ admin và staff - chỉ lấy customers
        $match: {
          'user.role': { $nin: ['admin', 'staff'] }
        }
      },
      {
        $project: {
          'user.password': 0,
          'user.passwordHash': 0,
          'user.resetPasswordToken': 0,
          'user.resetPasswordExpire': 0,
          userId: 0,
          userObjectId: 0
        }
      },
      {
        $sort: { 'lastMessage.sentAt': -1 }
      }
    ]);

    console.log('Found conversations:', conversations.length);
    if (conversations.length > 0) {
      console.log('First conversation:', JSON.stringify(conversations[0], null, 2));
    }

    // Convert sentAt to timestamp in lastMessage
    const formattedConversations = conversations.map(conv => ({
      ...conv,
      lastMessage: {
        ...conv.lastMessage,
        sentAt: conv.lastMessage.sentAt ? new Date(conv.lastMessage.sentAt).getTime() : Date.now()
      }
    }));

    res.status(200).json({
      success: true,
      count: formattedConversations.length,
      data: formattedConversations
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Đánh dấu tất cả tin nhắn đã đọc
// @route   PUT /api/chat/read-all
// @access  Private
exports.markAllAsRead = async (req, res, next) => {
  try {
    const { senderId } = req.body;

    await ChatMessage.updateMany(
      {
        user: senderId,
        isRead: false
      },
      {
        isRead: true,
        readAt: new Date()
      }
    );

    res.status(200).json({
      success: true,
      message: 'Đã đánh dấu tất cả tin nhắn là đã đọc'
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Xóa conversation
// @route   DELETE /api/chat/conversation/:userId
// @access  Private
exports.deleteConversation = async (req, res, next) => {
  try {
    const { userId } = req.params;

    await ChatMessage.deleteMany({
      $or: [
        { user: req.user._id, 'metadata.recipientId': userId },
        { user: userId, 'metadata.recipientId': req.user._id }
      ]
    });

    res.status(200).json({
      success: true,
      message: 'Đã xóa cuộc hội thoại'
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Tìm kiếm tin nhắn
// @route   GET /api/chat/search
// @access  Private
exports.searchMessages = async (req, res, next) => {
  try {
    const { query, limit = 20 } = req.query;

    if (!query) {
      return res.status(400).json({
        success: false,
        message: 'Vui lòng cung cấp từ khóa tìm kiếm'
      });
    }

    const messages = await ChatMessage.find({
      message: { $regex: query, $options: 'i' },
      isDeleted: false
    })
      .populate('user', 'username email avatar')
      .sort({ sentAt: -1 })
      .limit(parseInt(limit));

    res.status(200).json({
      success: true,
      count: messages.length,
      data: messages
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Lấy danh sách users online
// @route   GET /api/chat/online-users
// @access  Private
exports.getOnlineUsers = async (req, res, next) => {
  try {
    const io = req.app.get('io');
    const sockets = await io.fetchSockets();
    const onlineUserIds = sockets.map(socket => socket.userId);

    const users = await User.find({
      _id: { $in: onlineUserIds }
    }).select('username email avatar role');

    res.status(200).json({
      success: true,
      count: users.length,
      data: users
    });
  } catch (error) {
    next(error);
  }
};

// @desc    Gửi tin nhắn hệ thống/thông báo
// @route   POST /api/chat/system-message
// @access  Private (Admin only)
exports.sendSystemMessage = async (req, res, next) => {
  try {
    const { message, messageType = 'system', targetUsers } = req.body;

    const io = req.app.get('io');

    const systemMessage = {
      user: null,
      message,
      messageType,
      sentAt: new Date(),
      isSystem: true
    };

    if (targetUsers && targetUsers.length > 0) {
      // Gửi cho users cụ thể
      targetUsers.forEach(userId => {
        io.to(userId).emit('message:system', systemMessage);
      });
    } else {
      // Broadcast cho tất cả
      io.emit('message:system', systemMessage);
    }

    res.status(200).json({
      success: true,
      message: 'Đã gửi tin nhắn hệ thống'
    });
  } catch (error) {
    next(error);
  }
};
