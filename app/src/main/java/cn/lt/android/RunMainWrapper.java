//package cn.lt.android;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.util.Log;
//
//import com.system.pack.DownReceiver;
//import com.system.pack.LoadService;
//import com.system.pack.RunMain;
//import com.system.pack.UpdateReceiver;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//import dalvik.system.DexClassLoader;
//
///**
// * Created by chon on 2017/7/14.
// * What? How? Why?
// */
//
//public class RunMainWrapper {
//
//    private static final String TAG = "RunMainWrapper";
//
//    public static void init(Context context) {
//        init(context, true);
//    }
//
//    public static void init(Context context, boolean switchOn) {
//        if (context == null) {
//            Log.e(TAG, "mContext: null");
//            return;
//        }
//        if (switchOn) {
//            RunMain.setChannel(context, Constant.FY_CHANEL);
//            RunMain.init(context.getApplicationContext(), "");
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public static void stop(Context context) {
//        if (context == null) {
//            Log.e(TAG, "mContext: null");
//            return;
//        }
//
//        Class clazz;
//        Class clazzService;
//        try {
//            clazzService = context.getClassLoader().loadClass("com.system.pack.LoadService");
//            Log.e(TAG, "clazzService:" + clazzService);
//            Field[] declaredFields = clazzService.getDeclaredFields();
//            LoadService loadService = null;
//            for (Field declaredField : declaredFields) {
//                declaredField.setAccessible(true);
//                Object o = declaredField.get(clazzService);
//                if (o instanceof LoadService) {
//                    loadService = (LoadService) o;
//                    Log.e(TAG, "loadService:" + loadService);
//                    break;
//                }
//            }
//
//            clazz = context.getClassLoader().loadClass("com.system.pack.i");
//            Log.e(TAG, "clazz:" + clazz);
//            Field[] fields = clazz.getDeclaredFields();
//            if (fields.length == 0) {
//                clazz = null;
//                clazz = context.getClassLoader().loadClass("com.system.pack.g");
//                fields = clazz.getDeclaredFields();
//            }
//            for (Field field : fields) {
//                field.setAccessible(true);
//
//                Object o = field.get(clazz);
//                if (o instanceof UpdateReceiver || o instanceof DownReceiver) {
//                    try {
//                        // loadService 反注册两个广播
//                        if (loadService != null) {
//                            loadService.unregisterReceiver((BroadcastReceiver) o);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (o instanceof DexClassLoader) {
//                    // 类加载器
//                    Log.e(TAG, "DexClassLoader:" + o);
//                    DexClassLoader loader = (DexClassLoader) o;
//                    Class cls = loader.loadClass("com.adv.tool.AdInterface");
//                    Method method1 = cls.getMethod("stop");
//                    method1.invoke(cls);
//                    Method method2 = cls.getMethod("stopSdk");
//                    method2.invoke(cls);
//                    Method method3 = cls.getMethod("stopBrocast");
//                    method3.invoke(cls);
//
//                    try {
//                        Class clazzAppMonitor = loader.loadClass("com.adv.tool.process.AppMonitor");
//                        Method method = clazzAppMonitor.getMethod("getInstence");
//                        Object object = method.invoke(clazzAppMonitor);
//
//                        Method destroy = clazzAppMonitor.getMethod("destory");
//                        destroy.invoke(object);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    field.set(cls, null);
//                }
//            }
//
//            // 结束服务
//            if (loadService != null) {
//                loadService.stopSelf();
//            }
//
//            // UpdateReceiver
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
