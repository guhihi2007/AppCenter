package cn.lt.android.main.requisite;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import cn.lt.android.Constant;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.main.requisite.state.IState;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.appstore.R;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;

/**
 * 精选必玩 展示框；
 *
 * @author dxx
 */
public class RequisiteActivity extends Dialog {

    private IState mState;

    private Context mContext;

//    private RequisiteManger mManger;

    public RequisiteActivity(Context context, IState state) {
        super(context, android.R.style.Theme);
        this.mState = state;
        this.mContext = context;
//        this.mManger = new RequisiteManger(mContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//设置状态栏透明色
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        try {
            mState.setContentView(this);
            this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            this.getWindow().setWindowAnimations(R.style.BottomSheetAnimationStyle);
            this.setCanceledOnTouchOutside(false);


        } catch (Exception e) {
            e.printStackTrace();
            cancel();
        }
    }

    /**
     * 显示该界面；
     *
     * @param result 结果；
     */
    public void startActivity(DataInfo result) {
        try {
            UpdateUtil.saveSpreadDialogThisTime(mContext, System.currentTimeMillis());//保存第一次弹窗推广弹出时间
            PreferencesUtils.putBoolean(mContext,  Constant.SELECTION_PLAY_SHOWED, true);
//            mManger.delData();
            if (result != null) {
                BaseBean bean = (BaseBean) result.getmData();
                mState.fillData(bean);
                show();
                if (bean != null) {
                    StatisticsEventData eventData = new StatisticsEventData();
                    eventData.setActionType(ReportEvent.ACTION_PAGEVIEW);
                    if (PresentType.necessary_apps.presentType.equals(bean.getLtType())) {
                        eventData.setPage(Constant.PAGE_BIBEI);
                    } else {
                        eventData.setPage(Constant.PAGE_SPREAD);
                    }
                    DCStat.pageJumpEvent(eventData);//页面统计
                }
            }
        } catch (Exception e) {
            LogUtils.i("GOOD", e.getMessage() + ",Requisite: " + result);
            e.printStackTrace();
            cancel();
        }
    }

    @Override
    public void cancel() {
        if (this.isShowing()) {
            EventBus.getDefault().post("refreshPage");
            super.cancel();
        }
    }

    /**
     * 返回键分发；
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // return true;
        }
        return super.dispatchKeyEvent(event);
    }


    public static class RequisiteItem<T> {

        private T mGame;
        /**
         * 是否被选中，
         */
        private boolean isChecked;

        public RequisiteItem() {
        }

        public RequisiteItem(T game) {
            this.mGame = game;
        }

        public RequisiteItem(T game, boolean isInProgress) {
            this.mGame = game;
            if (isInProgress) {
                this.isChecked = false;
            } else {
                this.isChecked = true;
            }
        }

        public T getmGame() {
            return mGame;
        }

        public void setmGame(T mGame) {
            this.mGame = mGame;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }

        public T getGameDetail() {
            return mGame;
        }

    }
}
