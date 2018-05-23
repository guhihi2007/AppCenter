package cn.lt.framework.crash.mailreporter;

import android.content.Context;

import java.io.File;

import cn.lt.framework.crash.AbstractCrashHandler;

/**
 * 已经实现的日志报告类，这里通过邮件方式发送日志报告
 * 
 */
public class CrashEmailReporter extends AbstractCrashHandler {
    private String mReceiveEmail;
    private String mSendEmail;
    private String mSendPassword;
    private String mHost;
    private String mPort;

    public CrashEmailReporter(Context context) {
        super(context);
    }

    /**
     * 设置接收者
     * 
     * @param receiveEmail
     */
    public void setReceiver(String receiveEmail) {
        mReceiveEmail = receiveEmail;
    }

    /**
     * 设置发送者邮箱
     * 
     * @param email
     */
    public void setSender(String email) {
        mSendEmail = email;
    }

    /**
     * 设置发送者密码
     * 
     * @param password
     */
    public void setSendPassword(String password) {
        mSendPassword = password;
    }

    /**
     * 设置SMTP 主机
     * 
     * @param host
     */
    public void setSMTPHost(String host) {
        mHost = host;
    }

    /**
     * 设置端口
     * 
     * @param port
     */
    public void setPort(String port) {
        mPort = port;
    }

    @Override
    protected void sendReport(String title, String body, File file) {
        LogMail sender = new LogMail().setUser(mSendEmail).setPass(mSendPassword)
                .setFrom(mSendEmail).setTo(mReceiveEmail).setHost(mHost).setPort(mPort)
                .setSubject(title).setBody(body);
        sender.init();
        try {
            sender.addAttachment(file.getPath(), file.getName());
            sender.send();
//            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
