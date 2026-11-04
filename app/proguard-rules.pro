# Retrofit / Gson DTOs are reflected over: keep their names and fields.
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class com.spacewalls.app.data.remote.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
