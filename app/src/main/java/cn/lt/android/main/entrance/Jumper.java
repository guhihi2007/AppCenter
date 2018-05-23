package cn.lt.android.main.entrance;

import android.app.Activity;
import android.content.Context;

import cn.lt.android.Constant;
import cn.lt.android.entity.ClickTypeDataBean;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.main.entrance.data.ClickType;
import cn.lt.android.main.recommend.NewAppActivity;
import cn.lt.android.main.specialtopic.SpecialTopicActivity;
import cn.lt.android.util.LogUtils;
import de.greenrobot.event.EventBus;

/***
 * Created by dxx on 2016/3/15.
 */

public class Jumper {
    /**
     * 点击同一个按钮，通过传入不同的数据，调到不同的地方
     *
     * @param type       点击类型；
     * @param mClickData 点击数据源；
     */
    public void jumper(Context context, ClickType type, ClickTypeDataBean mClickData, String mPageName, boolean isFromdeeplink) {
        try {
            if (mClickData != null) {
                switch (type) {
                    case h5:
                        cn.lt.android.main.UIController.goWebView(context, mClickData.getTitle(), mClickData.getUrl(), false);
                        break;
                    case app_info:
                        UIController.goAppDetail(context, false, "", mClickData.getId(), "", mClickData.getApps_type(), Constant.PAGE_LOADING, "", "", mClickData.getReportData());
                        break;
                    case topic_info:
                        // 专题详情
                        LogUtils.i("zzz", "专题Page" + mPageName);
                        UIController.goSpecialDetail(context, mClickData.getId(), mClickData.getTitle(), mPageName, false, false);
                        break;
                    case list:    //普通列表
                        UIController.goNecessary(context, mClickData.getId(), NewAppActivity.NORMAL, mClickData.getTitle());
                        break;
                    case tab_topic_info:  //新品、必备列表
                        UIController.goNecessary(context, mClickData.getId(), NewAppActivity.NECESSARY, null);
                        break;
                    case page:
                        jumpToPage(context, mClickData);
                        break;
                    case intellective_ads_lists:
                        LogUtils.i("Jumper", "" + Constant.JUMP_PAGE_INTELLECTIVE_ADS_LISTS);
                        // 智能列表
                        UIController.goSamrtList(context, mClickData.getId());
                        break;
                    case topic_list:
                        UIController.goSpecial(context, SpecialTopicActivity.DEFAULT);
                        break;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jumpToPage(Context context, ClickTypeDataBean mClickData) {
        String page = mClickData.getPage();
        LogUtils.i("Jumper", "-------" + page);
        switch (page) {
            case Constant.PAGE_RECOMMEND_SUB:
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_RECOMMEND_SUB));
                LogUtils.i("Jumper", "" + Constant.PAGE_RECOMMEND_SUB);

                break;
            case Constant.PAGE_NECESSARY:
                LogUtils.i("Jumper", "" + Constant.PAGE_NECESSARY);
                UIController.goNecessary(context, mClickData.getId(), NewAppActivity.NECESSARY, null);

                break;
            case Constant.PAGE_CLASSIFY:
                LogUtils.i("Jumper", "" + Constant.PAGE_CLASSIFY);
                UIController.goCategory(context);

                break;
            case Constant.PAGE_CLASSIFY_DETAIL:
                LogUtils.i("Jumper", "" + Constant.PAGE_CLASSIFY_DETAIL);
                UIController.goCategoryDetail(context, "", "", "");

                break;
            case Constant.PAGE_NEW:
                LogUtils.i("Jumper", "" + Constant.PAGE_NEW);
                // UIController.goNecessary(context,mClickData.getId());
                UIController.goNecessary(context, mClickData.getId(), NewAppActivity.NEWAPPS, null);

                break;
            case Constant.JUMP_PAGE_SPECIAL:
                LogUtils.i("Jumper", "" + Constant.JUMP_PAGE_SPECIAL);
                UIController.goSpecial(context, SpecialTopicActivity.DEFAULT);
                break;

            case Constant.PAGE_APP_SPECIAL:
                LogUtils.i("Jumper", "" + Constant.PAGE_APP_SPECIAL);
                UIController.goSpecial(context, Constant.SPECIALTOP_TYPE_SOFT);
                //              软件专题

                break;


            /***************************软件*****************************/
            case Constant.PAGE_APP_CHOICE:
                LogUtils.i("Jumper", "" + Constant.PAGE_APP_CHOICE);
                      /*  if(!mSpUtils.getBoolean("interfaceIsNotFirstPassed",false)){
                    if (carousJumpListener != null) {
                        carousJumpListener.jumpToInternal(Constant.JUMPTOINTERNALSOFT, 0);
                        carousJumpListener = null;
                        }
                            isInternalJump=true;
                    }*/
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_APP_CHOICE));
                //               软件精选 HighlySelectiveFragment--软件标志

                break;
            case Constant.PAGE_APP_LIST:
                LogUtils.i("Jumper", "" + Constant.PAGE_APP_LIST);
                     /*   if(!mSpUtils.getBoolean("interfaceIsNotFirstPassed",false)){
                    if (carousJumpListener != null) {
                        carousJumpListener.jumpToInternal(Constant.JUMPTOINTERNALSOFT, 1);
                        carousJumpListener = null;
                        }
                           isInternalJump=true;
                    }*/
                //下面的方式---soft，game fragment只要一创建就会把接口传过来
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_APP_LIST));
                //               软件榜单 HighlySelectiveFragment--软件标志

