package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:38
 **/

@Slf4j
@Service
@Transactional
public class ComponentService {

    private final ComponentRepository componentRepository;
    private final ComponentHistoryService componentHistoryService;

    @Autowired
    public ComponentService(ComponentRepository componentRepository, ComponentHistoryService componentHistoryService) {
        this.componentRepository = componentRepository;
        this.componentHistoryService = componentHistoryService;
    }

    // 根据工程保存组件
    public ComponentEntity saveComponentByProject(ProjectEntity projectEntity, ComponentEntity componentEntity) {
        if (StringUtils.isEmpty(componentEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_NAME_ARGS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(componentEntity.getVersion())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_VERSION_ARGS_NOT_FOUND);
        }
        if (hasComponentByNameAndVersionAndDeletedAndProject(componentEntity.getName(), componentEntity.getVersion(), false, projectEntity)) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_NAME_AND_VERSION_EXISTED + componentEntity.getName() + "-" + componentEntity.getVersion());
        }
        if (StringUtils.isEmpty(componentEntity.getRelativePath())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_RELATIVE_PATH_ARGS_NOT_FOUND);
        }
        componentEntity.setRelativePath(FormatUtils.formatPath(componentEntity.getRelativePath()));
        componentEntity.setProjectEntity(projectEntity);
        componentRepository.save(componentEntity);
        componentHistoryService.saveComponentHistoryByComponent(componentEntity);
        return componentEntity;
    }

    // 根据组件名称、版本、是否删除及工程查询组件是否存在
    public boolean hasComponentByNameAndVersionAndDeletedAndProject(String name, String version, boolean deleted, ProjectEntity projectEntity) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(version)) {
            return false;
        }
        return componentRepository.existsByNameAndVersionAndDeletedAndProjectEntity(name, version, deleted, projectEntity);
    }

    // 根据Id查询组件是否存在
    public boolean hasComponentById(String componentId) {
        if (StringUtils.isEmpty(componentId)) {
            return false;
        }
        return componentRepository.existsById(componentId);
    }

    // 查询所有组件
    public Page<ComponentEntity> getComponents(Pageable pageable) {
        return componentRepository.findAll(pageable);
    }

    // 根据Id查询组件
    public ComponentEntity getComponentById(String componentId) {
        if (hasComponentById(componentId)) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_ID_NOT_FOUND + componentId);
        }
        return componentRepository.findById(componentId).get();
    }

    // 根据工程查询组件
    public Page<ComponentEntity> getComponentsByDeletedAndProject(Pageable pageable, boolean deleted, ProjectEntity projectEntity) {
        return componentRepository.findByDeletedAndProjectEntity(pageable, deleted, projectEntity);
    }

    // 根据工程查询组件数量
    public long countComponentsByDeletedAndProject(boolean deleted, ProjectEntity projectEntity) {
        return componentRepository.countByDeletedAndProjectEntity(deleted, projectEntity);
    }

    // 生成不重复的组件名称
    public String getName(String name, String version, ProjectEntity projectEntity) {
        int index = 0;
        while (hasComponentByNameAndVersionAndDeletedAndProject(name, version, false, projectEntity)) {
            index = index + 1;
            name = name + "(" + index + ")";
        }
        return name;
    }
}
