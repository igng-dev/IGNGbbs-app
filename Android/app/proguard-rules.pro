-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep,includedescriptorclasses class net.igng.bbs.**$$serializer { *; }
-keepclassmembers class net.igng.bbs.** {
    *** Companion;
}
-keepclasseswithmembers class net.igng.bbs.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# AGP 9+ enables strictFullModeForKeepRules by default; preserve RoomDatabase
# generated *_Impl no-arg constructors instantiated via reflection (e.g. WorkDatabase_Impl).
-keep class * extends androidx.room.RoomDatabase {
    void <init>();
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
