package cn.lt.android.widget.dialog.holder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Time;
import android.view.View;

import java.io.File;

import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng on 2016/3/11.
 */
public class TakePhotoDialogHolder extends ADialogHolder {
    public static final int PHOTO_URL_RESULT = 10;
    public static final int TAKE_PICTURE = 11;
    public static Uri uri;
    private Activity activity;

    public TakePhotoDialogHolder(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void fillData(DataInfo info) {

    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        dialog.setContentView(R.layout.photo_dialog_layout);
        initView();
    }

    private void initView() {
        mView = mDialog.findViewById(R.id.ll_moveView);
        mDialog.findViewById(R.id.tv_album).setOnClickListener(this);
        mDialog.findViewById(R.id.tv_take_photo).setOnClickListener(this);
        mDialog.findViewById(R.id.tv_cancel).setOnClickListener(this);
        mDialog.findViewById(R.id.empty_view).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_take_photo:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                uri = getDateUri();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                activity.startActivityForResult(cameraIntent, TAKE_PICTURE);
                this.closeDialog();
                break;
            case R.id.tv_album:
                Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
                albumIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                activity.startActivityForResult(albumIntent, PHOTO_URL_RESULT);
                this.closeDialog();
                break;
            case R.id.tv_cancel:
            case R.id.empty_view:
                this.closeDialog();
                break;
        }
    }

    public static Uri getDateUri() {
        Time t = new Time();
        t.setToNow();
        int year = t.year;
        int month = t.month;
        int day = t.monthDay;
        int hour = t.hour;
        int minute = t.minute;
        int second = t.second;
        String filename = "" + year + month + day + hour + minute + second;
        // 创建文件
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "appcenter";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        File filee = new File(path + "/" + filename + ".jpg");
        // 格式化为Uri
        Uri fileImageFilePath = Uri.fromFile(filee);
        return fileImageFilePath;
    }

}
