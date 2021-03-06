/*
package cn.lt.android.widget.dialog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.permission.AndPermission;

import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.dialog.holder.supers.PDialogHolder;
import cn.lt.appstore.R;

*/
/**
 * Created by Administrator on 2017/9/22.
 *//*


public class SetPermissionDialog extends PDialogHolder {
    private View empty_view, mlayout;
    private TextView mTitle, mContent, mBtn, mNotice;

    public SetPermissionDialog(Activity activity, int requestCode) {
        super(activity, requestCode);
    }

    public SetPermissionDialog(Activity activity, int requestCode, String msg) {
        super(activity, requestCode, msg);
    }

    @Override
    protected void findViews() {
        mlayout = findViewById(R.id.layout_View);
        empty_view = findViewById(R.id.blank_View);
        mTitle = (TextView) findViewById(R.id.title_Tv);
        mContent = (TextView) findViewById(R.id.Content);
        mBtn = (TextView) findViewById(R.id.set_Tv);
//        mNotice = (TextView) findViewById(R.id.notice);
        mBtn.setOnClickListener(this);
    }

    @Override
    protected int layoutID() {
        return R.layout.permission_notice_dialog;
    }

    @Override
    protected TextView msgTextView() {
        return mContent;
    }

    @Override
    protected void setMsg() {
        String str;
        if (TextUtils.isEmpty(mMsg)) {
            str = "由于获取不了存储空间和设备信息权限，无法正常使用应用市场。";
        } else {
            str = "由于获取不了" + mMsg + "权限，无法正常使用应用市场。";
        }
        Spannable spannable = new SpannableString(str);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF8800")), 6, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (TextUtils.isEmpty(mMsg)) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF8800")), 11, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        msgTextView().setText(spannable);
    }

    @Override
    public void onClick(View v) {
        LogUtils.e("gpp", "setBtn--------" + mRequestCode);
        if (mRequestCode == 100) {
            AndPermission.with(mActivity).requestCode(mRequestCode).permission(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)//.rationale(retionalListenser)
                    .start();
            dismiss();
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
        intent.setData(uri);
        mActivity.startActivityForResult(intent, mRequestCode);
        dismiss();
    }
}
*/
