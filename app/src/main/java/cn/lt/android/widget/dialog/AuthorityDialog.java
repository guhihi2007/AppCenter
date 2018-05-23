/*
package cn.lt.android.widget.dialog;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.permission.Rationale;

import cn.lt.android.widget.dialog.holder.supers.PDialogHolder;
import cn.lt.appstore.R;

*/
/**
 * Created by Administrator on 2017/9/22.
 *//*


public class AuthorityDialog extends PDialogHolder {
    private View empty_view, mlayout;
    private TextView mTitle, mContent, mBtn;

    public AuthorityDialog(@NonNull Activity activity) {
        super(activity);
    }

    public AuthorityDialog(@NonNull Activity activity, String msg) {
        super(activity, msg);
    }
//
//    public AuthorityDialog(@NonNull Context context, Rationale rationale) {
//        super(context, rationale);
//    }
//    public AuthorityDialog(@NonNull Context context, Rationale rationale, String msg) {
//        super(context, rationale, msg);
//    }

    @Override
    protected void findViews() {
        mlayout = findViewById(R.id.layout_view);
        empty_view = findViewById(R.id.blank_view);
        mTitle = (TextView) findViewById(R.id.title_tv);
        mContent = (TextView) findViewById(R.id.mContent);
        mBtn = (TextView) findViewById(R.id.authority_tv);
        mBtn.setOnClickListener(this);
    }

    @Override
    protected int layoutID() {
        return R.layout.permission_dialog_layout;
    }

    @Override
    protected TextView msgTextView() {
        return mContent;
    }

    @Override
    public void onClick(View v) {
        mRationale.resume();
        dismiss();
    }
}
*/
