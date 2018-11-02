package com.rengu.operationsmanagementsuitev3.Utils;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileHistoryEntity;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 13:08
 **/
public class FormatUtils {

    /**
     * 格式化文件路径，将其中不规范的分隔转换为标准的分隔符,并且去掉末尾的"/"符号。
     *
     * @param path 文件路径
     * @return 格式化后的文件路径
     */
    public static String formatPath(String path) {
        String reg0 = "\\\\＋";
        String reg = "\\\\＋|/＋";
        String temp = path.trim().replaceAll(reg0, "/");
        temp = temp.replaceAll(reg, "/");
        if (temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        if (System.getProperty("file.separator").equals("\\")) {
            temp = temp.replace('/', '\\');
        }
        return FilenameUtils.separatorsToUnix(temp);
    }

    // 生成指定长度的字符串
    public static String getString(String string, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(string);
        stringBuilder.setLength(length);
        return stringBuilder.toString();
    }


    // 递归拼接path信息
    public static String getComponentFileRelativePath(ComponentFileEntity componentFileEntity, String basePath) {
        if (basePath.isEmpty()) {
            if (componentFileEntity.isFolder()) {
                basePath = File.separatorChar + componentFileEntity.getName() + File.separatorChar;
            } else {
                basePath = File.separatorChar + componentFileEntity.getName() + "." + componentFileEntity.getFileEntity().getType();
            }
        }
        while (componentFileEntity.getParentNode() != null) {
            componentFileEntity = componentFileEntity.getParentNode();
            basePath = File.separatorChar + componentFileEntity.getName() + basePath;
            getComponentFileRelativePath(componentFileEntity, basePath);
        }
        return FormatUtils.formatPath(basePath);
    }

    // 递归拼接path信息
    public static String getComponentFileHistoryRelativePath(ComponentFileHistoryEntity componentFileHistoryEntity, String basePath) {
        if (basePath.isEmpty()) {
            if (componentFileHistoryEntity.isFolder()) {
                basePath = File.separatorChar + componentFileHistoryEntity.getName() + File.separatorChar;
            } else {
                basePath = File.separatorChar + componentFileHistoryEntity.getName() + "." + componentFileHistoryEntity.getFileEntity().getType();
            }
        }
        while (componentFileHistoryEntity.getParentNode() != null) {
            componentFileHistoryEntity = componentFileHistoryEntity.getParentNode();
            basePath = File.separatorChar + componentFileHistoryEntity.getName() + basePath;
            getComponentFileHistoryRelativePath(componentFileHistoryEntity, basePath);
        }
        return FormatUtils.formatPath(basePath);
    }
}
