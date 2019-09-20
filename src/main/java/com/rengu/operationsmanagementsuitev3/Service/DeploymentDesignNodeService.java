package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.DeployMetaEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignNodeRepository;
import com.rengu.operationsmanagementsuitev3.Repository.DeviceRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 10:01
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignNodeService {

    private final DeploymentDesignNodeRepository deploymentDesignNodeRepository;
    private final DeployMetaService deployMetaService;
    private final DeploymentDesignDetailService deploymentDesignDetailService;
    private final DeviceRepository deviceRepository;

    @Autowired
    public DeploymentDesignNodeService(DeploymentDesignNodeRepository deploymentDesignNodeRepository, DeployMetaService deployMetaService, DeploymentDesignDetailService deploymentDesignDetailService,DeviceRepository deviceRepository) {
        this.deploymentDesignNodeRepository = deploymentDesignNodeRepository;
        this.deployMetaService = deployMetaService;
        this.deploymentDesignDetailService = deploymentDesignDetailService;
        this.deviceRepository = deviceRepository;
    }

    // 根据部署设计保存部署节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public DeploymentDesignNodeEntity saveDeploymentDesignNodeByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity, DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        deploymentDesignNodeEntity.setDeploymentDesignEntity(deploymentDesignEntity);
        deploymentDesignNodeEntity.setDeviceEntity( deviceRepository.save(deviceEntity));
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据Id和设备建立部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public DeploymentDesignNodeEntity saveDeploymentDesignNodeByDeploymentDesignAndDevice(DeploymentDesignEntity deploymentDesignEntity, DeploymentDesignNodeEntity deploymentDesignNodeEntity, DeviceEntity deviceEntity) {
        if (hasDeploymentDesignNodeByDeviceAndDeploymentDesign(deviceEntity, deploymentDesignNodeEntity.getDeploymentDesignEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_EXISTED + deviceEntity.getHostAddress());
        }
        deploymentDesignNodeEntity.setDeviceEntity(deviceEntity);
        deploymentDesignNodeEntity.setDeploymentDesignEntity(deploymentDesignEntity);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据部署设计复制部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public void copyDeploymentDesignNodeByDeploymentDesign(DeploymentDesignEntity sourceDeploymentDesign, DeploymentDesignEntity targetDeploymentDesign) {
        for (DeploymentDesignNodeEntity deploymentDesignNodeArgs : getDeploymentDesignNodesByDeploymentDesign(sourceDeploymentDesign)) {
            DeploymentDesignNodeEntity deploymentDesignNodeEntity = new DeploymentDesignNodeEntity();
            BeanUtils.copyProperties(deploymentDesignNodeArgs, deploymentDesignNodeEntity, "id", "createTime", "deploymentDesignEntity");
            deploymentDesignNodeEntity.setDeploymentDesignEntity(targetDeploymentDesign);
            deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
            deploymentDesignDetailService.copyDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeArgs, deploymentDesignNodeEntity);
        }
    }

    // 根据Id删除部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public DeploymentDesignNodeEntity deleteDeploymentDesignNodeById(String deploymentDesignNodeId) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        deploymentDesignDetailService.deleteDeploymentDesignDetailByDeploymentDesignNode(deploymentDesignNodeEntity);
        deploymentDesignNodeRepository.delete(deploymentDesignNodeEntity);
        return deploymentDesignNodeEntity;
    }

    // 根据Id删除部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public List<DeploymentDesignNodeEntity> deleteDeploymentDesignNodeByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity) {
        List<DeploymentDesignNodeEntity> deploymentDesignNodeEntityList = getDeploymentDesignNodesByDeploymentDesign(deploymentDesignEntity);
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeEntityList) {
            deleteDeploymentDesignNodeById(deploymentDesignNodeEntity.getId());
        }
        return deploymentDesignNodeEntityList;
    }

    // 根据Id删除部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public List<DeploymentDesignNodeEntity> deleteDeploymentDesignNodeByDevice(DeviceEntity deviceEntity) {
        List<DeploymentDesignNodeEntity> deploymentDesignNodeEntityList = getDeploymentDesignNodesByDevice(deviceEntity);
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeEntityList) {
            deploymentDesignNodeEntity.setDeviceEntity(null);
            deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
        }
        return deploymentDesignNodeEntityList;
    }

    // 根据Id绑定设备
    @CacheEvict(value = {"DeploymentDesignNode_Cache", "DeploymentDesignDetail_Cache"}, allEntries = true)
    public DeploymentDesignNodeEntity bindDeviceById(String deploymentDesignNodeId, DeviceEntity deviceEntity) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        if (hasDeploymentDesignNodeByDeviceAndDeploymentDesign(deviceEntity, deploymentDesignNodeEntity.getDeploymentDesignEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_EXISTED + deviceEntity.getHostAddress());
        }
        deploymentDesignNodeEntity.setDeviceEntity(deviceEntity);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据Id解绑设备
    @CacheEvict(value = {"DeploymentDesignNode_Cache", "DeploymentDesignDetail_Cache"}, allEntries = true)
    public DeploymentDesignNodeEntity unbindDeviceById(String deploymentDesignNodeId) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        deploymentDesignNodeEntity.setDeviceEntity(null);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据Id判断部署设计节点是否存在
    public boolean hasDeploymentDesignNodeById(String deploymentDesignNodeId) {
        if (StringUtils.isEmpty(deploymentDesignNodeId)) {
            return false;
        }
        return deploymentDesignNodeRepository.existsById(deploymentDesignNodeId);
    }

    // 根据设备和部署设计查询实存已存在该部署节点
    public boolean hasDeploymentDesignNodeByDeviceAndDeploymentDesign(DeviceEntity deviceEntity, DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.existsByDeviceEntityAndDeploymentDesignEntity(deviceEntity, deploymentDesignEntity);
    }

    // 根据id查询部署设计节点
    @Cacheable(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignNodeId")
    public DeploymentDesignNodeEntity getDeploymentDesignNodeById(String deploymentDesignNodeId) {
        if (!hasDeploymentDesignNodeById(deploymentDesignNodeId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_ID_NOT_FOUND + deploymentDesignNodeId);
        }
        return deploymentDesignNodeRepository.findById(deploymentDesignNodeId).get();
    }

    // 根据部署设计查询部署设计节点
    public Page<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDeploymentDesign(Pageable pageable, DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.findAllByDeploymentDesignEntity(pageable, deploymentDesignEntity);
    }

    // 根据部署设计查询部署设计节点
    @Cacheable(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignEntity.getId()")
    public List<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.findAllByDeploymentDesignEntity(deploymentDesignEntity);
    }

    // 根据部署设计查询部署设计节点
    @Cacheable(value = "DeploymentDesignNode_Cache", key = "#deviceEntity.getId()")
    public List<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDevice(DeviceEntity deviceEntity) {
        return deploymentDesignNodeRepository.findAllByDeviceEntity(deviceEntity);
    }

    // 根据部署设计查询设备
    public List<DeviceEntity> getDevicesByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity) {
        List<DeviceEntity> deviceEntityList = new ArrayList<>();
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : getDeploymentDesignNodesByDeploymentDesign(deploymentDesignEntity)) {
            if (deploymentDesignNodeEntity.getDeviceEntity() != null) {
                deviceEntityList.add(deploymentDesignNodeEntity.getDeviceEntity());
            }
        }
        return deviceEntityList;
    }

    // 根据部署设计节点部署
    @Async
    public void deployDeploymentDesignNodeById(String deploymentDesignNodeId) throws IOException {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
        }
        List<DeployMetaEntity> deployMetaEntityList = deployMetaService.createDeployMeta(deploymentDesignDetailService.getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity));
        deployMetaService.deployMeta(deploymentDesignNodeEntity.getDeploymentDesignEntity(), deviceEntity, deployMetaEntityList);
    }
}
