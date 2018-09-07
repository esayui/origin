package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
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

    public static final Map<String, List<DiskScanResultEntity>> DISK_SCAN_RESULT = new ConcurrentHashMap<>();
    public static final Map<String, List<ProcessScanResultEntity>> PROCESS_SCAN_RESULT = new ConcurrentHashMap<>();
    public static final Map<String, List<ScanResultEntity>> DEPLOY_DESIGN_SCAN_RESULT = new ConcurrentHashMap<>();

    private final ComponentFileHistoryService componentFileHistoryService;

    @Autowired
    public ScanHandlerService(ComponentFileHistoryService componentFileHistoryService) {
        this.componentFileHistoryService = componentFileHistoryService;
    }

    @Async
    // 扫描设备磁盘处理线程
    public Future<List<DiskScanResultEntity>> diskScanHandler(OrderEntity orderEntity) {
        long startTime = System.currentTimeMillis();
        while (true) {
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
            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT) {
                throw new RuntimeException(ApplicationMessages.SCAN_PROCESS_TIME_OUT);
            }
            if (PROCESS_SCAN_RESULT.containsKey(orderEntity.getId())) {
                return new AsyncResult<>(PROCESS_SCAN_RESULT.get(orderEntity.getId()));
            }
        }
    }

    @Async
    public Future<DeploymentDesignScanResultEntity> deploymentDesignScanHandler(OrderEntity orderEntity, DeploymentDesignDetailEntity deploymentDesignDetailEntity) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime >= ApplicationConfig.SCAN_TIME_OUT * 5) {
                throw new RuntimeException(ApplicationMessages.SCAN_DEPLOY_DESIGN_TIME_OUT);
            }
            if (DEPLOY_DESIGN_SCAN_RESULT.containsKey(orderEntity.getId())) {
                List<ScanResultEntity> scanResultEntityList = DEPLOY_DESIGN_SCAN_RESULT.get(orderEntity.getId());
                DeploymentDesignNodeEntity deploymentDesignNodeEntity = deploymentDesignDetailEntity.getDeploymentDesignNodeEntity();
                ComponentHistoryEntity componentHistoryEntity = deploymentDesignDetailEntity.getComponentHistoryEntity();
                String targetPath = orderEntity.getTargetPath();
                // 初始化结果列表
                List<ScanResultEntity> correctFiles = new ArrayList<>();
                List<ScanResultEntity> modifyedFiles = new ArrayList<>();
                List<ScanResultEntity> unknownFiles = new ArrayList<>();
                List<ScanResultEntity> missingFiles = new ArrayList<>();
                for (ScanResultEntity scanResultEntity : scanResultEntityList) {
                    boolean hasFile = false;
                    String relativePath = scanResultEntity.getTargetPath().replace(targetPath, "");
                    for (ComponentFileHistoryEntity componentFileHistoryEntity : componentFileHistoryService.getComponentFileHistorysByComponentHistory(componentHistoryEntity)) {
                        if (!componentFileHistoryEntity.isFolder()) {
                            if (relativePath.equals(FormatUtils.getComponentFileHistoryRelativePath(componentFileHistoryEntity, ""))) {
                                hasFile = true;
                                // 路径相同
                                if (scanResultEntity.getMd5().equals(componentFileHistoryEntity.getFileEntity().getMD5())) {
                                    // MD5相同
                                    correctFiles.add(scanResultEntity);
                                    break;
                                } else {
                                    // MD5变化
                                    modifyedFiles.add(scanResultEntity);
                                    break;
                                }
                            }
                        }
                    }
                    // 未知文件
                    if (!hasFile) {
                        unknownFiles.add(scanResultEntity);
                    }
                }
                // 生成缺失文件
                List<ComponentFileHistoryEntity> componentFileHistoryEntityList = componentFileHistoryService.getComponentFileHistorysByComponentHistory(componentHistoryEntity);
                Iterator<ComponentFileHistoryEntity> componentFileHistoryEntityIterator = componentFileHistoryEntityList.iterator();
                while (componentFileHistoryEntityIterator.hasNext()) {
                    ComponentFileHistoryEntity componentFileHistoryEntity = componentFileHistoryEntityIterator.next();
                    if (componentFileHistoryEntity.isFolder()) {
                        componentFileHistoryEntityIterator.remove();
                    } else {
                        for (ScanResultEntity scanResultEntity : scanResultEntityList) {
                            String relativePath = scanResultEntity.getTargetPath().replace(targetPath, "");
                            // 路径相同，文件发现，移除
                            if (FormatUtils.getComponentFileHistoryRelativePath(componentFileHistoryEntity, "").equals(relativePath)) {
                                componentFileHistoryEntityIterator.remove();
                                break;
                            }
                        }
                    }
                }
                // 生成缺失文件结果
                for (ComponentFileHistoryEntity componentFileHistoryEntity : componentFileHistoryEntityList) {
                    ScanResultEntity scanResultEntity = new ScanResultEntity();
                    scanResultEntity.setDeploymentDesignNodeId(deploymentDesignNodeEntity.getId());
                    scanResultEntity.setDeploymentDesignDetailId(deploymentDesignDetailEntity.getId());
                    scanResultEntity.setTargetPath(FormatUtils.formatPath(orderEntity.getTargetPath() + FormatUtils.getComponentFileHistoryRelativePath(componentFileHistoryEntity, "")));
                    scanResultEntity.setMd5(componentFileHistoryEntity.getFileEntity().getMD5());
                    missingFiles.add(scanResultEntity);
                }
                DeploymentDesignScanResultEntity deploymentDesignScanResultEntity = new DeploymentDesignScanResultEntity();
                deploymentDesignScanResultEntity.setDeploymentDesignDetailEntity(deploymentDesignDetailEntity);
                deploymentDesignScanResultEntity.setCorrectFiles(correctFiles);
                deploymentDesignScanResultEntity.setModifyedFiles(modifyedFiles);
                deploymentDesignScanResultEntity.setUnknownFiles(unknownFiles);
                deploymentDesignScanResultEntity.setMissingFiles(missingFiles);
                return new AsyncResult<>(deploymentDesignScanResultEntity);
            }
        }
    }
}
