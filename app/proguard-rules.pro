# --- Retrofit & OkHttp ---
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# --- Gson (Serialização JSON) ---
# Impede que o ProGuard mude o nome dos campos dos teus modelos de dados (ex: 'productId')
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.jefferson.antenas.data.model.** { *; }
-keep class com.google.gson.** { *; }

# --- Hilt/Dagger ---
-keep class com.google.dagger.** { *; }
-keep class dagger.** { *; }
-keep class * extends dagger.internal.Factory

# --- Room ---
-keep class androidx.room.paging.** { *; }

-keep class androidx.compose.ui.platform.LocalSoftwareKeyboardController { *; }
-keep class com.stripe.android.** { *; }