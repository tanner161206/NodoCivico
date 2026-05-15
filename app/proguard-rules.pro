# Nodo Cívico — ProGuard Rules
# Mantener modelos Room y Retrofit
-keep class com.nodocivico.app.data.local.entity.** { *; }
-keep class com.nodocivico.app.data.remote.** { *; }
-keep class com.nodocivico.app.domain.model.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