                break;
            case Constant.PAGE_APP_CLASSIFY:
                LogUtils.i("Jumper", "" + Constant.PAGE_APP_CLASSIFY);
                    /*    if(!mSpUtils.getBoolean("interfaceIsNotFirstPassed",false)){
                    if (carousJumpListener != null) {
                        carousJumpListener.jumpToInternal(Constant.JUMPTOINTERNALSOFT, 2);
                        carousJumpListener = null;
                        }
                            isInternalJump = true;
                    }*/
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_APP_CLASSIFY));
                //本Rootfragment内跳转方式：由软件精选调到  软件分类 --回调让 softRoot选中2


                //               软件分类 CategoryFragment--软件标志

                break;


            /***************************游戏*****************************/
            case Constant.PAGE_GAME_CHOICE:
                LogUtils.i("Jumper", "" + Constant.PAGE_GAME_CHOICE);
                       /* if (!mSpUtils.getBoolean("interfaceIsNotFirstPassed",false)){
                    if (carousJumpListener != null) {
                        carousJumpListener.jumpToInternal(Constant.JUMPTOINTERNALGAME, 0);
                        carousJumpListener = null;
                        }
                            isInternalJump=true;
                    }*/
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_GAME_CHOICE));
                //                游戏精选 HighlySelectiveFragment

                break;
            case Constant.PAGE_GAME_LIST:
                LogUtils.i("Jumper", "" + Constant.PAGE_GAME_LIST);
                      /*  if (!mSpUtils.getBoolean("interfaceIsNotFirstPassed",false)){
                    if (carousJumpListener != null) {
                        carousJumpListener.jumpToInternal(Constant.JUMPTOINTERNALGAME, 1);
                        carousJumpListener = null;
                        }
                            isInternalJump=true;
                    }*/
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_GAME_LIST));

                //                游戏榜单 RankRootFragment

                break;
            case Constant.PAGE_GAME_CLASSIFY:
                LogUtils.i("Jumper", "" + Constant.PAGE_GAME_CLASSIFY);
              /*  if (!mSpUtils.getBoolean("interfaceIsNotFirstPassed",false)){
                    if (carousJumpListener != null) {
                        carousJumpListener.jumpToInternal(Constant.JUMPTOINTERNALGAME, 2);
                        carousJumpListener = null;
                    }
                    isInternalJump=true;
                }*/
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_GAME_CLASSIFY));
                //               游戏分类 CategoryFragment
                break;

            /***************************榜单*****************************/
            case Constant.PAGE_LIST:
                LogUtils.i("Jumper", "" + Constant.PAGE_RETRIEVE_PASSWORD);
                EventBus.getDefault().post(new MainActivity.EventBean(Constant.PAGE_LIST));
                break;


            case Constant.PAGE_GAME_SPECIAL:
                LogUtils.i("Jumper", "" + Constant.PAGE_GAME_SPECIAL);
                UIController.goSpecial(context, Constant.SPECIALTOP_TYPE_GAME);
                //游戏专题

                break;
            case Constant.JUMP_PAGE_SEARCH:
                LogUtils.i("Jumper", "" + Constant.JUMP_PAGE_SEARCH);
                UIController.goSearchActivity(context, "", false, "","");

                break;
            case Constant.PAGE_DETAIL:
                LogUtils.i("Jumper", "" + Constant.PAGE_DETAIL);
                //未知 应用详情页面

                break;
            case Constant.PAGE_TASK_MANAGEMENT:
                LogUtils.i("Jumper", "" + Constant.PAGE_TASK_MANAGEMENT);
                UIController.goTaskManager(context);

                break;
            case Constant.PAGE_LOGIN_JUMP:
                LogUtils.i("Jumper", "" + Constant.PAGE_LOGIN_JUMP);
                //跳到LoginFragment
                UIController.goAccountCenter((Activity) context, Constant.USER_LOGIN);

                break;
            case Constant.PAGE_REGISTER_JUMP:
                LogUtils.i("Jumper", "" + Constant.PAGE_REGISTER_JUMP);
                UIController.goAccountCenter((Activity) context, Constant.USER_REGISTER);
                //跳到RegisterFragment

                break;
            case Constant.PAGE_APP_MANAGEMENT:
                LogUtils.i("Jumper", "" + Constant.PAGE_APP_MANAGEMENT);
                UIController.goAppUninstallActivity(context);

                break;
            case Constant.PAGE_SET:
                LogUtils.i("Jumper", "" + Constant.PAGE_SET);
                UIController.goSetting(context);

                break;
            case Constant.PAGE_FEEDBACK:
                LogUtils.i("Jumper", "" + Constant.PAGE_FEEDBACK);
                UIController.goFeedback(context);

                break;
            case Constant.PAGE_ABOUT_US_JUMP:
                LogUtils.i("Jumper", "" + Constant.PAGE_ABOUT_US);
                UIController.goAboutUs(context);

                break;
            case Constant.PAGE_ID_MANAGEMENT:
                LogUtils.i("Jumper", "" + Constant.PAGE_ID_MANAGEMENT);
                UIController.goAccountCenter((Activity) context, Constant.USER_INFO);
                //AccountManageFragment账号管理页面

                break;
            case Constant.PAGE_CHANGE_NICKNAME:
                LogUtils.i("Jumper", "" + Constant.PAGE_CHANGE_NICKNAME);
                UIController.goUserInfoEditPage((Activity) context, Constant.MODIFY_NICKNAME);
                //ModifyNickNameFragment修改呢称

                break;
            case Constant.PAGE_CHANGE_PHONE_NUMBER:
                LogUtils.i("Jumper", "" + Constant.PAGE_CHANGE_PHONE_NUMBER);
                UIController.goUserInfoEditPage((Activity) context, Constant.MODIFY_MOBILE);
                //                ModifyMobileFragment

                break;
            case Constant.PAGE_CHANGE_PASSWORD:
                LogUtils.i("Jumper", "" + Constant.PAGE_CHANGE_PASSWORD);
                UIController.goUserInfoEditPage((Activity) context, Constant.MODIFY_PWD);
                //                ModifyPwdFragment

                break;
            case Constant.PAGE_FORGOT_PASSWORD:
                LogUtils.i("Jumper", "" + Constant.PAGE_FORGOT_PASSWORD);
                UIController.goAccountCenter((Activity) context, Constant.GET_BACK_PWD);
                //                ModifyPwdFragment忘记密码MODIFY_PWD

                break;
            case Constant.PAGE_RETRIEVE_PASSWORD:
                LogUtils.i("Jumper", "" + Constant.PAGE_RETRIEVE_PASSWORD);
                //                GetBackPwdFragmen 找回密码 GET_BACK_PWD
                UIController.goAccountCenter((Activity) context, Constant.GET_BACK_PWD);
                break;

            case Constant.JUMP_PAGE_INTELLECTIVE_ADS_LISTS:
                LogUtils.i("Jumper", "" + Constant.JUMP_PAGE_INTELLECTIVE_ADS_LISTS);
                // 智能列表
                UIController.goSamrtList(context, mClickData.getId());
                break;

        }
    }
}
