package cn.lt.android.main.personalcenter.feedback;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.main.Item;
import cn.lt.android.main.personalcenter.model.FeedBackBean;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.TakePhotoDialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.util.BitmapUtils;
import cn.lt.framework.util.DeviceUtils;
import cn.lt.framework.util.NetWorkUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zhengweijian on 15/8/24.
 */
public class FeedBackActivity extends BaseAppCompatActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, FeedbackAdapter.FeedBackCallBack {
    /**
     * 左侧文本
     */
    public static final int MSG_LEFT_TEXT   = 0;
    /**
     * 右侧文本
     */
    public static final int MSG_RIGHT_TEXT  = 1;
    /**
     * 左侧图片
     */
    public static final int MSG_LEFT_IMAGE  = 2;
    /**
     * 右侧图片
     */
    public static final int MSG_RIGHT_IMAGE = 3;
    /**
     * 系统默认样式
     */
    public static final int MSG_DEFAULT   = 4;

    /**
     * 发送状态，发送成功
     */
    public static final int SEND_SUCCESS = 0;
    /**
     * 发送失败
     */
    public static final int SEND_FAILED  = 1;
    /**
     * 正在发送
     */
    public static final int SEND_ING     = 2;
    private List<Item<FeedBackBean>> itemList = new ArrayList<>();

    private ActionBar          mActionBar;
    private TextView           mAppVersion;
    private TextView           mAndroidVersion;
    private TextView           mDeviceNo;
    private TextView           mNetworkType;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView FBListView;
    private ImageView          mCamera;
    private EditText           mInput;
    private TextView           mSend;
    private boolean hasNextPage;
    private int curPage = 1;

    private FeedbackAdapter mAdapter;
    
