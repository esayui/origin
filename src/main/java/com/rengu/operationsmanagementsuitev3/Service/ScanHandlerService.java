package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-30 15:38
 **/

@Slf4j
@Service
public class ScanHandlerService {

    public static final int SCAN_TYPE_CORRECT = 0;
    public static final int SCAN_TYPE_MODIFYED = 1;
    public static final int SCAN_TYPE_UNKNOWN = 2;
    public static final int SCAN_TYPE_MISSING = 3;

    public static final Map<String, List<DiskScanResultEntity>> DISK_SCAN_RESULT = new ConcurrentHashMap<>();
    public static final Map<String, List<ProcessScanResultEntity>> PROCESS_SCAN_RESULT = new ConcurrentHashMap<>();
    public static final Map<String, List<DeploymentDesignScanResultDetailEntity>> DEPLOY_DESIGN_SCAN_RESULT = new ConcurrentHashMap<>();

    //private final ComponentFileHistoryService componentFileHistoryService;


    @Autowired
    public ScanHandlerService() {
        //this.componentFileHistoryService = componentFileHistoryService;

    }

    @Async
    // 扫描设备磁盘处理线程
    public Future<List<DiskScanResultEntity>> diskScanHandler(OrderEntity orderEntity) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(orderEntity.getTargetDevice().getHostAddress())) {
                throw new RuntimeException(ApplicationMessages.DEVICE_IS_OFFLINE + orderEntity.getTargetDevice().getHostAddress());
            }
            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT) {
                throw new RuntimeException(ApplicationMessages.SCAN_DISK_TIME_OUT);
            }
            if (DISK_SCAN_RESULT.containsKey(orderEntity.getId())) {
                return new AsyncResult<>(DISK_SCAN_RESULT.get(orderEntity.getId()));
            }
        }
    }

    @Async
    // 扫描设备进程处理线程
    public Future<List<ProcessScanResultEntity>> processScanHandler(OrderEntity orderEntity) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(orderEntity.getTargetDevice().getHostAddress())) {
                throw new RuntimeException(ApplicationMessages.DEVICE_IS_OFFLINE + orderEntity.getTargetDevice().getHostAddress());
            }
            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT) {
                throw new RuntimeException(ApplicationMessages.SCAN_PROCESS_TIME_OUT);
            }
            if (PROCESS_SCAN_RESULT.containsKey(orderEntity.getId())) {
                return new AsyncResult<>(PROCESS_SCAN_RESULT.get(orderEntity.getId()));
            }
        }
    }

    //@Async
