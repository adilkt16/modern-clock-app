#!/bin/bash

# Build script for Modern Clock App
# This script automates the build and testing process

echo "🏗️  Building Modern Clock App..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Check if we're in the right directory
if [ ! -f "settings.gradle" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

# Clean previous builds
print_status "Cleaning previous builds..."
./gradlew clean

# Check if clean was successful
if [ $? -ne 0 ]; then
    print_error "Clean failed. Please check Gradle configuration."
    exit 1
fi

# Build debug version
print_status "Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -ne 0 ]; then
    print_error "Debug build failed. Please check the build logs."
    exit 1
fi

# Run unit tests
print_status "Running unit tests..."
./gradlew testDebugUnitTest

# Check if tests passed
if [ $? -ne 0 ]; then
    print_warning "Some unit tests failed. Check test results for details."
else
    print_status "All unit tests passed! ✅"
fi

# Run lint checks
print_status "Running lint checks..."
./gradlew lintDebug

# Check if lint passed
if [ $? -ne 0 ]; then
    print_warning "Lint found some issues. Check lint report for details."
else
    print_status "Lint checks passed! ✅"
fi

# Build release version (unsigned)
print_status "Building release APK (unsigned)..."
./gradlew assembleRelease

# Check if release build was successful
if [ $? -ne 0 ]; then
    print_error "Release build failed. Please check the build logs."
    exit 1
fi

print_status "Build completed successfully! 🎉"
print_status "APK files are located in app/build/outputs/apk/"
print_status ""
print_status "📱 To install on device:"
print_status "  Debug: adb install app/build/outputs/apk/debug/app-debug.apk"
print_status "  Release: adb install app/build/outputs/apk/release/app-release-unsigned.apk"
print_status ""
print_status "🧪 To run instrumentation tests:"
print_status "  ./gradlew connectedDebugAndroidTest"