package com.rengu.operationsmanagementsuitev3.ActiveMQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-08-27 11:10
 **/

@Slf4j
@Service
public class ActiveMQMessageConsumer {

    @JmsListener(destination = "QUEUE.HEARTBEAT")
    public void heartbeatHandler(String message) {
        log.info(message);
    }

    @JmsListener(destination = "QUEUE.PROCESS_SCAN_RESULT")
    public void processScanResultHandler(String message) {
        log.info(message);
    }

    @JmsListener(destination = "QUEUE.DISK_SCAN_RESULT")
    public void diskScanResultHandler(String message) {
        log.info(message);
    }
}
