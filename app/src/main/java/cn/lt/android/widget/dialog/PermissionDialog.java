/*
package cn.lt.android.widget.dialog;

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
import cn.lt.appstore.R;
*/
/**
 * Created by Administrator on 2017/9/22.
 *//*


public class PermissionDialog extends Dialog implements View.OnClickListener {
    private Rationale mRationale;
    private String mMsg;
    private View empty_view, mlayout;
    private TextView mTitle, mContent, mBtn;

    public PermissionDialog(Context context, Rationale rationale) {
        super(context, android.R.style.Theme);
        style();
        mRationale = rationale;
    }

    public PermissionDialog(Context context, Rationale rationale, String msg) {
        super(context, android.R.style.Theme);
        style();
        mRationale = rationale;
        mMsg = msg;
    }

    private void style() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_dialog_layout);
        findViews();
    }

    private void findViews() {
        mlayout = findViewById(R.id.layout_view);
        empty_view = findViewById(R.id.blank_view);
        mTitle = (TextView) findViewById(R.id.title_tv);
        mContent = (TextView) findViewById(R.id.mContent);
        mBtn = (TextView) findViewById(R.id.authority_tv);
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
        mRationale.resume();
        dismiss();
    }
}
*/
