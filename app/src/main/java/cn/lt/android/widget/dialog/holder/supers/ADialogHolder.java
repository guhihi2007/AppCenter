package cn.lt.android.widget.dialog.holder.supers;

import android.app.Dialog;
import android.view.View;

import cn.lt.android.widget.dialog.DataInfo;

/***
 * Created by dxx on 2016/3/9.
 */
public abstract class ADialogHolder implements View.OnClickListener {
    protected View mView;
    protected Dialog mDialog;

    public abstract void fillData(DataInfo info);

    public abstract void setContentView(Dialog dialog);

    public void closeDialog() {
        if (mDialog != null) {
            mDialog.cancel();
            mDialog = null;
        }
    }

}
