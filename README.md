# HashCheck 🏷️

![Android SDK](https://img.shields.io/badge/Android-SDK%2026%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)
![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-green?style=flat-square&logo=shield)

**HashCheck** is a high-performance, private, and lightweight cryptographic hash calculator for Android. Built with 100% Jetpack Compose and Material 3, it enables fast checksum calculation and verification for both plain text and files of any size without compromising your privacy.

---

## ✨ Features

- 🔒 **100% Offline & Private**: Zero network permissions required (`android.permission.INTERNET` is omitted). No analytics, ads, tracking, or cloud uploads. All calculations happen strictly in local memory and temporary streaming buffers.
- ⚡ **High-Performance Streaming File Hashing**: Calculates multiple cryptographic hashes in a single pass using a 64 KB memory-efficient buffer stream. Easily process large files with real-time progress indicators.
- ✍️ **Text Hashing**: Instant auto-calculation as you type (with intelligent debouncing) or manual trigger. Displays character length and UTF-8 byte count.
- 🎯 **Checksum Matching & Verification**: Compare calculated hashes against expected checksums with real-time visual feedback (Match / Mismatch).
- ⚙️ **Customizable Active Algorithms**: Enable or disable algorithms based on your needs.
- 🔠 **Hex Formatting**: Toggle between **Lowercase** (`9e107d9d...`) and **Uppercase** (`9E107D9D...`) hexadecimal outputs.
- 🛡️ **Auto-Wipe on Exit**: Optional security feature to automatically purge input text, file references, and generated hashes when leaving the app.
- 🎨 **Material 3 Design & Themes**: Supports System Default, Light, and Dark themes with haptic feedback support.

---

## 🧮 Supported Hash Algorithms

| Algorithm | Digest Size | Status | Cryptographic Note |
| :--- | :---: | :---: | :--- |
| **MD5** | 128-bit | ⚠️ Legacy | Vulnerable to collision attacks; suitable for non-critical legacy checksums only. |
| **SHA-1** | 160-bit | ⚠️ Legacy | Deprecated for security-sensitive applications due to collision vulnerability. |
| **SHA-224** | 224-bit | ✅ Recommended | SHA-2 family truncated variant offering strong collision resistance. |
| **SHA-256** | 256-bit | ✅ Recommended | Industry standard for file integrity verification and digital signatures. |
| **SHA-384** | 384-bit | ✅ Recommended | High-security SHA-2 variant. |
| **SHA-512** | 512-bit | ✅ Recommended | Maximum security SHA-2 digest length. |

---

## 📱 Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (2.2.10)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: Clean MVVM (Model-ViewModel-StateFlow)
- **Asynchronous Processing**: Kotlin Coroutines (`Dispatchers.IO` & `Dispatchers.Default`)
- **Storage & Settings**: Jetpack DataStore (Preferences)
- **Navigation**: Jetpack Navigation Compose
- **Target SDK**: Android 16 (API Level 37) | **Min SDK**: Android 8.0 (API Level 26)

---

## 🛠️ Building & Running

### Prerequisites
- Android Studio Ladybug (2024.2) or higher
- JDK 11 or higher
- Android SDK 37

### Build Commands
Clone the repository and build using Gradle:

```bash
# Clone the repository
git clone https://github.com/abdullah-ateeq/HashCheck.git
cd HashCheck

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew test
```

---

## 🛡️ Privacy Statement

HashCheck is engineered under a zero-trust privacy model:
1. **No Internet Access**: The application does not declare internet permissions in its `AndroidManifest.xml`.
2. **Local Processing**: File and text data are processed entirely on-device via `java.security.MessageDigest`.
3. **No Persistence of Sensitive Inputs**: Input data is kept in transient memory only and can be configured to auto-clear on backgrounding.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
