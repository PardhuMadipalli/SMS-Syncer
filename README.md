# SMS syncer

## Goal
The goal is to build an android app that can send SMS messages after filtering them to ntfy.sh topic.

## Features
1. Send SMS messages to ntfy.sh topic
2. ✅ **Customizable SMS Filtering**: Advanced filtering system with customizable rules for important senders, keywords, and spam detection. Users can add/remove filter criteria and reset to defaults.
3. ✅ **SMS Permission Management**: The app automatically checks for SMS read permissions and requests them if not present. Users can grant permissions through the app interface.
4. ✅ **Contact Name Resolution**: Automatically displays contact names instead of phone numbers in notifications when contacts permission is granted. Falls back to phone numbers if permission is denied.
5. ✅ **Message Encryption**: SMS messages are encrypted using AES-256-CBC before sending to ntfy.sh. Users set a password that must match the shell script configuration for decryption.
6. ✅ **Multi-Device Support**: Multiple Android devices can send to the same ntfy.sh topic. Device names are displayed in notifications to distinguish between devices.
7. ✅ **Secure Topic Configuration**: Users can configure their ntfy.sh topic name securely with confirmation dialog. The topic is stored using Android's EncryptedSharedPreferences.
8. ✅ **Topic Display**: The app shows a masked version of the configured topic (first 2 and last 2 characters visible).
9. ✅ **Material Design 3 UI**: Modern, beautiful interface following Google's latest design guidelines with card-based layout, professional icons, and enhanced user experience.

## Security & Google Play Protect Compliance

### Security Measures Implemented
- **HTTPS Only**: All network communication uses secure HTTPS connections
- **Code Obfuscation**: Release builds use ProGuard for code protection
- **Network Security**: Network security configuration prevents insecure connections
- **Input Sanitization**: All data is sanitized before transmission
- **No Sensitive Logging**: SMS content is not logged or exposed in error messages
- **Permission Validation**: Proper permission checking and user consent
- **Secure Permissions**: Uses `BROADCAST_SMS` permission for receiver protection
- **Selective Encryption**: Topic names and passwords use EncryptedSharedPreferences with AES256 encryption, filter settings use standard storage
- **Message Encryption**: SMS messages are encrypted with AES-256-CBC using user-defined passwords before transmission
- **Topic Masking**: Topic names are displayed with only first 2 and last 2 characters visible for privacy

### Privacy Protection
- **Local Processing**: All SMS filtering and contact resolution happens locally on your device
- **No Data Storage**: SMS messages and contact information are not stored locally or on servers
- **Personal Topic**: Messages are sent only to your personal ntfy.sh topic
- **No Third-Party Access**: We do not have access to your ntfy.sh topic or messages
- **Contact Privacy**: Contact names are only used for display purposes and are not stored or transmitted

## Technology Stack

### UI Framework
- **Android Material Design Components (MDC)**: `com.google.android.material:material:1.11.0`
- **Layout System**: Traditional Android XML layouts (not Jetpack Compose)
- **View Binding**: Type-safe view binding for XML layouts
- **Theme**: Material Design 3 with AppCompat compatibility
- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 14 (API 34)

### Core Dependencies
- **AndroidX Core KTX**: Kotlin extensions for Android
- **AndroidX Lifecycle**: Lifecycle-aware components
- **AndroidX AppCompat**: Backward compatibility
- **AndroidX Security**: EncryptedSharedPreferences for sensitive topic data and passwords, standard SharedPreferences for filter settings
- **ConstraintLayout**: Advanced layout management
- **CardView & RecyclerView**: Material Design components

## Implementation Details

### Permission Handling
- The app checks for `RECEIVE_SMS`, `READ_SMS`, and `READ_CONTACTS` permissions on startup
- If permissions are not granted, the app shows a clear status message and provides a button to request permissions
- The app provides user-friendly explanations about why permissions are needed
- Permission status is visually indicated with color-coded status messages
- Contact names are displayed when contacts permission is granted, otherwise phone numbers are used
- Password configuration is required before the app can send encrypted messages

