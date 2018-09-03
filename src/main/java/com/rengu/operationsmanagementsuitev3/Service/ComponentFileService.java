package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileMetaEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentFileRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.CompressUtils;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-29 14:19
 **/

@Slf4j
@Service
@Transactional
public class ComponentFileService {

    private final ComponentFileRepository componentFileRepository;
    private final FileService fileService;
    private final ComponentHistoryService componentHistoryService;
    private final ComponentFileHistoryService componentFileHistoryService;

    @Autowired
    public ComponentFileService(ComponentFileRepository componentFileRepository, FileService fileService, ComponentHistoryService componentHistoryService, ComponentFileHistoryService componentFileHistoryService) {
        this.componentFileRepository = componentFileRepository;
        this.fileService = fileService;
        this.componentHistoryService = componentHistoryService;
        this.componentFileHistoryService = componentFileHistoryService;
    }

    // 根据组件父节点创建文件夹
    public ComponentFileEntity saveComponentFileByParentNodeAndComponent(ComponentEntity componentEntity, String parentNodeId, ComponentFileEntity componentFileEntity) {
        ComponentFileEntity parentNode = hasComponentFileById(parentNodeId) ? getComponentFileById(parentNodeId) : null;
        if (StringUtils.isEmpty(componentFileEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_FILE_NAME_ARGS_NOT_FOUND);
        }
        componentFileEntity.setName(getName(componentFileEntity.getName(), parentNode, componentEntity));
        componentFileEntity.setFolder(true);
        componentFileEntity.setParentNode(parentNode);
        componentFileEntity.setComponentEntity(componentEntity);
        componentFileRepository.save(componentFileEntity);
        componentFileHistoryService.saveComponentFileHistorysByComponent(componentEntity, componentHistoryService.saveComponentHistoryByComponent(componentEntity));
        return componentFileEntity;
    }

