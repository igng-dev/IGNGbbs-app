-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep,includedescriptorclasses class net.igng.bbs.**$$serializer { *; }
-keepclassmembers class net.igng.bbs.** {
    *** Companion;
}
-keepclasseswithmembers class net.igng.bbs.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
