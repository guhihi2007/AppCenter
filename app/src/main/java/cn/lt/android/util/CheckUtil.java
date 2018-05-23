package cn.lt.android.util;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.lt.appstore.R;


/**
 * 数据格式验证，返回false是验证不通过
 *
 * @author LT
 */
public class CheckUtil {

    public final static String checkPassWorld = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return 不是返回false，是返回true
     */
    public static boolean isEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机格式
     *
     * @param mobiles 手机号
     * @return 不是返回false，是返回true
     */
    public static boolean isMobileNO(String mobiles) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
        String telRegex = "[1][34578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles)) return false;
        else return mobiles.matches(telRegex);
    }

    public static boolean checkPassWordLength(String passWord) {
        if (passWord.getBytes().length < 6 || passWord.getBytes().length > 20) {
            return false;
        }
        return true;
    }

    public static boolean checkNickNameLength(String nickName) {
        int length = nickName.getBytes().length;
        try {
            length = nickName.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (length > 14 || length < 1) {
            return false;
        }
        return true;
    }

    public static boolean checkNickNameSpace(String nickName) {
        if (nickName.indexOf(' ') == -1) {
            return true;
        }
        return false;
    }

    public static boolean checkNickName(EditText etNick, String nickName) {
        if (TextUtils.isEmpty(nickName)) {
            etNick.setFocusable(true);
            etNick.setFocusableInTouchMode(true);
            etNick.requestFocus();
            ToastUtils.showToast("昵称不能为空");
            return false;
        }
        if (!CheckUtil.checkNickNameSpace(nickName)) {
            etNick.setFocusable(true);
            etNick.setFocusableInTouchMode(true);
            etNick.requestFocus();
            ToastUtils.showToast("昵称中不能含有空格");
            return false;
        } else
//        if (!CheckUtil.checkNickNameLength(nickName)) {
//            etNick.setFocusable(true);
//            etNick.setFocusableInTouchMode(true);
//            etNick.requestFocus();
//            ToastUtils.showToast("昵称长度需要保持在1-14个字节之间");
//            return false;
//        }
            return true;
    }

    public static boolean checkSignature(String signature) {
        if (!CheckUtil.checkNickNameLength(signature)) {
            ToastUtils.showToast("昵称最多支持50个字符");
            return false;
        }
        return true;
    }

    public static boolean checkPhoneRegisterInfo( EditText etPhone, EditText etPass, String userName, String passWord, String code, CheckBox box) {
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord) || TextUtils.isEmpty(code)) {
            ToastUtils.showToast("账号、密码以及验证码不能为空");
            return false;
        } else if (!isMobileNO(userName)) {
            etPhone.setFocusable(true);
            etPhone.setFocusableInTouchMode(true);
            etPhone.requestFocus();
            showKeyboard(etPhone);
            ToastUtils.showToast("账号格式错误");
            return false;
        } else if (!checkPassWordLength(passWord)) {
            etPass.setFocusable(true);
            etPass.setFocusableInTouchMode(true);
            etPass.requestFocus();
            box.setChecked(true);
            etPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showKeyboard(etPass);
            ToastUtils.showToast("密码长度需要保持在6-20个字符之间");
            return false;
        }
        return true;
    }

    public static boolean checkLoginInfo( EditText eUserName, EditText ePwd, String userName, String passWord, CheckBox box) {
        if (TextUtils.isEmpty(userName)) {
            eUserName.setFocusable(true);
            eUserName.setFocusableInTouchMode(true);
            eUserName.requestFocus();
            ToastUtils.showToast("手机号不能为空");
            return false;
        } else if (TextUtils.isEmpty(passWord)) {
            ePwd.setFocusable(true);
            ePwd.setFocusableInTouchMode(true);
            ePwd.requestFocus();
            ToastUtils.showToast("密码不能为空");
            return false;
        } else if (!isMobileNO(userName) && !isEmail(userName)) {
            eUserName.setFocusable(true);
            eUserName.setFocusableInTouchMode(true);
            eUserName.requestFocus();
            showKeyboard(eUserName);
            ToastUtils.showToast("账号格式错误");
            return false;
        } else if (!checkPassWordLength(passWord)) {
            ePwd.setFocusable(true);
            ePwd.setFocusableInTouchMode(true);
            ePwd.requestFocus();
            box.setChecked(true);
            ePwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("密码长度需要保持在6-20个字符之间");
            return false;
        }
        return true;
    }

    public static boolean obtainCode( EditText eUserName, EditText ePwd, String userName, String passWord, CheckBox box) {
        if (TextUtils.isEmpty(userName)) {
            eUserName.setFocusable(true);
            eUserName.setFocusableInTouchMode(true);
            eUserName.requestFocus();
            ToastUtils.showToast("手机号不能为空");
            return false;
        } else if (TextUtils.isEmpty(passWord)) {
            ePwd.setFocusable(true);
            ePwd.setFocusableInTouchMode(true);
            ePwd.requestFocus();
            ToastUtils.showToast("密码不能为空");
            return false;
        } else if (!isMobileNO(userName)) {
            eUserName.setFocusable(true);
            eUserName.setFocusableInTouchMode(true);
            eUserName.requestFocus();
            showKeyboard(eUserName);
            ToastUtils.showToast("账号格式错误");
            return false;
        } else if (!checkPassWordLength(passWord)) {
            ePwd.setFocusable(true);
            ePwd.setFocusableInTouchMode(true);
            ePwd.requestFocus();
            box.setChecked(true);
            ePwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("密码长度需要保持在6-20个字符之间");
            return false;
        }
        return true;
    }

    public static boolean checkModifyPassWordInfo(CheckBox box01, CheckBox box02, CheckBox box03, EditText etOld, EditText etNew, EditText etNew2, String oldPassWord, String newPassWord1, String newPassWord2) {
        if (TextUtils.isEmpty(oldPassWord)) {
            etOld.setFocusable(true);
            etOld.setFocusableInTouchMode(true);
            etOld.requestFocus();
            ToastUtils.showToast("旧密码不能为空");
            return false;
        } else if (TextUtils.isEmpty(newPassWord1)) {
            etNew.setFocusable(true);
            etNew.setFocusableInTouchMode(true);
            etNew.requestFocus();
            ToastUtils.showToast("新密码不能为空");
            return false;
        } else if (TextUtils.isEmpty(newPassWord2)) {
            etNew2.setFocusable(true);
            etNew2.setFocusableInTouchMode(true);
            etNew2.requestFocus();
            ToastUtils.showToast("请重复输入一次新密码");
            return false;
        } else if (!checkPassWordLength(oldPassWord)) {
            etOld.setFocusable(true);
            etOld.setFocusableInTouchMode(true);
            etOld.requestFocus();
            box01.setChecked(true);
            etOld.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("旧密码输入错误");
            return false;
        } else if (!checkPassWordLength(newPassWord1)) {
            etNew.setFocusable(true);
            etNew.setFocusableInTouchMode(true);
            etNew.requestFocus();
            box02.setChecked(true);
            etNew.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("新设置的密码长度需要保持在6-20个字符之间");
            return false;
        } else if (!newPassWord1.equals(newPassWord2)) {
            etNew2.setFocusable(true);
            etNew2.setFocusableInTouchMode(true);
            etNew2.requestFocus();
            box02.setChecked(true);
            box03.setChecked(true);
            etNew.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            etNew2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("两次输入的新密码不相同");
            return false;
        } else if (oldPassWord.equals(newPassWord1)) {
            etNew.setFocusable(true);
            etNew.setFocusableInTouchMode(true);
            etNew.requestFocus();
            box02.setChecked(true);
            etNew.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("新密码不能与旧密码相同");
            return false;
        }
        return true;
    }

    public static boolean checkFindPassWord(CheckBox box01, CheckBox box02, EditText etNew, EditText etNew2, String newPassWord1, String newPassWord2) {
        if (TextUtils.isEmpty(newPassWord1)) {
            etNew.setFocusable(true);
            etNew.setFocusableInTouchMode(true);
            etNew.requestFocus();
            ToastUtils.showToast("新密码不能为空");
            return false;
        } else if (TextUtils.isEmpty(newPassWord2)) {
            etNew2.setFocusable(true);
            etNew2.setFocusableInTouchMode(true);
            etNew2.requestFocus();
            ToastUtils.showToast("请重复输入一次新密码");
            return false;
        } else if (!checkPassWordLength(newPassWord1)) {
            etNew.setFocusable(true);
            etNew.setFocusableInTouchMode(true);
            etNew.requestFocus();
            box01.setChecked(true);
            etNew.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("新设置的密码长度需要保持在6-20个字符之间");
            return false;
        } else if (!newPassWord1.equals(newPassWord2)) {
            etNew2.setFocusable(true);
            etNew2.setFocusableInTouchMode(true);
            etNew2.requestFocus();
            box01.setChecked(true);
            box02.setChecked(true);
            etNew.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            etNew2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ToastUtils.showToast("两次输入的新密码不相同");
            return false;
        }
        return true;
    }

    public static boolean checkBindPhone(EditText etPhone, EditText etCode, String phoneNember, String code) {
        if (TextUtils.isEmpty(phoneNember)) {
            etPhone.setFocusable(true);
            etPhone.setFocusableInTouchMode(true);
            etPhone.requestFocus();
            ToastUtils.showToast("手机号码不能为空");
            return false;
        } else if (TextUtils.isEmpty(code)) {
            etCode.setFocusable(true);
            etCode.setFocusableInTouchMode(true);
            etCode.requestFocus();
            ToastUtils.showToast("验证码不能为空");
            return false;
        } else if (!isMobileNO(phoneNember)) {
            etPhone.setFocusable(true);
            etPhone.setFocusableInTouchMode(true);
            etPhone.requestFocus();
            ToastUtils.showToast("手机号码格式错误");
            return false;
        }
        return true;
    }

    public static boolean checkBindEmail(Context context, String emailNember) {
        if (!isEmail(emailNember)) {
            Toast.makeText(context, "邮箱格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 检查验证码是否可以继续发送
     */
    public static boolean checkCode(long codeTime, TextView btnSendVerifyCode, int count) {
        long time = System.currentTimeMillis();
        if (time - codeTime < 60000) {
            btnSendVerifyCode.setEnabled(false);
            btnSendVerifyCode.setBackgroundColor(Color.parseColor("#888888"));
            btnSendVerifyCode.setText((60 - (time - codeTime) / 1000) + "s后重试");
            return false;
        } else {
            btnSendVerifyCode.setBackgroundResource(R.drawable.appdetail_downloadbar_selector);
            if (count > 0) {
                btnSendVerifyCode.setText("重新获取");
            } else {
                btnSendVerifyCode.setText("获取验证码");
            }
            btnSendVerifyCode.setEnabled(true);
            return true;
        }
    }

    public static void showKeyboard(final EditText mEditText) {
        LogUtils.i("zzz", "弹出键盘");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mEditText, 0);
            }

        }, 998);
    }

}
