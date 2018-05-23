package cn.lt.android.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.bean.wdj.AdsImageBean;
import cn.lt.android.entity.ClickTypeDataBean;
import cn.lt.android.install.AdMd5;
import cn.lt.android.main.entrance.Jumper;
import cn.lt.android.main.entrance.data.ClickType;
import cn.lt.android.util.CallServer;
import cn.lt.android.util.LogUtils;

public class LoadingImgWorker {
    private static final String PRE_KEY_CLICK_TYP = "click_type";
    private static final String PRE_KEY_PAGE = "page";
    private static final String PRE_KEY_URL = "url";
    private static final String PRE_KEY_TITLE = "title";
    private static final String PRE_KEY_ID = "id";

    // 玩咖的数据
    private static final String PRE_KEY_REPORT_DATA = "reportData";
    private static final String PRE_KEY_REPORT_TYPE = "reportType";

    static final String PRE_KEY_IMG_MD5 = "img_md5";
    private static LoadingImgWorker instance;
    private SharedPreferences loadingImgSp;// 保存loading图片链接的sharedPreferences
    private SharedPreferences.Editor loadingImgEditor;// 保存loading图片链接的editor
    private AdsImageBean mBean;

    private LoadingImgWorker() {
        loadingImgSp = LTApplication.shareApplication().getSharedPreferences("loadingImg", Context.MODE_PRIVATE);
        loadingImgEditor = loadingImgSp.edit();
    }

    public static LoadingImgWorker getInstance() {
        if (instance == null) {
            synchronized (LoadingImgWorker.class) {
                if (instance == null) {
                    instance = new LoadingImgWorker();
                }
            }
        }
//        instance.mContext = context;
        return instance;
    }

    public AdsImageBean getmBean() {
        return mBean;
    }

    public void setmBean(AdsImageBean mBean) {
        this.mBean = mBean;
        String imgUrl = mBean.getImage();
        mBean.setImage(GlobalConfig.combineImageUrl(imgUrl));
        saveTopReferenceFile();
    }

    private void saveTopReferenceFile() {
        loadingImgEditor.putString(PRE_KEY_CLICK_TYP, mBean.getClick_type());
        switch (ClickType.valueOf(mBean.getClick_type())) {
            case h5:
                saveH5();
                break;
            case app_info:
            case topic_info:
            case tab_topic_info:
            case list:
                saveId();
                break;
            case page:
                savePage();
                break;
        }

    }

    private void savePage() {
        loadingImgEditor.putString(PRE_KEY_PAGE, mBean.getData().getPage());
    }

    private void saveH5() {
        loadingImgEditor.putString(PRE_KEY_URL, mBean.getData().getUrl());
        loadingImgEditor.putString(PRE_KEY_TITLE, mBean.getData().getTitle());
        loadingImgEditor.commit();
    }

    private void saveId() {
        loadingImgEditor.putInt(PRE_KEY_ID, Integer.valueOf(mBean.getData().getId()));
        loadingImgEditor.putString(PRE_KEY_TITLE, mBean.getData().getTitle());
        if (mBean.getData().getReportData() != null) {
            loadingImgEditor.putString(PRE_KEY_REPORT_DATA, mBean.getData().getReportData().toString());
        }
        loadingImgEditor.commit();
    }

