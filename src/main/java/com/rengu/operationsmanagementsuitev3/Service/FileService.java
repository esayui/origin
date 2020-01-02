package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ChunkEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentFileRepository;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignNodeRepository;
import com.rengu.operationsmanagementsuitev3.Repository.FileRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.NodeCreatorUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

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

    private final DeploymentDesignNodeService deploymentDesignNodeService;
    private final DeploymentDesignNodeRepository deploymentDesignNodeRepository;
    private final ComponentFileRepository componentFileRepository;


    @Autowired
    public FileService(FileRepository fileRepository,DeploymentDesignNodeService deploymentDesignNodeService,DeploymentDesignNodeRepository deploymentDesignNodeRepository,ComponentFileRepository componentFileRepository) {
        this.fileRepository = fileRepository;
        this.deploymentDesignNodeRepository = deploymentDesignNodeRepository;
        this.deploymentDesignNodeService = deploymentDesignNodeService;
        this.componentFileRepository = componentFileRepository;
    }

    // 保存文件块
    public void saveChunk(ChunkEntity chunkEntity, MultipartFile multipartFile) throws IOException {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        chunk.getParentFile().mkdirs();
        chunk.createNewFile();
        if(multipartFile == null){
            return;
        }
        IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(chunk));
    }

    // 保存文件信息
    @CacheEvict(value = "File_Cache", allEntries = true)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public FileEntity saveFile(File file) throws IOException {
        FileEntity fileEntity = new FileEntity();
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        String MD5 = DigestUtils.md5Hex(fileInputStream);
        if (hasFileByMD5(MD5)) {
            throw new RuntimeException(ApplicationMessages.FILE_MD5_EXISTED + MD5);
        }
        fileEntity.setMD5(MD5);
        fileEntity.setType(FilenameUtils.getExtension(file.getName()));
        fileEntity.setSize(FileUtils.sizeOf(file));
        fileEntity.setLocalPath(file.getAbsolutePath());
        FileEntity receiveFile = fileRepository.save(fileEntity);


        if(file.getName().equals("hosts")){

            InputStream is = new FileInputStream(file);
            NodeCreatorUtil.inputStream = is;
        }

        if(deploymentDesignNodeRepository.existsById(file.getName().split("\\.")[0])){
            //TODO
            //这里给C1发送file
            DeploymentDesignNodeEntity deploymentDesignNodeEntity = deploymentDesignNodeRepository.findById(file.getName().split("\\.")[0]).get();
            ComponentFileEntity componentFileEntity = new ComponentFileEntity();
            componentFileEntity.setHistory(true);
            componentFileEntity.setType(2);
            componentFileEntity.setExtension(file.getName().split("\\.")[1]);
            componentFileEntity.setFolder(false);
            componentFileEntity.setFileEntity(receiveFile);
            componentFileEntity.setName(file.getName().split("\\.")[0]);
            componentFileEntity.setComponentEntity(deploymentDesignNodeEntity.getDeploymentDesignEntity().getComponentEntity());
            componentFileRepository.save(componentFileEntity);
            deploymentDesignNodeService.sendFile(receiveFile,ApplicationConfig.CLIENT_ADDRESS,ApplicationConfig.CLIENT1_TCP_RECEIVE,file.getName().substring(0,file.getName().lastIndexOf(".")));

        };



        return receiveFile;
    }

    // 根据Id删除文件
    @CacheEvict(value = "File_Cache", allEntries = true)
    public FileEntity deleteFileById(String fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        FileUtils.forceDeleteOnExit(new File(fileEntity.getLocalPath()));
        fileRepository.delete(fileEntity);
        return fileEntity;
    }

    // 检查文件块是否存在
    public boolean hasChunk(ChunkEntity chunkEntity) {
        File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + chunkEntity.getChunkNumber() + ".tmp");
        return chunk.exists() && chunkEntity.getChunkSize() == FileUtils.sizeOf(chunk);
    }

    // 根据Id判断文件是否存在
    public boolean hasFileById(String fileId) {
        if (StringUtils.isEmpty(fileId)) {
            return false;
        }
        return fileRepository.existsById(fileId);
    }

    // 根据Md5判断文件是否存在
    public boolean hasFileByMD5(String MD5) {
        if (StringUtils.isEmpty(MD5)) {
            return false;
        }
        return fileRepository.existsByMD5(MD5);
    }

    // 根据Id查询文件
    @Cacheable(value = "File_Cache", key = "#fileId")
    public FileEntity getFileById(String fileId) {
        if (!hasFileById(fileId)) {
            throw new RuntimeException(ApplicationMessages.FILE_ID_NOT_FOUND + fileId);
        }
        return fileRepository.findById(fileId).get();
    }

    // 根据MD5查询文件
    @Cacheable(value = "File_Cache", key = "#MD5")
    public FileEntity getFileByMD5(String MD5) {
        if (!hasFileByMD5(MD5)) {
            throw new RuntimeException(ApplicationMessages.FILE_MD5_NOT_FOUND + MD5);
        }
        return fileRepository.findByMD5(MD5).get();
    }

    // 查询所有文件
    public Page<FileEntity> getFiles(Pageable pageable) {
        return fileRepository.findAll(pageable);
    }

    // 合并文件块
    public FileEntity mergeChunks(ChunkEntity chunkEntity) throws IOException {
        if (hasFileByMD5(chunkEntity.getIdentifier())) {
            return getFileByMD5(chunkEntity.getIdentifier());
        } else {
            File file = null;
            String extension = FilenameUtils.getExtension(chunkEntity.getFilename());
            if (StringUtils.isEmpty(extension)) {
                file = new File(ApplicationConfig.FILES_SAVE_PATH + File.separator + chunkEntity.getFilename());
            } else {
                file = new File(ApplicationConfig.FILES_SAVE_PATH + File.separator + chunkEntity.getFilename());
            }
            return mergeChunks(file, chunkEntity);
        }
    }

    private FileEntity mergeChunks(File file, ChunkEntity chunkEntity) throws IOException {
        file.delete();
        file.getParentFile().mkdirs();
        file.createNewFile();
        for (int i = 1; i <= chunkEntity.getTotalChunks(); i++) {
            File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + chunkEntity.getIdentifier() + File.separator + i + ".tmp");
            if (chunk.exists()) {
                FileUtils.writeByteArrayToFile(file, FileUtils.readFileToByteArray(chunk), true);
            } else {
                throw new RuntimeException(ApplicationMessages.FILE_CHUNK_NOT_FOUND + chunk.getAbsolutePath());
            }
        }
        FileInputStream fileInputStream = null;
        fileInputStream = new FileInputStream(file);
        if (!chunkEntity.getIdentifier().equals(DigestUtils.md5Hex(fileInputStream))) {
            System.out.println("MD5:"+DigestUtils.md5Hex(fileInputStream)+"不一致");
           // throw new RuntimeException("文件合并失败，请检查：" + file.getAbsolutePath() + "是否正确。");
        }
        fileInputStream.close();

        return saveFile(file);
    }
}
