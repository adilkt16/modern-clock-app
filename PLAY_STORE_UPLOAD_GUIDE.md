# Play Store Upload Guide - ClockApp v1.2

## 📋 Pre-Upload Checklist

- [x] Modern UI redesign implemented
- [x] Contrast and readability improved
- [x] Toggle sections enhanced
- [x] Legal content colors updated
- [x] All features tested
- [x] Version code incremented (2 → 3)
- [x] Version name updated (1.1 → 1.2)
- [x] Keystore available

## 🚀 Quick Start - Build Release

Run the following command from the ClockApp directory:

```bash
./build-release.sh
```

This script will:
1. Check if keystore exists
2. Prompt for keystore password (hidden input)
3. Prompt for key password (hidden input)
4. Build the signed release AAB
5. Show you the output location

**Output file**: `app/build/outputs/bundle/release/app-release.aab`

---

## 📤 Upload to Play Store

### Step 1: Access Play Console
1. Go to https://play.google.com/console
2. Sign in with your developer account
3. Select **ClockApp** from your app list

### Step 2: Create New Release
1. In the left sidebar, go to **Production** (or **Testing** if you want to test first)
2. Click **Create new release**

### Step 3: Upload Bundle
1. Click **Upload** in the "App bundles" section
2. Select the file: `app/build/outputs/bundle/release/app-release.aab`
3. Wait for upload and processing (usually 1-2 minutes)

### Step 4: Release Notes
Copy the content from `RELEASE_NOTES_v1.2.md` (short version) and paste into the "Release notes" field for each supported language.

**English (US) - Release Notes:**
```
What's New in v1.2:
• Complete modern UI redesign with glassmorphism
• Improved readability with enhanced contrast
• Beautiful toggle sections with card styling
• Updated colors for better visibility
• Smooth animations and micro-interactions
• Enhanced settings screen design
• Better accessibility with WCAG AA compliance
• Professional and cohesive design language
```

### Step 5: Review and Rollout
1. Review the release details
2. Click **Review release**
3. If everything looks good, click **Start rollout to Production**
4. Choose rollout percentage (recommend 100% or staged rollout)
5. Confirm the rollout

---

## ⏱️ Timeline

- **Upload & Processing**: 1-2 minutes
- **Google Review**: Usually 1-3 days (can be faster)
- **Live on Play Store**: Immediately after approval
- **Users receive update**: Within 24 hours (gradual rollout)

---

## 🔍 Verification

After upload, verify:
1. **Version code** shows as **3**
2. **Version name** shows as **1.2**
3. **Target SDK** shows as **35 (Android 15)**
4. **App size** is reasonable (should be ~5-10 MB)
5. No security warnings

---

## 📊 Version History

| Version | Version Code | Release Date | Key Changes |
|---------|--------------|--------------|-------------|
| 1.0     | 1            | Initial      | First release |
| 1.1     | 2            | Previous     | Bug fixes |
| **1.2** | **3**        | **Today**    | **Modern UI redesign** |

---

## 🆘 Troubleshooting

### Build fails with signing error
- Verify keystore password is correct
- Check that `clockapp-release-key.jks` exists at `../clockapp-release-key.jks`

### Upload fails
- Check file size (should be < 150 MB)
- Ensure version code (3) is higher than current production (2)
- Verify the AAB is properly signed

### Version conflict
- Make sure version code in `app/build.gradle` is incremented
- Current version code should be **3**

---

## 📞 Support

If you encounter issues:
1. Check the build output for error messages
2. Verify keystore credentials
3. Review Play Console error messages
4. Check that all required metadata is filled in Play Console

---

## ✅ Post-Release

After the update is live:
1. Test the update on your device from Play Store
2. Monitor crash reports in Play Console
3. Check user reviews for feedback
4. Monitor download/update statistics
