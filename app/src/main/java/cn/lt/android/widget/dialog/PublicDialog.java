package cn.lt.android.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by atian on 2016/2/25.
 *
 * @desc 弹出框
 */
public class PublicDialog extends Dialog {
    private ADialogHolder mHolder;
    private OnDismissListener onDismissListener;
    private boolean KeybackClose = true;
//    private boolean isFirst = true;


    public PublicDialog(Context context, ADialogHolder mHolder) {
        super(context, R.style.ShareDialogStyle);
        this.mHolder = mHolder;
    }

/*    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFirst) {
            isFirst = false;
            mHolder.executeAnimtion(getContext());
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        win.setWindowAnimations(R.style.BottomSheetAnimationStyle);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        this.setCanceledOnTouchOutside(true);
        mHolder.setContentView(this);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(onDismissListener != null) {
                    onDismissListener.onDismiss(dialog);
                }

            }
        });
    }

    public void showDialog(DataInfo info) {
        mHolder.fillData(info);
        super.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && KeybackClose) {
            mHolder.closeDialog();
        }
        if (!KeybackClose) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public PublicDialog setOnTheDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return PublicDialog.this;
    }

    /** 设置点击返回键时不关闭弹窗*/
    public PublicDialog setKeybackNotClose() {
        this.KeybackClose = false;
        return this;
    }
}
