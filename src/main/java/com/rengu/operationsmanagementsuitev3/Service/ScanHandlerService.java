package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.DiskScanResultEntity;
import com.rengu.operationsmanagementsuitev3.Entity.OrderEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProcessScanResultEntity;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

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
}
