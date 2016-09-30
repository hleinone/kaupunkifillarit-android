# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Specifies to write out some more information during processing. If the program terminates with an
# exception, this option will print out the entire stack trace, instead of just the exception
# message.
-verbose

# Keep classes that are referenced on the AndroidManifest
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application

# Keep setters in Views so that animations can still work. See
# http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
  void set*(***);
  *** get*();
}

-keepclassmembers public class com.google.android.gms.maps.model.Marker {
  void set*(***);
  *** get*();
}

# Keep the R
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep line numbers
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# RxAndroid
-dontwarn rx.internal.util.unsafe.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
   long producerNode;
   long consumerNode;
}

# LoganSquare
-keep class com.bluelinelabs.logansquare.** { *; }
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *
-keep class **$$JsonObjectMapper { *; }

-dontwarn com.google.android.gms.**