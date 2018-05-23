package cn.lt.android.entity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.db.WakeTaskEntity;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.android.wake.NotificationClickReceiver;
import cn.lt.android.wake.WaKeLog;
import cn.lt.appstore.R;
import cn.lt.framework.util.BitmapUtils;
import cn.lt.framework.util.FileUtils;
import cn.lt.framework.util.ScreenUtils;

import static android.content.Context.NOTIFICATION_SERVICE;


/**
 * Created by chon on 2017/11/18.
 * What? How? Why?
 */

@SuppressWarnings("WeakerAccess")
public class SilentTask implements Comparable {
    public static final String TASK_PATH = LTApplication.instance.getCacheDir() + File.separator + "task.txt";
    //1.deeplink地址  2.H5地址  3.启动应用

    public static final String JUMP_TYPE_DEEPLINK = "1";
    public static final String JUMP_TYPE_H5 = "2";
    public static final String JUMP_TYPE_LUANCHER= "3";

    public int task_id;
    public Long id;//对应数据库主键
    @Type
    public String type;                 // wake、launch、notification、heads-up
    public String package_name;
    public long user_cycle;
    public long task_cycle;
    public int show_type;
    public int show_time = 5000;        // 悬浮窗显示时间
    public String title;
    public String sub_title;
    public String image;
    public String action_name;
    public String class_name;
    public String deep_link;
    public String jump_type;
    public JsonObject extra;

    public long lastExecuteTime;        // 上一次执行时间(客户端写入)
    public int executeTimes;            // 执行次数(客户端写入)

    public WakeTaskEntity toDBEntity() {
        WakeTaskEntity wakeTaskEntity = new WakeTaskEntity();
        wakeTaskEntity.setTask_id(task_id);
        wakeTaskEntity.setId(id);
        wakeTaskEntity.setType(type);
        wakeTaskEntity.setPackage_name(package_name);
        wakeTaskEntity.setUser_cycle(user_cycle);
        wakeTaskEntity.setTask_cycle(task_cycle);
        wakeTaskEntity.setShow_type(show_type);
        wakeTaskEntity.setShow_time(show_time);
        wakeTaskEntity.setTitle(title);
        wakeTaskEntity.setSub_title(sub_title);
        wakeTaskEntity.setImage(image);
        wakeTaskEntity.setAction_name(action_name);
        wakeTaskEntity.setClass_name(class_name);
        wakeTaskEntity.setDeep_link(deep_link);
        wakeTaskEntity.setJump_type(jump_type);
        if (extra != null) {
            wakeTaskEntity.setExtra(extra.toString());
        }
        wakeTaskEntity.setLastExecuteTime(lastExecuteTime);
        wakeTaskEntity.setExecuteTimes(executeTimes);
        return wakeTaskEntity;
    }

    SilentTask() {
    }

    SilentTask(WakeTaskEntity entity) {
        id = entity.getId();
        task_id = entity.getTask_id();
        type = entity.getType();
        package_name = entity.getPackage_name();
        user_cycle = entity.getUser_cycle();
        task_cycle = entity.getTask_cycle();
        show_type = entity.getShow_type();
        show_time = entity.getShow_time();
        title = entity.getTitle();
        sub_title = entity.getSub_title();
        image = entity.getImage();
        action_name = entity.getAction_name();
        class_name = entity.getClass_name();
        deep_link = entity.getDeep_link();
        extra = new JsonParser().parse(entity.getExtra()).getAsJsonObject();
        lastExecuteTime = entity.getLastExecuteTime();
        executeTimes = entity.getExecuteTimes();
        jump_type = entity.getJump_type();
    }


   public static List<SilentTask> entities2Tasks(List<WakeTaskEntity> entities) {
        List<SilentTask> silentTasks = new ArrayList<>();

        if (entities != null) {
            for (WakeTaskEntity taskEntity : entities) {
                silentTasks.add(new SilentTask(taskEntity));
            }
        }
        return silentTasks;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        SilentTask rhs = (SilentTask) another;
        if (executeTimes != rhs.executeTimes) {
            return executeTimes - rhs.executeTimes;
        }

        if (task_cycle != rhs.task_cycle) {
            return (int) (task_cycle - rhs.task_cycle);
        }

        if (lastExecuteTime != rhs.lastExecuteTime) {
            return (int) (lastExecuteTime - rhs.lastExecuteTime);
        }

        return package_name.compareTo(rhs.package_name);
    }