    // 根据组件父节点保存文件
    public List<ComponentFileEntity> saveComponentFilesByParentNodeAndComponent(ComponentEntity componentEntity, String parentNodeId, List<FileMetaEntity> fileMetaEntityList) {
        List<ComponentFileEntity> componentFileEntityList = new ArrayList<>();
        for (FileMetaEntity fileMetaEntity : fileMetaEntityList) {
            ComponentFileEntity parentNode = hasComponentFileById(parentNodeId) ? getComponentFileById(parentNodeId) : null;
            for (String path : fileMetaEntity.getRelativePath().split("/")) {
                if (!StringUtils.isEmpty(path)) {
                    if (path.equals(fileMetaEntity.getName())) {
                        // 文件节点，先判断是否存在该节点
                        if (hasComponentFileByNameAndParentNodeAndComponent(path, parentNode, componentEntity)) {
                            ComponentFileEntity componentFileEntity = getComponentFileByNameAndParentNodeAndComponent(path, parentNode, componentEntity);
                            componentFileEntity.setCreateTime(new Date());
                            componentFileEntity.setName(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                            componentFileEntity.setFolder(false);
                            componentFileEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                            componentFileEntityList.add(componentFileRepository.save(componentFileEntity));
                        } else {
                            ComponentFileEntity componentFileEntity = new ComponentFileEntity();
                            componentFileEntity.setName(StringUtils.isEmpty(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath())) ? "-" : FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
                            componentFileEntity.setFolder(false);
                            componentFileEntity.setFileEntity(fileService.getFileById(fileMetaEntity.getFileId()));
                            componentFileEntity.setParentNode(parentNode);
                            componentFileEntity.setComponentEntity(componentEntity);
                            componentFileEntityList.add(componentFileRepository.save(componentFileEntity));
                        }
                    } else {
                        // 路径节点，先判断是否存在该节点
                        if (hasComponentFileByNameAndParentNodeAndComponent(path, parentNode, componentEntity)) {
                            parentNode = getComponentFileByNameAndParentNodeAndComponent(path, parentNode, componentEntity);
                        } else {
                            ComponentFileEntity componentFileEntity = new ComponentFileEntity();
                            componentFileEntity.setName(path);
                            componentFileEntity.setFolder(true);
                            componentFileEntity.setParentNode(parentNode);
                            componentFileEntity.setComponentEntity(componentEntity);
                            parentNode = componentFileRepository.save(componentFileEntity);
                            componentFileEntityList.add(componentFileEntity);
                        }
                    }
                }
            }
        }
        if (componentFileEntityList.isEmpty()) {
            componentFileHistoryService.saveComponentFileHistorysByComponent(componentEntity, componentHistoryService.saveComponentHistoryByComponent(componentEntity));
        }
        return componentFileEntityList;
    }

    // 根据Id复制组件文件
    public ComponentFileEntity copyComponentFileById(String sourceNodeId, String targetNodeId, ComponentEntity targetComponent) {
        ComponentFileEntity sourceNode = getComponentFileById(sourceNodeId);
        ComponentFileEntity targetNode = hasComponentFileById(targetNodeId) ? getComponentFileById(targetNodeId) : null;
        copyComponentFiles(sourceNode, sourceNode.getComponentEntity(), targetNode, targetComponent);
        componentFileHistoryService.saveComponentFileHistorysByComponent(targetComponent, componentHistoryService.saveComponentHistoryByComponent(targetComponent));
        return sourceNode;
    }

    // 根据组件复制组件文件
    public void copyComponentFileByComponent(ComponentEntity sourceComponent, ComponentEntity targetComponent) {
        for (ComponentFileEntity componentFileEntity : getComponentFilesByParentNodeAndComponent(null, sourceComponent)) {
            copyComponentFiles(componentFileEntity, sourceComponent, null, targetComponent);
        }
    }

    // 复制组件文件
    public void copyComponentFiles(ComponentFileEntity sourceNode, ComponentEntity sourceComponent, ComponentFileEntity targetNode, ComponentEntity targetComponent) {
        ComponentFileEntity copyNode;
        // 目标路径下是否有同名节点
        if (hasComponentFileByNameAndParentNodeAndComponent(sourceNode.getName(), targetNode, targetComponent)) {
            copyNode = getComponentFileByNameAndParentNodeAndComponent(sourceNode.getName(), targetNode, targetComponent);
        } else {
            copyNode = new ComponentFileEntity();
            BeanUtils.copyProperties(sourceNode, copyNode, "id", "createTime", "parentNode", "componentEntity");
            copyNode.setParentNode(targetNode);
            copyNode.setComponentEntity(targetComponent);
            componentFileRepository.save(copyNode);
        }
        // 递归遍历子节点进行复制
        for (ComponentFileEntity tempComponentFile : getComponentFilesByParentNodeAndComponent(copyNode.getId(), sourceComponent)) {
            copyComponentFiles(tempComponentFile, sourceComponent, copyNode, targetComponent);
        }
    }

    // 根据Id移动组件文件
    public ComponentFileEntity moveComponentFileById(String sourceNodeId, String targetNodeId, ComponentEntity targetComponent) {
        ComponentFileEntity sourceComponentFile = getComponentFileById(sourceNodeId);
        ComponentFileEntity targetComponentFile = hasComponentFileById(targetNodeId) ? getComponentFileById(targetNodeId) : null;
        sourceComponentFile.setParentNode(targetComponentFile);
        sourceComponentFile.setComponentEntity(targetComponent);
        componentFileRepository.save(sourceComponentFile);
        componentFileHistoryService.saveComponentFileHistorysByComponent(targetComponent, componentHistoryService.saveComponentHistoryByComponent(targetComponent));
        return targetComponentFile;
    }

    // 根据Id删除组件文件
    public ComponentFileEntity deleteComponentFileById(String componentfileId) throws IOException {
        ComponentFileEntity componentFileEntity = getComponentFileById(componentfileId);
        if (componentFileEntity.isFolder()) {
            // 是文件夹, 获取子文件遍历递归
            for (ComponentFileEntity tempComponentFile : getComponentFilesByParentNodeAndComponent(componentFileEntity.getId(), componentFileEntity.getComponentEntity())) {
                deleteComponentFileById(tempComponentFile.getId());
            }
            componentFileRepository.deleteById(componentFileEntity.getId());
        } else {
            // 是文件，检查是否需要删除实际文件
            if (!hasComponentFileByFile(componentFileEntity.getFileEntity()) && !componentFileHistoryService.hasComponentFileHistoryByFile(componentFileEntity.getFileEntity())) {
                fileService.deleteFileById(componentFileEntity.getFileEntity().getId());
            }
            componentFileRepository.deleteById(componentFileEntity.getId());
        }
        componentFileHistoryService.saveComponentFileHistorysByComponent(componentFileEntity.getComponentEntity(), componentHistoryService.saveComponentHistoryByComponent(componentFileEntity.getComponentEntity()));
        return componentFileEntity;
    }

    // 根据Id修改组件文件
    public ComponentFileEntity updateComponentFileById(String componentfileId, ComponentFileEntity componentFileArgs) {
        ComponentFileEntity componentFileEntity = getComponentFileById(componentfileId);
        if (!StringUtils.isEmpty(componentFileArgs.getName()) && !componentFileEntity.getName().equals(FilenameUtils.getBaseName(componentFileArgs.getName()))) {
            if (hasComponentFileByNameAndParentNodeAndComponent(FilenameUtils.getBaseName(componentFileArgs.getName()), componentFileEntity.getParentNode(), componentFileEntity.getComponentEntity())) {
                throw new RuntimeException(ApplicationMessages.COMPONENT_FILE_NAME_EXISTED + componentFileArgs.getName());
            }
            componentFileEntity.setName(FilenameUtils.getBaseName(componentFileArgs.getName()));
        }
        componentFileRepository.save(componentFileEntity);
        componentFileHistoryService.saveComponentFileHistorysByComponent(componentFileEntity.getComponentEntity(), componentHistoryService.saveComponentHistoryByComponent(componentFileEntity.getComponentEntity()));
        return componentFileEntity;
    }

    // 根据Id查询组件文件是否存在
    public boolean hasComponentFileById(String componentFileId) {
        if (StringUtils.isEmpty(componentFileId)) {
            return false;
        }
        return componentFileRepository.existsById(componentFileId);
    }

    // 根据名称、父节点及组件检查文件是否存在
    public boolean hasComponentFileByNameAndParentNodeAndComponent(String name, ComponentFileEntity parentNode, ComponentEntity componentEntity) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return componentFileRepository.existsByNameAndParentNodeAndComponentEntity(name, parentNode, componentEntity);
    }

