package cn.lt.android.wake;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.db.WakeTaskEntity;
import cn.lt.android.db.WakeTaskEntityDao;
import cn.lt.android.entity.Configure;
import cn.lt.android.entity.SilentTask;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.bean.WakeTaskResult;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.PersistUtil;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.framework.util.PreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chon on 2017/11/18.
 * What? How? Why?
 */

public class SilentManager {

    private static final String LAST_FETCH_TASK_TIME = "last_Fetch_task_Time";
    private static final String FETCH_TASK_CYCLE = "fetch_task_cycle";
    private static final String TASK_EXECUTE_TIME = "_task_execute_time";

    // 默认延时时间
    private static final long DEFAULT_DELAY_TIME = 0;

    private SilentManager() {
    }

    private static class SingleTonBuilder {
        private static SilentManager singleTon = new SilentManager();
    }

    public static SilentManager getInstance() {
        return SingleTonBuilder.singleTon;
    }

    // 特定类型任务执行的定时器
    private Map<String, Handler> handlerMap = new HashMap<>();

    // 获取任务列表的定时器
    private Handler fetchTaskHandler = new Handler();

    public void onCreate() {
        WaKeLog.i("SilentManager onCreate");
        long lastFetchTime = (long) SharePreferenceUtil.get(LAST_FETCH_TASK_TIME, 0L);
        long fetchCycle = (long) SharePreferenceUtil.get(FETCH_TASK_CYCLE, 0L);
        long delayTime = calcDelayTime(lastFetchTime, fetchCycle);
        WaKeLog.w("上次拉取时间：" + lastFetchTime + "，任务拉取周期：" + fetchCycle + "，" + delayTime + "ms 后拉取任务列表");

        if (delayTime != DEFAULT_DELAY_TIME && isWakeSwitchOpen()) {
            WaKeLog.i("拉活开关打开，执行拉活流程");
            doNext();
        }

        // 定时获取任务列表
        fetchTaskHandler.postDelayed(fetchTaskRunnable, delayTime);

    }

    private long calcDelayTime(long lastTime, long cycle) {
        long delayTime;
        if (lastTime == 0) {
            // 从未拉取任务，立即拉取，且定时拉取周期执行
            // 上一次此种类型任务执行时间 = 0，立马执行，且定时用户周期执行
            delayTime = DEFAULT_DELAY_TIME;
        } else if (Math.abs(System.currentTimeMillis() - lastTime) >= cycle) {
            // 当前时间 - 上一次拉取任务列表时间 > 用户周期，立马执行，且定时拉取周期执行
            // 当前时间 - 上一次此种类型任务执行时间 > 用户周期，立马执行，且定时用户周期执行
            delayTime = DEFAULT_DELAY_TIME;
        } else {
            // 当前时间 - 上一次拉取任务列表时间 < 用户周期，(用户周期 - (当前时间 - 上一次执行时间)) 后执行，且定时拉取周期执行
            // 当前时间 - 上一次此种类型任务执行时间 < 用户周期，(用户周期 - (当前时间 - 上一次执行时间)) 后执行，且定时用户周期执行
            delayTime = cycle - Math.abs(System.currentTimeMillis() - lastTime);
        }

        return delayTime;
    }

    private boolean isWakeSwitchOpen() {
//        Configure configure = (Configure) PersistUtil.readData(Constant.PULLLIVE_STATUS);
//        String state = configure == null ? Constant.STATUS_CLOSE : configure.getStatus();

        boolean state = PreferencesUtils.getBoolean(LTApplication.instance,Constant.PULLLIVE_STATUS);

        WaKeLog.i("拉活开关状态：" + state);
        return state;
    }

