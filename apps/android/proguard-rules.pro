# Peek Chat ProGuard Rules

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.peekchat.**$$serializer { *; }
-keepclassmembers class com.peekchat.** {
    *** Companion;
}
-keepclasseswithmembers class com.peekchat.** {
    kotlinx.serialization.KSerializer serializer(...);
}
