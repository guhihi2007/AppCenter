package cn.lt.android.umsharesdk;

/**
 * Created by LinJunSheng on 2016/3/29.
 */
public class ShareStrings {

    public static final int WeiXin = 1;
    public static final int WeiXin_Circle = 2;
    public static final int QQ = 3;
    public static final int sinaWeiBo = 4;

    public static String getSoftwareTitle(String appName, int platType) {
        switch (platType) {
            case WeiXin :
                return weiXinTitle_software(appName);
            case WeiXin_Circle :
                return friendCircleTitle_software(appName);
            case QQ :
                return QQTitle_software(appName);
            case sinaWeiBo :
                return sinaWeiBoTitle_software(appName);
            default:
                return "";
        }
    }

    public static String getSoftwareContent(String appName, int platType) {
        switch (platType) {
            case WeiXin :
            case QQ :{
                return content_Software(appName);
            }
            case WeiXin_Circle :
                return "hello 应用市场";
            case sinaWeiBo :
                return "少侠！请留步！" + content_Software(appName);
            default:
                return "";
        }
    }

    public static String getGameTitle(String appName, int platType) {
        switch (platType) {
            case WeiXin :
                return weiXinTitle_game(appName);
            case WeiXin_Circle :
                return friendCircleTitle_game(appName);
            case QQ :
                return QQTitle_game(appName);
            case sinaWeiBo :
                return sinaWeiBoTitle_game(appName);
            default:
                return "";
        }
    }

    public static String getGameContent(String appName, int platType) {
        switch (platType) {
            case WeiXin :
            case WeiXin_Circle :
            case QQ :{
                return content_game(appName);
            }
            case sinaWeiBo :
                return "少侠！请留步！" + content_game(appName);
            default:
                return "";
        }
    }

    public static String geTitle_AppCenter() {
        return "应用市场 - 应用因选择而不同";
    }

    public static String getContent_AppCenter() {
        return "朋友朋友,你知道「应用市场」吗？没时间解释了，有趣的应用都在这，赶快来下载哦。";
    }






    // 各平台应用分享文案
    private static String weiXinTitle_software(String softwareName) {
        return "「" + softwareName + "」少侠！请留步！赠你一款有意思的应用";
    }

    private static String friendCircleTitle_software(String softwareName) {
        return "「" + softwareName + "」少侠！请留步！赠你一款有意思的应用";
    }

    private static String QQTitle_software(String softwareName) {
        return "「" + softwareName + "」少侠！请留步！赠你一款有意思的应用";
    }

    private static String sinaWeiBoTitle_software(String softwareName) {
        return "少侠！请留步！刚在应用市场下载了一款有意思的应用「" + softwareName + "」，赶紧下载体验吧！";
    }

    private static String content_Software(String softwareName) {
        return "刚在应用市场下载了一款有意思的应用「" + softwareName + "」，赶紧下载体验吧！";
    }




    // 各平台游戏分享文案
    private static String weiXinTitle_game(String gameName) {
        return "【" + gameName + "】少侠！请留步！送你一款有趣的游戏";
    }

    private static String friendCircleTitle_game(String gameName) {
        return "【" + gameName + "】少侠！请留步！送你一款有趣的游戏";
    }

    private static String QQTitle_game(String gameName) {
        return "【" + gameName + "】少侠！请留步！送你一款有趣的游戏";
    }

    private static String sinaWeiBoTitle_game(String gameName) {
        return "少侠！请留步！刚在 #应用市场# 下载了【" + gameName + "】，你要跟我一起玩吗？ 赶快来下载哦";
    }

    private static String content_game(String gameName) {
        return "刚在应用市场下载了一款好玩的游戏【" + gameName + "】，你要跟我一起玩吗？赶快来下载哦";
    }


}
