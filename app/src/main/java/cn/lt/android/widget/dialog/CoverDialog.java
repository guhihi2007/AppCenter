package cn.lt.android.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import cn.lt.appstore.R;

/**
 * @author chengyong
 * @time 2016/8/15 16:28
 * @des  蒙层  :用户首次开启apk时的提示操作
 */
public class CoverDialog extends Dialog {
    public CoverDialog(Context context) {
        super(context, R.style.CoverDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cover_layout);
        this.setCanceledOnTouchOutside(true);
        findViewById(R.id.cover_dialog_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消
                dismiss();
            }
        });


    }
}
