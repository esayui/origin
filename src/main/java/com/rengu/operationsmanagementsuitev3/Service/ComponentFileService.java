package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileMetaEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentFileRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Autowired
    public ComponentFileService(ComponentFileRepository componentFileRepository, FileService fileService) {
        this.componentFileRepository = componentFileRepository;
        this.fileService = fileService;
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
        return componentFileRepository.save(componentFileEntity);
    }

    // 根据组件父节点保存文件
    public List<ComponentFileEntity> saveComponentFilesByParentNodeAndComponent(ComponentEntity componentEntity, String parentNodeId, List<FileMetaEntity> fileMetaEntityList) {
        List<ComponentFileEntity> componentFileEntityList = new ArrayList<>();
        for (FileMetaEntity fileMetaEntity : fileMetaEntityList) {
            ComponentFileEntity parentNode = hasComponentFileById(parentNodeId) ? getComponentFileById(parentNodeId) : null;
            for (String path : fileMetaEntity.getRelativePath().split("/")) {
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
                        componentFileEntity.setName(FilenameUtils.getBaseName(fileMetaEntity.getRelativePath()));
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
                    }
                }
            }
        }
        return componentFileEntityList;
    }

    // 根据Id移动组件文件
    public ComponentFileEntity moveComponentFileById(String componentfileId, String targetNodeId, ComponentEntity componentEntity) {
        ComponentFileEntity sourceComponentFile = getComponentFileById(componentfileId);
        ComponentFileEntity targetComponentFile = hasComponentFileById(targetNodeId) ? getComponentFileById(targetNodeId) : null;
        sourceComponentFile.setParentNode(targetComponentFile);
        sourceComponentFile.setComponentEntity(componentEntity);
        return componentFileRepository.save(sourceComponentFile);
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
            if (!hasComponentFileByFile(componentFileEntity.getFileEntity())) {
                fileService.deleteFileById(componentFileEntity.getFileEntity().getId());
            }
            componentFileRepository.deleteById(componentFileEntity.getId());
        }
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
        return componentFileRepository.save(componentFileEntity);
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
}
