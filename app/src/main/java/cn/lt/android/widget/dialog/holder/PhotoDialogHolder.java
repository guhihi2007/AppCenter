package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;

import java.io.File;

import cn.lt.android.Constant;
import cn.lt.android.manager.fs.LTDirType;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by ltbl on 2016/3/11.
 */
public class PhotoDialogHolder extends ADialogHolder {
    public static final int ALBUM = 1;
    public static final int PHOTO = 2;
    private Fragment mFragment;

    public PhotoDialogHolder(Fragment fragment) {
        this.mFragment = fragment;
    }

    @Override
    public void fillData(DataInfo info) {

    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        dialog.setContentView(R.layout.photo_dialog_layout);
        initView();
        StatisticsEventData eventData = new StatisticsEventData();
        eventData.setActionType(ReportEvent.ACTION_PAGEVIEW);
        eventData.setPage(Constant.PAGE_MODIFY_AVATOR);
//        eventData.setFrom_page(FromPageUtil.getLastPage(Constant.PAGE_MODIFY_AVATOR));
        DCStat.pageJumpEvent(eventData);
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
                File fos = null;
                try {
                    fos = new File(LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.image) +
                            File.separator + "ltapp.jpg");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Uri u = Uri.fromFile(fos);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                mFragment.startActivityForResult(intent, PHOTO);
                this.closeDialog();
                break;
            case R.id.tv_album:
                Intent intent2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                /* 开启Pictures画面Type设定为image */
                intent2.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                intent2.putExtra("crop", "true");
                // aspectX aspectY 是宽高的比例
                intent2.putExtra("aspectX", 1);
                intent2.putExtra("aspectY", 1);
                // outputX outputY 是裁剪图片宽高
                intent2.putExtra("outputX", 100);
                intent2.putExtra("outputY", 100);
                intent2.putExtra("return-data", true);
                intent2.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                intent2.putExtra("noFaceDetection", true);
                mFragment.startActivityForResult(intent2, ALBUM);
                this.closeDialog();
                break;
            case R.id.tv_cancel:
            case R.id.empty_view:
                this.closeDialog();
                break;
        }
    }
}