    /***
     * 下载启动页图片并保存到本地，以备下次启动更换
     */
    public void downloadImg() {
        if (!TextUtils.isEmpty(mBean.getImage())) {
            final String oldImgMd5 = loadingImgSp.getString(PRE_KEY_IMG_MD5, "");
            final String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TT_AppCenter/image/";
            final String newImgMd5 = AdMd5.MD5(mBean.getImage());
            final String oldImgpath = folder + oldImgMd5;
            String newPath = folder + newImgMd5;
            final File oldImgFile = new File(oldImgpath);
            if (!newImgMd5.equals(oldImgMd5) || !oldImgFile.exists()) {
                judgeMd5IsExists(oldImgMd5, newPath);
                Request<Bitmap> request = NoHttp.createImageRequest(mBean.getImage());
                if (request != null)
                    CallServer.getRequestInstance().add(0, request, new OnResponseListener<Bitmap>() {
                        @Override
                        public void onStart(int what) {
                        }

                        @Override
                        public void onSucceed(int what, Response<Bitmap> response) {
                            Bitmap bt = response.get();
                            LogUtils.i("LoadingImage", "图片下载成功");
                            saveLoadingImg(folder, newImgMd5, bt);
                            // 下载之后保存md5
                            loadingImgEditor.putString(PRE_KEY_IMG_MD5, newImgMd5);
                            loadingImgEditor.commit();
                            if (!newImgMd5.equals(oldImgMd5) && oldImgFile.exists()) {
                                oldImgFile.delete();
                                LogUtils.i("LoadingImage", "新的图片下载成功，与newImage的md5不相同的oldImgFile删除掉了");
                            }
                        }

                        @Override
                        public void onFailed(int what, Response<Bitmap> response) {
                            LogUtils.i("LoadingImage", "图片下载失败");
//                            loadingImgEditor.putString(PRE_KEY_IMG_MD5, newImgMd5);
//                            loadingImgEditor.commit();
                        }

                        @Override
                        public void onFinish(int what) {

                        }
                    });
            }
        }
        saveH5();
    }

    /**
     * 保存方法
     */
    public void saveLoadingImg(String forlderName, String fileName, Bitmap mBitmap) {
        File file = new File(forlderName, fileName);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            LogUtils.i("LoadingImage", "图片保存成功");
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 解决app卸载后，重装APP新的启动图片下载不了
     */
    private void judgeMd5IsExists(String oldImgMd5, String newPath) {
        if (TextUtils.isEmpty(oldImgMd5)) {
            LogUtils.i("nidaye", "oldImgMd5为空哦！");
            File newImgFile = new File(newPath);
            if (newImgFile.exists()) {
                LogUtils.i("nidaye", "newPath的file居然存在哦！干掉它！");
                newImgFile.delete();
            } else {
                LogUtils.i("nidaye", "newPath的file不存在!");
            }
        }

    }


    void toJump(Context context) {
        int id = 0;
        String title = "";
        String page = null;
        String url = "";
        ClickTypeDataBean bean = new ClickTypeDataBean();
        ClickType type = null;
        if (mBean == null) {
            type = ClickType.valueOf(loadingImgSp.getString(PRE_KEY_CLICK_TYP, ""));
            switch (type) {
                case h5:
                    bean.setTitle(loadingImgSp.getString(PRE_KEY_TITLE, ""));
                    bean.setUrl(loadingImgSp.getString(PRE_KEY_URL, ""));
                    break;
                case app_info:
                    bean.setId(loadingImgSp.getInt(PRE_KEY_ID, 0) + "");
                    break;
                case topic_info:
                    bean.setId(loadingImgSp.getInt(PRE_KEY_ID, 0) + "");
                    break;
                case tab_topic_info:
                    bean.setId(loadingImgSp.getInt(PRE_KEY_ID, 0) + "");
                    break;
                case page:
                    bean.setPage(loadingImgSp.getString(PRE_KEY_PAGE, null));
                    break;
                case list:
                    bean.setId(loadingImgSp.getInt(PRE_KEY_ID, 0) + "");
                    bean.setTitle(loadingImgSp.getString(PRE_KEY_TITLE, ""));
                    break;
            }
        } else {
            ClickTypeDataBean entity = mBean.getData();
            type = ClickType.valueOf(mBean.getClick_type());
            switch (type) {
                case h5:
                    bean.setTitle(entity.getTitle());
                    bean.setUrl(entity.getUrl());
                    break;
                case app_info:
                    bean.setId(entity.getId());
                    break;
                case topic_info:
                    bean.setId(entity.getId());
                    break;
                case tab_topic_info:
                    bean.setId(entity.getId());
                    break;
                case page:
                    bean.setPage(entity.getPage());
                    break;
                case list:
                    bean.setId(entity.getId());
                    bean.setTitle(entity.getTitle());
                    break;
            }
            new Jumper().jumper(context, type, entity, type.toString(), false);
        }
    }
}
