#!/bin/bash

# Script để khởi động 2 Android Emulator cùng lúc

EMULATOR_PATH="/Users/mycomputer/Library/Android/sdk/emulator/emulator"
ADB_PATH="/Users/mycomputer/Library/Android/sdk/platform-tools/adb"

echo "🚀 Khởi động Pixel_7..."
nohup $EMULATOR_PATH -avd Pixel_7 > /tmp/emulator_pixel7.log 2>&1 &
EMULATOR1_PID=$!
echo "✅ Pixel_7 đang khởi động (PID: $EMULATOR1_PID)"

echo ""
echo "⏳ Đợi 5 giây trước khi khởi động emulator thứ 2..."
sleep 5

echo "🚀 Khởi động Pixel_7_Pro..."
nohup $EMULATOR_PATH -avd Pixel_7_Pro > /tmp/emulator_pixel7pro.log 2>&1 &
EMULATOR2_PID=$!
echo "✅ Pixel_7_Pro đang khởi động (PID: $EMULATOR2_PID)"

echo ""
echo "⏳ Đợi 20 giây để emulator khởi động hoàn tất..."
sleep 20

echo ""
echo "📱 Danh sách devices đang chạy:"
$ADB_PATH devices -l

echo ""
echo "✅ Hoàn tất! Cả 2 emulator đang chạy."
echo ""
echo "📝 Lưu ý:"
echo "   - Để xem log Pixel_7: tail -f /tmp/emulator_pixel7.log"
echo "   - Để xem log Pixel_7_Pro: tail -f /tmp/emulator_pixel7pro.log"
echo "   - Để tắt: killall emulator"
