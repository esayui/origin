package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignDetailRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 11:25
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignDetailService {

    private final DeploymentDesignDetailRepository deploymentDesignDetailRepository;
    private final OrderService orderService;
    private final ScanHandlerService scanHandlerService;
    private final DeploymentDesignScanResultService deploymentDesignScanResultService;

    @Autowired
    public DeploymentDesignDetailService(DeploymentDesignDetailRepository deploymentDesignDetailRepository, OrderService orderService, ScanHandlerService scanHandlerService, DeploymentDesignScanResultService deploymentDesignScanResultService) {
        this.deploymentDesignDetailRepository = deploymentDesignDetailRepository;
        this.orderService = orderService;
        this.scanHandlerService = scanHandlerService;
        this.deploymentDesignScanResultService = deploymentDesignScanResultService;
    }

    // 根据组件历史和部署设计节点保存部署设计详情
    @CacheEvict(value = "DeploymentDesignDetail_Cache", allEntries = true)
    public DeploymentDesignDetailEntity saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistory(DeploymentDesignNodeEntity deploymentDesignNodeEntity, ComponentHistoryEntity componentHistoryEntity) {
        if (hasDeploymentDesignDetailByDeploymentDesignNodeAndComponent(deploymentDesignNodeEntity, componentHistoryEntity.getComponentEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_DETAIL_COMPONENT_EXISTED + componentHistoryEntity.getComponentEntity().getName() + "-" + componentHistoryEntity.getComponentEntity().getVersion());
        }
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = new DeploymentDesignDetailEntity();
        deploymentDesignDetailEntity.setDeploymentDesignNodeEntity(deploymentDesignNodeEntity);
        deploymentDesignDetailEntity.setComponentHistoryEntity(componentHistoryEntity);
        deploymentDesignDetailEntity.setComponentEntity(componentHistoryEntity.getComponentEntity());
        deploymentDesignDetailEntity.setDeploymentDesignEntity(deploymentDesignNodeEntity.getDeploymentDesignEntity());
        return deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
    }

    // 根据组件历史和部署设计节点保存部署设计详情
    @CacheEvict(value = "DeploymentDesignDetail_Cache", allEntries = true)
    public List<DeploymentDesignDetailEntity> saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistorys(DeploymentDesignNodeEntity deploymentDesignNodeEntity, List<ComponentHistoryEntity> componentHistoryEntityList) {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = new ArrayList<>();
        for (ComponentHistoryEntity componentHistoryEntity : componentHistoryEntityList) {
            deploymentDesignDetailEntityList.add(saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistory(deploymentDesignNodeEntity, componentHistoryEntity));
        }
        return deploymentDesignDetailEntityList;
    }

    // 根据部署设计节点复制部署设计详情
    @CacheEvict(value = "DeploymentDesignDetail_Cache", allEntries = true)
    public void copyDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity sourceDeploymentDesignNode, DeploymentDesignNodeEntity targetDeploymentDesignNode) {
        for (DeploymentDesignDetailEntity deploymentDesignDetailArgs : getDeploymentDesignDetailsByDeploymentDesignNode(sourceDeploymentDesignNode)) {
            DeploymentDesignDetailEntity deploymentDesignDetailEntity = new DeploymentDesignDetailEntity();
            BeanUtils.copyProperties(deploymentDesignDetailArgs, deploymentDesignDetailEntity, "id", "createTime", "deploymentDesignNodeEntity", "deploymentDesignEntity");
            deploymentDesignDetailEntity.setDeploymentDesignNodeEntity(targetDeploymentDesignNode);
            deploymentDesignDetailEntity.setDeploymentDesignEntity(targetDeploymentDesignNode.getDeploymentDesignEntity());
            deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
        }
    }

    // 根据Id删除部署设计详情
    @CacheEvict(value = "DeploymentDesignDetail_Cache", allEntries = true)
    public DeploymentDesignDetailEntity deleteDeploymentDesignDetailById(String deploymentDesignDetailId) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        deploymentDesignScanResultService.deleteDeploymentDesignScanResultByDeploymentDesignDetail(deploymentDesignDetailEntity);
        deploymentDesignDetailRepository.delete(deploymentDesignDetailEntity);
        return deploymentDesignDetailEntity;
    }

    // 根据Id删除部署设计详情
    @CacheEvict(value = "DeploymentDesignDetail_Cache", allEntries = true)
    public List<DeploymentDesignDetailEntity> deleteDeploymentDesignDetailByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity);
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntityList) {
            deleteDeploymentDesignDetailById(deploymentDesignDetailEntity.getId());
        }
        return deploymentDesignDetailEntityList;
    }

    // 根据Id删除部署设计详情
    @CacheEvict(value = "DeploymentDesignDetail_Cache", allEntries = true)
    public List<DeploymentDesignDetailEntity> deleteDeploymentDesignDetailByComponent(ComponentEntity componentEntity) {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = getDeploymentDesignDetailsByComponentEntity(componentEntity);
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntityList) {
            deleteDeploymentDesignDetailById(deploymentDesignDetailEntity.getId());
        }
        return deploymentDesignDetailEntityList;
    }

    // 根据组件和部署设计节点判断是否存在
    public boolean hasDeploymentDesignDetailByDeploymentDesignNodeAndComponent(DeploymentDesignNodeEntity deploymentDesignNodeEntity, ComponentEntity componentEntity) {
        return deploymentDesignDetailRepository.existsByDeploymentDesignNodeEntityAndComponentEntity(deploymentDesignNodeEntity, componentEntity);
    }

    // 根据Id判断部署设计详情是否存在
    public boolean hasDeploymentDesignDetailById(String deploymentDesignDetailId) {
        if (StringUtils.isEmpty(deploymentDesignDetailId)) {
            return false;
        }
        return deploymentDesignDetailRepository.existsById(deploymentDesignDetailId);
    }

    // 根据Id查询部署设计详情
    @Cacheable(value = "DeploymentDesignDetail_Cache", key = "#deploymentDesignDetailId")
    public DeploymentDesignDetailEntity getDeploymentDesignDetailById(String deploymentDesignDetailId) {
        if (!hasDeploymentDesignDetailById(deploymentDesignDetailId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_DETAIL_ID_NOT_FOUND + deploymentDesignDetailId);
        }
        return deploymentDesignDetailRepository.findById(deploymentDesignDetailId).get();
    }

    // 根据部署设计节点查询部署设计详情
    @Cacheable(value = "DeploymentDesignDetail_Cache", key = "#deploymentDesignNodeEntity.getId()")
    public List<DeploymentDesignDetailEntity> getDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        return deploymentDesignDetailRepository.findAllByDeploymentDesignNodeEntity(deploymentDesignNodeEntity);
    }

    // 根据部署设计节点查询部署设计详情
    @Cacheable(value = "DeploymentDesignDetail_Cache", key = "#componentEntity.getId()")
    public List<DeploymentDesignDetailEntity> getDeploymentDesignDetailsByComponentEntity(ComponentEntity componentEntity) {
        return deploymentDesignDetailRepository.findAllByComponentEntity(componentEntity);
    }

    public List<DeploymentDesignScanResultEntity> scanDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity, String[] extensions) throws InterruptedException, ExecutionException, IOException, TimeoutException {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity);
        List<DeploymentDesignScanResultEntity> deploymentDesignScanResultEntityList = new ArrayList<>();
        String orderId = UUID.randomUUID().toString();
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntityList) {
            deploymentDesignScanResultEntityList.add(scanDeploymentDesignDetailsById(deploymentDesignDetailEntity.getId(), extensions, orderId));
        }
        return deploymentDesignScanResultEntityList;
    }

    public DeploymentDesignScanResultEntity scanDeploymentDesignDetailsById(String deploymentDesignDetailId, String[] extensions, String orderId) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = deploymentDesignDetailEntity.getDeploymentDesignNodeEntity();
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
        }
        OrderEntity orderEntity = new OrderEntity();
        if (extensions == null || extensions.length == 0) {
            orderEntity.setTag(OrderService.DEPLOY_DESIGN_SCAN);
        } else {
            orderEntity.setTag(OrderService.DEPLOY_DESIGN_SCAN_WITH_EXTENSIONS);
            orderEntity.setExtension(Arrays.toString(extensions).replace("[", "").replace("]", "").replaceAll("\\s*", ""));
        }
        orderEntity.setDeploymentDesignNodeId(deploymentDesignNodeEntity.getId());
        orderEntity.setDeploymentDesignDetailId(deploymentDesignDetailEntity.getId());
        orderEntity.setTargetPath(deviceEntity.getDeployPath() + deploymentDesignDetailEntity.getComponentHistoryEntity().getRelativePath());
        orderEntity.setTargetDevice(deviceEntity);
        orderService.sendDeployDesignScanOrderByUDP(orderEntity);
        Future<DeploymentDesignScanResultEntity> result = scanHandlerService.deploymentDesignScanHandler(orderEntity, deploymentDesignDetailEntity);
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT * 6) {
                if (!ScanHandlerService.DEPLOY_DESIGN_SCAN_RESULT.containsKey(orderEntity.getId())) {
                    log.info("扫描Id：" + orderEntity.getId() + ",扫描超时，程序退出。");
                    throw new RuntimeException(ApplicationMessages.SCAN_DEPLOY_DESIGN_TIME_OUT);
                }
            }
            if (result.isDone()) {
                DeploymentDesignScanResultEntity deploymentDesignScanResultEntity = result.get();
                if (StringUtils.isEmpty(orderId)) {
                    orderId = UUID.randomUUID().toString();
                }
                deploymentDesignScanResultEntity.setOrderId(orderId);
                ScanHandlerService.DEPLOY_DESIGN_SCAN_RESULT.remove(orderEntity.getId());
                log.info("扫描Id：" + orderEntity.getId() + ",扫描结束。");
                return deploymentDesignScanResultService.saveDeploymentDesignScanResult(deploymentDesignScanResultEntity);
            }
        }
    }
}
