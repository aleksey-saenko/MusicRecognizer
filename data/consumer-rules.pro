# Keep DataStore fields
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite* {
   <fields>;
}

# With R8 full mode generic signatures are stripped for classes that are not kept.
# Suspend functions are wrapped in continuations where the type argument is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# TODO() delete after upgrading OkHttp to stable 5.0 version
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# TODO() there is a problem with RemoteRecognitionDataResult when R8 full mode enabled, detete this stub after fix
-keep class com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult { *; }