### UI Features
- **Material Design 3**: Modern, beautiful interface following Google's latest design guidelines
- **Android MDC**: Uses Material Design Components library for consistent, professional UI
- **XML-based Layouts**: Traditional Android XML layouts with Material Design components
- **Card-based Layout**: Information organized in clean, elevated cards for better readability
- **Top App Bar**: Professional toolbar with settings and help menu options
- **Bottom Navigation**: Easy access to topic configuration and filter customization
- **Smart Status Updates**: Status indicators with intelligent notifications only when status changes
- **Secure Topic Configuration**: Enhanced dialog with Material Design input fields and validation
- **Masked Topic Display**: Privacy-focused topic name masking (e.g., "ab***cd" for "abcdef")
- **Smart Action Buttons**: Context-aware buttons that appear when needed
- **Filter Customization Interface**: Dedicated activity page for modifying SMS filtering rules with chip-based editing and outlined action buttons
- **Scrollable Content**: All filter sections are properly scrollable for better usability
- **Color-coded Status System**:
  - 🟢 Green: Active (permissions granted, topic configured)
  - 🟠 Orange: Topic configuration required
  - 🔴 Red: Permission required/denied
- **Custom Material Icons**: Professional vector icons throughout the interface
- **Dark Mode Support**: Automatic theme switching with Material Design 3 color system
- **Responsive Design**: Adapts to different screen sizes and orientations
- **Accessibility**: Built-in accessibility features following Material Design guidelines
- **Smooth Animations**: Subtle animations and transitions for better user experience

### SMS Filtering
The app intelligently filters SMS messages to forward only important ones:
- **Important senders**: Banks, delivery services, ride-sharing apps
- **Important keywords**: OTP, urgent, delivery, payment, etc.
- **Numeric codes**: OTP and verification codes
- **Spam filtering**: Excludes promotional messages
- **Customizable rules**: Users can modify all filter criteria through an intuitive settings interface
- **Forward all option**: Toggle to ignore all filters and forward every SMS message
- **Reset to defaults**: Easy restoration of original filter settings

### Technical Implementation
- Uses Android's BroadcastReceiver to listen for incoming SMS
- **Background Operation**: SMS monitoring works even when app is closed from recent apps list
- Implements proper permission request flow for Android 6.0+
- Secure topic storage using EncryptedSharedPreferences with AES256 encryption, filter settings using standard SharedPreferences
- **Customizable SMS Filtering**: Advanced filtering system with persistent storage of custom rules
- **Contact Name Resolution**: Local contact lookup to display names instead of phone numbers
- **Message Encryption**: AES-256-CBC encryption of SMS messages before transmission to ntfy.sh
- Sends encrypted messages to ntfy.sh via HTTPS POST
- Handles network operations on background threads
- Provides user feedback through Toast messages
- Implements proper error handling without exposing sensitive data
- Topic configuration with confirmation dialog for first-time setup

### UI Architecture
- **Android Material Design Components (MDC)**: Uses `com.google.android.material:material:1.11.0` for modern UI components
- **XML Layouts**: Traditional Android XML-based layouts (not Jetpack Compose)
- **Material Design 3**: Latest Material Design 3 theming and components
- **CoordinatorLayout**: Advanced layout management for scrolling and FAB behavior
- **MaterialCardView**: Elevated cards for content organization
- **MaterialButton**: Enhanced buttons with proper styling and states
- **TextInputLayout**: Professional input fields with validation and error states
- **MaterialToolbar**: Modern app bar with menu integration
- **Custom Navigation Bar**: Modular bottom navigation with Material Design 3 surface colors and proper separation
- **AppCompat Theme**: Proper theme inheritance for Material Design compatibility
- **ViewBinding**: Type-safe view binding for XML layouts
- **Enhanced Error Handling**: Comprehensive try-catch blocks to prevent crashes on real devices
- **ProGuard Protection**: Proper obfuscation rules to prevent Material Design component crashes
- **Network Security**: Strict HTTPS-only configuration for real device compatibility
- **Permission Safety**: Robust permission checking with fallback error handling

### Build Configuration
- **Release Builds**: Code obfuscation and resource shrinking enabled
- **Network Security**: Prevents cleartext traffic in release builds
- **ProGuard Rules**: Protects SMS-related code while enabling optimization
- **Security Dependencies**: Includes Android Security Crypto library

