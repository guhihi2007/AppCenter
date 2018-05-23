package cn.lt.greendaogenerator;

import org.json.JSONObject;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by wenchao on 2016/2/22.
 * 自动生成dao实现
 */
public class Generator {
    //数据库版本
    public static final int DB_VERSION = 1;
    //生成后的包名
    public static final String PACKAGE_NAME = "cn.lt.android.db";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(DB_VERSION, PACKAGE_NAME);
//        createAppInfoTable(schema);
//        createSearchHistoryTable(schema);
//        createIgnoreUpgradeAppTable(schema);
//        createStatisticsTable(schema);
//        createLoginHistoryTable(schema);
//        createRetryStatDataTable(schema);
//        createUserTable(schema);
        createWakeTaskTable(schema);
        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }


    /**
     * 创建 拉活任务表
     *
     * @param schema
     */
    private static void createWakeTaskTable(Schema schema) {
        Entity entity = schema.addEntity("WakeTaskEntity");
        entity.addIdProperty();
        entity.addIntProperty("task_id");// 任务id
        entity.addStringProperty("type");// wake、launch、notification、heads-up
        entity.addStringProperty("package_name");
        entity.addLongProperty("user_cycle");
        entity.addLongProperty("task_cycle");
        entity.addIntProperty("show_type");
        entity.addStringProperty("title");
        entity.addStringProperty("sub_title");
        entity.addStringProperty("image");
        entity.addStringProperty("action_name");
        entity.addStringProperty("class_name");
        entity.addStringProperty("deep_link");
        entity.addStringProperty("extra");
        entity.addLongProperty("lastExecuteTime");// 上一次执行时间(客户端写入)
        entity.addIntProperty("executeTimes");// 执行次数(客户端写入)
        entity.addIntProperty("show_time");// 展示时间
        entity.addStringProperty("jump_type");// 1.deeplink地址  2.H5地址  3.启动应用
    }

    /**
     * 创建 当前登录账号表
     *
     * @param schema
     */
    private static void createUserTable(Schema schema) {
        Entity entity = schema.addEntity("UserEntity");
        entity.addIdProperty();
        entity.addStringProperty("token");
        entity.addStringProperty("number");
        entity.addStringProperty("userid");

        entity.addStringProperty("avatar");
        entity.addStringProperty("email");
        entity.addStringProperty("nickname");
        entity.addStringProperty("sex");
        entity.addStringProperty("birthday");
        entity.addStringProperty("address");
        entity.addStringProperty("userName");
    }

    /**
     * 创建历史记录的表
     *
     * @param schema
     */
    private static void createLoginHistoryTable(Schema schema) {
        Entity entity = schema.addEntity("LoginHistoryEntity");
        entity.addIdProperty();
        entity.addStringProperty("avatar");
        entity.addStringProperty("mobile");
        entity.addStringProperty("email");
        entity.addStringProperty("token");
        entity.addLongProperty("userId");
        entity.addStringProperty("nickName");
    }

    private static void createAppInfoTable(Schema schema) {
        Entity entity = schema.addEntity("AppEntity");
        entity.addIdProperty();
        entity.addStringProperty("packageName");
        entity.addStringProperty("name");
        entity.addStringProperty("alias");
        entity.addStringProperty("downloadUrl");
        entity.addStringProperty("savePath");
        entity.addStringProperty("iconUrl");
        entity.addStringProperty("packageSize");
        entity.addStringProperty("apps_type");
        entity.addLongProperty("apps_startDownloadTime");
        entity.addLongProperty("apps_endDownloadTime");
        entity.addBooleanProperty("lackofmemory");

        entity.addStringProperty("package_md5");
        entity.addStringProperty("version_code");
        entity.addStringProperty("version_name");
        entity.addStringProperty("corner_url");
        entity.addStringProperty("reviews");
        entity.addStringProperty("created_at");
        entity.addStringProperty("download_count");
        entity.addStringProperty("description");
        entity.addStringProperty("category");
        entity.addBooleanProperty("isAD");
        entity.addBooleanProperty("isOrderWifiDownload");
        entity.addBooleanProperty("isAppAutoUpgrade");

        entity.addBooleanProperty("canReplace");
        entity.addStringProperty("adMold");
        entity.addStringProperty("reportType");
        entity.addStringProperty("reportData");

    }

    private static void createSearchHistoryTable(Schema schema) {
        Entity entity = schema.addEntity("SearchHistoryEntity");
        entity.addIdProperty();
        entity.addStringProperty("title");
    }

    private static void createIgnoreUpgradeAppTable(Schema schema) {
        Entity entity = schema.addEntity("IgnoreUpgradeAppEntity");
        entity.addIdProperty().primaryKey();
        entity.addStringProperty("name");
        entity.addStringProperty("packageName");
        entity.addStringProperty("versionName");
    }

    /***
     * 统计表
     *
     * @param schema
     */
    private static void createStatisticsTable(Schema schema) {
        Entity entity = schema.addEntity("StatisticsEntity");
        entity.addIdProperty().primaryKey();
        entity.addStringProperty("mGameID");
        entity.addStringProperty("mPkgName");
        entity.addStringProperty("mActionType");
        entity.addStringProperty("mDownloadType");
        entity.addStringProperty("mRemark");
        entity.addStringProperty("mPage");
        entity.addStringProperty("mPageID");
        entity.addStringProperty("mDownloadMode");
        entity.addStringProperty("mInstallMode");
        entity.addStringProperty("mInstallWay");
        entity.addStringProperty("mAdType");
        entity.addIntProperty("mCount");
        entity.addDoubleProperty("mTimeMillis");
        entity.addStringProperty("from_page");
        entity.addStringProperty("resource_type");
        entity.addStringProperty("word");
        entity.addStringProperty("from_id");
        entity.addIntProperty("P1");
        entity.addIntProperty("P2");
    }

    /**
     * 创建重新上报数据的表
     *
     * @param schema
     */
    private static void createRetryStatDataTable(Schema schema) {
        Entity entity = schema.addEntity("RetryStatisticsEntity");
        entity.addIdProperty();
        entity.addStringProperty("mUploadFailureDataByJsonString");
    }

}
