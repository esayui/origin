package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentHistoryEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ComponentFileHistoryService componentFileHistoryService;

    @Autowired
    public ComponentHistoryService(ComponentHistoryRepository componentHistoryRepository, ComponentFileHistoryService componentFileHistoryService) {
        this.componentHistoryRepository = componentHistoryRepository;
        this.componentFileHistoryService = componentFileHistoryService;
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
}