//    public Future<DeploymentDesignScanResultEntity> deploymentDesignScanHandler(OrderEntity orderEntity, DeploymentDesignDetailEntity deploymentDesignDetailEntity) {
//        long startTime = System.currentTimeMillis();
//        while (true) {
//            if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(orderEntity.getTargetDevice().getHostAddress())) {
//                throw new RuntimeException(ApplicationMessages.DEVICE_IS_OFFLINE + orderEntity.getTargetDevice().getHostAddress());
//            }
//            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT * 12) {
//                throw new RuntimeException(ApplicationMessages.SCAN_DEPLOY_DESIGN_TIME_OUT);
//            }
//            if (DEPLOY_DESIGN_SCAN_RESULT.containsKey(orderEntity.getId())) {
//                List<DeploymentDesignScanResultDetailEntity> deploymentDesignScanResultDetailEntityList = DEPLOY_DESIGN_SCAN_RESULT.get(orderEntity.getId());
//                ComponentHistoryEntity componentHistoryEntity = deploymentDesignDetailEntity.getComponentHistoryEntity();
//                String targetPath = orderEntity.getTargetPath();
//                // 初始化结果列表
//                List<DeploymentDesignScanResultDetailEntity> resultList = new ArrayList<>();
//                for (DeploymentDesignScanResultDetailEntity deploymentDesignScanResultDetailEntity : deploymentDesignScanResultDetailEntityList) {
//                    boolean hasFile = false;
//                    String relativePath = deploymentDesignScanResultDetailEntity.getTargetPath().replace(targetPath, "");
//                    for (ComponentFileHistoryEntity componentFileHistoryEntity : componentFileHistoryService.getComponentFileHistorysByComponentHistory(componentHistoryEntity)) {
//                        if (!componentFileHistoryEntity.isFolder()) {
//                            if (relativePath.equals(FormatUtils.getComponentFileHistoryRelativePath(componentFileHistoryEntity, ""))) {
//                                hasFile = true;
//                                // 路径相同
//                                if (deploymentDesignScanResultDetailEntity.getMd5().equals(componentFileHistoryEntity.getFileEntity().getMD5())) {
//                                    // MD5相同
//                                    deploymentDesignScanResultDetailEntity.setType(SCAN_TYPE_CORRECT);
//                                    log.info("发现正确部署文件：" + deploymentDesignScanResultDetailEntity.getTargetPath());
//                                    resultList.add(deploymentDesignScanResultDetailEntity);
//                                    break;
//                                } else {
//                                    // MD5变化
//                                    deploymentDesignScanResultDetailEntity.setType(SCAN_TYPE_MODIFYED);
//                                    log.info("发现修改文件：" + deploymentDesignScanResultDetailEntity.getTargetPath() + ",上报MD5：" + deploymentDesignScanResultDetailEntity.getMd5() + ",服务器MD5：" + componentFileHistoryEntity.getFileEntity().getMD5());
//                                    resultList.add(deploymentDesignScanResultDetailEntity);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    // 未知文件
//                    if (!hasFile) {
//                        log.info("发现未知文件：" + deploymentDesignScanResultDetailEntity.getTargetPath() + ",局部替换后路径：" + relativePath);
//                        deploymentDesignScanResultDetailEntity.setType(SCAN_TYPE_UNKNOWN);
//                        resultList.add(deploymentDesignScanResultDetailEntity);
//                    }
//                }
//                // 生成缺失文件
//                List<ComponentFileHistoryEntity> componentFileHistoryEntityList = componentFileHistoryService.getComponentFileHistorysByComponentHistory(componentHistoryEntity);
//                Iterator<ComponentFileHistoryEntity> componentFileHistoryEntityIterator = componentFileHistoryEntityList.iterator();
//                while (componentFileHistoryEntityIterator.hasNext()) {
//                    ComponentFileHistoryEntity componentFileHistoryEntity = componentFileHistoryEntityIterator.next();
//                    if (componentFileHistoryEntity.isFolder()) {
//                        componentFileHistoryEntityIterator.remove();
//                    } else {
//                        for (DeploymentDesignScanResultDetailEntity deploymentDesignScanResultDetailEntity : deploymentDesignScanResultDetailEntityList) {
//                            String relativePath = deploymentDesignScanResultDetailEntity.getTargetPath().replace(targetPath, "");
//                            // 路径相同，文件发现，移除
//                            if (FormatUtils.getComponentFileHistoryRelativePath(componentFileHistoryEntity, "").equals(relativePath)) {
//                                componentFileHistoryEntityIterator.remove();
//                                break;
//                            }
//                        }
//                    }
//                }
//                // 生成缺失文件结果
//                for (ComponentFileHistoryEntity componentFileHistoryEntity : componentFileHistoryEntityList) {
//                    DeploymentDesignScanResultDetailEntity deploymentDesignScanResultDetailEntity = new DeploymentDesignScanResultDetailEntity();
//                    deploymentDesignScanResultDetailEntity.setType(SCAN_TYPE_MISSING);
//                    deploymentDesignScanResultDetailEntity.setTargetPath(FormatUtils.formatPath(orderEntity.getTargetPath() + FormatUtils.getComponentFileHistoryRelativePath(componentFileHistoryEntity, "")));
//                    deploymentDesignScanResultDetailEntity.setName(FilenameUtils.getName(deploymentDesignScanResultDetailEntity.getTargetPath()));
//                    deploymentDesignScanResultDetailEntity.setMd5(componentFileHistoryEntity.getFileEntity().getMD5());
//                    log.info("发现缺失文件：" + deploymentDesignScanResultDetailEntity.getTargetPath());
//                    resultList.add(deploymentDesignScanResultDetailEntity);
//                }
//                DeploymentDesignScanResultEntity deploymentDesignScanResultEntity = new DeploymentDesignScanResultEntity();
//                deploymentDesignScanResultEntity.setDeploymentDesignDetailEntity(deploymentDesignDetailEntity);
//                deploymentDesignScanResultEntity.setResult(resultList);
//                log.info("指令Id：" + orderEntity.getId() + ",总计文件数量：" + deploymentDesignScanResultDetailEntityList.size() + ",扫描总计耗时：" + (System.currentTimeMillis() - startTime) / 1000 + "秒。");
//                return new AsyncResult<>(deploymentDesignScanResultEntity);
//            }
//        }
//    }
}
