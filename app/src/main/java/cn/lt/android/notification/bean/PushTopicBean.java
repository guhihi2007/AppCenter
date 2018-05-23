package cn.lt.android.notification.bean;

/**
 * Created by LinJunSheng on 2016/3/22.
 */
public class PushTopicBean extends PushBaseBean{
    private String summary;
    private String updated_at;
    private String title;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
