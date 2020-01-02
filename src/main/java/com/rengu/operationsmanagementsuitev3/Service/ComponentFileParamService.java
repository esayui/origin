package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileParamEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentParamEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentFileParamRepository;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentParamRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;


@Slf4j
@Service
@Transactional
public class ComponentFileParamService {

    private final ComponentFileParamRepository componentFileParamRepository;


    @Autowired
    public ComponentFileParamService(ComponentFileParamRepository componentFileParamRepository){
        this.componentFileParamRepository = componentFileParamRepository;

    }


    //保存参数配置
    @CacheEvict(value = "ComponentFileParam_Cache", allEntries = true)
    public ComponentFileParamEntity saveComponentFileParamByComponentFile(ComponentFileEntity componentFileEntity, ComponentFileParamEntity componentFileParamEntity){
        componentFileParamEntity.setComponentFileEntity(componentFileEntity);

        return componentFileParamRepository.save(componentFileParamEntity);
    }


    //更新参数配置
    @CacheEvict(value = "ComponentFileParam_Cache", allEntries = true)
    public ComponentFileParamEntity updateComponentFileParamById(String componentFileParamId,ComponentFileParamEntity componentFileParamEntity){
        ComponentFileParamEntity componentFileParamEntityOld = getComponentFileParamById(componentFileParamId);
        if(StringUtils.isEmpty(componentFileParamEntity.getName())||StringUtils.isEmpty(componentFileParamEntity.getType())){
            throw new RuntimeException(ApplicationMessages.COMPONENT_PARAM_NAMEANDVALUE_EXISTED + componentFileParamEntity.getName());
        }
        if(!StringUtils.isEmpty(componentFileParamEntity.getDescription())){
            componentFileParamEntityOld.setDescription(componentFileParamEntity.getDescription());
        }
        componentFileParamEntityOld.setName(componentFileParamEntity.getName());
        componentFileParamEntityOld.setType(componentFileParamEntity.getType());
        componentFileParamEntityOld.setValue(null);
        return componentFileParamRepository.save(componentFileParamEntityOld);
    }

    @CacheEvict(value = "ComponentFileParam_Cache", allEntries = true)
    public ComponentFileParamEntity updateComponentFileParamValueById(String componentFileParamId,ComponentFileParamEntity componentFileParamEntity){

        ComponentFileParamEntity componentFileParamEntityOld = getComponentFileParamById(componentFileParamId);


        componentFileParamEntityOld.setValue(componentFileParamEntity.getValue());

        return componentFileParamRepository.save(componentFileParamEntityOld);
    }



    @CacheEvict(value = "ComponentFileParam_Cache", allEntries = true)
    public ComponentFileParamEntity deleteComponentFileParamById(String componentFileParamId){

        ComponentFileParamEntity componentFileParamEntityOld = getComponentFileParamById(componentFileParamId);
        componentFileParamRepository.deleteById(componentFileParamId);

        return componentFileParamEntityOld;
    }

    // 根据Id查询组件参数设置是否存在
    public boolean hasComponentFileParamById(String componentFileParamId) {
        if (StringUtils.isEmpty(componentFileParamId)) {
            return false;
        }
        return componentFileParamRepository.existsById(componentFileParamId);
    }

    public ComponentFileParamEntity getComponentFileParamById(String componentFileParamId) {
        if(!hasComponentFileParamById(componentFileParamId)){
            throw new RuntimeException(ApplicationMessages.COMPONENT_FILE_PARAM_ID_NOT_FOUND + componentFileParamId);
        }

        return componentFileParamRepository.findById(componentFileParamId).get();

    }


    public List<ComponentFileParamEntity> saveComponentFileParamsByComponentFile(ComponentFileEntity componentFileEntity, ComponentFileParamEntity...componentFileParamEntities) {
        List<ComponentFileParamEntity> coms = new ArrayList<>();
        String pid = UUID.randomUUID().toString();
        Date date = new Date();
        List<ComponentFileParamEntity> coml = new ArrayList<>(componentFileParamEntities.length);
        Collections.addAll(coml,componentFileParamEntities);
        Consumer<ComponentFileParamEntity> consumer = x->{
           // x.setPid(pid);
            x.setCreateTime(date);
            x.setComponentFileEntity(componentFileEntity);
            coms.add(x);
        };
        coml.forEach(consumer::accept);
        componentFileParamRepository.saveAll(coms);
        return coms;
    }

    public List<ComponentFileParamEntity> getComponentFileParamsByComponent(ComponentFileEntity componentFileEntity) {

        return componentFileParamRepository.findAllByComponentFileEntity(componentFileEntity);

    }


}
