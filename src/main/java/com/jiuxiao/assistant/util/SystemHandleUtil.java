package com.jiuxiao.assistant.util;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;

import java.awt.datatransfer.StringSelection;

/**
 * 系统交互操作工具类
 */
public class SystemHandleUtil {

    /**
     * 提示消息
     *
     * @param message 消息
     */
    public static void showMessage(String message) {
        Messages.showInfoMessage(message, "提示");
    }

    /**
     * 复制字符串到剪切板
     *
     * @param content 复制的内容
     */
    public static void copyToClipboard(String content) {
        CopyPasteManager manager = CopyPasteManager.getInstance();
        manager.setContents(new StringSelection(content));
    }
}
