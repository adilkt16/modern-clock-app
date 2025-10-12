# 🕒 Modern Clock App

A sophisticated Android clock and alarm application built with Kotlin, featuring modern UI design, comprehensive alarm management, and professional settings.

## ✨ Features

### 🕐 Advanced Clock Display
- **Real-time clock** with customizable format (24-hour/12-hour)
- **Current date display** with full date information
- **Smooth updates** every second with optimized performance
- **Modern glass-card design** with gradient backgrounds

### ⏰ Comprehensive Alarm System
- **Intuitive time picker** with hour/minute selection
- **AM/PM toggle** for 12-hour format
- **Optional end time** for alarm duration control
- **Persistent alarm storage** - alarms survive app restarts and device reboots
- **Full-screen alarm interface** with puzzle-based dismissal
- **Vibration and sound alerts** with customizable ringtones
- **Background alarm service** ensures alarms work even when app is closed

### ⚙️ Professional Settings Page
- **Time format toggle** (24-hour/AM-PM)
- **App information** with version details
- **Developer contact** (tap to reveal and copy)
- **Privacy policy** integration with GitHub link
- **License information** and legal details
- **Modern UI** matching app theme

### 🎨 Modern UI/UX Design
- **Gradient backgrounds** with professional color schemes
- **Glass-morphism cards** for content sections
- **Responsive layout** optimized for all screen sizes
- **Intuitive navigation** with proper visual feedback
- **AltRise branding** with professional logo integration

### 🔒 Privacy & Security
- **Local data storage** - no cloud data collection
- **Transparent privacy policy** hosted on GitHub
- **Minimal permissions** - only what's needed for functionality
- **No user tracking** or analytics

## 🛠️ Technical Specifications

- **Language**: Kotlin
- **Platform**: Android (API 24+ / Android 7.0+)
- **Architecture**: Single Activity with programmatic UI
- **Build System**: Gradle with Kotlin DSL
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 24 (Android 7.0)

### Key Technologies
- **AlarmManager** for precise alarm scheduling
- **Foreground Services** for reliable alarm execution
- **SharedPreferences** for settings persistence
- **Notification System** with proper channels
- **Boot Receiver** for alarm restoration after device restart

## 🚀 Installation & Setup

### For Users
1. Download the APK from releases
2. Enable "Install from unknown sources" if needed
3. Install and enjoy!

### For Developers
```bash
# Clone repository
git clone https://github.com/adilkt16/modern-clock-app.git
cd modern-clock-app

# Open in Android Studio
# Or build via command line:
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK with API 24+ 
- Device/emulator running Android 7.0+

## 📱 Usage Guide

### Setting Alarms
1. **Open the app** and scroll to the alarm section
2. **Select time** using the hour/minute pickers
3. **Choose format** (24hr/AM-PM) via toggle
4. **Set end time** (optional) for auto-dismiss
5. **Tap "Set Alarm"** to activate

### Managing Settings
1. **Tap the gear icon** (⚙️) in the top-right corner
2. **Toggle time format** between 24-hour and AM/PM
3. **View app information** including version
4. **Access privacy policy** via direct GitHub link
5. **Contact developer** (tap to reveal email)

### Dismissing Alarms
1. **Solve the math puzzle** when alarm triggers
2. **Use "Stop Alarm" button** in full-screen interface
3. **Auto-dismiss** if end time is configured

## 🏗️ Project Structure

```
ClockApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/modernclockapp/
│   │   │   ├── MainActivity.kt              # Main app interface
│   │   │   ├── SettingsActivity.kt          # Settings management
│   │   │   ├── alarm/
│   │   │   │   ├── AlarmReceiver.kt         # Alarm broadcast receiver
│   │   │   │   ├── AlarmScheduler.kt        # Alarm management system
│   │   │   │   ├── AlarmDismissActivity.kt  # Full-screen alarm interface
│   │   │   │   ├── AlarmNotificationService.kt # Background alarm service
│   │   │   │   └── BootCompletedReceiver.kt # Boot restoration
│   │   │   ├── models/
│   │   │   │   └── Alarm.kt                 # Alarm data model
│   │   │   └── storage/
│   │   │       └── AlarmStorage.kt          # Persistent storage
│   │   ├── res/                             # Resources and layouts
│   │   └── AndroidManifest.xml              # App configuration
│   ├── build.gradle                         # App-level build config
│   └── proguard-rules.pro                   # Code obfuscation rules
├── playstore-assets/                        # Play Store submission assets
├── privacy-policy.md                        # Privacy policy document
├── clockapp-release-key.jks                 # Release signing key
└── build.gradle                             # Project-level build config
```

## 🔐 Privacy Policy

This app respects your privacy and operates with minimal data collection:

### Information We Collect
- **Alarm settings** (stored locally on device)
- **App preferences** (time format, etc.)
- **No personal data** or usage analytics

### Data Usage
- All data stays **locally on your device**
- **No cloud storage** or external servers
- **No data sharing** with third parties
- **No user tracking** or behavioral analytics

### Permissions
- **SCHEDULE_EXACT_ALARM**: Set precise alarms
- **WAKE_LOCK**: Ensure alarms work when device sleeps
- **VIBRATE**: Provide vibration alerts
- **RECEIVE_BOOT_COMPLETED**: Restore alarms after restart
- **FOREGROUND_SERVICE**: Run alarm service in background
- **POST_NOTIFICATIONS**: Show alarm notifications

**Full Privacy Policy**: [GitHub Repository](https://github.com/adilkt16/modern-clock-app)

## 🎯 Play Store Information

- **App Name**: Modern Clock App
- **Category**: Tools / Productivity
- **Content Rating**: Everyone
- **Price**: Free
- **Size**: ~5MB
- **Developer**: ADIL
- **Contact**: adilkt16@gmail.com

## 🔄 Version History

### v1.0 (Current)
- ✅ Core clock and alarm functionality
- ✅ Modern UI with gradient design
- ✅ Professional settings page
- ✅ Privacy policy integration
- ✅ 24hr/12hr format support
- ✅ Persistent alarm storage
- ✅ Boot restoration
- ✅ Math puzzle dismissal
- ✅ Production-ready release

## 🤝 Contributing

We welcome contributions! Please:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit changes** (`git commit -m 'Add AmazingFeature'`)
4. **Push to branch** (`git push origin feature/AmazingFeature`)
5. **Open a Pull Request**

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
Copyright (c) 2025 ADIL

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

## 📞 Contact & Support

- **Developer**: ADIL
- **Email**: adilkt16@gmail.com
- **GitHub**: [adilkt16](https://github.com/adilkt16)
- **Repository**: [modern-clock-app](https://github.com/adilkt16/modern-clock-app)

For bug reports, feature requests, or general support, please create an issue on GitHub or contact the developer directly.

---

**Built with ❤️ using Kotlin & Android SDK**