    @StringDef({Type.WAKE, Type.LAUNCH, Type.NOTIFICATION, Type.HEADS_UP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        String WAKE = "wake";

        String LAUNCH = "launch";

        String NOTIFICATION = "notification";

        String HEADS_UP = "heads-up";
    }

    public void execute() {
        switch (type) {
            case Type.WAKE:
                startService();
                break;
            case Type.LAUNCH:
                // 先存着，手机亮屏启动,调用 startActivity
                Gson gson = new Gson();
                String json = gson.toJson(this);
                FileUtils.writeFile(TASK_PATH, json);
                WaKeLog.w("存储启动页面的任务，解锁屏幕之后再执行：" + json);
                break;
            case Type.NOTIFICATION:
                executeNotification();
                break;
            case Type.HEADS_UP:
                executeHeadsUp();
                break;
        }
    }

    private void startService() {
        try {
            Intent wakeIntent = getWakeIntent();
            LTApplication.instance.startService(wakeIntent);
            DCStat.activeEvent(ReportEvent.event_awake, String.valueOf(task_id), SilentTask.this, null);
        } catch (Exception e) {
            WaKeLog.e("启动<" + package_name + ">唤醒服务出了点问题" + e.toString());
        } finally {
            update();
        }
    }

    public void startActivity() {
        try {
            WaKeLog.w("屏幕解锁启动页面：" + package_name);
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent wakeIntent = getWakeIntent();
                    wakeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LTApplication.instance.startActivity(wakeIntent);
                    DCStat.activeEvent(ReportEvent.event_open, String.valueOf(task_id), SilentTask.this, null);
                }
            }, 3000);

        } catch (Exception e) {
            WaKeLog.e("启动<" + package_name + ">页面出了点问题" + e.toString());
        } finally {
            update();
        }
    }

    private void update() {
        // 更新任务执行时间，执行次数，存储到数据库
        lastExecuteTime = System.currentTimeMillis();
        executeTimes++;
        GlobalParams.getWakeTaskDao().update(toDBEntity());

        SharePreferenceUtil.put(type, System.currentTimeMillis());
        WaKeLog.e("当前类型<" + type + ">(" + package_name + ")执行，保存执行时间:" + System.currentTimeMillis() + ",执行次数：" + executeTimes);
    }

    private Intent getWakeIntent() {
        // 额外参数
        Intent intent = new Intent();
        if (extra != null) {
            try {
                Set<Map.Entry<String, JsonElement>> entries = extra.entrySet();
                for (Map.Entry<String, JsonElement> next : entries) {
                    String key = next.getKey();
                    String value = next.getValue().getAsString();
                    WaKeLog.i("intent<= key>" + key + "intent<= value>" + value);
                    intent.putExtra(key, value);
                }
            } catch (Exception e) {
                WaKeLog.e(e.toString());
            }
        }

        // uri 形式
        if (!TextUtils.isEmpty(deep_link)) {
            if(JUMP_TYPE_H5.equals(jump_type) || TextUtils.isEmpty(jump_type)){//兼容470版本
                if(TextUtils.isEmpty(jump_type))WaKeLog.e("这是470升上来的:h5 way!!");
                WaKeLog.e("openApp:h5 way!");
                intent.setAction("android.intent.action.VIEW");
            }
            intent.setData(Uri.parse(deep_link));
            WaKeLog.e("deep_link===open=>"+deep_link);
            return intent;
        }

        // package + action 形式
        if (!TextUtils.isEmpty(action_name)) {
            intent.setPackage(package_name);
            intent.setAction(action_name);
            return intent;
        }

        // class名称 样式
        // The name of the class inside of <var>pkg</var> that implements the component.  Can not be null.
        if (!TextUtils.isEmpty(class_name)) {
            ComponentName componentName = new ComponentName(package_name, class_name);
            intent.setComponent(componentName);
            return intent;
        }

        // 包名启动应用形式
        if(JUMP_TYPE_LUANCHER.equals(jump_type) || TextUtils.isEmpty(jump_type)){ //兼容470版本
            PackageManager packageManager = LTApplication.instance.getPackageManager();
            intent =packageManager.getLaunchIntentForPackage(package_name);
            if(TextUtils.isEmpty(jump_type))WaKeLog.e("这是470升上来的:only package way!");
            WaKeLog.e("openApp:only package way!");
            return intent;
        }
        return intent;
    }


    private void executeNotification() {
        final NotificationManager notificationManager = (NotificationManager) LTApplication.instance.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(LTApplication.instance);

        builder.setSmallIcon(R.drawable.icon_nodata);
        builder.setPriority(Notification.PRIORITY_MAX);

        Intent clickIntent = new Intent(LTApplication.instance, NotificationClickReceiver.class); //点击通知之后要发送的广播
        clickIntent.putExtra(SilentTask.class.getName(), toDBEntity());
        clickIntent.putExtra(NotificationClickReceiver.WAKE_INTENT, getWakeIntent());
        PendingIntent contentIntent = PendingIntent.getBroadcast(LTApplication.instance, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        builder.setAutoCancel(true);
        builder.setContentTitle(title);
        builder.setContentText(sub_title);

        if (!TextUtils.isEmpty(image)) {
            // 试图加载远程图片 Bitmap
            ImageloaderUtil.loadImageCallBack(LTApplication.instance, image, new SimpleTarget<GlideDrawable>() {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    Bitmap bitmap = BitmapUtils.drawable2Bitmap(resource.getCurrent());
                    builder.setLargeIcon(bitmap);
                    notificationManager.notify(task_id, builder.build());
                    DCStat.activeEvent(ReportEvent.event_show, String.valueOf(task_id), SilentTask.this, null);
                }
            });
        } else {
            builder.setLargeIcon(BitmapFactory.decodeResource(LTApplication.instance.getResources(), R.drawable.icon_nodata));
            // 多个通知栏任务时候，保留多个
            notificationManager.notify(task_id, builder.build());
            DCStat.activeEvent(ReportEvent.event_show, String.valueOf(task_id), SilentTask.this, null);
        }

        update();
    }


    @SuppressLint({"RtlHardcoded", "InflateParams"})
    private void executeHeadsUp() {
        final View view = generateHeadsUpView();
        if (view == null) {
            return;
        }

        final WindowManager mWindowManager = (WindowManager) LTApplication.instance.getSystemService(Context.WINDOW_SERVICE);
        if (mWindowManager == null) {
            return;
        }
        final WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST, 0, PixelFormat.TRANSPARENT
        );

        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = 0;

        // <动画>这里需要花费点精力获取view的高度，写好400
        ViewCompat.setTranslationY(view, -400);
        ValueAnimator animator = ValueAnimator.ofInt(-400, 0);
        animator.setDuration(400);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int curValue = (int) animation.getAnimatedValue();
                ViewCompat.setTranslationY(view, curValue);
            }
        });
        animator.start();


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ViewCompat.isAttachedToWindow(view)) {
                    mWindowManager.removeView(view);
                }
                DCStat.activeEvent(ReportEvent.event_clicked, String.valueOf(task_id), SilentTask.this, null);

                Intent wakeIntent = getWakeIntent();
                if (wakeIntent != null) {
                    try {
                        WaKeLog.e("启动悬浮窗=>包名：" + wakeIntent.getPackage()+"，上下文="+LTApplication.instance+"，deeplink="+wakeIntent.getData());
                        wakeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        LTApplication.instance.startActivity(wakeIntent);
                        DCStat.activeEvent(ReportEvent.event_open, String.valueOf(task_id), SilentTask.this, null);
                    } catch (Exception e) {
                        WaKeLog.e("启动悬浮窗出了点问题" + e.toString());
                    }
                }
            }
        });

        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ViewCompat.isAttachedToWindow(view)) {
                    ValueAnimator animator = ValueAnimator.ofInt(0, -view.getHeight());
                    animator.setDuration(300);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int curValue = (int) animation.getAnimatedValue();
                            ViewCompat.setTranslationY(view, curValue);
                        }
                    });

                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (ViewCompat.isAttachedToWindow(view)) {
                                mWindowManager.removeViewImmediate(view);
                            }
                        }
                    });

                    animator.start();
                }
            }
        }, show_time);

        DCStat.activeEvent(ReportEvent.event_show, String.valueOf(task_id), SilentTask.this, null);
        mWindowManager.addView(view, mWindowParams);

        update();
    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    private View generateHeadsUpView() {
        // 根据show_type 展示不一样的样式
        View view = null;
        switch (show_type) {
            case 1:
                view = LayoutInflater.from(LTApplication.instance).inflate(R.layout.item_wake_style1, null);
                if (!TextUtils.isEmpty(image)) {
                    ImageView imageView = (ImageView) view.findViewById(R.id.iv_icon);
                    Glide.with(LTApplication.instance).load(image).animate(R.anim.item_alpha_in).into(imageView);
                }

                TextView tvContent = (TextView) view.findViewById(R.id.tv_content_text);
                tvContent.setText(title);
                TextView tvSubContent = (TextView) view.findViewById(R.id.tv_content_sub_text);
                tvSubContent.setText(sub_title);
                break;
            case 2:
            case 3:
                view = LayoutInflater.from(LTApplication.instance).inflate(R.layout.item_wake_style2, null);
                final ImageView adsImageView = (ImageView) view.findViewById(R.id.iv_banner);

                if (!TextUtils.isEmpty(image)) {
                    ImageloaderUtil.loadImageCallBack(LTApplication.instance, image, new SimpleTarget<GlideDrawable>() {

                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            Bitmap bitmap = BitmapUtils.drawable2Bitmap(resource.getCurrent());

                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) adsImageView.getLayoutParams();
                            layoutParams.width = ScreenUtils.getScreenWidth(adsImageView.getContext());
                            layoutParams.height = layoutParams.width * bitmap.getHeight() / bitmap.getWidth();
                            WaKeLog.e("Heads-up，w = " + bitmap.getWidth() + ",h = " + bitmap.getHeight() + ",本地宽：" + layoutParams.width + ",本地高：" + layoutParams.height);

                            adsImageView.setLayoutParams(layoutParams);
                            adsImageView.setImageBitmap(bitmap);
                        }
                    });
                }
                break;
        }
        return view;
    }

}