## Privacy Policy
A comprehensive privacy policy is available in `PRIVACY_POLICY.md` that explains:
- How SMS data is handled
- Security measures implemented
- User control over data
- Compliance with privacy regulations

## macOS Notification System

The project includes shell scripts for receiving SMS notifications on macOS:

### Setup
1. **Create Configuration File**: Create `/Users/Shared/SMS_Syncer_Listener/config.json` with your ntfy.sh topic and encryption password:
   ```bash
   echo '{"topic_name":"your_topic_name","encryption_password":"your_password"}' > /Users/Shared/SMS_Syncer_Listener/config.json
   ```

2. **Install Dependencies**: Ensure you have the required tools:
   ```bash
   brew install jq  # For JSON parsing
   ```

3. **Make Scripts Executable**:
   ```bash
   chmod +x manage_ntfy.sh
   chmod +x ntfy_notifier.sh
   ```

### Usage
- **Start/Stop/Restart**: `./manage_ntfy.sh [start|stop|restart]`
- **Check Status**: `./manage_ntfy.sh status`
- **View Logs**: `./manage_ntfy.sh logs`
- **Default Action**: Running `./manage_ntfy.sh` without arguments will restart the notifier

### Features
- **Configuration-Based**: Topic name and encryption password are read from config file, not hardcoded in scripts
- **Message Decryption**: Automatically decrypts AES-256-CBC encrypted messages from the Android app
- **Automatic Reconnection**: Handles network disconnections gracefully
- **Log Management**: Automatic log rotation to prevent disk space issues
- **Singleton Protection**: Prevents multiple instances from running
- **System Notifications**: Displays decrypted SMS messages as native macOS notifications with device identification

## Encryption System

The app implements end-to-end encryption for SMS messages:

### How It Works
1. **Android App**: Encrypts SMS messages using AES-256-CBC with a user-defined password
2. **Transmission**: Encrypted messages are sent to ntfy.sh via HTTPS
3. **macOS Script**: Decrypts messages using the same password and displays them as notifications

### Security Features
- **AES-256-CBC Encryption**: Industry-standard encryption algorithm
- **Password-Based**: User controls the encryption key
- **Base64 Encoding**: Encrypted data is base64 encoded for safe transmission
- **IV (Initialization Vector)**: Random IV ensures each message is uniquely encrypted
- **Shell-Compatible**: Uses OpenSSL for decryption, compatible with standard shell tools

### Configuration
- **Android**: Set encryption password in the app (minimum 8 characters)
- **macOS**: Configure the same password in `/Users/Shared/SMS_Syncer_Listener/config.json`
- **Password Storage**: Passwords are stored securely using Android's EncryptedSharedPreferences

### Multi-Device Support
Multiple Android devices can send SMS messages to the same ntfy.sh topic:

**Requirements:**
- All devices must use the **same encryption password**
- All devices must use the **same ntfy.sh topic**
- Each device will be identified by its user-defined device name from System Settings

**Notification Display:**
- **Title**: "SMS from [Contact Name]"
- **Subtitle**: "Received on [Device Name]"
- **Message**: The actual SMS content

**Setting Device Names:**
1. **Android**: Go to **Settings** → **About phone** → **Device name** (or **Settings** → **Connected devices** → **Connection preferences** → **Device name**)
2. **Set a meaningful name** like "John's Phone", "Work Phone", "Family Tablet", etc.
3. **The app will automatically use this name** in notifications
4. **Fallback**: If no custom name is set, the app will use the device model name (e.g., "Pixel 7")

**Example Notifications:**
```
Title: SMS from John Doe
Subtitle: Received on John's Phone
Message: Hey, are you free for lunch?

Title: SMS from Jane Smith  
Subtitle: Received on Work Phone
Message: Meeting moved to 3 PM
```

**Important Limitation:**
All devices sharing the same topic **must use the same encryption password**. This is because the shell script uses a single password to decrypt all messages from the topic.

## Installation
The app is designed to pass Google Play Protect scans and follows Android security best practices. Users may need to:
1. Grant SMS permissions when prompted
2. Trust the app in Google Play Protect settings if needed
3. Ensure the app has network access for ntfy.sh communication