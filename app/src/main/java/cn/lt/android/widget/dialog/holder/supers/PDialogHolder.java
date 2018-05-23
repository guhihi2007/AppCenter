/*
package cn.lt.android.widget.dialog.holder.supers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.yanzhenjie.permission.Rationale;

import cn.lt.android.util.LogUtils;


*/
/**
 * Created by Administrator on 2017/9/22.
 *//*


public abstract class PDialogHolder extends Dialog implements View.OnClickListener {
    protected String mMsg;
    protected Rationale mRationale;
    protected int mRequestCode;
    protected Activity mActivity;
    protected Context mContext;

    public PDialogHolder(@NonNull Activity activity) {
        super(activity, android.R.style.Theme);
        style();
        mActivity = activity;
    }

    public PDialogHolder(@NonNull Activity activity, String msg) {
        super(activity, android.R.style.Theme);
        style();
        this.mMsg = msg;
        mActivity = activity;
    }

//    public PDialogHolder(@NonNull Context context, Rationale rationale) {
//        super(context, android.R.style.Theme);
//        style();
//        this.mRationale = rationale;
//        mContext = context;
//    }
//
//    public PDialogHolder(@NonNull Context context, Rationale rationale, String msg) {
//        super(context, android.R.style.Theme);
//        style();
//        this.mMsg = msg;
//        this.mRationale = rationale;
//        mContext = context;
//    }

    public PDialogHolder(Activity activity, int requestCode) {
        super(activity, android.R.style.Theme);
        style();
        this.mRequestCode = requestCode;
        this.mActivity = activity;
    }

    public PDialogHolder(Activity activity, int requestCode, String msg) {
        super(activity, android.R.style.Theme);
        style();
        this.mMsg = msg;
        this.mRequestCode = requestCode;
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutID());
        findViews();
        setMsg();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mActivity != null) {
                dismiss();
                mActivity.finish();
            }
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected abstract void findViews();

    protected abstract int layoutID();

    protected void setMsg() {
        String str;
        if (TextUtils.isEmpty(mMsg)) {
            str = "应用市场需要获取存储空间和设备信息权限，为您推荐最适合的应用，保障下载更新顺畅与设备安全。";
        } else {
            str = "应用市场需要获取" + mMsg + "权限，为您推荐最适合的应用，保障下载更新顺畅与设备安全。";
        }
        Spannable spannable = new SpannableString(str);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF8800")), 8, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (TextUtils.isEmpty(mMsg)) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF8800")), 13, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        msgTextView().setText(spannable);
    }

    protected abstract TextView msgTextView();

    private void style() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

}
*/
