#!/bin/bash

# Script to build signed release AAB for Play Store upload
# This will prompt for keystore passwords securely

echo "═══════════════════════════════════════════════════"
echo "  ClockApp - Play Store Release Build Script"
echo "═══════════════════════════════════════════════════"
echo ""

# Check if keystore exists
KEYSTORE_PATH="../clockapp-release-key.jks"
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "❌ ERROR: Keystore not found at $KEYSTORE_PATH"
    exit 1
fi

echo "✓ Keystore found"
echo ""

# Prompt for passwords (hidden input)
echo "Please enter your keystore credentials:"
echo ""
read -s -p "Keystore Password: " KEYSTORE_PASSWORD
echo ""
read -s -p "Key Password: " KEY_PASSWORD
echo ""
echo ""

# Export as environment variables
export KEYSTORE_PASSWORD
export KEY_PASSWORD

echo "Building release AAB (Android App Bundle)..."
echo ""

# Build the release bundle
./gradlew :app:bundleRelease

BUILD_EXIT_CODE=$?

if [ $BUILD_EXIT_CODE -eq 0 ]; then
    echo ""
    echo "═══════════════════════════════════════════════════"
    echo "  ✅ BUILD SUCCESSFUL!"
    echo "═══════════════════════════════════════════════════"
    echo ""
    echo "📦 Release bundle location:"
    echo "   app/build/outputs/bundle/release/app-release.aab"
    echo ""
    echo "📊 Bundle info:"
    ls -lh app/build/outputs/bundle/release/app-release.aab
    echo ""
    echo "🚀 Next steps:"
    echo "   1. Go to https://play.google.com/console"
    echo "   2. Select your app (ClockApp)"
    echo "   3. Go to 'Production' or 'Testing' track"
    echo "   4. Create new release"
    echo "   5. Upload: app/build/outputs/bundle/release/app-release.aab"
    echo ""
    echo "📋 Version Info:"
    echo "   Version Code: 3"
    echo "   Version Name: 1.2"
    echo ""
    echo "✨ What's new in this version:"
    echo "   - Modern 2025 UI redesign with glassmorphism"
    echo "   - Improved readability with enhanced contrast"
    echo "   - Beautiful toggle sections with card styling"
    echo "   - Updated legal content colors"
    echo "   - Smooth micro-interactions and animations"
    echo ""
else
    echo ""
    echo "❌ BUILD FAILED"
    echo "Please check the error messages above"
    exit 1
fi