    // 根据引用文件判断是否存在
    public boolean hasComponentFileByFile(FileEntity fileEntity) {
        return componentFileRepository.existsByFileEntity(fileEntity);
    }

    // 根据名称、父节点及组件查询文件
    public ComponentFileEntity getComponentFileByNameAndParentNodeAndComponent(String name, ComponentFileEntity parentNode, ComponentEntity componentEntity) {
        return componentFileRepository.findByNameAndParentNodeAndComponentEntity(name, parentNode, componentEntity).get();
    }

    // 根据id查询组件文件
    public ComponentFileEntity getComponentFileById(String componentFileId) {
        if (!hasComponentFileById(componentFileId)) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_FILE_ID_NOT_FOUND + componentFileId);
        }
        return componentFileRepository.findById(componentFileId).get();
    }

    // 查询父节点和组件查询组件文件
    public List<ComponentFileEntity> getComponentFilesByParentNodeAndComponent(String parentNodeId, ComponentEntity componentEntity) {
        ComponentFileEntity parentNode = hasComponentFileById(parentNodeId) ? getComponentFileById(parentNodeId) : null;
        return componentFileRepository.findByParentNodeAndComponentEntity(parentNode, componentEntity);
    }

    // 获取不重复的文件/文件夹名
    public String getName(String name, ComponentFileEntity parentNode, ComponentEntity componentEntity) {
        int index = 0;
        while (hasComponentFileByNameAndParentNodeAndComponent(name, parentNode, componentEntity)) {
            index = index + 1;
            name = name + "(" + index + ")";
        }
        return name;
    }

    // 根据Id导出组件文件
    public File exportComponentFileById(String componentfileId) throws IOException {
        ComponentFileEntity componentFileEntity = getComponentFileById(componentfileId);
        if (componentFileEntity.isFolder()) {
            // 初始化导出目录
            File exportDir = new File(FileUtils.getTempDirectoryPath() + File.separator + UUID.randomUUID().toString());
            exportDir.mkdirs();
            exportComponentFiles(componentFileEntity, exportDir);
            return CompressUtils.compress(exportDir, new File(FileUtils.getTempDirectoryPath() + File.separator + System.currentTimeMillis() + ".zip"));
        } else {
            File exportFile = new File(FileUtils.getTempDirectoryPath() + File.separator + componentFileEntity.getName() + "." + componentFileEntity.getFileEntity().getType());
            FileUtils.copyFile(new File(componentFileEntity.getFileEntity().getLocalPath()), exportFile);
            return exportFile;
        }
    }

    // 导出组件文件
    public File exportComponentFiles(ComponentFileEntity componentFileEntity, File exportDir) throws IOException {
        // 检查是否为文件夹
        if (componentFileEntity.isFolder()) {
            for (ComponentFileEntity tempComponentFile : getComponentFilesByParentNodeAndComponent(componentFileEntity.getId(), componentFileEntity.getComponentEntity())) {
                exportComponentFiles(tempComponentFile, exportDir);
            }
        } else {
            File file = new File(exportDir.getAbsolutePath() + File.separator + FormatUtils.getComponentFileRelativePath(componentFileEntity, ""));
            FileUtils.copyFile(new File(componentFileEntity.getFileEntity().getLocalPath()), file);
        }
        return exportDir;
    }
}
