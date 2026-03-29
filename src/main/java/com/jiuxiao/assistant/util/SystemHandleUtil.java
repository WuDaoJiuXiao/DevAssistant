package com.jiuxiao.assistant.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 系统交互操作工具类
 */
public class SystemHandleUtil {

    private static final String NOTIFICATION_GROUP_ID = "DevAssistant.NotificationGroup";

    private static final Long STATUS_BAR_TIP_SECONDS = 3500L;

    /**
     * 复制字符串到剪切板
     *
     * @param content 复制的内容
     */
    public static void copyToClipboard(String content) {
        CopyPasteManager manager = CopyPasteManager.getInstance();
        manager.setContents(new StringSelection(content));
    }

    /**
     * 成功消息提示
     *
     * @param project 项目对象
     * @param content 消息内容
     */
    public static void showSuccess(Project project, String content) {
        showNotify(project, "成功", content, NotificationType.INFORMATION);
    }

    /**
     * 错误消息提示
     *
     * @param project 项目对象
     * @param content 消息内容
     */
    public static void showError(Project project, String content) {
        showNotify(project, "失败", content, NotificationType.ERROR);
    }

    /**
     * 警告消息提示
     *
     * @param project 项目对象
     * @param content 消息内容
     */
    public static void showWarning(Project project, String content) {
        showNotify(project, "提示", content, NotificationType.WARNING);
    }

    /**
     * 状态栏消息提示
     *
     * @param project 项目对象
     * @param message 消息内容
     */
    public static void showStatusBarTip(Project project, String message) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (Objects.isNull(statusBar)) {
            return;
        }

        statusBar.setInfo(message);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                statusBar.setInfo("");
            }
        }, STATUS_BAR_TIP_SECONDS);
    }

    /**
     * 消息通知
     *
     * @param project          项目对象
     * @param title            通知标题
     * @param message          通知内容
     * @param notificationType 通知类型
     */
    private static void showNotify(Project project, String title, String message, NotificationType notificationType) {
        NotificationGroupManager instance = NotificationGroupManager.getInstance();
        NotificationGroup notificationGroup = instance.getNotificationGroup(NOTIFICATION_GROUP_ID);
        if (Objects.isNull(notificationGroup)) {
            return;
        }
        Notification notification = notificationGroup.createNotification(title, message, notificationType);
        notification.notify(project);
    }
}
