#!/bin/bash

echo "🕐 Modern Android Clock App - Device Testing"
echo "============================================"

# Navigate to project directory
cd /home/user/Desktop/projects/alone/ClockApp

# Check if device is connected
echo "📱 Checking for connected devices..."
adb devices

# Count connected devices
device_count=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)

if [ $device_count -eq 0 ]; then
    echo "❌ No devices connected!"
    echo "Please make sure:"
    echo "1. Your device is connected via USB"
    echo "2. USB Debugging is enabled"
    echo "3. You've allowed debugging on your device"
    exit 1
fi

echo "✅ Found $device_count connected device(s)"
echo ""

# Build the app
echo "🔧 Building debug APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"
echo ""

# Install the app
echo "📲 Installing app on your device..."
./gradlew installDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 SUCCESS! App installed on your device!"
    echo ""
    echo "📱 Next steps:"
    echo "1. Look for 'Modern Clock' app on your device"
    echo "2. Tap to open the app"
    echo "3. Test all features:"
    echo "   • Digital/Analog Clock"
    echo "   • Alarms"
    echo "   • Timer"
    echo "   • Stopwatch"
    echo ""
    echo "🧪 You can also run tests with:"
    echo "./test-device.sh test"
else
    echo "❌ Installation failed!"
    echo "Try disconnecting and reconnecting your device"
fi