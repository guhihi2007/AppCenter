package cn.lt.android.main.personalcenter.model;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by LinJunSheng on 2016/4/8.
 * 反馈的实体
 */
public class FeedBackBean extends BaseBean {
    public static final String SYSTEM = "system";
    public static final String USER = "user";
    public static final String TEXT_FEEDBACK = "text_feedback";
    public static final String IMAGE_FEEDBACK = "image_feedback";

    private String identifyUser;
    private String created_at;

    private String content;// 文本才有

    private String image_url;// 图片才有的字段
    private String thumb_url;// 图片才有的字段
    private String imagePath;

    private int sendState;// 自己的发送状态
    private boolean needShowTime = false;

    private int imageProgress = 0;
    private boolean showImageProgress = false;

    public String getIdentifyUser() {
        return identifyUser;
    }

    public void setIdentifyUser(String identifyUser) {
        this.identifyUser = identifyUser;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    public boolean isNeedShowTime() {
        return needShowTime;
    }

    public void setNeedShowTime(boolean needShowTime) {
        this.needShowTime = needShowTime;
    }

    public int getImageProgress() {
        return imageProgress;
    }

    public void setImageProgress(int imageProgress) {
        this.imageProgress = imageProgress;
    }

    public boolean isShowImageProgress() {
        return showImageProgress;
    }

    public void setShowImageProgress(boolean showImageProgress) {
        this.showImageProgress = showImageProgress;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
