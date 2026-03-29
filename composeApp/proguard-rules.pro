# Luna — composeApp ProGuard / R8 rules

# --- Kotlin ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontnote kotlin.**
-keepclassmembers class ** {
    public static ** INSTANCE;
    public static ** Companion;
}

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.debug.*
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.** { *** Companion; }
-keepclasseswithmembers @kotlinx.serialization.Serializable class * { *; }
-keep,includedescriptorclasses class com.luna.** implements kotlinx.serialization.KSerializer { *; }
-keep,includedescriptorclasses class com.luna.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.luna.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers enum com.luna.** { *; }

# --- Ktor (OkHttp engine) ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontnote io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# --- Koin 4.x ---
-keep class org.koin.** { *; }
-dontwarn org.koin.**
-keep class com.luna.chat.presentation.viewmodel.** { *; }
-keep class com.luna.chat.domain.** { *; }
-keep class com.luna.chat.data.** { *; }
-keep class com.luna.chat.platform.** { *; }
-keep class com.luna.chat.di.** { *; }

# --- SQLDelight 2.x ---
-keep class app.cash.sqldelight.** { *; }
-dontwarn app.cash.sqldelight.**
-keep class com.luna.chat.**Database { *; }
-keep class com.luna.chat.**DatabaseImpl { *; }
-keep class com.luna.chat.**Queries { *; }
-keep class com.luna.chat.**QueriesImpl { *; }

# --- SQLCipher ---
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# --- Android Speech / TTS ---
-keep interface android.speech.RecognitionListener { *; }
-keep class * implements android.speech.RecognitionListener { *; }
-keep interface android.speech.tts.TextToSpeech$OnInitListener { *; }
-keep class * implements android.speech.tts.TextToSpeech$OnInitListener { *; }

# --- AndroidX ---
-keep class androidx.activity.result.contract.** { *; }
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# --- Misc ---
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.instrument.**
-dontwarn org.slf4j.**

# --- Stack traces ---
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
