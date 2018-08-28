package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ChunkEntity;
import com.rengu.operationsmanagementsuitev3.Repository.FileRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
        File chunkFile = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        chunkFile.getParentFile().mkdirs();
        chunkFile.createNewFile();
        IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(chunkFile));
    }

    // 检查文件块是否存在
    public boolean hasChunk(ChunkEntity chunkEntity) {
        return new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp").exists();
    }
}
