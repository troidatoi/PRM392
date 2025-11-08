const express = require('express');
const { body } = require('express-validator');
const { listUsers, getUser, updateUser, deleteUser, getTotalUsers } = require('../controllers/userController');
const { protect, authorize } = require('../middleware/auth');

const router = express.Router();

// Admin or staff only
router.use(protect);
router.use(authorize('admin', 'staff'));

router.get('/count/total', getTotalUsers);
router.get('/', listUsers);
router.get('/:id', getUser);
router.put(
  '/:id',
  [
    body('email').optional().isEmail().withMessage('Email không hợp lệ'),
    body('role').optional().isIn(['customer', 'admin', 'staff']).withMessage('Role không hợp lệ'),
    body('isActive').optional().isBoolean().withMessage('isActive phải là boolean')
  ],
  updateUser
);
router.delete('/:id', deleteUser);

module.exports = router;

