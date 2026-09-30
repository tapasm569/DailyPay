# ================= Kotlinx Serialization =================
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.**

# Keep serializer helper methods and serializers
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }

# ================= DailyPay Data Models & Room =================
-keep class com.dailypay.app.data.model.** { *; }
-keep class com.dailypay.app.data.local.entity.** { *; }
-keep class com.dailypay.app.data.local.dao.** { *; }
-keep class com.dailypay.app.data.local.DailyPayDatabase { *; }

# Keep Enum names for string serialization
-keepclassmembers enum * {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================= Ktor, Supabase & Coroutines =================
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**
-dontwarn kotlinx.coroutines.**
