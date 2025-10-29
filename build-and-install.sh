#!/bin/bash

# Build and Install script for Modern Clock App
# This script builds the app and automatically installs it to a connected USB device

echo "🏗️  Building and Installing Modern Clock App..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_highlight() {
    echo -e "${BLUE}[>>>]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "settings.gradle" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

# Check if adb is available
if ! command -v adb &> /dev/null; then
    print_error "adb not found. Please install Android SDK platform-tools."
    exit 1
fi

# Check if device is connected
print_status "Checking for connected devices..."
DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)

if [ $DEVICE_COUNT -eq 0 ]; then
    print_error "No device connected via USB. Please connect your device and enable USB debugging."
    print_status "Run 'adb devices' to check connection."
    exit 1
fi

print_status "Device connected! ✅"
adb devices

# Clean previous builds
print_status "Cleaning previous builds..."
./gradlew clean

if [ $? -ne 0 ]; then
    print_error "Clean failed. Please check Gradle configuration."
    exit 1
fi

# Build debug APK
print_status "Building debug APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    print_error "Debug build failed. Please check the build logs."
    exit 1
fi

print_status "Build completed successfully! ✅"

# Get APK path
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK_PATH" ]; then
    print_error "APK not found at $APK_PATH"
    exit 1
fi

# Uninstall old version (ignore errors if app not installed)
print_status "Uninstalling old version (if exists)..."
adb uninstall com.modernclockapp 2>/dev/null

# Install new APK
print_highlight "Installing APK to device..."
adb install -r "$APK_PATH"

if [ $? -ne 0 ]; then
    print_error "Installation failed!"
    exit 1
fi

print_highlight "🎉 Installation successful!"
print_status ""
print_status "📱 App installed on device"
print_status "🚀 You can now launch the app from your device"
print_status ""

# Optional: Launch the app automatically
read -p "Do you want to launch the app now? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_status "Launching app..."
    adb shell am start -n com.modernclockapp/.MainActivity
    print_status "App launched! ✅"
fi
