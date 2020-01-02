package com.rengu.operationsmanagementsuitev3.Utils;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-31 17:23
 **/

@Slf4j
public class CompressUtils {

    // 相关提示信息
    private static final String COMPRESS_FILE_TYPE_ERROR = "未知的文件类型:";

    private static final String ZIP = "zip";

    // 压缩方法
    public static File compress(File srcDir, File compressFile) throws IOException {
        if (!srcDir.isDirectory()) {
            throw new RuntimeException(srcDir.getAbsolutePath() + "不是文件夹");
        }
        String extension = FilenameUtils.getExtension(compressFile.getName());
        switch (extension) {
            case ZIP:
                return compressZip(srcDir, compressFile);
            default:
                throw new RuntimeException(COMPRESS_FILE_TYPE_ERROR + extension);
        }
    }

    // 解压方法
    public static File decompress(File compressFile, File outputDir) throws ZipException {
        String extension = FilenameUtils.getExtension(compressFile.getName());
        switch (extension) {
            case ZIP:
                return decompressZip(compressFile, outputDir);
            default:
                throw new RuntimeException(COMPRESS_FILE_TYPE_ERROR + extension);
        }
    }

    private static File compressZip(File srcDir, File compressFile) throws IOException {
        String tempFolderPath = srcDir.getPath();
        ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(compressFile);
        Collection<File> fileCollection = FileUtils.listFiles(srcDir, null, true);
        for (File file : fileCollection) {
            FileInputStream fileInputStream = new FileInputStream(file);
            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, file.getPath().replace(tempFolderPath, FilenameUtils.getBaseName(compressFile.getName())));
            zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
            IOUtils.copy(fileInputStream, zipArchiveOutputStream);
            zipArchiveOutputStream.closeArchiveEntry();
            fileInputStream.close();
        }
        zipArchiveOutputStream.finish();
        zipArchiveOutputStream.close();
        return compressFile;
    }

    private static File decompressZip(File compressFile, File outputDir) throws ZipException {
        // 首先创建ZipFile指向磁盘上的.zip文件
        ZipFile zipFile = new ZipFile(compressFile);
        if (!zipFile.isValidZipFile()) {
            // 验证.zip文件是否合法，包括文件是否存在、是否为zip文件、是否被损坏等
            throw new ZipException("压缩文件验证失败，解压缩失败.");
        }
        if (outputDir.isDirectory() && !outputDir.exists()) {
            outputDir.mkdir();
        }
        // 将文件抽出到解压目录(解压)
        zipFile.extractAll(outputDir.getAbsolutePath());
        return outputDir;
    }


    /**
     * 将多个文件压缩到指定输出流中
     *
     * @param files 需要压缩的文件列表
     * @param outputStream  压缩到指定的输出流
     * @author hongwei.lian
     * @date 2018年9月7日 下午3:11:59
     */
    public static void compressZip(List<File> files, OutputStream outputStream) {
        ZipOutputStream zipOutStream = null;
        try {
            //-- 包装成ZIP格式输出流
            zipOutStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
            // -- 设置压缩方法
            zipOutStream.setMethod(ZipOutputStream.DEFLATED);
            //-- 将多文件循环写入压缩包
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                FileInputStream filenputStream = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                filenputStream.read(data);
                //-- 添加ZipEntry，并ZipEntry中写入文件流，这里，加上i是防止要下载的文件有重名的导致下载失败
                zipOutStream.putNextEntry(new ZipEntry(i + file.getName()));
                zipOutStream.write(data);
                filenputStream.close();
                zipOutStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            try {
                if (Objects.nonNull(zipOutStream)) {
                    zipOutStream.flush();
                    zipOutStream.close();
                }
                if (Objects.nonNull(outputStream)) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
