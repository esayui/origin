package com.rengu.operationsmanagementsuitev3.Utils;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
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
}