    private Runnable fetchTaskRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isWakeSwitchOpen()) {
                WaKeLog.i("拉活开关关闭，5分钟之后尝试拉取");
                fetchTaskHandler.postDelayed(fetchTaskRunnable, 300000);
                return;
            }

            // 将之前的定时器全部取消，接着重置
            if (handlerMap != null) {
                for (Map.Entry<String, Handler> handlerEntry : handlerMap.entrySet()) {
//                    WaKeLog.e("销毁 mTaskHandler222 = " + handlerEntry.getValue());//第一时间销毁避免可能的同用户hander的异步问题
                    handlerEntry.getValue().removeCallbacksAndMessages(null);
                }
                handlerMap.clear();
            }

            // 获取任务列表
            NetWorkClient.getHttpClient().setHostType(HostType.NORMAL_CENTER).setCallback(new Callback<WakeTaskResult<List<SilentTask>>>() {
                @Override
                public void onResponse(Call<WakeTaskResult<List<SilentTask>>> call, final Response<WakeTaskResult<List<SilentTask>>> response) {
                    List<SilentTask> tasks = response.body().tasks;
                    // 在获取任务列表成功回调中，保存拉取周期，保存上次拉取时间
                    final long pullCycle = response.body().pull_cycle;
//                    WaKeLog.i("服务端返回拉取周期：" + pullCycle);
                    SharePreferenceUtil.put(FETCH_TASK_CYCLE, pullCycle);
                    SharePreferenceUtil.put(LAST_FETCH_TASK_TIME, System.currentTimeMillis());
                    if(null==tasks)return;

                    GlobalParams.getWakeTaskDao().deleteAll();
                    for(SilentTask task:tasks){
                        DCStat.activeEvent(ReportEvent.event_received, String.valueOf(task.task_id), null, null);
                    }
                    Observable.from(tasks)
                            .subscribeOn(Schedulers.io())
                            .filter(new Func1<SilentTask, Boolean>() {
                                @Override
                                public Boolean call(SilentTask silentTask) {
                                    return AppUtils.isInstalled(silentTask.package_name);
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<SilentTask>() {
                                @Override
                                public void onCompleted() {
                                    WaKeLog.e("服务端任务过滤完毕，" + pullCycle + " ms后再次拉取任务列表");
                                    fetchTaskHandler.postDelayed(fetchTaskRunnable, pullCycle);
                                    doNext();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    fetchTaskHandler.postDelayed(fetchTaskRunnable, 60000);
                                    WaKeLog.e("服务端任务过滤 onError，" + e.getMessage());
                                    doNext();
                                }

                                @Override
                                public void onNext(SilentTask silentTask) {
                                    WaKeLog.e("符合条件的加入:" + silentTask.package_name);
                                    // 插入数据库 字段完整
                                    silentTask.id = (long) silentTask.task_id;
                                    GlobalParams.getWakeTaskDao().insert(silentTask.toDBEntity());
                                }
                            });
                }

                @Override
                public void onFailure(Call<WakeTaskResult<List<SilentTask>>> call, Throwable t) {
                    WaKeLog.i("向服务端请求任务失败~~~" + t.toString());
                    fetchTaskHandler.postDelayed(fetchTaskRunnable, 60000);
                    doNext();
                }
            }).bulid().requestWakeTaskList();
        }
    };

    private void doNext() {
        // 从数据库查询任务列表 任务列表过滤(根据包名)
        List<WakeTaskEntity> list = GlobalParams.getWakeTaskDao().queryBuilder().list();
        if (list == null || list.size() == 0) {
            WaKeLog.e("本地数据库没有缓存的任务，不执行");
            return;
        }

        List<SilentTask> tasks = SilentTask.entities2Tasks(list);
        // 任务列表分类 Map<type,List<SilentTask>>
        Map<String, List<SilentTask>> taskMap = new HashMap<>();
        for (SilentTask task : tasks) {
            String type = task.type;

            // 指定类型的任务
            List<SilentTask> silentTasks = taskMap.get(type);
            if (silentTasks == null) {
                silentTasks = new ArrayList<>();
                taskMap.put(type, silentTasks);
            }
            silentTasks.add(task);
        }

        for (String type : taskMap.keySet()) {
            // 指定类型的所有任务
            List<SilentTask> silentTasks = taskMap.get(type);
            if (silentTasks == null || silentTasks.size() == 0) {
                // 无指定类型之任务
                WaKeLog.e("当前类型<" + type + ">,没有任务，跳过");
                continue;
            }

            // 上一次此种类型任务执行时间
            long lastExecuteTime = (long) SharePreferenceUtil.get(type, 0L);
            long userCycle = silentTasks.get(0).user_cycle;
            long delayTime = calcDelayTime(lastExecuteTime, userCycle);
            WaKeLog.w("当前类型<" + type + ">，任务 size = " + silentTasks.size()
                    + "，用户周期是：" + userCycle + "，上次执行时间：" + lastExecuteTime
                    + "," + delayTime + "ms后执行任务");

            // 分类型创建 类型用户周期定时器(Handler) 执行定时器
            Handler handler = handlerMap.get(type);
            if (handler == null) {
                SharePreferenceUtil.put(type + TASK_EXECUTE_TIME, 0L);
                handler = new Handler();
//                WaKeLog.i("创建 mTaskHandler111 = " + handler);
                handler.postDelayed(new TaskRunnable(type, handler), delayTime);
                handlerMap.put(type, handler);
            }
        }
    }

    class TaskRunnable implements Runnable {
        String taskType;
        Handler mTaskHandler;

        TaskRunnable(String taskType, Handler handler) {
            this.taskType = taskType;
            this.mTaskHandler = handler;
        }

        @Override
        public void run() {
            List<WakeTaskEntity> entities = GlobalParams
                    .getWakeTaskDao()
                    .queryBuilder()
                    .where(WakeTaskEntityDao.Properties.Type.eq(taskType))
                    .list();

            if (entities == null || entities.size() == 0) {
                WaKeLog.e("当前类型<" + taskType + ">,没有任务了,轮询退出");
                return;
            }

            // 上一次这行代码执行时间
            final String KEY = taskType + TASK_EXECUTE_TIME;
            long lastExecuteTime = (long) SharePreferenceUtil.get(KEY, 0L);
            long userCycle = entities.get(0).getUser_cycle();
            if (Math.abs(System.currentTimeMillis() - lastExecuteTime) < userCycle && lastExecuteTime != 0) {
                // 用户周期还没到
                WaKeLog.e("当前类型<" + taskType + "> 用户周期还没到，不需要执行");
                return;
            }
            SharePreferenceUtil.put(KEY, System.currentTimeMillis());

            // 获取任务列表（任务周期到的，且是已安装）
            TreeSet<SilentTask> suitableSilentTasks = new TreeSet<>();
            for (SilentTask next : SilentTask.entities2Tasks(entities)) {
                if (!AppUtils.isInstalled(next.package_name)) {
                    WaKeLog.e("当前包<" + next.type + ">(" + next.package_name + ")用户已卸载，跳过");
                    continue;
                }

                if (System.currentTimeMillis() - next.lastExecuteTime >= next.task_cycle) {
                    // 任务周期到
                    WaKeLog.i("当前包<" + next.type + ">(" + next.package_name + ")任务周期到");
                    suitableSilentTasks.add(next);
                } else {
                    WaKeLog.i("当前包<" + next.type + ">(" + next.package_name + ")任务周期还没打到");
                }
            }

            if (suitableSilentTasks.size() > 0) {
                final SilentTask suitableSilentTask = suitableSilentTasks.first();
                WaKeLog.e("根据策略选择包<" + suitableSilentTask.type + ">(" + suitableSilentTask.package_name + ")执行，用户周期：" + suitableSilentTask.user_cycle);

                NetWorkClient.getHttpClient().setHostType(HostType.NORMAL_CENTER).setCallback(new Callback<WakeTaskResult>() {

                    @Override
                    public void onResponse(Call<WakeTaskResult> call, Response<WakeTaskResult> response) {
                        WaKeLog.i("验证拉活任务是否有效<" + suitableSilentTask.type + ">(" + suitableSilentTask.package_name + ")：" + response.body().is_valid);
                        if ("available".equals(response.body().is_valid)) {
                            // available 有效才能执行
                            suitableSilentTask.execute();
                            mTaskHandler.postDelayed(TaskRunnable.this, suitableSilentTask.user_cycle);
                        } else {
                            // unavailable 无效删除任务,立马执行下一个任务
                            GlobalParams.getWakeTaskDao().delete(suitableSilentTask.toDBEntity());
                            mTaskHandler.postDelayed(TaskRunnable.this, 0);
                        }

//                        WaKeLog.w("执行 mTaskHandler333 = " + mTaskHandler);
                    }

                    @Override
                    public void onFailure(Call<WakeTaskResult> call, Throwable t) {
                        WaKeLog.e("验证拉活任务<" + suitableSilentTask.type + ">(" + suitableSilentTask.package_name + ")是否有效 onFailure:：" + t.getMessage());
                        // 网络请求失败，等到下一个用户周期再执行，且当前任务不执行
                        mTaskHandler.postDelayed(TaskRunnable.this, suitableSilentTask.user_cycle);
                    }
                }).bulid().checkTaskIsAvailable(String.valueOf(suitableSilentTask.task_id));

//                WaKeLog.i("当前类型<" + taskType + ">,任务总个数：" + entities.size() + "，满足包未被删除且任务周期到的任务个数：" + suitableSilentTasks.size());
            } else {
                WaKeLog.i("当前类型<" + taskType + ">,没有任务周期到的任务,等待下个用户周期: " + userCycle);
                mTaskHandler.postDelayed(TaskRunnable.this, userCycle);
            }

        }
    }

    public void onDestroy() {
        WaKeLog.e("silentManager onDestroy");
        if (handlerMap != null) {
            for (Map.Entry<String, Handler> handlerEntry : handlerMap.entrySet()) {
                handlerEntry.getValue().removeCallbacksAndMessages(null);
            }
            handlerMap.clear();
        }
        if (fetchTaskHandler != null) {
            fetchTaskHandler.removeCallbacksAndMessages(null);
        }
    }

}
