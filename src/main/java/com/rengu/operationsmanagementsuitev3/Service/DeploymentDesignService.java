package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-03 17:31
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignService {

    private final DeploymentDesignRepository deploymentDesignRepository;
    private final DeploymentDesignNodeService deploymentDesignNodeService;

    @Autowired
    public DeploymentDesignService(DeploymentDesignRepository deploymentDesignRepository, DeploymentDesignNodeService deploymentDesignNodeService) {
        this.deploymentDesignRepository = deploymentDesignRepository;
        this.deploymentDesignNodeService = deploymentDesignNodeService;
    }

    // 根据工程保存部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public DeploymentDesignEntity saveDeploymentDesignByComponent(ComponentEntity componentEntity, DeploymentDesignEntity deploymentDesignEntity) {
        if (StringUtils.isEmpty(deploymentDesignEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NAME_ARGS_NOT_FOUND);
        }
        if (hasDeploymentDesignByNameAndDeletedAndComponent(deploymentDesignEntity.getName(), false, componentEntity)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NAME_EXISTED + deploymentDesignEntity.getName());
        }
        deploymentDesignEntity.setComponentEntity(componentEntity);

        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据Id复制部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public DeploymentDesignEntity copyDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignArgs = getDeploymentDesignById(deploymentDesignId);
        DeploymentDesignEntity deploymentDesignEntity = new DeploymentDesignEntity();
        BeanUtils.copyProperties(deploymentDesignArgs, deploymentDesignEntity, "id", "createTime", "baseline", "name");
        deploymentDesignEntity.setName(getDeploymentDesignName(deploymentDesignArgs));
        deploymentDesignRepository.save(deploymentDesignEntity);
        deploymentDesignNodeService.copyDeploymentDesignNodeByDeploymentDesign(deploymentDesignArgs, deploymentDesignEntity);
        return deploymentDesignEntity;
    }

    // 根据Id创建基线
    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public DeploymentDesignEntity baselineDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = copyDeploymentDesignById(deploymentDesignId);
        deploymentDesignEntity.setBaseline(true);



        deploymentDesignRepository.save(deploymentDesignEntity);
        return deploymentDesignEntity;
    }

    // 根据id删除部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public DeploymentDesignEntity deleteDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        deploymentDesignEntity.setDeleted(true);
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据id撤销删除部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public DeploymentDesignEntity restoreDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        deploymentDesignEntity.setName(getDeploymentDesignName(deploymentDesignEntity));
        deploymentDesignEntity.setDeleted(false);
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据id清除部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public DeploymentDesignEntity cleanDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        deploymentDesignNodeService.deleteDeploymentDesignNodeByDeploymentDesign(deploymentDesignEntity);
        deploymentDesignRepository.delete(deploymentDesignEntity);
        return deploymentDesignEntity;
    }

    public List<DeploymentDesignEntity> deleteDeploymentDesignByComponent(ComponentEntity componentEntity) {
        List<DeploymentDesignEntity> deploymentDesignEntityList = getDeploymentDesignsByComponent(componentEntity);
        for (DeploymentDesignEntity deploymentDesignEntity : deploymentDesignEntityList) {
            cleanDeploymentDesignById(deploymentDesignEntity.getId());
        }
        return deploymentDesignEntityList;
    }

    // 根据id修改部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public DeploymentDesignEntity updateDeploymentDesignById(String deploymentDesignId, DeploymentDesignEntity deploymentDesignArgs) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        if (!StringUtils.isEmpty(deploymentDesignArgs.getName()) && !deploymentDesignEntity.getName().equals(deploymentDesignArgs.getName())) {
            if (hasDeploymentDesignByNameAndDeletedAndComponent(deploymentDesignArgs.getName(), false, deploymentDesignEntity.getComponentEntity())) {
                throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NAME_EXISTED + deploymentDesignArgs.getName());
            }
            deploymentDesignEntity.setName(deploymentDesignArgs.getName());
        }
        if (deploymentDesignArgs.getDescription() != null && !deploymentDesignEntity.getDescription().equals(deploymentDesignArgs.getDescription())) {
            deploymentDesignEntity.setDescription(deploymentDesignArgs.getDescription());
        }
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据名称、是否删除及工程判断是否存在工程
    public boolean hasDeploymentDesignByNameAndDeletedAndComponent(String name, boolean deleted, ComponentEntity componentEntity) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return deploymentDesignRepository.existsByNameAndDeletedAndComponentEntity(name, deleted, componentEntity);
    }

    // 根据Id判断部署设计是否存在
    public boolean hasDeploymentDesignById(String deploymentDesignId) {
        if (StringUtils.isEmpty(deploymentDesignId)) {
            return false;
        }
        return deploymentDesignRepository.existsById(deploymentDesignId);
    }

    // 下发整个部署设计
    public void deployDeploymentDesignById(String deploymentDesignId) throws IOException {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeService.getDeploymentDesignNodesByDeploymentDesign(deploymentDesignEntity)) {
            deploymentDesignNodeService.deployDeploymentDesignNodeById(deploymentDesignNodeEntity.getId());
        }
    }

    // 查询全部部署组件
    public Page<DeploymentDesignEntity> getDeploymentDesigns(Pageable pageable) {
        return deploymentDesignRepository.findAll(pageable);
    }

    // 根据Id查询部署设计
    @Cacheable(value = "DeploymentDesign_Cache", key = "#deploymentDesignId")
    public DeploymentDesignEntity getDeploymentDesignById(String deploymentDesignId) {
        if (!hasDeploymentDesignById(deploymentDesignId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_ID_NOT_FOUND + deploymentDesignId);
        }
        return deploymentDesignRepository.findById(deploymentDesignId).get();
    }

    // 根据是否删除及工程查询部署设计
    public Page<DeploymentDesignEntity> getDeploymentDesignsByDeletedAndComponent(Pageable pageable, boolean deleted, ComponentEntity componentEntity) {
        return deploymentDesignRepository.findAllByDeletedAndComponentEntity(pageable, deleted, componentEntity);
    }

    // 根据是否删除及工程查询部署设计
    public List<DeploymentDesignEntity> getDeploymentDesignsByDeletedAndComponent(boolean deleted, ComponentEntity componentEntity) {
        return deploymentDesignRepository.findAllByDeletedAndComponentEntity(deleted, componentEntity);
    }

    // 根据是否删除及工程查询部署设计
    public List<DeploymentDesignEntity> getDeploymentDesignsByComponent(ComponentEntity componentEntity) {
        return deploymentDesignRepository.findAllByComponentEntity(componentEntity);
    }

    // 根据是否删除及工程查询部署设计数量
    public long countDeploymentDesignsByDeletedAndComponent(boolean deleted, ComponentEntity componentEntity) {
        return deploymentDesignRepository.countAllByDeletedAndComponentEntity(deleted, componentEntity);
    }

    // 生成不重复的部署设计名称
    private String getDeploymentDesignName(DeploymentDesignEntity deploymentDesignEntity) {
        String name = deploymentDesignEntity.getName();
        if (hasDeploymentDesignByNameAndDeletedAndComponent(name, false, deploymentDesignEntity.getComponentEntity())) {
            int index = 0;
            String tempName = name;
            if (name.contains("@")) {
                tempName = name.substring(0, name.lastIndexOf("@"));
                index = Integer.parseInt(name.substring(name.lastIndexOf("@") + 1)) + 1;
                name = tempName + "@" + index;
            }
            while (hasDeploymentDesignByNameAndDeletedAndComponent(name, false, deploymentDesignEntity.getComponentEntity())) {
                name = tempName + "@" + index;
                index = index + 1;
            }
            return name;
        } else {
            return name;
        }
    }
}
