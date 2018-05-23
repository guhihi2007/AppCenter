package cn.lt.android.widget.dialog.holder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;


/**
 * Created by Administrator on 2017/9/22.
 */

public class PermissionNoticeDialog extends Dialog implements View.OnClickListener {
    private View empty_view, mlayout;
    private TextView mTitle, mContent, mBtn, mNotice;
    private Activity activity;
    private int requestCode;
    private String mMsg;

    public PermissionNoticeDialog(Activity activity, int requestCode) {
        super(activity, android.R.style.Theme);
        style();
        this.activity = activity;
        this.requestCode = requestCode;
    }

    private void style() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public PermissionNoticeDialog(Activity activity, int requestCode, String msg) {
        super(activity, android.R.style.Theme);
        style();
        this.activity = activity;
        this.requestCode = requestCode;
        mMsg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_notice_dialog);
        findViews();
    }

    private void findViews() {
        mlayout = findViewById(R.id.layout_View);
        empty_view = findViewById(R.id.blank_View);
        mTitle = (TextView) findViewById(R.id.title_Tv);
        mContent = (TextView) findViewById(R.id.Content);
        mBtn = (TextView) findViewById(R.id.set_Tv);
//        mNotice = (TextView) findViewById(R.id.notice);
        mBtn.setOnClickListener(this);
        setMsg();
    }

    private void setMsg() {
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
        mContent.setText(spannable);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        LogUtils.e("gpp", "setBtn--------" + requestCode);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, requestCode);
        dismiss();
    }
}
