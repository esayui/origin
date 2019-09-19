package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentParamEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentParamRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Author: XYmar
 * Date: 2019/9/19 14:54
 */

@Slf4j
@Service
@Transactional
public class ComponentParamService {

    private final ComponentParamRepository componentParamRepository;


    @Autowired
    public ComponentParamService(ComponentParamRepository componentParamRepository){
        this.componentParamRepository = componentParamRepository;

    }



    @CacheEvict(value = "ComponentParam_Cache", allEntries = true)
    public ComponentParamEntity saveComponentParamByComponent(ComponentEntity componentEntity,ComponentParamEntity componentParamEntity){
        componentParamEntity.setComponentEntity(componentEntity);

        return componentParamRepository.save(componentParamEntity);
    }

    @CacheEvict(value = "ComponentParam_Cache", allEntries = true)
    public ComponentParamEntity updateComponentParamById(String componentParamId,ComponentParamEntity componentParamEntity){


        ComponentParamEntity componentParamEntityOld = getComponentParamById(componentParamId);
        if(!StringUtils.isEmpty(componentParamEntity.getName())&&!(componentParamEntityOld.getName().equals(componentParamEntity.getName())&&componentParamEntityOld.getType()==componentParamEntity.getType())){
            throw new RuntimeException(ApplicationMessages.COMPONENT_PARAM_NAMEANDVALUE_EXISTED + componentParamEntity.getName());
        }
        if(!StringUtils.isEmpty(componentParamEntity.getDescription())){
            componentParamEntityOld.setDescription(componentParamEntity.getDescription());
        }

        componentParamEntityOld.setName(componentParamEntity.getName());
        componentParamEntityOld.setType(componentParamEntity.getType());


        return componentParamRepository.save(componentParamEntityOld);
    }

    @CacheEvict(value = "ComponentParam_Cache", allEntries = true)
    public ComponentParamEntity updateComponentParamValueById(String componentParamId,ComponentParamEntity componentParamEntity){


        ComponentParamEntity componentParamEntityOld = getComponentParamById(componentParamId);
        componentParamEntityOld.setValue(componentParamEntity.getValue());

        return componentParamRepository.save(componentParamEntityOld);
    }



    @CacheEvict(value = "ComponentParam_Cache", allEntries = true)
    public ComponentParamEntity deleteComponentParamById(String componentParamId){

        ComponentParamEntity componentParamEntityOld = getComponentParamById(componentParamId);
        componentParamRepository.deleteById(componentParamId);

        return componentParamEntityOld;
    }

    // 根据Id查询组件参数设置是否存在
    public boolean hasComponentParamById(String componentParamId) {
        if (StringUtils.isEmpty(componentParamId)) {
            return false;
        }
        return componentParamRepository.existsById(componentParamId);
    }

    public ComponentParamEntity getComponentParamById(String componentParamId) {
        if(!hasComponentParamById(componentParamId)){
            throw new RuntimeException(ApplicationMessages.COMPONENT_PARAM_ID_NOT_FOUND + componentParamId);
        }

        return componentParamRepository.findById(componentParamId).get();

    }


    public List<ComponentParamEntity> saveComponentParamsByComponent(ComponentEntity componentEntity, ComponentParamEntity[] componentParamEntities) {
        List<ComponentParamEntity> coms = new ArrayList<>();
        String pid = UUID.randomUUID().toString();
        Date date = new Date();
        for(ComponentParamEntity componentParamEntity:componentParamEntities){
            componentParamEntity.setPid(pid);
            componentParamEntity.setCreateTime(date);
            coms.add(saveComponentParamByComponent(componentEntity,componentParamEntity));

        }
        return coms;
    }

    public Optional<ComponentParamEntity> getComponentParamsByComponent(ComponentEntity componentEntity) {

        return componentParamRepository.findAllByComponentEntity(componentEntity);

    }
}