    private final String TAG = "fankui";
    private final int APPEND_DATA = 201;
    private final int SET_DATA = 201;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_FEEDBACK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setStatusBar();
        assignViews();
        initState();
        requestData();
    }


    private void assignViews() {
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mAppVersion = (TextView) findViewById(R.id.app_version);
        mAndroidVersion = (TextView) findViewById(R.id.android_version);
        mDeviceNo = (TextView) findViewById(R.id.device_no);
        mNetworkType = (TextView) findViewById(R.id.network_type);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#1D84ED"));
        FBListView = (ListView) findViewById(R.id.FBListView);
        mCamera = (ImageView) findViewById(R.id.camera);
        mInput = (EditText) findViewById(R.id.input);
        mSend = (TextView) findViewById(R.id.send);
        FBListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        mActionBar.setTitle(getString(R.string.feedback));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSend.setOnClickListener(this);
        mCamera.setOnClickListener(this);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.feedBackItemSpace);
        FBListView.setDividerHeight(spacingInPixels);
        mAdapter = new FeedbackAdapter(this);
        FBListView.setAdapter(mAdapter);
        mAdapter.setFeedBackCallBack(this);

        // 显示加载中的转圈圈
        showRefreshing();

    }

    private void initState() {
        String clientVersion = null;
        try {
            clientVersion = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAppVersion.setText(clientVersion);
        mDeviceNo.setText(DeviceUtils.getDeviceName());
        mAndroidVersion.setText("Android " + android.os.Build.VERSION.RELEASE);
        mNetworkType.setText(NetWorkUtils.getNetWorkType(this));
    }

    private void requestData() {
        if(!NetWorkUtils.isConnected(this)){
            hideRefreshing();
            ToastUtils.showToast("当前网络不好哦~");
            return;
        }
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<FeedBackBean>>() {
            @Override
            public void onResponse(Call<List<FeedBackBean>> call, Response<List<FeedBackBean>> response) {
                List<FeedBackBean> feedBackList = response.body();
                if(feedBackList != null && feedBackList.size() > 0) {
                    if(curPage == 1) {
                        setFeedbackData(feedBackList);
                        LogUtils.i(TAG, "请求反馈记录数据成功，" + feedBackList.size() + "条记录");
                    } else {
                        appendFeedbackData(feedBackList);
                        LogUtils.i(TAG, "请求追加记录数据成功，" + feedBackList.size() + "条记录");
                    }

                }else if(feedBackList != null && feedBackList.size() == 0) {
                    // 没有任何记录时，添加默认提示item
                    addDefaultItem();
                    LogUtils.i(TAG, "请求反馈记录数据成功，暂没有任何记录");
                }
                hideRefreshing();
                hasNextPage = hasNextPage(response);
                curPage ++;
            }

            @Override
            public void onFailure(Call<List<FeedBackBean>> call, Throwable t) {
                // 请求失败时，添加默认提示item
                addDefaultItem();
                hideRefreshing();
                LogUtils.i(TAG, "请求反馈记录失败，" + t.getMessage());
            }
        }).bulid().requestFeedBacks(curPage);
    }

    /** 追加反馈信息数据（加载更多数据时用）*/
    private void appendFeedbackData(List<FeedBackBean> feedBackList) {
        transformAppendItem(feedBackList);
        mAdapter.handleTimeTag();
        mAdapter.notifyDataSetInvalidated();
    }

    /** 设置反馈列表数据（第一次加载时用）*/
    private void setFeedbackData(List<FeedBackBean> feedBackList) {
        transformFirstItem(feedBackList);
        mAdapter.setList(itemList);
        toListBottom();
    }

    /** 添加默认item消息（当没有任何消息记录或获取记录失败时）*/
    private void addDefaultItem() {
        if(itemList.size() == 0) {
            Item<FeedBackBean> item = new Item<FeedBackBean>();
            item.data = new FeedBackBean();
            String curTime = cn.lt.framework.util.TimeUtils.getCurrentTimeInString();
            item.data.setCreated_at(curTime);
            item.viewType = MSG_DEFAULT;
            itemList.add(item);
            mAdapter.setList(itemList);
            toListBottom();
        }
    }

    /** 把从服务器请求到的数据转换成ListView所需的数据（第一次加载的数据）*/
    private void transformFirstItem(List<FeedBackBean> feedBackList) {
        itemList.clear();
        // 倒序添加到list里面
        for (int i = feedBackList.size() -1; i >= 0; i--) {
            FeedBackBean feedBcak = feedBackList.get(i);
            Item<FeedBackBean> item = new Item<>();

            String feedbackType = feedBcak.getLtType();
            String identifyUser = feedBcak.getIdentifyUser();

            // 判断单条反馈数据的类型
            if(feedbackType.equals(FeedBackBean.TEXT_FEEDBACK)) {// 文本信息
                if(identifyUser.equals(FeedBackBean.SYSTEM)) {
                    item.viewType = MSG_LEFT_TEXT;// 系统文字信息
                } else if(identifyUser.equals(FeedBackBean.USER)) {
                    item.viewType = MSG_RIGHT_TEXT;// 自己的文字信息
                }

            } else if(feedbackType.equals(FeedBackBean.IMAGE_FEEDBACK)) {// 图片信息
                if(identifyUser.equals(FeedBackBean.SYSTEM)) {// 系统图片信息
                    item.viewType = MSG_LEFT_IMAGE;
                } else if(identifyUser.equals(FeedBackBean.USER)) {// 自己的图片信息
                    item.viewType = MSG_RIGHT_IMAGE;
                }
            }

            item.data = feedBcak;
            itemList.add(item);
        }
    }
    /** 加载请求到的数据转换成ListView所需的数据(追加的消息数据)*/
    private void transformAppendItem(List<FeedBackBean> feedBackList) {
        List<Item<FeedBackBean>> typeList = new ArrayList<>();
        // 按顺序添加到list的首位
        for (int i =  0; i < feedBackList.size(); i++) {
            FeedBackBean feedBcak = feedBackList.get(i);
            Item<FeedBackBean> item = new Item<>();

            String feedbackType = feedBcak.getLtType();
            String identifyUser = feedBcak.getIdentifyUser();

            // 判断单条反馈数据的类型
            if(feedbackType.equals(FeedBackBean.TEXT_FEEDBACK)) {// 文本信息
                if(identifyUser.equals(FeedBackBean.SYSTEM)) {
                    item.viewType = MSG_LEFT_TEXT;// 系统文字信息
                } else if(identifyUser.equals(FeedBackBean.USER)) {
                    item.viewType = MSG_RIGHT_TEXT;// 自己的文字信息
                }

            } else if(feedbackType.equals(FeedBackBean.IMAGE_FEEDBACK)) {// 图片信息
                if(identifyUser.equals(FeedBackBean.SYSTEM)) {// 系统图片信息
                    item.viewType = MSG_LEFT_IMAGE;
                } else if(identifyUser.equals(FeedBackBean.USER)) {// 自己的图片信息
                    item.viewType = MSG_RIGHT_IMAGE;
                }
            }

            item.data = feedBcak;
            typeList.add(0, item);
        }
        itemList.addAll(0, typeList);
    }


    /** 向服务器发送文字请求*/
    private void sendText(final Item<FeedBackBean> msg) {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<FeedBackBean>>() {
            @Override
            public void onResponse(Call<List<FeedBackBean>> call, Response<List<FeedBackBean>> response) {
                List<FeedBackBean> feedBackList = response.body();
                if(feedBackList != null && feedBackList.size() != 0) {
                    appendSendSuccessData(msg, feedBackList);
                    toListBottom();
                    mInput.getText().clear();
                    LogUtils.i(TAG, "发送文本消息成功");
                }
            }

            @Override
            public void onFailure(Call<List<FeedBackBean>> call, Throwable t) {
                msg.data.setSendState(SEND_FAILED);
                mAdapter.notifyDataSetChanged();
                LogUtils.i(TAG, "发送文本消息失败");
                mInput.getText().clear();
            }
        }).bulid().requestFeedBacks(msg.data.getContent());
    }



    /** 发送新的文字消息*/
    private void sendNewText(String text) {
        Item<FeedBackBean> msg = addUserTextItem(text);
        sendText(msg);
    }

    /** 加入文字消息item*/
    private Item<FeedBackBean> addUserTextItem(String text) {
        FeedBackBean bean = new FeedBackBean();
        bean.setIdentifyUser(FeedBackBean.USER);
        String curTime = cn.lt.framework.util.TimeUtils.getCurrentTimeInString();
        bean.setCreated_at(curTime);
        bean.setContent(text);
        bean.setSendState(SEND_ING);

        Item<FeedBackBean> item = new Item<>();
        item.viewType = MSG_RIGHT_TEXT;
        item.data = bean;
        itemList.add(item);
        mAdapter.notifyDataSetChanged();
        return item;
    }

    /** 重新发送文字消息*/
    private void reSendText(Item<FeedBackBean> item) {
        item.data.setSendState(SEND_ING);
        mAdapter.notifyDataSetChanged();
        sendText(item);
    }

    /** 添加发送成功的item数据*/
    private void appendSendSuccessData(Item<FeedBackBean> msg, List<FeedBackBean> feedBackList) {
        for (int i = 0; i < feedBackList.size(); i ++) {
            FeedBackBean feedBack = feedBackList.get(i);
            if(i == 0) {
                // 自己发送的消息，只需改变msg里面数据状态即可
                msg.data.setSendState(SEND_SUCCESS);
                msg.data.setCreated_at(feedBack.getCreated_at());
            } else {
                // 系统发送的，添加到itemList里面
                Item<FeedBackBean> item = new Item<>();
                // 判断消息类型
                if(feedBack.getLtType().equals(FeedBackBean.TEXT_FEEDBACK)) {
                    item.viewType = MSG_LEFT_TEXT;
                } else if(feedBack.getLtType().equals(FeedBackBean.IMAGE_FEEDBACK)){
                    item.viewType = MSG_LEFT_IMAGE;
                }
                item.data = feedBack;
                mAdapter.addItem(item);
            }

        }
        mAdapter.notifyDataSetChanged();
    }

    /** 向服务器发送图片请求*/
    private void sendImage(final Item<FeedBackBean> imageMsg, File imageFile) {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCls(FeedBackBean.class).setCallback(new Callback<List<FeedBackBean>>() {
            @Override
            public void onResponse(Call<List<FeedBackBean>> call, Response<List<FeedBackBean>> response) {
                List<FeedBackBean> feedBackList = response.body();
                if(feedBackList != null && feedBackList.size() != 0) {
                    imageMsg.data.setSendState(SEND_SUCCESS);
                    imageMsg.data.setShowImageProgress(false);
                    appendSendSuccessData(imageMsg, feedBackList);
                }
                LogUtils.i(TAG, "图片上传成功");

            }

            @Override
            public void onFailure(Call<List<FeedBackBean>> call, Throwable t) {
                imageMsg.data.setSendState(SEND_FAILED);
                imageMsg.data.setShowImageProgress(false);
                mAdapter.notifyDataSetChanged();
                LogUtils.i(TAG, "图片上传失败，" + t.getMessage());
            }
        }).bulid().requestFeedBacks(RequestBody.create(MediaType.parse("application/octet-stream"), imageFile));
    }


    private void sendNewImage(String imagePath) {
        Item<FeedBackBean> imageMsg = addUserImageItem(imagePath);
        mAdapter.notifyDataSetChanged();

        File file = getImageFile(imageMsg.data.getImagePath());
        sendImage(imageMsg, file);
//        final int itemPisition = itemList.indexOf(item);
//        new Thread(new Runnable() {
//            int progress = 0;
//            @Override
//            public void run() {
//                while (progress < 50) {
//                    progress += 5;
//                    bean.setImageProgress(progress);
//
//                    FeedBackActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mAdapter.updateProgress(itemPisition, FBListView);
//                        }
//                    });
//
//                    try {
//                        Thread.sleep(300);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
////                bean.setSendState(SEND_SUCCESS);
//                bean.setSendState(SEND_FAILED);
//                bean.setShowImageProgress(false);
//
//                FeedBackActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAdapter.updateProgress(itemPisition, FBListView);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                });
//
//
//
//            }
//        }).start();
    }


    /** 加入图片消息item*/
    private Item<FeedBackBean> addUserImageItem(String imagePath) {
        FeedBackBean bean = new FeedBackBean();
        bean.setIdentifyUser(FeedBackBean.USER);
        String curTime = cn.lt.framework.util.TimeUtils.getCurrentTimeInString();
        bean.setCreated_at(curTime);
        bean.setSendState(SEND_ING);
        bean.setShowImageProgress(true);
        bean.setImagePath(imagePath);
        Item<FeedBackBean> item = new Item<>();
        item.viewType = MSG_RIGHT_IMAGE;
        item.data = bean;
        itemList.add(item);
        return item;
    }

    /** 重新发送图片消息*/
    private void reSendImage(Item<FeedBackBean> item) {
        item.data.setSendState(SEND_ING);
        item.data.setShowImageProgress(true);
        mAdapter.notifyDataSetChanged();
        sendImage(item, getImageFile(item.data.getImagePath()));
    }

    /** 获得图片地址*/
    private File getImageFile(String imagePath) {
        File file = new File(imagePath);
        LogUtils.i(TAG,"filePath = "+ file.getAbsolutePath());
        LogUtils.i(TAG,"file is "+file.exists());
        return file;
    }



    private void toListBottom() {
        FBListView.setSelection(FBListView.getBottom());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send :
                String text = mInput.getText().toString();
                if(text == null || text.equals("")) {
                    return;
                }
                sendNewText(text);
                break;
            case R.id.camera :
                new PublicDialog(FeedBackActivity.this, new TakePhotoDialogHolder(FeedBackActivity.this)).showDialog(null);
                break;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TakePhotoDialogHolder.PHOTO_URL_RESULT:

                    if (data != null) {
                        android.net.Uri uri  = data.getData();
                        String path = BitmapUtils.getImagePath(this, uri);
                        LogUtils.i(TAG, path);
                        sendNewImage(path);
                    }
                    break;
                case TakePhotoDialogHolder.TAKE_PICTURE:
                    if (TakePhotoDialogHolder.uri != null) {
                        String path = TakePhotoDialogHolder.uri.getPath();
                        LogUtils.i(TAG, path);
                        sendNewImage(path);
                    }

                    break;
                default:
            }
        }
    }

    @Override
    public void onRefresh() {
        if(hasNextPage){
            requestData();
        } else {
            ToastUtils.showToast("已经全部加载完了");
            hideRefreshing();
            mSwipeRefreshLayout.setNestedScrollingEnabled(false);
            mSwipeRefreshLayout.stopNestedScroll();
        }
    }




    @Override
    public void onTextFailureClick(Item<FeedBackBean> item) {
        reSendText(item);
    }


    @Override
    public void onImageFailureClick(Item<FeedBackBean> item) {
        reSendImage(item);
    }



//    private void sendImage(Item<FeedBackBean> item) {
//        final FeedBackBean bean = item.data;
//        final int itemPisition = itemList.indexOf(item);
//        new Thread(new Runnable() {
//            int progress = 0;
//            @Override
//            public void run() {
//                while (progress < 50) {
//                    progress += 5;
//                    bean.setImageProgress(progress);
//
//                    FeedBackActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mAdapter.updateProgress(itemPisition, FBListView);
//                        }
//                    });
//
//                    try {
//                        Thread.sleep(300);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                bean.setSendState(SEND_SUCCESS);
////                bean.setSendState(SEND_FAILED);
//                bean.setShowImageProgress(false);
//
//                FeedBackActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        mAdapter.updateProgress(itemPisition, FBListView);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                });
//
//
//
//            }
//        }).start();
//    }

    /**
     * 判断是否还有下一页
     * @param response
     * @return
     */
    private boolean hasNextPage(Response response) {
        try {
            int lastPage = Constant.getLastPage(response.headers());
            if (curPage < lastPage) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showRefreshing() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void hideRefreshing() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
