# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/android-sdk/tools/proguard/proguard-android.txt

# Keep Room entities
-keep class com.therapycompanion.data.db.** { *; }

# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.therapycompanion.backup.**$$serializer { *; }
-keepclassmembers class com.therapycompanion.backup.** {
    *** Companion;
}
-keepclasseswithmembers class com.therapycompanion.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}
