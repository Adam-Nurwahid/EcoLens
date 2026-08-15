# ==========================================
# ProGuard Rules untuk EcoLens (LiteRT, Room, Firebase)
# ==========================================

# 1. LiteRT / TensorFlow Lite
-keep class com.google.ai.edge.litert.** { *; }
-keepclassmembers class * {
    @com.google.ai.edge.litert.** *;
}
-dontwarn com.google.ai.edge.litert.**

# 2. Room Database
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.Entity

# 3. Firebase
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn com.google.firebase.**