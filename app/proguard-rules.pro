# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Firebase / Google libraries
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Hilt / Dagger generated code
-keep class dagger.hilt.internal.generated.** { *; }
-keep class **_HiltModules { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# WorkManager reflection
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.impl.background.systemalarm.SystemAlarmService { *; }
-keep class androidx.work.ListenableWorker { *; }

# osmdroid
-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }

# Retrofit / Gson model reflection
-keep class pmf.rma.cityexplorerosm.data.remote.model.** { *; }
-keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }

# Room entities (avoid stripping annotations)
-keep class pmf.rma.cityexplorerosm.data.local.entities.** { *; }

# Prevent stripping of FCM service
-keep class pmf.rma.cityexplorerosm.fcm.AppFirebaseMessagingService { *; }
