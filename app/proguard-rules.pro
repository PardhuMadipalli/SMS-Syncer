# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep SMS-related classes for proper functionality
-keep class com.pardhu.smssyncer.MainActivity { *; }
-keep class com.pardhu.smssyncer.MainActivity$SmsReceiver { *; }

# Keep Android framework classes needed for SMS
-keep class android.telephony.SmsMessage { *; }
-keep class android.content.BroadcastReceiver { *; }

# Security: Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Security: Obfuscate sensitive strings
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Security: Protect against reflection attacks
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Security: Remove debug information
-renamesourcefileattribute SourceFile

# Security: Suppress warnings for missing annotation classes
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy

# Security: Keep EncryptedSharedPreferences classes
-keep class androidx.security.crypto.** { *; }
-keep class android.security.keystore.** { *; }

# Keep Material Design components - CRITICAL for real devices
-keep class com.google.android.material.** { *; }
-keep class androidx.material.** { *; }
-keep class androidx.material3.** { *; }
-keep class com.google.android.material.theme.** { *; }
-keep class com.google.android.material.resources.** { *; }

# Keep all app classes - prevent crashes on real devices
-keep class com.pardhu.smssyncer.** { *; }
-keepclassmembers class com.pardhu.smssyncer.** { *; }

# Keep Android framework classes needed for SMS
-keep class android.telephony.SmsMessage { *; }
-keep class android.content.BroadcastReceiver { *; }

# Keep Material Design resources
-keep class **.R$* {
    public static <fields>;
}

# Keep Material Design attributes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions