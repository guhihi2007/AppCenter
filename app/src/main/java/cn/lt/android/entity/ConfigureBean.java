package cn.lt.android.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.lt.android.network.netdata.bean.BaseBean;

import static cn.lt.android.Constant.AUTO_INSTALL_PERIOD;
import static cn.lt.android.Constant.AUTO_INSTALL_SHOWED;
import static cn.lt.android.Constant.AUTO_INSTALL_STATE;
import static cn.lt.android.Constant.BAIDU_PERIOD;
import static cn.lt.android.Constant.BAIDU_SHOW;
import static cn.lt.android.Constant.BAIDU_STATUS;
import static cn.lt.android.Constant.CLIENT_UPDATE_SHOWED;
import static cn.lt.android.Constant.CLIENT_UPDATE_STATE;
import static cn.lt.android.Constant.FEIYANG_STATUS;
import static cn.lt.android.Constant.GDT_PERIOD;
import static cn.lt.android.Constant.GDT_SHOW;
import static cn.lt.android.Constant.GDT_STATUS;
import static cn.lt.android.Constant.PULLLIVE_STATUS;
import static cn.lt.android.Constant.RUIWEI_STATUS;
import static cn.lt.android.Constant.SELECTION_PLAY_POP_STATE;
import static cn.lt.android.Constant.SELECTION_PLAY_SHOWED;
import static cn.lt.android.Constant.SPREAD_PERIOD;
import static cn.lt.android.Constant.UPGRADE_POP_PERIOD;
import static cn.lt.android.Constant.WK_SWITCH;
import static cn.lt.android.SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE;
import static cn.lt.android.SharePreferencesKey.AUTO_UPGRADE_DIALOG_SHOW;
import static cn.lt.android.SharePreferencesKey.AUTO_UPGRADE_DIALOG_SWITCH;
import static cn.lt.android.SharePreferencesKey.FLOAT_AD_PERIOD_TIME;
import static cn.lt.android.SharePreferencesKey.NEED_SHOW_FLOAT_AD;
import static cn.lt.android.SharePreferencesKey.SHOW_FLOAT_AD;

/**
 * Created by ltbl on 2016/7/8.
 */
public class ConfigureBean extends BaseBean {
    @ConfigureKey({AUTO_INSTALL_STATE,AUTO_INSTALL_PERIOD,AUTO_INSTALL_SHOWED})
    private Configure auto_install;
    @ConfigureKey({CLIENT_UPDATE_STATE,UPGRADE_POP_PERIOD,CLIENT_UPDATE_SHOWED})
    private Configure client_update;
    @ConfigureKey({SELECTION_PLAY_POP_STATE,SPREAD_PERIOD,SELECTION_PLAY_SHOWED})
    private Configure spread;

    @ConfigureKey({AUTO_UPGRADE_DIALOG_SWITCH,AUTO_UPGRADE_DIALOG_JIAN_GE,AUTO_UPGRADE_DIALOG_SHOW})
    private Configure auto_upgrade;

    @ConfigureKey({WK_SWITCH})
    private Configure third_party_wk_app;

    @ConfigureKey({GDT_STATUS,GDT_PERIOD,GDT_SHOW})
    private Configure guangdiantong_ads;

//    @ConfigureKey({FEIYANG_STATUS})
//    private Configure feiyang_ads;

    @ConfigureKey({NEED_SHOW_FLOAT_AD,FLOAT_AD_PERIOD_TIME,SHOW_FLOAT_AD})
    private Configure floating_ads;

    @ConfigureKey({BAIDU_STATUS,BAIDU_PERIOD,BAIDU_SHOW})
    private Configure baidu_ads;

    @ConfigureKey({PULLLIVE_STATUS})
    private Configure app_pulllive;

    @ConfigureKey({RUIWEI_STATUS})
    private Configure ruiwei_ads;



    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ConfigureKey {
        String[] value();
    }

    public Configure getBaidu_ads() {
        return baidu_ads;
    }

    public void setBaidu_ads(Configure baidu_ads) {
        this.baidu_ads = baidu_ads;
    }

//    public Configure getFeiyang_ads() {
//        return feiyang_ads;
//    }
//
//    public void setFeiyang_ads(Configure feiyang_ads) {
//        this.feiyang_ads = feiyang_ads;
//    }

    public Configure getGuangdiantong_ads() {
        return guangdiantong_ads;
    }

    public void setGuangdiantong_ads(Configure guangdiantong_ads) {
        this.guangdiantong_ads = guangdiantong_ads;
    }

    public Configure getAuto_install() {
        return auto_install;
    }

    public void setAuto_install(Configure auto_install) {
        this.auto_install = auto_install;
    }

    public Configure getClient_update() {
        return client_update;
    }

    public void setClient_update(Configure client_update) {
        this.client_update = client_update;
    }

    public Configure getSpread() {
        return spread;
    }

    public void setSpread(Configure spread) {
        this.spread = spread;
    }

    public Configure getAuto_upgrade() {
        return auto_upgrade;
    }

    public void setAuto_upgrade(Configure auto_upgrade) {
        this.auto_upgrade = auto_upgrade;
    }

    public Configure getThird_party_wk_app() {
        return third_party_wk_app;
    }

    public void setThird_party_wk_app(Configure third_party_wk_app) {
        this.third_party_wk_app = third_party_wk_app;
    }

    public Configure getApp_pulllive() {
        return app_pulllive;
    }

    public void setApp_pulllive(Configure app_pulllive) {
        this.app_pulllive = app_pulllive;
    }

    public Configure getFloating_ads() {
        return floating_ads;
    }

    public void setFloating_ads(Configure floating_ads) {
        this.floating_ads = floating_ads;
    }

    public Configure getRuiwei_ads() {
        return ruiwei_ads;
    }

    public void setRuiwei_ads(Configure ruiwei_ads) {
        this.ruiwei_ads = ruiwei_ads;
    }
}
