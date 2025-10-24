const Cart = require('../models/Cart');
const CartItem = require('../models/CartItem');
const Bike = require('../models/Bike');
const Store = require('../models/Store');
const Inventory = require('../models/Inventory');

// Create new cart
const createCart = async (req, res) => {
  try {
    const { userId } = req.body;
    
    // Check if user already has an active cart
    const existingCart = await Cart.findOne({ 
      user: userId, 
      status: 'active' 
    });
    
    if (existingCart) {
      return res.json({
        success: true,
        message: 'Giỏ hàng đã tồn tại',
        data: existingCart
      });
    }
    
    // Create new cart
    const cart = new Cart({
      user: userId,
      status: 'active',
      totalPrice: 0,
      isMultiStore: false
    });
    
    await cart.save();
    
    res.json({
      success: true,
      message: 'Tạo giỏ hàng thành công',
      data: cart
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Add item to cart
const addItemToCart = async (req, res) => {
  try {
    const { cartId, productId, storeId, quantity } = req.body;
    
    // Validate required fields
    if (!cartId || !productId || !storeId || !quantity) {
      return res.status(400).json({
        success: false,
        message: 'Thiếu thông tin bắt buộc'
      });
    }
    
    // Check if cart exists
    const cart = await Cart.findById(cartId);
    if (!cart) {
      return res.status(404).json({
        success: false,
        message: 'Giỏ hàng không tồn tại'
      });
    }
    
    // Check if product exists
    const product = await Bike.findById(productId);
    if (!product) {
      return res.status(404).json({
        success: false,
        message: 'Sản phẩm không tồn tại'
      });
    }
    
    // Check if store exists
    const store = await Store.findById(storeId);
    if (!store) {
      return res.status(404).json({
        success: false,
        message: 'Cửa hàng không tồn tại'
      });
    }
    
    // Check if item already exists in cart
    const existingItem = await CartItem.findOne({
      cart: cartId,
      product: productId,
      store: storeId
    });
    
    if (existingItem) {
      // Update quantity
      existingItem.quantity += quantity;
      await existingItem.save();
    } else {
      // Create new cart item
      const cartItem = new CartItem({
        cart: cartId,
        product: productId,
        store: storeId,
        quantity
      });
      
      await cartItem.save();
      
      // Add to cart
      cart.items.push(cartItem._id);
    }
    
    // Check if multi-store
    const cartItems = await CartItem.find({ cart: cartId });
    const uniqueStores = [...new Set(cartItems.map(item => item.store.toString()))];
    cart.isMultiStore = uniqueStores.length > 1;
    
    await cart.save();
    
    res.json({
      success: true,
      message: 'Thêm sản phẩm vào giỏ hàng thành công',
      data: cart
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Remove item from cart
const removeItemFromCart = async (req, res) => {
  try {
    const { itemId } = req.params;
    
    const cartItem = await CartItem.findById(itemId);
    if (!cartItem) {
      return res.status(404).json({
        success: false,
        message: 'Sản phẩm không tồn tại trong giỏ hàng'
      });
    }
    
    // Remove from cart
    await Cart.findByIdAndUpdate(cartItem.cart, {
      $pull: { items: itemId }
    });
    
    // Delete cart item
    await CartItem.findByIdAndDelete(itemId);
    
    // Check if multi-store
    const cart = await Cart.findById(cartItem.cart);
    const cartItems = await CartItem.find({ cart: cart._id });
    const uniqueStores = [...new Set(cartItems.map(item => item.store.toString()))];
    cart.isMultiStore = uniqueStores.length > 1;
    
    await cart.save();
    
    res.json({
      success: true,
      message: 'Xóa sản phẩm khỏi giỏ hàng thành công'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Update cart item quantity
const updateCartItemQuantity = async (req, res) => {
  try {
    const { itemId } = req.params;
    const { quantity } = req.body;
    
    if (quantity <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Số lượng phải lớn hơn 0'
      });
    }
    
    const cartItem = await CartItem.findById(itemId);
    if (!cartItem) {
      return res.status(404).json({
        success: false,
        message: 'Sản phẩm không tồn tại trong giỏ hàng'
      });
    }
    
    cartItem.quantity = quantity;
    await cartItem.save();
    
    // Check if multi-store
    const cart = await Cart.findById(cartItem.cart);
    const cartItems = await CartItem.find({ cart: cart._id });
    const uniqueStores = [...new Set(cartItems.map(item => item.store.toString()))];
    cart.isMultiStore = uniqueStores.length > 1;
    await cart.save();
    
    res.json({
      success: true,
      message: 'Cập nhật số lượng thành công',
      data: cartItem
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Clear cart
const clearCart = async (req, res) => {
  try {
    const { userId } = req.params;
    
    const cart = await Cart.findOne({ user: userId, status: 'active' });
    if (!cart) {
      return res.status(404).json({
        success: false,
        message: 'Giỏ hàng không tồn tại'
      });
    }
    
    // Delete all cart items
    await CartItem.deleteMany({ cart: cart._id });
    
    // Reset cart
    cart.items = [];
    cart.isMultiStore = false;
    await cart.save();
    
    res.json({
      success: true,
      message: 'Xóa tất cả sản phẩm khỏi giỏ hàng thành công'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get cart grouped by store
const getCart = async (req, res) => {
  try {
    const { userId } = req.params;
    
    const cart = await Cart.findOne({ 
      user: userId, 
      status: 'active' 
    })
    .populate({
      path: 'items',
      populate: [
        {
          path: 'product',
          select: 'name brand price originalPrice images status'
        },
        {
          path: 'store',
          select: 'name address city'
        }
      ]
    });
    
    if (!cart) {
      return res.json({
        success: true,
        data: null,
        message: 'Giỏ hàng trống'
      });
    }
    
    // Group items by store
    const itemsByStore = {};
    
    cart.items.forEach(item => {
      const storeId = item.store._id.toString();
      if (!itemsByStore[storeId]) {
        itemsByStore[storeId] = {
          store: item.store,
          items: []
        };
      }
      
      itemsByStore[storeId].items.push(item);
    });
    
    // Update cart multi-store status
    cart.isMultiStore = Object.keys(itemsByStore).length > 1;
    await cart.save();
    
    // Calculate total price for each store
    const itemsByStoreWithPrice = {};
    let grandTotal = 0;
    
    for (const [storeId, storeData] of Object.entries(itemsByStore)) {
      let storeTotal = 0;
      const simplifiedItems = [];
      
      for (const item of storeData.items) {
        const itemTotal = item.quantity * item.product.price;
        storeTotal += itemTotal;
        
        // Get stock from Inventory for this specific product and store
        const inventory = await Inventory.findOne({
          product: item.product._id,
          store: storeId
        });
        
        simplifiedItems.push({
          _id: item._id,
          product: {
            _id: item.product._id,
            name: item.product.name,
            brand: item.product.brand,
            price: item.product.price,
            originalPrice: item.product.originalPrice,
            stock: inventory ? inventory.stock : 0,
            images: item.product.images.slice(0, 1) // Chỉ lấy 1 ảnh đầu
          },
          quantity: item.quantity,
          totalPrice: itemTotal
        });
      }
      
      itemsByStoreWithPrice[storeId] = {
        store: {
          _id: storeData.store._id,
          name: storeData.store.name,
          city: storeData.store.city
        },
        items: simplifiedItems,
        storeTotal: storeTotal
      };
      
      grandTotal += storeTotal;
    }
    
    res.json({
      success: true,
      data: {
        cart: {
          _id: cart._id,
          isMultiStore: cart.isMultiStore,
          itemCount: cart.items.length,
          storeCount: Object.keys(itemsByStore).length,
          grandTotal: grandTotal
        },
        itemsByStore: Object.values(itemsByStoreWithPrice),
        summary: {
          totalStores: Object.keys(itemsByStore).length,
          totalItems: cart.items.length,
          grandTotal: grandTotal
        }
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Update cart item price
const updateItemPrice = async (req, res) => {
  try {
    const { itemId } = req.params;
    const { action } = req.body; // 'lock', 'unlock', 'update'
    
    const cartItem = await CartItem.findById(itemId)
      .populate('product', 'name price');
    
    if (!cartItem) {
      return res.status(404).json({
        success: false,
        message: 'Sản phẩm không tồn tại trong giỏ hàng'
      });
    }
    
    switch (action) {
      case 'lock':
        await cartItem.lockPrice();
        break;
        
      case 'unlock':
        await cartItem.unlockPrice();
        break;
        
      case 'update':
        await cartItem.updateCurrentPrice();
        break;
        
      default:
        return res.status(400).json({
          success: false,
          message: 'Hành động không hợp lệ'
        });
    }
    
    res.json({
      success: true,
      message: 'Cập nhật giá thành công',
      data: cartItem
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Check price changes before checkout
const checkPriceChanges = async (req, res) => {
  try {
    const { userId } = req.params;
    
    const cart = await Cart.findOne({ 
      user: userId, 
      status: 'active' 
    }).populate('items');
    
    if (!cart) {
      return res.status(404).json({
        success: false,
        message: 'Giỏ hàng không tồn tại'
      });
    }
    
    const priceChanges = [];
    let totalSavings = 0;
    let totalIncrease = 0;
    
    for (const item of cart.items) {
      await item.updateCurrentPrice();
      
      if (item.hasPriceChanged) {
        const change = item.priceDifference * item.quantity;
        
        priceChanges.push({
          productId: item.product,
          productName: item.product.name,
          oldPrice: item.price,
          newPrice: item.currentPrice,
          quantity: item.quantity,
          changeAmount: change,
          changePercentage: item.priceChangePercentage
        });
        
        if (change > 0) {
          totalIncrease += change;
        } else {
          totalSavings += Math.abs(change);
        }
      }
    }
    
    res.json({
      success: true,
      data: {
        hasChanges: priceChanges.length > 0,
        priceChanges,
        summary: {
          totalSavings,
          totalIncrease,
          netChange: totalSavings - totalIncrease
        }
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Lock all prices in cart
const lockAllPrices = async (req, res) => {
  try {
    const { userId } = req.params;
    
    const cart = await Cart.findOne({ 
      user: userId, 
      status: 'active' 
    }).populate('items');
    
    if (!cart) {
      return res.status(404).json({
        success: false,
        message: 'Giỏ hàng không tồn tại'
      });
    }
    
    for (const item of cart.items) {
      await item.lockPrice();
    }
    
    res.json({
      success: true,
      message: 'Đã khóa tất cả giá trong giỏ hàng',
      data: cart
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

// Get price change history for an item
const getPriceHistory = async (req, res) => {
  try {
    const { itemId } = req.params;
    
    const cartItem = await CartItem.findById(itemId);
    
    if (!cartItem) {
      return res.status(404).json({
        success: false,
        message: 'Sản phẩm không tồn tại'
      });
    }
    
    res.json({
      success: true,
      data: {
        originalPrice: cartItem.price,
        currentPrice: cartItem.currentPrice,
        priceUpdatedAt: cartItem.priceUpdatedAt,
        priceLocked: cartItem.priceLocked,
        hasPriceChanged: cartItem.hasPriceChanged,
        priceChangePercentage: cartItem.priceChangePercentage
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Lỗi server',
      error: error.message
    });
  }
};

module.exports = {
  createCart,
  addItemToCart,
  removeItemFromCart,
  updateCartItemQuantity,
  clearCart,
  getCart,
  updateItemPrice,
  checkPriceChanges,
  lockAllPrices,
  getPriceHistory
};
