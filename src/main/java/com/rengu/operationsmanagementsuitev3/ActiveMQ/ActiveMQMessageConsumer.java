package com.rengu.operationsmanagementsuitev3.ActiveMQ;

import com.rengu.operationsmanagementsuitev3.Entity.HeartbeatEntity;
import com.rengu.operationsmanagementsuitev3.Utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-08-27 11:10
 **/

@Slf4j
@Service
public class ActiveMQMessageConsumer {

    @JmsListener(destination = "QUEUE.HEARTBEAT")
    public void heartbeatHandler(String message) throws IOException {
        HeartbeatEntity heartbeatEntity = JsonUtils.readValue(message, HeartbeatEntity.class);
        log.info(heartbeatEntity.toString());
    }

    @JmsListener(destination = "QUEUE.DISK_SCAN_RESULT")
    public void diskScanResultHandler(String message) {
        log.info(message);
    }
}
