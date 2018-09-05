package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileHistoryEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeployMetaEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignDetailEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-05 12:44
 **/

@Slf4j
@Service
public class DeployMetaService {

    public static final Map<String, DeviceEntity> DEPLOYING_DEVICE = new ConcurrentHashMap<>();

    private final ComponentFileHistoryService componentFileHistoryService;

    @Autowired
    public DeployMetaService(ComponentFileHistoryService componentFileHistoryService) {
        this.componentFileHistoryService = componentFileHistoryService;
    }

    // 根据部署设计详情创建部署信息
    public List<DeployMetaEntity> createDeployMeta(List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList) {
        List<DeployMetaEntity> deployMetaEntityList = new ArrayList<>();
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntityList) {
            for (ComponentFileHistoryEntity componentFileHistoryEntity : componentFileHistoryService.getComponentFileHistorysByComponentHistory(deploymentDesignDetailEntity.getComponentHistoryEntity())) {
                if (!componentFileHistoryEntity.isFolder()) {
                    DeployMetaEntity deployMetaEntity = new DeployMetaEntity();
                    deployMetaEntity.setDeviceEntity(deploymentDesignDetailEntity.getDeploymentDesignNodeEntity().getDeviceEntity());
                    deployMetaEntity.setComponentHistoryEntity(deploymentDesignDetailEntity.getComponentHistoryEntity());
                    deployMetaEntity.setFileEntity(componentFileHistoryEntity.getFileEntity());
                    deployMetaEntityList.add(deployMetaEntity);
                }
            }
        }
        return deployMetaEntityList;
    }

    // 部署元数据
    public void deployMeta(DeviceEntity deviceEntity, List<DeployMetaEntity> deployMetaEntityList) {
        if (DEPLOYING_DEVICE.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_IS_DEPOLOYING + deviceEntity.getHostAddress());
        } else {
            DEPLOYING_DEVICE.put(deviceEntity.getHostAddress(), deviceEntity);
        }
    }
}
