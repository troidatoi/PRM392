# Bottom Navigation Bar - Hướng dẫn sử dụng

## 📱 Giao diện Bottom Navigation Bar đã được đồng bộ và cải thiện

### ✨ Cải tiến giao diện:

1. **Giao diện hiện đại hơn:**
   - Icon lớn hơn (28dp thay vì 26dp)
   - Elevation cao hơn (12dp) tạo hiệu ứng nổi rõ ràng
   - Màu sắc rõ ràng: Active (#2196F3 - Blue), Inactive (#9E9E9E - Gray)
   - Badge đỏ cho số lượng giỏ hàng

2. **Đồng bộ giữa các trang:**
   - Sử dụng chung 1 layout: `bottom_navigation_bar.xml`
   - Tự động highlight tab hiện tại
   - Animation mượt mà khi chuyển trang

### 🔧 Cách sử dụng trong Activity:

#### Bước 1: Thêm include vào layout XML

Thay thế bottom navigation bar cũ bằng:

```xml
<!-- Bottom Navigation Bar -->
<include layout="@layout/bottom_navigation_bar" />
```

#### Bước 2: Setup trong Activity Java

Trong method `onCreate()` hoặc `onResume()`, thêm:

```java
import com.example.project.utils.BottomNavigationHelper;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_xxx);
    
    // Setup bottom navigation
    // activeTab: 0=Home, 1=Products, 2=Cart, 3=Account
    BottomNavigationHelper.setupBottomNavigation(this, 0); // 0 cho trang Home
}

@Override
protected void onResume() {
    super.onResume();
    // Update lại trạng thái khi quay lại activity
    BottomNavigationHelper.setupBottomNavigation(this, 0);
}
```

#### Bước 3: Cập nhật số lượng giỏ hàng (optional)

```java
// Hiển thị badge với số lượng items
BottomNavigationHelper.updateCartBadge(this, cartItemCount);

// Ẩn badge
BottomNavigationHelper.updateCartBadge(this, 0);
```

### 📝 Ví dụ cụ thể cho từng Activity:

#### HomeActivity.java
```java
BottomNavigationHelper.setupBottomNavigation(this, 0); // Tab Home active
```

#### ShopActivity.java
```java
BottomNavigationHelper.setupBottomNavigation(this, 1); // Tab Products active
```

#### CartActivity.java
```java
BottomNavigationHelper.setupBottomNavigation(this, 2); // Tab Cart active
BottomNavigationHelper.updateCartBadge(this, cartItems.size()); // Show badge
```

#### AccountActivity.java
```java
BottomNavigationHelper.setupBottomNavigation(this, 3); // Tab Account active
```

### 🎨 File đã tạo:

1. **Layout:**
   - `/app/src/main/res/layout/bottom_navigation_bar.xml` - Layout chung cho bottom nav
   - `/app/src/main/res/drawable/badge_background.xml` - Background cho badge đỏ

2. **Java Helper:**
   - `/app/src/main/java/com/example/project/utils/BottomNavigationHelper.java` - Class quản lý navigation

3. **Activities đã cập nhật:**
   - `activity_home.xml` ✅
   - `activity_shop.xml` ✅
   - `activity_cart.xml` ✅
   - `activity_account.xml` ✅

### 🚀 Lợi ích:

- ✅ **Dễ bảo trì:** Chỉ cần sửa 1 file layout thay vì nhiều file
- ✅ **Nhất quán:** Giao diện giống nhau ở mọi trang
- ✅ **Dễ dùng:** Chỉ 1 dòng code để setup
- ✅ **Linh hoạt:** Dễ dàng thêm/bớt tab hoặc thay đổi màu sắc
- ✅ **Hiện đại:** Giao diện đẹp, professional hơn

### 🎯 Tùy chỉnh màu sắc:

Nếu muốn đổi màu, sửa trong `BottomNavigationHelper.java`:

```java
private static final int COLOR_ACTIVE = 0xFF2196F3; // Màu khi active
private static final int COLOR_INACTIVE = 0xFF9E9E9E; // Màu khi inactive
```

### 💡 Lưu ý:

- Đảm bảo tất cả activities có `android:id="@+id/bottomNavBar"` trong layout
- Badge sẽ tự động ẩn khi số lượng = 0
- Badge hiển thị "99+" khi số lượng > 99

