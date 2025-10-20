const mongoose = require('mongoose');

const categorySchema = new mongoose.Schema({
  categoryName: {
    type: String,
    required: [true, 'Tên danh mục là bắt buộc'],
    unique: true,
    trim: true,
    maxlength: [100, 'Tên danh mục không được vượt quá 100 ký tự']
  },
  description: {
    type: String,
    trim: true,
    maxlength: [500, 'Mô tả danh mục không được vượt quá 500 ký tự']
  },
  image: {
    type: String,
    trim: true
  },
  isActive: {
    type: Boolean,
    default: true
  },
  sortOrder: {
    type: Number,
    default: 0
  },
  parentCategory: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Category',
    default: null
  },
  metadata: {
    seoTitle: {
      type: String,
      trim: true,
      maxlength: [60, 'SEO title không được vượt quá 60 ký tự']
    },
    seoDescription: {
      type: String,
      trim: true,
      maxlength: [160, 'SEO description không được vượt quá 160 ký tự']
    },
    keywords: [{
      type: String,
      trim: true
    }]
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Virtual for product count
categorySchema.virtual('productCount', {
  ref: 'Bike',
  localField: '_id',
  foreignField: 'category',
  count: true
});

// Virtual for subcategories
categorySchema.virtual('subcategories', {
  ref: 'Category',
  localField: '_id',
  foreignField: 'parentCategory'
});

// Index for better search performance
categorySchema.index({ categoryName: 'text', description: 'text' });
categorySchema.index({ isActive: 1, sortOrder: 1 });
categorySchema.index({ parentCategory: 1 });

// Pre-save middleware
categorySchema.pre('save', function(next) {
  // Auto-generate SEO title if not provided
  if (!this.metadata.seoTitle && this.categoryName) {
    this.metadata.seoTitle = this.categoryName;
  }
  
  // Auto-generate SEO description if not provided
  if (!this.metadata.seoDescription && this.description) {
    this.metadata.seoDescription = this.description.substring(0, 160);
  }
  
  next();
});

module.exports = mongoose.model('Category', categorySchema);
