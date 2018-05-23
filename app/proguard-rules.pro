# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in d:\Android\sdk/tools/proguard/proguard-android.txt
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


#指定代码的压缩级别
-optimizationpasses 5
#包明不混合大小写
-dontusemixedcaseclassnames
#不去忽略非公共的库类和成员
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
 #优化  不优化输入的类文件
-dontoptimize
 #预校验
#-dontpreverify
 #混淆时是否记录日志
-verbose
 # 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#trinea-android-common需要
-optimizations !code/simplification/cast
-allowaccessmodification
# To prevent name conflict in incremental obfuscation.
-useuniqueclassmembernames
#忽略警告
-ignorewarnings
#sharesdk需要
-keepattributes InnerClasses,LineNumberTable
#保存源码行数，便于错误日志分析
-keepattributes SourceFile,LineNumberTable

-dontwarn com.google.ads.**

-keep class com.google.**  {*;}

#保护注解
-keepattributes *Annotation*
#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment
# 保持哪些类不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class cn.lt.android.ads.AdFillMachine
#保持 native 方法不被混淆
-keepclasseswithmembers class * {
    native <methods>;
}
#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
#保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
    public void *ButtonClicked(android.view.View);
}
#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable
#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#保持aidl文件
-keep class * implements android.os.IInterface {*;}
-keep class android.content.update.IPlatUpdateService { *; }
-keep class android.content.update.IPlatUpdateCallback { *; }
-keep class android.content.update.PlatUpdateInfo { *; }

#保持view不被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

#trinea-android-common需要
# class$ methods are inserted by some compilers to implement .class construct,
# see http://proguard.sourceforge.net/manual/examples.html#library
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

#trinea-android-common需要
# Keep classes and methods that have the guava @VisibleForTesg annotation
-keep @com.google.common.annotations.VisibleForTesting class *
-keepclassmembers class * {
@com.google.common.annotations.VisibleForTesting *;
}



#####################记录生成的日志数据,gradle build时在本项目根目录输出################
#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
# 生成补丁前用
-printmapping mapping.txt
# 生成补丁后用
#-applymapping mapping.txt

#####################记录生成的日志数据，gradle build时 在本项目根目录输出-end################

-dontwarn android.support.**

#保持 应用中心 的GSON解析所用数据Bean
#-keep class * extends cn.lt.android.network.netdata.bean.BaseBean

#如果用用到Gson解析包的，直接添加下面这几行就能成功混淆，不然会报错。
#gson
#-libraryjars libs/gson-2.2.2.jar
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keep class com.lidroid.xutils.** {*;}

#保持crash框架
-keep class org.acra.** {*;}


#GreenDaoGenerator
-dontwarn cn.lt.greendaogenerator.**
#-libraryjars ..\\GreenDaoGenerator
-keep class cn.lt.greendaogenerator.** { *; }

#保持GreenDao自动生成的类
-keep class cn.lt.android.db.** { *; }

#LTcommon
-dontwarn cn.lt.framework.**
#-libraryjars ..\\LTcommon
-keep class cn.lt.framework.** { *; }

#LTdownload
-dontwarn cn.lt.download.**
#-libraryjars ..\\LTdownload
-keep class cn.lt.download.** { *; }

#LTdownload
-dontwarn cn.lt.pullandloadmore.**
#-libraryjars ..\\LTpullandloadmore
-keep class cn.lt.pullandloadmore.** { *; }


-keep class cn.lt.android.download.** { *; }

#保持JSON解析用的实体类
-keep class cn.lt.android.network.** { *; }
-keep class cn.lt.android.GlobalConfig { *; }
-keep class cn.lt.android.util.MetaDataUtil { *; }
-keep class cn.lt.android.bean.** { *; }
-keep class cn.lt.android.entity.** { *; }
-keep class cn.lt.android.umsharesdk.** { *; }
-keep class cn.lt.android.statistics.eventbean.** { *; }
-keep class cn.lt.android.db.** { *; }
-keep class cn.lt.android.main.WebViewActivity$Params {*; }
-keep class cn.lt.android.main.personalcenter.model.** {*; }
-keep class cn.lt.android.network.bean.** {*; }
-keep class cn.lt.android.notification.bean.** {*; }
-keep class cn.lt.android.plateform.update.entiy.** {*; }
-keep class cn.lt.android.main.WebViewActivity$JavascriptInterface {*;}
-keep class cn.lt.android.ads.bean.** {*; }
-keepnames class cn.lt.android.ads.wanka.WanKa {
    public <fields>;
}
-keepnames class cn.lt.android.ads.wanka.WanKaRequestBean {
    <fields>;
}
-keepnames class cn.lt.android.ads.wanka.WanKaRequestBean$* {
    <fields>;
}



#保持eventbus
-keepclassmembers class ** {
    public void onEvent*(**);
}
-keepclassmembers class ** {
	public void onEventMainThread*(**);
}
-keep class de.greenrobot.event.** { *;}
-keep class de.greenrobot.event.util.** { *; }
-keep class de.greenrobot.dao.** { *;}
-keep class de.greenrobot.dao.async.** { *;}
-keep class de.greenrobot.dao.identityscope.** { *;}
-keep class de.greenrobot.dao.internal.** { *;}
-keep class de.greenrobot.dao.query.** { *;}





#保持小米推送不被混淆
-keep class cn.lt.android.push.xiaomi.XiaoMiPushReceiver {*;}
#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**

#保持个推送不被混淆
-dontwarn com.igexin.**
-keep class com.igexin.**{*;}
-keep class cn.lt.android.push.getui.GeTuiService {*;}
-keep class cn.lt.android.push.getui.GeTuiIntentService {*;}

#保持友盟分享不被混淆，，开始
-dontusemixedcaseclassnames
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**
-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
-keep public class com.umeng.socialize.* {*;}


-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class UMMoreHandler{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements   com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
-keep class com.tencent.mm.sdk.** {*;}
-keep class com.tencent.mm.opensdk.** {*;}
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep public class com.umeng.com.umeng.soexample.R$*{
public static final int *;
}
-keep public class com.linkedin.android.mobilesdk.R$*{
public static final int *;
   }
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {*;}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}

-keep class com.linkedin.** { *; }
-keepattributes Signature
#保持友盟分享不被混淆，，结束

#保持百度统计不被混淆
-keep class com.baidu.bottom.** { *; }
-keep class com.baidu.kirin.** { *; }
-keep class com.baidu.mobstat.** { *; }


#保持Rxjava不被混淆
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
#腾讯Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#广点通开屏广告
-keep class com.qq.e.** {
      public protected *;
  }
-keep class android.support.v4.app.NotificationCompat**{
      public *;
  }

#百度开屏广告
 -keepclassmembers class * extends android.app.Activity {
  public void *(android.view.View);
 }
  -keepclassmembers enum * {
   public static **[] values();
   public static ** valueOf(java.lang.String);
  }
  -keep class com.baidu.mobads.*.** { *; }

  #芮薇混淆
  -dontwarn com.eoim.aweiz.**
  -keep class com.eoim.aweiz.**{ *; }
  -dontwarn com.spr.sum.**
  -keep class com.spr.sum.**{ *; }
  -dontwarn com.aut.wtr.**
  -keep class com.aut.wtr.**{ *; }
  -keep class com.spl.dlutil.** { *; }