package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ChunkEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import com.rengu.operationsmanagementsuitev3.Repository.FileRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 11:03
 **/

@Slf4j
@Service
@Transactional
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // 保存文件块
    public void saveChunk(ChunkEntity chunkEntity, MultipartFile multipartFile) throws IOException {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        chunk.getParentFile().mkdirs();
        chunk.createNewFile();
        IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(chunk));
    }

    // 保存文件信息
    public FileEntity saveFile(File file) throws IOException {
        FileEntity fileEntity = new FileEntity();
        String MD5 = DigestUtils.md5Hex(new FileInputStream(file));
        if (hasFileByMD5(MD5)) {
            throw new RuntimeException(ApplicationMessages.FILE_MD5_EXISTED + MD5);
        }
        fileEntity.setMD5(MD5);
        fileEntity.setType(FilenameUtils.getExtension(file.getName()));
        fileEntity.setSize(FileUtils.sizeOf(file));
        fileEntity.setLocalPath(file.getAbsolutePath());
        return fileRepository.save(fileEntity);
    }

    // 检查文件块是否存在
    public boolean hasChunk(ChunkEntity chunkEntity) {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        return chunk.exists() && chunkEntity.getChunkSize() == FileUtils.sizeOf(chunk);
    }

    // 根据Md5判断文件是否存在
    public boolean hasFileByMD5(String MD5) {
        if (StringUtils.isEmpty(MD5)) {
            return false;
        }
        return fileRepository.existsByMD5(MD5);
    }

    // 根据MD5查询文件
    public FileEntity getFileByMD5(String MD5) {
        if (!hasFileByMD5(MD5)) {
            throw new RuntimeException(ApplicationMessages.FILE_MD5_NOT_FOUND + MD5);
        }
        return fileRepository.findByMD5(MD5).get();
    }

    // 合并文件块
    public FileEntity mergeChunk(ChunkEntity chunkEntity) throws IOException {
        if (hasFileByMD5(chunkEntity.getIdentifier())) {
            return getFileByMD5(chunkEntity.getIdentifier());
        } else {
            File file = new File(ApplicationConfig.FILES_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + "." + FilenameUtils.getExtension(chunkEntity.getFilename()));
            file.getParentFile().mkdirs();
            file.createNewFile();
            for (int i = 1; i <= chunkEntity.getTotalChunks(); i++) {
                File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + i + ".tmp");
                if (chunk.exists()) {
                    FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(new FileInputStream(chunk)), true);
                } else {
                    throw new RuntimeException(ApplicationMessages.FILE_CHUNK_NOT_FOUND + chunk.getAbsolutePath());
                }
            }
            return saveFile(file);
        }
    }
}
