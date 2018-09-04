package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentHistoryEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentHistoryRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 14:10
 **/

@Slf4j
@Service
@Transactional
public class ComponentHistoryService {

    private final ComponentHistoryRepository componentHistoryRepository;
    @Autowired
    private ComponentFileHistoryService componentFileHistoryService;

    @Autowired
    public ComponentHistoryService(ComponentHistoryRepository componentHistoryRepository) {
        this.componentHistoryRepository = componentHistoryRepository;
    }

    // 根据组件保存组件历史
    public ComponentHistoryEntity saveComponentHistoryByComponent(ComponentEntity sourceComponent) {
        ComponentHistoryEntity componentHistoryEntity = new ComponentHistoryEntity();
        BeanUtils.copyProperties(sourceComponent, componentHistoryEntity, "id");
        componentHistoryEntity.setTag(System.currentTimeMillis());
        componentHistoryEntity.setComponentEntity(sourceComponent);
        componentHistoryRepository.save(componentHistoryEntity);
        componentFileHistoryService.saveComponentFileHistorysByComponent(sourceComponent, componentHistoryEntity);
        return componentHistoryEntity;
    }

    // 根据Id查询组件历史是否存在
    public boolean hasComponentHistoryById(String componentHistoryId) {
        if (StringUtils.isEmpty(componentHistoryId)) {
            return false;
        }
        return componentHistoryRepository.existsById(componentHistoryId);
    }

    //根据Id查询组件历史
    public ComponentHistoryEntity getComponentHistoryById(String componentHistoryId) {
        if (!hasComponentHistoryById(componentHistoryId)) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_HISTORY_ID_NOT_FOUND + componentHistoryId);
        }
        return componentHistoryRepository.findById(componentHistoryId).get();
    }

    // 根据组件查询组件历史
    public Page<ComponentHistoryEntity> getComponentHistorysByComponent(Pageable pageable, ComponentEntity componentEntity) {
        return componentHistoryRepository.findAllByComponentEntity(pageable, componentEntity);
    }
}
